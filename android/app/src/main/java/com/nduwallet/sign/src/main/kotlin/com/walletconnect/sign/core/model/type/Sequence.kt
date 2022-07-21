package com.nduwallet.sign.core.model.type

import com.nduwallet.sign.core.model.vo.ExpiryVO
import com.nduwallet.sign.core.model.vo.TopicVO

internal interface Sequence {
    val topic: TopicVO
    val expiry: ExpiryVO
}