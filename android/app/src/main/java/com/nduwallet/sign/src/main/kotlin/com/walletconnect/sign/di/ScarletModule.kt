package com.nduwallet.sign.di

import android.os.Build
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.lifecycle.LifecycleRegistry
import com.tinder.scarlet.lifecycle.android.AndroidLifecycle
import com.tinder.scarlet.messageadapter.moshi.MoshiMessageAdapter
import com.tinder.scarlet.retry.LinearBackoffStrategy
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import com.nduwallet.sign.network.RelayInterface
import com.nduwallet.sign.network.data.adapter.FlowStreamAdapter
import com.nduwallet.sign.network.data.connection.ConnectionType
import com.nduwallet.sign.network.data.connection.controller.ConnectionController
import com.nduwallet.sign.network.data.connection.lifecycle.ManualConnectionLifecycle
import com.nduwallet.sign.network.data.service.RelayService
import com.nduwallet.sign.network.domain.RelayClient
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

@JvmSynthetic
internal fun scarletModule(serverUrl: String, jwt: String, connectionType: ConnectionType, relay: RelayInterface?) = module {
    val DEFAULT_BACKOFF_MINUTES = 5L
    val TIMEOUT_TIME = 5000L

    // TODO: Setup env variable for version and tag. Use env variable here instead of hard coded version
    single {
        OkHttpClient.Builder()
            .addInterceptor {
                val updatedRequest = it.request().newBuilder()
                    .addHeader("User-Agent", """wc-2/kotlin-2.0.0-rc.0/android-${Build.VERSION.RELEASE}""")
                    .build()

                it.proceed(updatedRequest)
            }
            .writeTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .readTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .callTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .connectTimeout(TIMEOUT_TIME, TimeUnit.MILLISECONDS)
            .build()
    }

    single { MoshiMessageAdapter.Factory(get()) }

    single { FlowStreamAdapter.Factory() }

    single { LinearBackoffStrategy(TimeUnit.MINUTES.toMillis(DEFAULT_BACKOFF_MINUTES)) }

    single {
        if (connectionType == ConnectionType.MANUAL) {
            ConnectionController.Manual()
        } else {
            ConnectionController.Automatic
        }
    }

    single {
        if (connectionType == ConnectionType.MANUAL) {
            ManualConnectionLifecycle(get(), LifecycleRegistry())
        } else {
            AndroidLifecycle.ofApplicationForeground(androidApplication())
        }
    }

    single {
        Scarlet.Builder()
            .backoffStrategy(get<LinearBackoffStrategy>())
            .webSocketFactory(get<OkHttpClient>().newWebSocketFactory("$serverUrl&auth=$jwt"))
            .lifecycle(get())
            .addMessageAdapterFactory(get<MoshiMessageAdapter.Factory>())
            .addStreamAdapterFactory(get<FlowStreamAdapter.Factory>())
            .build()
    }

    single { get<Scarlet>().create(RelayService::class.java) }

    single { relay ?: RelayClient(get(), get()) }
}