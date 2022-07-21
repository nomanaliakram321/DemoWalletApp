package com.nduwallet.sign.di

import com.nduwallet.sign.client.Sign
import com.nduwallet.sign.client.mapper.toEngineAppMetaData
import com.nduwallet.sign.engine.domain.SignEngine
import org.koin.dsl.module

@JvmSynthetic
internal fun engineModule(metadata: Sign.Model.AppMetaData) = module {

    single {
        metadata.toEngineAppMetaData()
    }

    single {
        SignEngine(get(), get(), get(), get())
    }
}