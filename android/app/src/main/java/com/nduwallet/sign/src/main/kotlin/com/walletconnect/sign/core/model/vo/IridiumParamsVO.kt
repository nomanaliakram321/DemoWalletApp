package com.nduwallet.sign.core.model.vo

import com.nduwallet.sign.core.model.type.enums.Tags

internal data class IridiumParamsVO(val tag: Tags, val ttl: TtlVO, val prompt: Boolean = false)