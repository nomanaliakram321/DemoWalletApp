package com.nduwallet.sign.common.network.adapters

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.nduwallet.sign.core.adapters.TtlAdapter
import com.nduwallet.sign.core.model.vo.TtlVO
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TtlAdapterTest {
    private val moshi = Moshi.Builder()
        .add { _, _, _ ->
            TtlAdapter
        }
        .add(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun toJson() {
        val ttl = TtlVO(100L)
        val expected = """"${ttl.seconds}""""

        val ttlJson = moshi.adapter(TtlVO::class.java).toJson(ttl)

        Assertions.assertEquals(expected, """"$ttlJson"""")
    }
}