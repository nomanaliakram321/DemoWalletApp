package com.nduwallet.sign.core.model.vo.clientsync.session.payload

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.nduwallet.sign.core.adapters.TopicAdapter
import com.nduwallet.sign.core.model.vo.TopicVO

@JsonClass(generateAdapter = true)
internal data class SessionSignalVO(
    @Json(name = "method")
    val method: String = "pairing",
    @Json(name = "params")
    val params: Params
) {

    internal data class Params(
        @field:TopicAdapter.Qualifier
        val topic: TopicVO
    )
}