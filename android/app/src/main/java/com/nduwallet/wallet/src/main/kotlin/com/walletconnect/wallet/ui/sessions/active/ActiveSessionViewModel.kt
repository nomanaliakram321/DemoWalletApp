package com.nduwallet.wallet.ui.sessions.active

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nduwallet.wallet.domain.WalletDelegate
import com.nduwallet.sign.client.SignClient
import kotlinx.coroutines.flow.*

class ActiveSessionViewModel : ViewModel() {
    val activeSessionUI: StateFlow<List<ActiveSessionUI>> = WalletDelegate.wcEventModels
        .filterNotNull()
        .map {
            getLatestActiveSessions()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), getLatestActiveSessions())

    private fun getLatestActiveSessions(): List<ActiveSessionUI> {
        return SignClient.getListOfSettledSessions().filter { wcSession ->
            wcSession.metaData != null
        }.map { wcSession ->
            ActiveSessionUI(
                icon = wcSession.metaData!!.icons.first(),
                name = wcSession.metaData!!.name,
                url = wcSession.metaData!!.url,
                topic = wcSession.topic
            )
        }
    }
}