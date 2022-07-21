@file:JvmSynthetic

package com.nduwallet.sign.crypto

import com.nduwallet.sign.core.model.vo.PublicKey
import com.nduwallet.sign.core.model.vo.SymmetricKey
import com.nduwallet.sign.core.model.vo.TopicVO

internal interface KeyManagementRepository {
    fun generateSymmetricKey(topic: TopicVO): SymmetricKey
    fun setSymmetricKey(topic: TopicVO, symmetricKey: SymmetricKey)
    fun getSymmetricKey(topic: TopicVO): SymmetricKey

    fun generateSymmetricKeyFromKeyAgreement(self: PublicKey, peer: PublicKey): SymmetricKey
    fun generateTopicFromKeyAgreement(self: PublicKey, peer: PublicKey): TopicVO
    fun getKeyAgreement(topic: TopicVO): Pair<PublicKey, PublicKey>
    fun generateKeyPair(): PublicKey

    fun removeKeys(tag: String)

    // Needs to be added with Chat SDK
}