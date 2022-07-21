@file:JvmSynthetic

package com.nduwallet.sign.crypto

import com.nduwallet.sign.core.model.type.enums.EnvelopeType
import com.nduwallet.sign.core.model.vo.PublicKey
import com.nduwallet.sign.core.model.vo.TopicVO
import com.nduwallet.sign.core.model.vo.sync.ParticipantsVO

internal interface Codec {
    fun encrypt(topic: TopicVO, payload: String, envelopeType: EnvelopeType, participants: ParticipantsVO? = null): String
    fun decrypt(topic: TopicVO, cipherText: String, receiverPublicKey: PublicKey? = null): String
}