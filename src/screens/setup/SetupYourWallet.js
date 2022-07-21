/* ----- ---- --- -- -
 * Copyright 2020 The Axiom Foundation. All Rights Reserved.
 *
 * Licensed under the Apache License 2.0 (the "License").  You may not use
 * this file except in compliance with the License.  You can obtain a copy
 * in the file LICENSE in the source distribution or at
 * https://www.apache.org/licenses/LICENSE-2.0.txt
 * - -- --- ---- -----
 */

import React, { Component } from 'react'
import {  NativeModules } from 'react-native'
import SetupStore from '../../stores/SetupStore'
import EntropyHelper from '../../helpers/EntropyHelper'
import { SetupContainer } from '../../components/setup'
import AppConstants from '../../AppConstants'
import LogStore from '../../stores/LogStore'
import FlashNotification from '../../components/common/FlashNotification'
import { LargeButtons, ParagraphText } from '../../components/common'
var _ = require('lodash')

class SetupYourWallet extends Component {
  constructor (props) {
    super()
    this.state={
      recoveryPhrase: [],
    }
    props.navigation.addListener('blur', FlashNotification.hideMessage)
  }
  showNextSetup = async () => {
   
    
     this.props.navigation.navigate('SetupRecoveryPhrase',{
      recoveryPhrase:this.state.recoveryPhrase,
     })
  }

  componentDidMount = () => {
    try{
      this.getPhraseRecovery()
      }
      catch(e){
        console.log('error 2',e);
      }
    this.fromHamburger = this.props.route.params?.fromHamburger ?? null
    console.log('this.fromHamburger',this.fromHamburger)


  }

  getPhraseRecovery=async()=>{
   const entropy= await EntropyHelper.generateEntropy()
console.log('SetupStore.entropy',SetupStore.entropy,'entropy',entropy)
    const KeyaddrManager = NativeModules.KeyaddrManager;
        const seed = await KeyaddrManager.keyaddrWordsFromBytes(
          AppConstants.APP_LANGUAGE,
          entropy
        )
    
    console.log('seed',seed);
    
        const seedBytes = await KeyaddrManager.keyaddrWordsToBytes(
          AppConstants.APP_LANGUAGE,
          seed
        )
        console.log('seedBytes',seedBytes);
        if (!_(seedBytes).isEqual(entropy)) {
          this.showExitApp()
        } else {
          LogStore.log(`the seedBytes and entropy are equal.`)
        }
      // console.log(bip39.generateMnemonic(),"bip39"
      // console.log(randomWords(12),"random number")
        // const recoveryPhrase = randomWords(12)
          // const recoveryPhrase =a;
          const recoveryPhrase = seed.split(/\s+/g)
          
        this.setState({ recoveryPhrase: recoveryPhrase })

  }

  showExitApp () {
    Alert.alert(
      '',
      `Problem occurred validating the phrase, please contact Oneiro.`,
      [
        {
          text: 'Exit app',
          onPress: () => {
            RNExitApp.exitApp()
          }
        }
      ],
      { cancelable: false }
    )
  }
  goBack = () => {
    this.props.navigation.goBack()
  }

  render () {
    return (
      <SetupContainer
        {...this.props}
        goBack={this.fromHamburger ? this.goBack : null}
        pageNumber={2}
      >
        <ParagraphText>
          Next we will give you a recovery phrase. This is critical to restoring
          your wallet. You risk losing access to your funds if you do not WRITE
          IT DOWN and store it in a secure location. Do not save this phrase on
          your device or in the cloud. Do not do this step in a public place
          where someone looking over your shoulder could see this phrase.
        </ParagraphText>
        <LargeButtons
          scroll
          sideMargins
          bottom
          onPress={() => this.showNextSetup()}
        >
          Get my recovery phrase
        </LargeButtons>
      </SetupContainer>
    )
  }
}

export default SetupYourWallet
