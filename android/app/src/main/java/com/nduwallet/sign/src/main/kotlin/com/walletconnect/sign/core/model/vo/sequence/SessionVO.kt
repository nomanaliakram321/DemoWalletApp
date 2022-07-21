package com.nduwallet.sign.core.model.vo.sequence

import com.nduwallet.sign.core.model.type.Sequence
import com.nduwallet.sign.core.model.vo.ExpiryVO
import com.nduwallet.sign.core.model.vo.PublicKey
import com.nduwallet.sign.core.model.vo.TopicVO
import com.nduwallet.sign.core.model.vo.clientsync.common.MetaDataVO
import com.nduwallet.sign.core.model.vo.clientsync.common.NamespaceVO
import com.nduwallet.sign.core.model.vo.clientsync.common.SessionParticipantVO
import com.nduwallet.sign.core.model.vo.clientsync.pairing.params.PairingParamsVO
import com.nduwallet.sign.core.model.vo.clientsync.session.params.SessionParamsVO
import com.nduwallet.sign.engine.model.EngineDO
import com.nduwallet.sign.engine.model.mapper.toMapOfNamespacesVOSession

internal data class SessionVO(
    override val topic: TopicVO,
    override val expiry: ExpiryVO,
    val relayProtocol: String,
    val relayData: String?,
    val controllerKey: PublicKey? = null,
    val selfPublicKey: PublicKey,
    val selfMetaData: MetaDataVO? = null,
    val peerPublicKey: PublicKey? = null,
    val peerMetaData: MetaDataVO? = null,
    val namespaces: Map<String, NamespaceVO.Session>,
    val proposalNamespaces: Map<String, NamespaceVO.Proposal>,
    val isAcknowledged: Boolean,
) : Sequence {
    val isPeerController: Boolean = peerPublicKey?.keyAsHex == controllerKey?.keyAsHex
    val isSelfController: Boolean = selfPublicKey.keyAsHex == controllerKey?.keyAsHex

    internal companion object {

        @JvmSynthetic
        internal fun createUnacknowledgedSession(
            sessionTopic: TopicVO,
            proposal: PairingParamsVO.SessionProposeParams,
            selfParticipant: SessionParticipantVO,
            sessionExpiry: Long,
            namespaces: Map<String, EngineDO.Namespace.Session>
        ): SessionVO {
            return SessionVO(
                sessionTopic,
                ExpiryVO(sessionExpiry),
                relayProtocol = proposal.relays.first().protocol,
                relayData = proposal.relays.first().data,
                peerPublicKey = PublicKey(proposal.proposer.publicKey),
                peerMetaData = proposal.proposer.metadata,
                selfPublicKey = PublicKey(selfParticipant.publicKey),
                selfMetaData = selfParticipant.metadata,
                controllerKey = PublicKey(selfParticipant.publicKey),
                namespaces = namespaces.toMapOfNamespacesVOSession(),
                proposalNamespaces = proposal.namespaces,
                isAcknowledged = false
            )
        }

        @JvmSynthetic
        internal fun createAcknowledgedSession(
            sessionTopic: TopicVO,
            settleParams: SessionParamsVO.SessionSettleParams,
            selfPublicKey: PublicKey,
            selfMetadata: MetaDataVO,
            proposalNamespaces: Map<String, NamespaceVO.Proposal>
        ): SessionVO {
            return SessionVO(
                sessionTopic,
                ExpiryVO(settleParams.expiry),
                relayProtocol = settleParams.relay.protocol,
                relayData = settleParams.relay.data,
                peerPublicKey = PublicKey(settleParams.controller.publicKey),
                peerMetaData = settleParams.controller.metadata,
                selfPublicKey = selfPublicKey,
                selfMetaData = selfMetadata,
                controllerKey = PublicKey(settleParams.controller.publicKey),
                namespaces = settleParams.namespaces,
                proposalNamespaces = proposalNamespaces,
                isAcknowledged = true
            )
        }
    }
}