@file:JvmSynthetic

package com.nduwallet.sign.crypto

import com.nduwallet.sign.core.exceptions.client.WalletConnectException
import com.nduwallet.sign.core.model.vo.Key

internal interface KeyStore {
    fun getSymmetricKey(tag: String): String
    fun setSymmetricKey(tag: String, key: Key)

    @Throws(WalletConnectException.InternalError::class)
    fun getKeys(tag: String): Pair<String, String>
    fun setKeys(tag: String, key1: Key, key2: Key)

    fun deleteKeys(tag: String)
    fun checkKeys(tag: String): Boolean
}