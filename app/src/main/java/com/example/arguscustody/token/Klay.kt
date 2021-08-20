package com.example.arguscustody.token

import com.klaytn.caver.crypto.KlayCredentials
import com.klaytn.caver.methods.response.KlayTransactionReceipt
import com.klaytn.caver.transaction.type.ValueTransfer
import com.klaytn.caver.utils.Convert
import com.klaytn.caver.wallet.keyring.KeyringFactory
import xyz.groundx.caver_ext_kas.CaverExtKAS
import java.math.BigInteger


// AccessKey ID: KASKLZ4IJ6W17FUFYXBC1K2A
// Secret AccessKey: zebqwA3Ym5e2ZUn3-Gw2qt3BjS-WA6Dag-4E01O0
// Authorization: Basic S0FTS0xaNElKNlcxN0ZVRllYQkMxSzJBOnplYnF3QTNZbTVlMlpVbjMtR3cycXQzQmpTLVdBNkRhZy00RTAxTzA=

// Project
// ProjectId: KASPMHXL9G9FGHK4ACCH7ZHA
// ProjectKey: TolvuMToKCWV3Nuad4IxiAa72NxiRVHfWJ2cs-gn

// Wallet / Account
// address: 0xd5fAD43640b9F5288d7403C60cFF5101439f9209
// public key: 0x04064039616d206745b18e9ab7fd4f30a6d430909a704bbc19494828e971b05891cb2ae1031e77e152ec1ef5c49d40b54cf3d7f7afc87bb313b55c201d725a0bf4

var KLAY_API = "https://node-api.klaytnapi.com/v1/klaytn"
val BaoBab = 1001
val accessKey = "KASKLZ4IJ6W17FUFYXBC1K2A"
val secretAccessKey = "zebqwA3Ym5e2ZUn3-Gw2qt3BjS-WA6Dag-4E01O0"
private const val CREDENTIALS_LEN = 64
val myKlayAddress = "0xb92965b97ff7ebd00281aed1359969ec49a65a6a"

fun klay_getBalance(address: String): String {
	val caverExtKAS = CaverExtKAS()
	caverExtKAS.initKASAPI(BaoBab, accessKey, secretAccessKey)

	val quantity = caverExtKAS.rpc.klay.getBalance(address).send().value
	val klay = Convert.fromPeb(quantity.toString(), Convert.Unit.KLAY)
	return klay.toString() + " KLAY"
}

// prepare wallet "0x0ac1aa23b15a995b0bd8e5d4e165fb5507655114"
// val privatekey = "1e84cb45d863d98c78c5f023891d6dae2c4d6339da2f1b75079f4adfb643e46b"
//	val credentials =
//		Credentials.create(privatekey) // ECKeyPair.create(privatekey.toBigInteger(16))
//	Log.d(
//		"ETH", credentials.address + "-" +
//						"%x".format(credentials.ecKeyPair.privateKey) + "-" +
//						"%x".format(credentials.ecKeyPair.publicKey)
//	)

fun klay_Send(privatekey: String, toAddress: String?, klayAmount: String) : String {
	val caverExtKAS = CaverExtKAS()
	caverExtKAS.initKASAPI(BaoBab, accessKey, secretAccessKey)

	val keyring = KeyringFactory.createFromPrivateKey(privatekey)
	val fromaddress = keyring.getAddress()
	val toaddress = toAddress // "0xd8a60A93510d86eF01125939207ecCDb3898f004"
	val amount = Convert.toPeb(klayAmount, Convert.Unit.KLAY)

	val valueTransfer: ValueTransfer = ValueTransfer.Builder()
		.setKlaytnCall(caverExtKAS.rpc.klay)
		.setFrom(fromaddress)
		.setTo(toaddress)
		.setValue(BigInteger.valueOf(amount.toLong()))
		.setGas(BigInteger.valueOf(30000))
		.build()
	valueTransfer.sign(keyring);
	val rlpEncoded = valueTransfer.rlpEncoding

	val sendResult = caverExtKAS.rpc.klay.sendRawTransaction(rlpEncoded).send()
	if (sendResult.hasError()) {
		//do something to handle error
	}
	val txHash: String = sendResult.getResult()
	println("Transaction Hash : $txHash")
	return txHash
}
