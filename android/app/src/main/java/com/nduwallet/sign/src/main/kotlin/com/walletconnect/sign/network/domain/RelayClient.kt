@file:JvmSynthetic

package com.nduwallet.sign.network.domain

import com.nduwallet.sign.core.exceptions.WRONG_CONNECTION_TYPE
import com.nduwallet.sign.core.model.client.Relay
import com.nduwallet.sign.core.model.vo.SubscriptionIdVO
import com.nduwallet.sign.core.model.vo.TopicVO
import com.nduwallet.sign.core.model.vo.TtlVO
import com.nduwallet.sign.core.scope.scope
import com.nduwallet.sign.network.RelayInterface
import com.nduwallet.sign.network.data.connection.controller.ConnectionController
import com.nduwallet.sign.network.data.service.RelayService
import com.nduwallet.sign.network.model.RelayDTO
import com.nduwallet.sign.network.model.toRelayAcknowledgment
import com.nduwallet.sign.network.model.toRelayEvent
import com.nduwallet.sign.network.model.toRelayRequest
import com.nduwallet.sign.util.Logger
import com.nduwallet.sign.util.generateId
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

internal class RelayClient internal constructor(
    private val connectionController: ConnectionController,
    private val relay: RelayService,
) : RelayInterface {

    override val eventsFlow: SharedFlow<Relay.Model.Event> = relay
        .observeWebSocketEvent()
        .map { event -> event.toRelayEvent() }
        .shareIn(scope, SharingStarted.Lazily, REPLAY)


    override fun connect(onError: (String) -> Unit) {
        when (connectionController) {
            is ConnectionController.Automatic -> onError(WRONG_CONNECTION_TYPE)
            is ConnectionController.Manual -> connectionController.connect()
        }
    }

    override fun disconnect(onError: (String) -> Unit) {
        when (connectionController) {
            is ConnectionController.Automatic -> onError(WRONG_CONNECTION_TYPE)
            is ConnectionController.Manual -> connectionController.disconnect()
        }
    }

    override val subscriptionRequest: Flow<Relay.Model.Call.Subscription.Request> =
        relay.observeSubscriptionRequest()
            .map { request -> request.toRelayRequest() }
            .onEach { relayRequest -> supervisorScope { publishSubscriptionAcknowledgement(relayRequest.id) } }

    override fun publish(
        topic: String,
        message: String,
        params: Relay.Model.IridiumParams,
        onResult: (Result<Relay.Model.Call.Publish.Acknowledgement>) -> Unit,
    ) {
        val (tag, ttl, prompt) = params
        val publishParams = RelayDTO.Publish.Request.Params(TopicVO(topic), message, TtlVO(ttl), tag, prompt)
        val request = RelayDTO.Publish.Request(generateId(), params = publishParams)

        observePublishAcknowledgement { acknowledgement -> onResult(Result.success(acknowledgement)) }
        observePublishError { error -> onResult(Result.failure(error)) }
        relay.publishRequest(request)
    }

    override fun subscribe(topic: String, onResult: (Result<Relay.Model.Call.Subscribe.Acknowledgement>) -> Unit) {
        val subscribeRequest = RelayDTO.Subscribe.Request(id = generateId(), params = RelayDTO.Subscribe.Request.Params(TopicVO(topic)))
        observeSubscribeAcknowledgement { acknowledgement -> onResult(Result.success(acknowledgement)) }
        observeSubscribeError { error -> onResult(Result.failure(error)) }
        relay.subscribeRequest(subscribeRequest)
    }

    override fun unsubscribe(
        topic: String,
        subscriptionId: String,
        onResult: (Result<Relay.Model.Call.Unsubscribe.Acknowledgement>) -> Unit,
    ) {
        val unsubscribeRequest = RelayDTO.Unsubscribe.Request(id = generateId(),
            params = RelayDTO.Unsubscribe.Request.Params(TopicVO(topic), SubscriptionIdVO(subscriptionId)))
        observeUnSubscribeAcknowledgement { acknowledgement -> onResult(Result.success(acknowledgement)) }
        observeUnSubscribeError { error -> onResult(Result.failure(error)) }
        relay.unsubscribeRequest(unsubscribeRequest)
    }

    private fun publishSubscriptionAcknowledgement(id: Long) {
        val publishRequest = RelayDTO.Subscription.Acknowledgement(id = id, result = true)
        relay.publishSubscriptionAcknowledgement(publishRequest)
    }

    private fun observePublishAcknowledgement(onResult: (Relay.Model.Call.Publish.Acknowledgement) -> Unit) {
        scope.launch {
            relay.observePublishAcknowledgement()
                .map { ack -> ack.toRelayAcknowledgment() }
                .catch { exception -> Logger.error(exception) }
                .collect { acknowledgement ->
                    supervisorScope {
                        onResult(acknowledgement)
                        cancel()
                    }
                }
        }
    }

    private fun observePublishError(onFailure: (Throwable) -> Unit) {
        scope.launch {
            relay.observePublishError()
                .onEach { jsonRpcError -> Logger.error(Throwable(jsonRpcError.error.errorMessage)) }
                .catch { exception -> Logger.error(exception) }
                .collect { errorResponse ->
                    supervisorScope {
                        onFailure(Throwable(errorResponse.error.errorMessage))
                        cancel()
                    }
                }
        }
    }

    private fun observeSubscribeAcknowledgement(onResult: (Relay.Model.Call.Subscribe.Acknowledgement) -> Unit) {
        scope.launch {
            relay.observeSubscribeAcknowledgement()
                .map { ack -> ack.toRelayAcknowledgment() }
                .catch { exception -> Logger.error(exception) }
                .collect { acknowledgement ->
                    supervisorScope {
                        onResult(acknowledgement)
                        cancel()
                    }
                }
        }
    }

    private fun observeSubscribeError(onFailure: (Throwable) -> Unit) {
        scope.launch {
            relay.observeSubscribeError()
                .onEach { jsonRpcError -> Logger.error(Throwable(jsonRpcError.error.errorMessage)) }
                .catch { exception -> Logger.error(exception) }
                .collect { errorResponse ->
                    supervisorScope {
                        onFailure(Throwable(errorResponse.error.errorMessage))
                        cancel()
                    }
                }
        }
    }

    private fun observeUnSubscribeAcknowledgement(onSuccess: (Relay.Model.Call.Unsubscribe.Acknowledgement) -> Unit) {
        scope.launch {
            relay.observeUnsubscribeAcknowledgement()
                .map { ack -> ack.toRelayAcknowledgment() }
                .catch { exception -> Logger.error(exception) }
                .collect { acknowledgement ->
                    supervisorScope {
                        onSuccess(acknowledgement)
                        cancel()
                    }
                }
        }
    }

    private fun observeUnSubscribeError(onFailure: (Throwable) -> Unit) {
        scope.launch {
            relay.observeUnsubscribeError()
                .onEach { jsonRpcError -> Logger.error(Throwable(jsonRpcError.error.errorMessage)) }
                .catch { exception -> Logger.error(exception) }
                .collect { errorResponse ->
                    supervisorScope {
                        onFailure(Throwable(errorResponse.error.errorMessage))
                        cancel()
                    }
                }
        }
    }

    private companion object {
        private const val REPLAY: Int = 1
    }
}