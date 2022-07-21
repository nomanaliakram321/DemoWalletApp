package com.nduwallet.sign.core.model.vo.sync

import com.nduwallet.sign.core.model.type.ClientParams
import com.nduwallet.sign.core.model.vo.TopicVO
import com.nduwallet.sign.core.model.vo.jsonRpc.JsonRpcResponseVO

internal data class WCResponseVO(
    val topic: TopicVO,
    val method: String,
    val response: JsonRpcResponseVO,
    val params: ClientParams,
)