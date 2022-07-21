package com.nduwallet.sign.core.model.vo.clientsync.common

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.nduwallet.sign.util.DefaultId

@JsonClass(generateAdapter = true)
internal data class ReasonVO(
    @Json(name = "code")
    val code: Int = Int.DefaultId,
    @Json(name = "message")
    val message: String
)