package com.nduwallet.sign.core.model.vo.clientsync.pairing.payload

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.nduwallet.sign.core.model.vo.clientsync.common.MetaDataVO

@JsonClass(generateAdapter = true)
internal data class SessionProposerVO(
    @Json(name = "publicKey")
    val publicKey: String,
    @Json(name = "metadata")
    val metadata: MetaDataVO,
)