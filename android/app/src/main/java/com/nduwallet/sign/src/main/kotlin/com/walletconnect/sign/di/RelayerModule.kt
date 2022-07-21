package com.nduwallet.sign.di

import com.nduwallet.sign.crypto.Codec
import com.nduwallet.sign.crypto.data.codec.ChaChaPolyCodec
import com.nduwallet.sign.json_rpc.data.JsonRpcSerializer
import com.nduwallet.sign.json_rpc.domain.RelayerInteractor
import com.nduwallet.sign.util.NetworkState
import org.koin.dsl.module

@JvmSynthetic
internal fun relayerModule() = module {

    single<Codec> {
        ChaChaPolyCodec(get())
    }

    single {
        JsonRpcSerializer(get())
    }

    single {
        NetworkState(get())
    }

    single {
        RelayerInteractor(get(), get(), get(), get(), get())
    }
}