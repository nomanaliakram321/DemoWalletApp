@file:JvmSynthetic

package com.nduwallet.sign.json_rpc.domain

import com.nduwallet.sign.core.exceptions.client.WalletConnectException
import com.nduwallet.sign.core.exceptions.peer.PeerError
import com.nduwallet.sign.core.model.client.Relay
import com.nduwallet.sign.core.model.type.ClientParams
import com.nduwallet.sign.core.model.type.JsonRpcClientSync
import com.nduwallet.sign.core.model.type.enums.EnvelopeType
import com.nduwallet.sign.core.model.vo.IridiumParamsVO
import com.nduwallet.sign.core.model.vo.SubscriptionIdVO
import com.nduwallet.sign.core.model.vo.TopicVO
import com.nduwallet.sign.core.model.vo.clientsync.session.SessionRpcVO
import com.nduwallet.sign.core.model.vo.jsonRpc.JsonRpcResponseVO
import com.nduwallet.sign.core.model.vo.sync.PendingRequestVO
import com.nduwallet.sign.core.model.vo.sync.WCRequestVO
import com.nduwallet.sign.core.model.vo.sync.WCResponseVO
import com.nduwallet.sign.core.scope.scope
import com.nduwallet.sign.crypto.Codec
import com.nduwallet.sign.json_rpc.data.JsonRpcSerializer
import com.nduwallet.sign.json_rpc.model.*
import com.nduwallet.sign.network.RelayInterface
import com.nduwallet.sign.storage.history.JsonRpcHistory
import com.nduwallet.sign.util.Empty
import com.nduwallet.sign.util.Logger
import com.nduwallet.sign.util.NetworkState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.HttpURLConnection

internal class RelayerInteractor(
    private val relay: RelayInterface,
    private val serializer: JsonRpcSerializer,
    private val chaChaPolyCodec: Codec,
    private val jsonRpcHistory: JsonRpcHistory,
    networkState: NetworkState, //todo: move to the RelayClient
) {
    private val _clientSyncJsonRpc: MutableSharedFlow<WCRequestVO> = MutableSharedFlow()
    val clientSyncJsonRpc: SharedFlow<WCRequestVO> = _clientSyncJsonRpc.asSharedFlow()

    private val _peerResponse: MutableSharedFlow<WCResponseVO> = MutableSharedFlow()
    val peerResponse: SharedFlow<WCResponseVO> = _peerResponse.asSharedFlow()

    private val _internalErrors = MutableSharedFlow<WalletConnectException.InternalError>()
    val internalErrors: SharedFlow<WalletConnectException.InternalError> = _internalErrors.asSharedFlow()

    private val _isNetworkAvailable: StateFlow<Boolean> = networkState.isAvailable
    private val _isWSSConnectionOpened: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val isConnectionAvailable: StateFlow<Boolean> =
        combine(_isWSSConnectionOpened, _isNetworkAvailable) { wss, internet -> wss && internet }
            .stateIn(scope, SharingStarted.Eagerly, false)

    private val subscriptions: MutableMap<String, String> = mutableMapOf()
    private val exceptionHandler = CoroutineExceptionHandler { _, exception -> handleError(exception.message ?: String.Empty) }

    @get:JvmSynthetic
    private val Throwable.toWalletConnectException: WalletConnectException
        get() =
            when {
                this.message?.contains(HttpURLConnection.HTTP_UNAUTHORIZED.toString()) == true ->
                    WalletConnectException.ProjectIdDoesNotExistException(this.message)
                this.message?.contains(HttpURLConnection.HTTP_FORBIDDEN.toString()) == true ->
                    WalletConnectException.InvalidProjectIdException(this.message)
                else -> WalletConnectException.GenericException(this.message)
            }

    val initializationErrorsFlow: Flow<WalletConnectException>
        get() = relay.eventsFlow
            .onEach { event: Relay.Model.Event ->
                Logger.log("$event")
                setIsWSSConnectionOpened(event)
            }
            .filterIsInstance<Relay.Model.Event.OnConnectionFailed>()
            .map { error -> error.throwable.toWalletConnectException }

    init {
        manageSubscriptions()
    }

    internal fun checkConnectionWorking() {
        if (!isConnectionAvailable.value) {
            throw WalletConnectException.MissingInternetConnectionException("No connection available")
        }
    }

    internal fun publishJsonRpcRequests(
        topic: TopicVO,
        params: IridiumParamsVO,
        payload: JsonRpcClientSync<*>,
        onSuccess: () -> Unit = {},
        onFailure: (Throwable) -> Unit = {},
    ) {
        checkConnectionWorking()
        val requestJson = serializer.serialize(payload)

        if (jsonRpcHistory.setRequest(payload.id, topic, payload.method, requestJson)) {
            val message = chaChaPolyCodec.encrypt(topic, requestJson, EnvelopeType.ZERO)

            relay.publish(topic.value, message, params.toRelay()) { result ->
                result.fold(
                    onSuccess = { onSuccess() },
                    onFailure = { error -> onFailure(error) }
                )
            }
        }
    }

    internal fun publishJsonRpcResponse(
        topic: TopicVO,
        response: JsonRpcResponseVO,
        params: IridiumParamsVO,
        onSuccess: () -> Unit = {},
        onFailure: (Throwable) -> Unit = {},
    ) {
        checkConnectionWorking()

        val jsonResponseDO = response.toRelayerDOJsonRpcResponse()
        val responseJson = serializer.serialize(jsonResponseDO)
        val message = chaChaPolyCodec.encrypt(topic, responseJson, EnvelopeType.ZERO)

        relay.publish(topic.value, message, params.toRelay()) { result ->
            result.fold(
                onSuccess = {
                    jsonRpcHistory.updateRequestWithResponse(response.id, responseJson)
                    onSuccess()
                },
                onFailure = { error -> onFailure(error) }
            )
        }
    }

    internal fun respondWithParams(request: WCRequestVO, clientParams: ClientParams, iridiumParams: IridiumParamsVO) {
        val result = JsonRpcResponseVO.JsonRpcResult(id = request.id, result = clientParams)

        publishJsonRpcResponse(request.topic, result, iridiumParams,
            onFailure = { error -> Logger.error("Cannot send the response, error: $error") })
    }

    internal fun respondWithSuccess(request: WCRequestVO, iridiumParams: IridiumParamsVO) {
        val result = JsonRpcResponseVO.JsonRpcResult(id = request.id, result = true)

        try {
            publishJsonRpcResponse(request.topic, result, iridiumParams,
                onFailure = { error -> Logger.error("Cannot send the response, error: $error") })
        } catch (e: Exception) {
            handleError(e.message ?: String.Empty)
        }
    }

    internal fun respondWithError(
        request: WCRequestVO,
        error: PeerError,
        iridiumParams: IridiumParamsVO,
        onFailure: (Throwable) -> Unit = {},
    ) {
        Logger.error("Responding with error: ${error.message}: ${error.code}")
        val jsonRpcError = JsonRpcResponseVO.JsonRpcError(id = request.id, error = JsonRpcResponseVO.Error(error.code, error.message))

        try {
            publishJsonRpcResponse(request.topic, jsonRpcError, iridiumParams,
                onFailure = { failure ->
                    Logger.error("Cannot respond with error: $failure")
                    onFailure(failure)
                })
        } catch (e: Exception) {
            handleError(e.message ?: String.Empty)
        }
    }

    internal fun subscribe(topic: TopicVO) {
        checkConnectionWorking()
        relay.subscribe(topic.value) { result ->
            result.fold(
                onSuccess = { acknowledgement -> subscriptions[topic.value] = acknowledgement.result },
                onFailure = { error -> Logger.error("Subscribe to topic: $topic error: $error") }
            )
        }
    }

    internal fun unsubscribe(topic: TopicVO) {
        checkConnectionWorking()
        if (subscriptions.contains(topic.value)) {
            val subscriptionId = SubscriptionIdVO(subscriptions[topic.value].toString())
            relay.unsubscribe(topic.value, subscriptionId.id) { result ->
                result.fold(
                    onSuccess = {
                        jsonRpcHistory.deleteRequests(topic)
                        subscriptions.remove(topic.value)
                    },
                    onFailure = { error -> Logger.error("Unsubscribe to topic: $topic error: $error") }
                )
            }
        }
    }

    internal fun getPendingRequests(topic: TopicVO): List<PendingRequestVO> =
        jsonRpcHistory.getRequests(topic)
            .filter { entry -> entry.response == null && entry.method == JsonRpcMethod.WC_SESSION_REQUEST }
            .filter { entry -> serializer.tryDeserialize<SessionRpcVO.SessionRequest>(entry.body) != null }
            .map { entry -> serializer.tryDeserialize<SessionRpcVO.SessionRequest>(entry.body)!!.toPendingRequestVO(entry) }

    private fun manageSubscriptions() {
        scope.launch(exceptionHandler) {
            relay.subscriptionRequest
                .map { relayRequest ->
                    val topic = TopicVO(relayRequest.subscriptionTopic)
                    val message = chaChaPolyCodec.decrypt(topic, relayRequest.message)

                    Pair(message, topic)
                }
                .collect { (decryptedMessage, topic) -> manageSubscriptions(decryptedMessage, topic) }
        }
    }

    private suspend fun manageSubscriptions(decryptedMessage: String, topic: TopicVO) {
        serializer.tryDeserialize<RelayerDO.ClientJsonRpc>(decryptedMessage)?.let { clientJsonRpc ->
            handleRequest(clientJsonRpc, topic, decryptedMessage)
        } ?: serializer.tryDeserialize<RelayerDO.JsonRpcResponse.JsonRpcResult>(decryptedMessage)?.let { result ->
            handleJsonRpcResult(result)
        } ?: serializer.tryDeserialize<RelayerDO.JsonRpcResponse.JsonRpcError>(decryptedMessage)?.let { error ->
            handleJsonRpcError(error)
        } ?: handleError("RelayerInteractor: Received unknown object type")
    }

    private suspend fun handleRequest(clientJsonRpc: RelayerDO.ClientJsonRpc, topic: TopicVO, decryptedMessage: String) {
        if (jsonRpcHistory.setRequest(clientJsonRpc.id, topic, clientJsonRpc.method, decryptedMessage)) {
            serializer.deserialize(clientJsonRpc.method, decryptedMessage)?.let { params ->
                _clientSyncJsonRpc.emit(WCRequestVO(topic, clientJsonRpc.id, clientJsonRpc.method, params))
            } ?: handleError("RelayerInteractor: Unknown request params")
        }
    }

    private suspend fun handleJsonRpcResult(jsonRpcResult: RelayerDO.JsonRpcResponse.JsonRpcResult) {
        val jsonRpcRecord = jsonRpcHistory.updateRequestWithResponse(jsonRpcResult.id, serializer.serialize(jsonRpcResult))

        if (jsonRpcRecord != null) {
            serializer.deserialize(jsonRpcRecord.method, jsonRpcRecord.body)?.let { params ->
                val responseVO = JsonRpcResponseVO.JsonRpcResult(jsonRpcResult.id, result = jsonRpcResult.result)
                _peerResponse.emit(jsonRpcRecord.toWCResponse(responseVO, params))
            } ?: handleError("RelayerInteractor: Unknown result params")
        }
    }

    private suspend fun handleJsonRpcError(jsonRpcError: RelayerDO.JsonRpcResponse.JsonRpcError) {
        val jsonRpcRecord = jsonRpcHistory.updateRequestWithResponse(jsonRpcError.id, serializer.serialize(jsonRpcError))

        if (jsonRpcRecord != null) {
            serializer.deserialize(jsonRpcRecord.method, jsonRpcRecord.body)?.let { params ->
                _peerResponse.emit(jsonRpcRecord.toWCResponse(jsonRpcError.toJsonRpcErrorVO(), params))
            } ?: handleError("RelayerInteractor: Unknown error params")
        }
    }

    private fun setIsWSSConnectionOpened(event: Relay.Model.Event) {
        if (event is Relay.Model.Event.OnConnectionOpened<*>) {
            _isWSSConnectionOpened.compareAndSet(expect = false, update = true)
        } else if (event is Relay.Model.Event.OnConnectionClosed || event is Relay.Model.Event.OnConnectionFailed) {
            _isWSSConnectionOpened.compareAndSet(expect = true, update = false)
        }
    }

    private fun handleError(errorMessage: String) {
        Logger.error(errorMessage)
        scope.launch {
            _internalErrors.emit(WalletConnectException.InternalError(errorMessage))
        }
    }
}