package com.nduwallet.sign.core.exceptions.peer

import com.nduwallet.sign.core.exceptions.DISCONNECT_MESSAGE

internal sealed class PeerReason {
    abstract val message: String
    abstract val code: Int


    object UserDisconnected : PeerError() {
        override val message: String = DISCONNECT_MESSAGE
        override val code: Int = 6000
    }
}