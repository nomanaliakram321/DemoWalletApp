@file:JvmSynthetic

package com.nduwallet.sign.core.model.vo.sync

import com.nduwallet.sign.core.model.vo.PublicKey

internal data class ParticipantsVO(
    val senderPublicKey: PublicKey,
    val receiverPublicKey: PublicKey,
)