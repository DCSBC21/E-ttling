package com.example.arguscustody.token

import android.util.Log
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.response.EthGetBalance
import org.web3j.protocol.core.methods.response.EthSendTransaction
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import java.math.BigInteger
import java.util.concurrent.ExecutionException
import org.jetbrains.anko.doAsync
import org.web3j.crypto.*
import java.util.*

//const ropstenWeb3 = new Web3(new Web3.providers.HttpProvider('https://ropsten.infura.io/v3/c5ead447a3b74d1294dadf082cbc5110'))
//const rinkebyWeb3 = new Web3(new Web3.providers.HttpProvider('https://rinkeby.infura.io/v3/f491db8c1a9141cf9677321929138b4a'))
//const kovanWeb3 = new Web3(new Web3.providers.HttpProvider('https://kovan.infura.io/v3/45ea6e32af764f3cb6df2c21240b0ff1'))
var KOVAN_API = "https://kovan.infura.io/v3/2dd969a35811462c85ae0ebd7ad768a2"
var ROPSTEN_API = "https://ropsten.infura.io/v3/2dd969a35811462c85ae0ebd7ad768a2"
var RINKEBY_API = "https://rinkeby.infura.io/v3/2dd969a35811462c85ae0ebd7ad768a2"
val web3j: Web3j = Web3j.build(HttpService(ROPSTEN_API))
private const val CREDENTIALS_LEN = 64
val myEthAddress = "0xd8a60A93510d86eF01125939207ecCDb3898f004"

//이더리움 노드에게 지정한 Address 의 잔액을 조회한다.
fun eth_getBalance(address: String): String {
	var result: String = "0"

	if (address.length > 0) {
		var ethGetBalance: EthGetBalance? = null
		try {
			ethGetBalance =
				web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST)
					.sendAsync().get()
			val wei = ethGetBalance.balance
			result = Convert.fromWei(wei.toString(), Convert.Unit.ETHER).toString()
		} catch (e: InterruptedException) {
			e.printStackTrace()
		} catch (e: ExecutionException) {
			e.printStackTrace()
		}
	}

	return result + " ETH"
}

/*
    1.setPot이라는 function을 호출하기 위한 이에 맞는 ABI encoding어야 합니다. Function은 그 기본이 됩니다.
    2.직접 만든 함수를 호출합니다.
    3.한 계정에서 비용이 발생하는 transaction을 발생시키기 위해서는 계정을 해제(본인인증)을 할 필요가 있습니다.
    따라서 PersonUnlockAccount를 통하여 unlock을 진행할 필요가 있습니다.
    4.account에 대한 nonce값을 가져옵니다.
    5.ethereum에 전송할 데이터를 제작합니다.
    6.ethereum을 호출하고 transaction Hash값을 얻어옵니다.
     eth_sendTransaction은 바로 결과값을 가져올 수 없습니다.
    결과값은 block에 쓰여졌을 때 log의 형태로 가져올 수 있습니다.
    7.ethSendTransaction method를 통해 호출 해서 반환받은 transaction Hash값을 통하여 Receipt를 얻기위한 method입니다.
    8.eth_GetTransactionReceipt를 호출합니다.
    9.transactionHash를 확인합니다.
    (결과값을 보시면 data가 없는것을 알 수 있습니다. 데이터를 receipt에서 바로 확인하시고 싶으시면, logs가 출력이되어야 합니다.
    이것은 event가 호출되었을 경우에만 호출되어집니다. 그렇지 않다면 return값을 가져야 하구요. event는 다음시간에 보겠습니다. )
     */
fun eth_createCredentials(seed: String): Credentials? {
	return if (seed.toByteArray().size == CREDENTIALS_LEN) {
		Credentials.create(String(seed.toByteArray(), 0, CREDENTIALS_LEN))
	} else {
		null
	}
}

fun eth_getAddress(credentials: Credentials) : String {
	return credentials.address
}

fun eth_getEcKeyPair(credentials: Credentials) : ECKeyPair {
	return credentials.ecKeyPair
}

fun eth_getAddressFromPublickey(publickey: String) : String {
	// NOT WORKING
	return Hash.sha3String(publickey).substring(22,42)
}

fun getWeiFromEther(value: String?): BigInteger? {
	return Convert.toWei(value, Convert.Unit.ETHER).toBigInteger()
}

@Throws(ExecutionException::class, InterruptedException::class)
fun getGasPrice(): BigInteger? {
	return web3j.ethGasPrice().sendAsync().get().getGasPrice()
}

fun getGasLimit(): BigInteger {
	return "21000".toBigInteger()
	// return Convert.toWei("21000", Convert.Unit.WEI).toBigInteger()
}

@Throws(java.lang.Exception::class)
fun getNonce(address: String?): BigInteger? {
	return web3j.ethGetTransactionCount(
		address,
		DefaultBlockParameterName.LATEST
	).sendAsync().get().getTransactionCount()
}

@Throws(Exception::class)
fun eth_createEtherTransaction(fromAddress: String?, toAddress: String?, value: String?): RawTransaction? {
	val value: BigInteger? = getWeiFromEther(value)
	val nonce: BigInteger? = getNonce(fromAddress)
	val gasPrice: BigInteger? = getGasPrice() // "10000".toBigInteger()
	val gasLimit: BigInteger = getGasLimit() // "21000".toBigInteger()
//	Log.d("ETH", gasPrice.toString() + "-" + gasLimit.toString())
	return RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, toAddress, value)
}

fun eth_Send(privatekey: String, toAddress: String?, ethAmount: String?) : String {
	// prepare wallet "0x0ac1aa23b15a995b0bd8e5d4e165fb5507655114"
//	val privatekey = "1e84cb45d863d98c78c5f023891d6dae2c4d6339da2f1b75079f4adfb643e46b"
	val credentials =
		Credentials.create(privatekey) // ECKeyPair.create(privatekey.toBigInteger(16))
	Log.d(
		"ETH", credentials.address + "-" +
						"%x".format(credentials.ecKeyPair.privateKey) + "-" +
						"%x".format(credentials.ecKeyPair.publicKey)
	)

	// send eth
	val fromaddress = credentials.address
	val toaddress = toAddress // "0xd8a60A93510d86eF01125939207ecCDb3898f004"
	val amount = ethAmount // "0.001"
	val rawTx = eth_createEtherTransaction(fromaddress, toaddress, amount)
	if (rawTx != null) {
		Log.d("ETH1", rawTx.nonce.toString(16))
		Log.d("ETH2", rawTx.gasPrice.toString(16))
		Log.d("ETH3", rawTx.gasLimit.toString(16))
		Log.d("ETH4", rawTx.to)
		Log.d("ETH5", rawTx.value.toString(16))
//		Log.d("ETH6", Base64.getEncoder().encodeToString(rawTx.data.toByteArray()))
//		Log.d("ETH7", rawTx.gasPremium.toString(16))
//		Log.d("ETH8", rawTx.feeCap.toString(16))
	}
	val signedMessage = TransactionEncoder.signMessage(rawTx, credentials)
	val hexValue = Numeric.toHexString(signedMessage)
	val ethSendTransaction: EthSendTransaction =
		web3j.ethSendRawTransaction(hexValue).send() // Send transaction
	Log.d("ETH9", hexValue)
	val transactionHash = ethSendTransaction.transactionHash
	Log.d("ETH", transactionHash)

	return transactionHash
}
