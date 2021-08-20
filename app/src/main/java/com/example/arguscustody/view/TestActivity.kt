package com.example.arguscustody.view

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.arguscustody.R
import com.example.arguscustody.model.*
import com.example.arguscustody.token.*
import com.klaytn.caver.crypto.KlayCredentials
import com.klaytn.caver.methods.response.KlayTransactionReceipt
import com.klaytn.caver.tx.ValueTransfer
import com.klaytn.caver.tx.model.ValueTransferTransaction
import com.klaytn.caver.utils.Convert
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.activity_test.*
import kotlinx.android.synthetic.main.dialog_confirm_create_wallet.view.*
import kotlinx.android.synthetic.main.dialog_default.view.*
import kotlinx.android.synthetic.main.fragment_my_page.view.*
import kotlinx.android.synthetic.main.progressbar_with_text.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.web3j.crypto.Credentials
import xyz.groundx.caver_ext_kas.CaverExtKAS


class TestActivity : AppCompatActivity() {
	companion object {
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_test)
		this.configureButtons()
		this.configureLibMPETest()
	}

	private fun configureButtons() {

		this.ethButton.setOnClickListener {
//			showConfirmCreateWalletDialog()
//			showExpireAuthValidTimeDialog()

			// ETH
			// Key String TEST
//			val stringKey = "1e84cb45d863d98c78c5f023891d6dae2c4d6339da2f1b75079f4adfb643e46b"
//			val s1: String = String(stringKey.toByteArray())
//			// 1e84cb45d863d98c78c5f023891d6dae2c4d6339da2f1b75079f4adfb643e46b
//			Log.d("TTT", s1 + "(" + stringKey.toByteArray().count().toString() + ")")
//			val s2: String = String(stringKey.toHex()) // ��E�cٌx��#�m�,Mc9�/u�J߶C�k
//			Log.d("TTT", s2 + "(" + stringKey.toHex().count().toString() + ")")
//			val s3: String = stringKey.toHex().toString() // [B@14c1637
//			Log.d("TTT", s3)
			// EOS
//			val key = eos_createKey("tigertigerks")

			// ETH TEST42 Transfer TEST
			doAsync {
				val fromAddress = "0x0ac1aa23b15a995b0bd8e5d4e165fb5507655114"
				val privatekey = "1e84cb45d863d98c78c5f023891d6dae2c4d6339da2f1b75079f4adfb643e46b"

				// Log Balance before TX
				var fromBalance = eth_getBalance(fromAddress)
				Log.d("ETH1", fromAddress.substring(0, 10) + " balance is " + fromBalance.toString())
				var toBalance = eth_getBalance(myKlayAddress)
				Log.d("ETH1", myKlayAddress.substring(0, 10) + " balance is " + toBalance.toString())

				val txhash = eth_Send(privatekey, myEthAddress, "0.001")

				// Log Balance after TX
				fromBalance = eth_getBalance(fromAddress)
				Log.d("ETH2", fromAddress.substring(0, 10) + " balance is " + fromBalance.toString())
				toBalance = eth_getBalance(myKlayAddress)
				Log.d("ETH2", myKlayAddress.substring(0, 10) + " balance is " + toBalance.toString())

				uiThread {
					Log.d("ETH9", "eth_Send Success : " + txhash)
				}
			}
		}

		this.klayButton.setOnClickListener {
			// Klaytn
			doAsync {
				val fromAddress = "0x0ac1aa23b15a995b0bd8e5d4e165fb5507655114"
				val privatekey = "1e84cb45d863d98c78c5f023891d6dae2c4d6339da2f1b75079f4adfb643e46b"

				// Log Balance before TX
				var fromBalance = klay_getBalance(fromAddress)
				Log.d("KLAY1", fromAddress.substring(0,10) + " balance is " + fromBalance.toString())
				var toBalance = klay_getBalance(myKlayAddress)
				Log.d("KLAY1", myKlayAddress.substring(0,10) + " balance is " + toBalance.toString())

				val txhash = klay_Send(privatekey, myKlayAddress, "0.001")

				// Log Balance after TX
				Thread.sleep(2000)
				fromBalance = klay_getBalance(fromAddress)
				Log.d("KLAY2", fromAddress.substring(0,10) + " balance is " + fromBalance.toString())
				toBalance = klay_getBalance(myKlayAddress)
				Log.d("KLAY2", myKlayAddress.substring(0,10) + " balance is " + toBalance.toString())

				uiThread {
					Log.d("KLAY9", "klay_Send Success : " + txhash)
				}
			}
		}
	} // configureButtons

	private fun configureLibMPETest() {
		text_server.setText(MPE_HOST())

		buttonGetKey.setOnClickListener {
			val vaultIdxString = textVaultIdx.text.toString()
			val vaultIdx = vaultIdxString.toInt()
			val key = getKeyStoreHWKEY(vaultIdx)
			Log.d("KEY"+vaultIdxString, key)
			text_keygen.setText("gg18key: " + key)

			// TEST
//			var vault = getVaultInStash(vaultIdx)
//			Log.d("TEST1", vault.getVaultInfoWithKey())
//			vault.key = getKeyStoreHWKEY(vault.vaultIdx)
//			putHwKey(this, {
//				Log.d("TEST2", "putHwKey success: " + vault.getVaultInfo())
//			}, vault)
		}

		buttonSetKey.setOnClickListener {
			val vaultIdxString = textVaultIdx.text.toString()
			val vaultIdx = vaultIdxString.toInt()
			val key = text_keygen.text.toString()
			putKeyStoreHWKEY(vaultIdx, key)
			Log.d("KEY"+vaultIdxString, key)
		}

		buttonGetSign.setOnClickListener {
			val vaultIdxString = textVaultIdx.text.toString()
			val sign = getSignStore(vaultIdxString.toInt())
			Log.d("SIGN"+vaultIdxString, sign)
			text_sign.setText("gg18sign: " + sign)
		}

		button_keygen.setOnClickListener {
//			val key = MainActivity.Instance.gg20keygen(text_server.text.toString(), text_parties.text.toString(), text_threshold.text.toString(), "11", "1")
			val key = MainActivity.Instance.gg18keygen(text_server.text.toString(), text_parties.text.toString(), text_threshold.text.toString(), "1", "1")
			text_keygen.setText("gg20keygen generated: " + key.substring(0, 200))
			Stash.put("KEYSTORE", key)
//			Log.d("KEYSTORE1", key)
		}

		button_sign.setOnClickListener {
			val key = Stash.getString("KEYSTORE")
//			Log.d("KEYSTORE2", key)
//			val sign = MainActivity.Instance.gg20sign(text_server.text.toString(), text_parties.text.toString(), text_threshold.text.toString(), key, text_message.text.toString(), "11", "1")
			val sign = MainActivity.Instance.gg18sign(text_server.text.toString(), text_parties.text.toString(), text_threshold.text.toString(), key, text_message.text.toString(), "1", "1")
			text_sign.setText("gg20sign generated: " + sign)
		}

		button_set.setOnClickListener {
			// save server
			val startidx = matchDetails(text_server.text.toString(), "://") + 3
			val endidx = matchDetails(text_server.text.toString(), ":", startidx)
			Log.d("HELLO1", startidx.toString() + "-" + endidx.toString())
			val server_ip = text_server.text.toString().substring(startidx, endidx)
			Log.d("HELLO2", server_ip + ":" + startidx.toString() + "-" + endidx.toString())
			Stash.put("SERVER_IP", server_ip)
			Log.d("HELLO3", Stash.getString("SERVER_IP"))
		}

		button_rust.setOnClickListener {
			// mpe library
			text_keygen.setText(MainActivity.Instance.helloGg20Keygen("World"))
			text_sign.setText(MainActivity.Instance.helloCommon("World"))

//			val x = "90edd3c25d2764e78e7ba077b0f6bcaa487be34fbe7f434f5394a09ec4628eb5"
//			val y = "07c6ad428f64d84d79a75702a8db3aacc6daa800473904e46dca89843fd6be8f"
//			val hex = x + y
//			val big_int = BigInteger(hex, 16)
//			val binary_string = String(big_int.toByteArray())
//			val address = eth_getAddressFromPublickey(binary_string)
//			Log.d("ETH", eth_getAddressFromPublickey(binary_string))
//			Log.d("ETH", eth_getAddressFromPublickey(binary_string.encodeUtf8()))

			// EOS Transfer TEST
//			val account1 = "tigertigerks"
//			val privatekey1 = "5JGHU9PBHuFkBgHevDvbtve5Qo3miafvR8xAwNfXEudgy56AnwM"
//			val account2 = "tigertigerkt"
//			val privatekey2 = "5JnYAvpKR79k3iQwGCv7omS5uCMi2z3enSmYNnFntPdbBUxJHCE"
//			val amount = "1.0000 EOS"
//			val memo = ""
//			eos_getBalance(account1)
//			eos_getBalance(account2)
//			eos_Transfer(account1, account2, privatekey1, amount, memo)
//			eos_getBalance(account1)
//			eos_getBalance(account2)
		}
	}

	private fun showConfirmCreateWalletDialog() {
		val mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_create_wallet, null)
		val mBuilder = AlertDialog.Builder(this).setView(mDialogView).create()

		mBuilder.show()

		mDialogView.confirmButton.setOnClickListener {
			val intent = Intent(this, MainActivity::class.java)
			intent.putExtra("selectTab", "WALLET")
			startActivity(intent)
		}
		mDialogView.cancelButton.setOnClickListener {
			mBuilder.dismiss()
		}
	}

	private fun showExpireAuthValidTimeDialog() {
		val mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_default, null)
		val mBuilder = android.app.AlertDialog.Builder(this).setView(mDialogView).create()

		mBuilder.show()

//		mDialogView.confirmTextView.text = "인증 시간 만료"
		mDialogView.dialogProgressBar.visibility = View.VISIBLE
		mDialogView.dialogProgressBar.setBackgroundColor(Color.argb(255, 255, 255, 255))
		mDialogView.progressBarText.setText("인증 시간 만료")

		mDialogView.confirmButton.setOnClickListener {
			mDialogView.dialogProgressBar.visibility = View.GONE
			mBuilder.cancel()
			//val intent = Intent(this, MainActivity::class.java)
			//intent.putExtra("selectTab", "WALLET")
			//startActivity(intent)
		}
	}
}