/* ----- ---- --- -- -
 * Copyright 2020 The Axiom Foundation. All Rights Reserved.
 *
 * Licensed under the Apache License 2.0 (the "License").  You may not use
 * this file except in compliance with the License.  You can obtain a copy
 * in the file LICENSE in the source distribution or at
 * https://www.apache.org/licenses/LICENSE-2.0.txt
 * - -- --- ---- -----
 */

import { Alert } from 'react-native'
import AppConstants from '../AppConstants'
import LogStore from '../stores/LogStore'

class SetupStore {
  constructor () {
    if (!SetupStore.instance) {
      SetupStore.instance = this
    }

    this._userId = ''
    // Default to 1 account created
    this._numberOfAccounts = 1
    this._qrCode = ''
    this._encryptionPassword = ''
    this._entropy = ''
    this._recoveryPhrase = ''
    this._shuffledMap = []
    this._addressType = AppConstants.MAINNET_ADDRESS
    this._walletId = ''
  }

  set userId (userId) {
    this._userId = userId
  }

  get userId () {
    return this._userId
  }

  set numberOfAccounts (numberOfAccounts) {
    console.log('numberOfAccounts:()')
    this._numberOfAccounts = numberOfAccounts
  }

  get numberOfAccounts () {
    console.log('numberOfAccounts:()')
    return this._numberOfAccounts
  }

  set qrCode (qrCode) {
    console.log('qrCode:()')
    this._qrCode = qrCode
  }

  get qrCode () {
    console.log('getqr:()')
    return this._qrCode
  }

  set encryptionPassword (encryptionPassword) {
    this._encryptionPassword = encryptionPassword
  }

  get encryptionPassword () {
    console.log('encryptionPassword:()')
    return this._encryptionPassword
  }

  set entropy (entropy) {
    console.log(' set entropy:()')
    this._entropy = entropy
  }

  get entropy () {
    console.log(' get recoveryPhrase:()')

    return this._entropy
  }

  set recoveryPhrase (recoveryPhrase) {
    console.log(' set recoveryPhrase:()')

    this._recoveryPhrase = recoveryPhrase.slice()
  }

  get recoveryPhrase () {
    console.log(' get recoveryPhrase:()')

    return this._recoveryPhrase
  }

  set shuffledWords (shuffledWords) {
    console.log(' set shuffledWords:()')

    this._shuffledWords = shuffledWords.slice()
  }

  get shuffledWords () {
    console.log(' get shuffledMap:()')
    return this._shuffledWords
  }

  set shuffledMap (shuffledMap) {
    console.log(' set shuffledMap:()')
    this._shuffledMap = shuffledMap.slice()
  }

  get shuffledMap () {
    console.log(' set shuffledMap:()')
    return this._shuffledMap
  }

  get addressType () {
    console.log(' get addressType:()')
    return this._addressType
  }

  set walletId (walletId) {
    console.log(' set walletId:()')
    this._walletId = walletId
  }

  get walletId () {
    console.log(' get walletId:()')
    return this._walletId
  }

  printData = () => {
    LogStore.log(`SetupStore.userId ${this._userId}`)
    LogStore.log(`SetupStore.numberOfAccounts ${this._numberOfAccounts}`)
    LogStore.log(`SetupStore.qrCode ${this._qrCode}`)
    LogStore.log(`SetupStore.entropy ${this._entropy}`)
    LogStore.log(`SetupStore.walletId ${this._walletId}`)
    LogStore.log(`SetupStore.addressType ${this._addressType}`)
  }

  reset = () => {
    this._userId = ''
    this._numberOfAccounts = 0
    this._qrCode = ''
    this._encryptionPassword = ''
    this._entropy = ''
    this._recoveryPhrase = ''
    this._shuffledMap = []
    this._addressType = AppConstants.MAINNET_ADDRESS
    this._walletId = ''
  }
}

const instance = new SetupStore()

export default instance
