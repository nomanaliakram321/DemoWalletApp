package com.nduwallet.sign.core.model.vo.clientsync.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.nduwallet.sign.core.model.vo.clientsync.session.payload.EventsVO
import com.nduwallet.sign.core.model.vo.clientsync.session.payload.JsonRpcVO

@JsonClass(generateAdapter = true)
internal data class SessionPermissionsVO(
    @Json(name = "jsonrpc")
    val jsonRpc: JsonRpcVO,
    @Json(name = "events")
    val events: EventsVO?,
)