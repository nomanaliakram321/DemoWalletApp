package com.nduwallet.sign.core.model.vo.sync

import com.nduwallet.sign.core.model.type.ClientParams
import com.nduwallet.sign.core.model.vo.TopicVO

internal data class WCRequestVO(
    val topic: TopicVO,
    val id: Long,
    val method: String,
    val params: ClientParams
)