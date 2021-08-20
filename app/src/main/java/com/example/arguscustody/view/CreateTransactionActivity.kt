package com.example.arguscustody.view

import Tx
import Vault
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.arguscustody.R
import com.example.arguscustody.model.*
import com.example.arguscustody.adapters.BottomDialogAdapter
import com.example.arguscustody.fragment.BottomDialogFragment
import com.example.arguscustody.fragment.BottomMultiSelectDialogFragment
import com.example.arguscustody.token.eth_getBalance
import com.example.arguscustody.token.klay_getBalance
import kotlinx.android.synthetic.main.activity_create_transaction.*
import kotlinx.android.synthetic.main.dialog_confirm_create_wallet.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class CreateTransactionActivity: AppCompatActivity() {

	lateinit var bottomDialogFragment: BottomDialogFragment<Account>
	lateinit var bottomMultiSelectDialogFragment: BottomMultiSelectDialogFragment<UserModel>
	lateinit var authValidTimeDialogFragment: BottomDialogFragment<String>
	lateinit var methodType: MethodType
	private var selectedUserList: MutableList<UserModel> = ArrayList()
	private val vault : Vault = popVault()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_create_transaction)

		methodType = MethodType.SSS2OF3

		this.configureToolBar()
		this.configureAccountEditText()
		this.configureByCoin()
		this.configureParticipantsEditText()
		this.configureStartTimeEditText()
		this.configureAuthValidTimeEditText()
		this.configureCreateButton()
	}

	private fun configureToolBar() {
		this.toolBar.setNavigationIcon(R.drawable.ic_back)
		this.toolBar.setNavigationOnClickListener {
			finish()
		}
	}

	private fun configureAccountEditText() {
		this.accountEditText.setText(vault.address)
		this.coinTypeTextView.setText(vault.coinName)
		this.tokenTypeTextView.setText(vault.keyDistributionType.name)
		this.coinTypeValueTextView.setText(vault.coinName)

		// TEST
		this.recipientAddressEditText.setText("0xd8a60A93510d86eF01125939207ecCDb3898f004")
		this.valueEditText.setText("0.001")

//		val array = mutableListOf(
//			Account("My Wallet1 (T-ECDSA)", MethodType.TECDSA),
//			Account("My Wallet2 (2 of 3 SSS)", MethodType.SSS2OF3),
//			Account("My Wallet3 (3 of 5 SSS)", MethodType.SSS3OF5)
//		)
//		this.accountEditText.setOnClickListener {
//			this.showAccountChoiceDialog(array, this.accountEditText)
//		}

		if (vault.coinName == "ETH") {
			coinBalanceTextView.setText("? ETH")
			doAsync {
				val balance = eth_getBalance(vault.address)
				uiThread {
					coinBalanceTextView.setText(balance)
				}
			}
		}
		if (vault.coinName == "KLAY") {
			coinBalanceTextView.setText("? KLAY")
			doAsync {
				val balance = klay_getBalance(vault.address)
				uiThread {
					coinBalanceTextView.setText(balance)
				}
			}
		}
	}

	private fun configureByCoin() {
		if (vault.coinName == "ETH" || vault.coinName == "KLAY") {
			this.gasLayout.visibility = View.VISIBLE
		}
		if (vault.coinName == "BTC") {
			this.gasLayout.visibility = View.GONE
		}

		if (vault.keyDistributionType.keyDistributionTypeIdx == MethodType.SSS2OF3.ordinal) {
			methodType = MethodType.SSS2OF3
		}
		if (vault.keyDistributionType.keyDistributionTypeIdx == MethodType.SSS3OF4.ordinal) {
			methodType = MethodType.SSS3OF4
		}
		if (vault.keyDistributionType.keyDistributionTypeIdx == MethodType.TECDSA2OF3.ordinal) {
			methodType = MethodType.TECDSA2OF3
		}
		if (vault.keyDistributionType.keyDistributionTypeIdx == MethodType.TECDSA3OF4.ordinal) {
			methodType = MethodType.TECDSA3OF4
		}
	}

	private fun configureParticipantsEditText() {

//		val array = mutableListOf<UserModel>(
//			UserModel(123, "이선생"),
//			UserModel(234, "김선생"),
//			UserModel(456, "박선생")
//		)
		var array: MutableList<UserModel> = mutableListOf()
		vault.vaultUsers?.forEach {
			array.add(UserModel(it.user.userIdx, it.user.name))
		}
		this.participantsEditText.setOnClickListener {
			this.showUserChoiceDialog(array, this.participantsEditText)
		}
	}

	private fun configureStartTimeEditText() {
		this.startTimeEditText.setOnClickListener {
			this.showDateTimePickerDialog(this.startTimeEditText)
		}
	}

	private fun configureAuthValidTimeEditText() {
		this.authValidTimeEditText.setOnClickListener {
			val array = mutableListOf<String>("5 min", "10 min", "15min", "30min", "1 hr")
			this.showAuthValidTimeChoiceDialog(array, this.authValidTimeEditText)
		}
	}

	private fun configureCreateButton() {
		this.createButton.setOnClickListener {
			pingAuth(this) {
				if (methodType == MethodType.SSS2OF3 || methodType == MethodType.SSS3OF4
					|| methodType == MethodType.TECDSA2OF3 || methodType == MethodType.TECDSA3OF4
				) {
					// save tx info
					var tx: Tx = Tx()
					tx.vaultIdx = vault.vaultIdx
					tx.amount = this.valueEditText.text.toString().toDouble()
					tx.fromAddress = this.accountEditText.text.toString()
					tx.toAddress = this.recipientAddressEditText.text.toString()
					tx.gasPrice = this.gasPriceEditText.text.toString().toInt()
					tx.gasLimit = this.gasLimitEditText.text.toString().toInt()
					tx.timeout = 600000
					tx.status = "CREATED"
					pushTx(tx.txIdx)
					selectedUserList.forEach {
						Log.d("###CreateTX", it.id.toString() + "/" + it.name)
					}

					val json = JSONObject()
					json.put("vaultIdx", tx.vaultIdx)
					json.put("fromAddress", tx.fromAddress)
					json.put("toAddress", tx.toAddress)
					json.put("amount", tx.amount)
					json.put("gasPrice", tx.gasPrice * 1000000000)
					json.put("gasLimit", tx.gasLimit)
					json.put("timeout", tx.timeout)
					val signers = JSONArray()
					selectedUserList.forEach {
						val user = JSONObject()
						user.put("userIdx", it.id)
						signers.put(user)
					}
					json.put("signers", signers)
//				traceJson(json)
					postTx(this, {
						val txIdx = Stash.getInt("TX_ID")
						fetchTx(this, {
							val intent = Intent(this, TransactionDetailActivity::class.java)
							intent.putExtra("methodType", methodType)
							finish()
							startActivity(intent)
						}, txIdx)
					}, json)
				} else {
					this.showTransactionStartTimeDialog()
				}
			}
		}
	}

	private fun showAccountChoiceDialog(array: MutableList<Account>, editText: EditText) {
		val adapter = BottomDialogAdapter<Account>(
			BottomDialogType.ACCOUNT
		) {
			this.bottomDialogFragment.dismiss()
			editText.setText(it.name)
			methodType = it.type
			if (methodType == MethodType.TECDSA2OF3 || methodType == MethodType.TECDSA3OF4) {
				this.tECDSALayout.visibility = View.VISIBLE
			} else {
				this.tECDSALayout.visibility = View.GONE
			}
		}
		adapter.setItem(array)
		this.bottomDialogFragment = BottomDialogFragment(adapter)
		this.bottomDialogFragment.show(supportFragmentManager, "TAG")
	}

	private fun showUserChoiceDialog(array: MutableList<UserModel>, editText: EditText) {
		this.bottomMultiSelectDialogFragment = BottomMultiSelectDialogFragment<UserModel>(array, selectedUserList) {
			selectedUserList = it
			editText.setText(it.joinToString { it.name })
		}
		this.bottomMultiSelectDialogFragment.show(supportFragmentManager, "TAG")
	}

	private fun showDateTimePickerDialog(editText: EditText) {
		val currentDateTime = Calendar.getInstance()
		val startYear = currentDateTime.get(Calendar.YEAR)
		val startMonth = currentDateTime.get(Calendar.MONTH)
		val startDay = currentDateTime.get(Calendar.DAY_OF_MONTH)
		val startHour = currentDateTime.get(Calendar.HOUR_OF_DAY)
		val startMinute = currentDateTime.get(Calendar.MINUTE)

		DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, day ->
			TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, hour, minute ->
				val pickedDateTime = Calendar.getInstance()
				pickedDateTime.set(year, month, day, hour, minute)

				val yearStr = year.toString()
				val monthStr = if (month + 1 < 10) "0" + (month + 1) else (month + 1).toString()
				val dayStr = if (day < 10) "0$day" else day.toString()
				val hourStr = if (hour < 10) "0$hour" else hour.toString()
				val minuteStr = if (minute < 10) "0$minute" else minute.toString()
				val pickedDate = "$yearStr-$monthStr-$dayStr $hourStr:$minuteStr"

				editText.setText(pickedDate)
			}, startHour, startMinute, false).show()
		}, startYear, startMonth, startDay).show()
	}

	private fun showAuthValidTimeChoiceDialog(array: MutableList<String>, editText: EditText) {
		val adapter = BottomDialogAdapter<String>(
			BottomDialogType.VALID_TIME
		) {
			this.authValidTimeDialogFragment.dismiss()
			editText.setText(it)
		}
		adapter.setItem(array)
		this.authValidTimeDialogFragment = BottomDialogFragment(adapter)
		this.authValidTimeDialogFragment.show(supportFragmentManager, "TAG")
	}

	private fun showTransactionStartTimeDialog() {
		val mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_default, null)
		val mBuilder = AlertDialog.Builder(this).setView(mDialogView).create()

		mBuilder.show()

		mDialogView.confirmTextView.text = "해당 트랜잭션은\n2020.02.26 13:00부터\n참여 가능합니다."
		mDialogView.confirmButton.setOnClickListener {
			val intent = Intent(this, TransactionDetailActivity::class.java)
			intent.putExtra("methodType", methodType)
			startActivity(intent)
		}
	}
}