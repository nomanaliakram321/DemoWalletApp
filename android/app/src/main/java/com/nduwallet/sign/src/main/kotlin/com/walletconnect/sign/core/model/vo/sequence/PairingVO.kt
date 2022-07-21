package com.nduwallet.sign.core.model.vo.sequence

import com.nduwallet.sign.core.model.type.Sequence
import com.nduwallet.sign.core.model.vo.ExpiryVO
import com.nduwallet.sign.core.model.vo.TopicVO
import com.nduwallet.sign.core.model.vo.clientsync.common.MetaDataVO
import com.nduwallet.sign.core.model.vo.clientsync.common.RelayProtocolOptionsVO
import com.nduwallet.sign.engine.model.EngineDO
import com.nduwallet.sign.engine.model.mapper.toAbsoluteString
import com.nduwallet.sign.util.Expiration

internal data class PairingVO(
    override val topic: TopicVO,
    override val expiry: ExpiryVO,
    val peerMetaData: MetaDataVO? = null,
    val relayProtocol: String,
    val relayData: String?,
    val uri: String,
    val isActive: Boolean,
) : Sequence {

    companion object {

        internal fun createInactivePairing(topic: TopicVO, relay: RelayProtocolOptionsVO, uri: String): PairingVO {
            return PairingVO(
                topic,
                ExpiryVO(Expiration.inactivePairing),
                uri = uri,
                relayProtocol = relay.protocol,
                relayData = relay.data,
                isActive = false
            )
        }

        internal fun createActivePairing(uri: EngineDO.WalletConnectUri): PairingVO {
            return PairingVO(
                uri.topic,
                ExpiryVO(Expiration.activePairing),
                uri = uri.toAbsoluteString(),
                relayProtocol = uri.relay.protocol,
                relayData = uri.relay.data,
                isActive = true
            )
        }
    }
}