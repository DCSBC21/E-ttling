package com.example.arguscustody.view

import CoinType
import KeyDistributionType
import User
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.arguscustody.R
import com.example.arguscustody.model.Stash
import com.example.arguscustody.adapters.BottomDialogAdapter
import com.example.arguscustody.fragment.BottomDialogFragment
import com.example.arguscustody.fragment.BottomMultiSelectDialogFragment
import com.example.arguscustody.model.*
import kotlinx.android.synthetic.main.activity_create_wallet.*
import kotlinx.android.synthetic.main.dialog_confirm_create_wallet.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


class CreateWalletActivity : AppCompatActivity() {
	var userList: ArrayList<User>? = arrayListOf()
	var coinTypeList: ArrayList<CoinType>? = arrayListOf()
	var keyDistributionTypeList: ArrayList<KeyDistributionType>? = arrayListOf()

	lateinit var coinTypeBottomDialogFragment: BottomDialogFragment<CoinTypeModel>
	lateinit var authMethodBottomDialogFragment: BottomDialogFragment<AuthMethodModel>
	lateinit var authValidTimeDialogFragment: BottomDialogFragment<String>
	lateinit var userBottomDialogFragment: BottomMultiSelectDialogFragment<UserModel>
	lateinit var adminBottomDialogFragment: BottomMultiSelectDialogFragment<UserModel>
	private var coinTypeIdx: Int = 0
	private var keyDistributionTypeidx: Int = 0
	lateinit var authMethodType: AuthMethodType
	private var selectedUserList: MutableList<UserModel> = ArrayList()
	private var selectedAdminList: MutableList<UserModel> = ArrayList()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_create_wallet)

		userList = Stash.getArrayList("USER_LIST", User::class.java)
//		userList?.forEach {
//			Log.d("###3 UserList", it.name)
//		}
		coinTypeList = Stash.getArrayList("COINTYPE_LIST", CoinType::class.java)
//		coinTypeList?.forEach {
//			Log.d("###3 CoinTypeList", it.name)
//		}
		keyDistributionTypeList = Stash.getArrayList("KEYDISTRIBUTIONTYPE_LIST", KeyDistributionType::class.java)
//		keyDistributionTypeList?.forEach {
//			Log.d("###3 KeyDistTypeList", it.type.toString())
//		}

		this.configureToolBar()
		this.configureCoinTypeEditText()
		this.configureAuthMethodEditText()
		this.configureStartTimeEditText()
		this.configureAuthValidTimeEditText()
		this.configureUserEditText()
		this.configureAdminEditText()
		this.configureCreateButton()
	}

	private fun configureToolBar() {
		this.toolBar.setNavigationIcon(R.drawable.ic_back)
		this.toolBar.setNavigationOnClickListener {
			finish()
		}
	}

	private fun configureCoinTypeEditText() {
		this.coinTypeEditText.setOnClickListener {
//			val array = mutableListOf(
//				CoinTypeModel(1, "Bitcoin"),
//				CoinTypeModel(2, "Ethereum"),
//				CoinTypeModel(3, "EOS")
//			)
			var array: MutableList<CoinTypeModel> = mutableListOf()
			coinTypeList = Stash.getArrayList("COINTYPE_LIST", CoinType::class.java)
			coinTypeList?.forEach {
				array.add(CoinTypeModel(it.coinTypeIdx, it.name))
			}
			this.showCoinTypeChoiceDialog(array, this.coinTypeEditText)
		}
	}

	private fun configureAuthMethodEditText() {
		this.authMethodEditText.setOnClickListener {
//			val array = mutableListOf(
//				AuthMethodModel(1, "2 of 3 SSS", AuthMethodType.SSS),
//				AuthMethodModel(2, "3 of 4 SSS", AuthMethodType.SSS),
//				AuthMethodModel(3, "2 of 3 T-ECDSA", AuthMethodType.TECDSA),
//				AuthMethodModel(4, "3 of 4 T-ECDSA", AuthMethodType.TECDSA)
//			)
			var array: MutableList<AuthMethodModel> = mutableListOf()
			keyDistributionTypeList = Stash.getArrayList("KEYDISTRIBUTIONTYPE_LIST", KeyDistributionType::class.java)
			keyDistributionTypeList?.forEach {
				if (it.type == 0) {
					array.add(AuthMethodModel(it.keyDistributionTypeIdx, it.name, AuthMethodType.SSS))
				} else if (it.type == 1) {
					array.add(AuthMethodModel(it.keyDistributionTypeIdx, it.name, AuthMethodType.TECDSA))
				}
			}
			this.showAuthMethodChoiceDialog(array, this.authMethodEditText)
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

	private fun configureUserEditText() {
		this.userEditText.setOnClickListener {
//			val array = mutableListOf(
//				UserModel(1, "홍길동"),
//				UserModel(2, "성춘향"),
//				UserModel(3, "이몽룡")
//			)
			var array: MutableList<UserModel> = mutableListOf()
			userList = Stash.getArrayList("USER_LIST", User::class.java)
			userList?.forEach {
				array.add(UserModel(it.userIdx, it.name))
			}
			this.showUserChoiceDialog(array, this.userEditText)
		}
	}

	private fun configureAdminEditText() {
		this.adminEditText.setOnClickListener {
//			val array = mutableListOf(
//				UserModel(1, "홍길동"),
//				UserModel(2, "성춘향"),
//				UserModel(3, "이몽룡")
//			)
			var array: MutableList<UserModel> = mutableListOf()
			userList = Stash.getArrayList("USER_LIST", User::class.java)
			userList?.forEach {
				array.add(UserModel(it.userIdx, it.name))
			}
			this.showAdminChoiceDialog(array, this.adminEditText)
		}
	}

	private fun configureCreateButton() {
		this.createButton.setOnClickListener {
			pingAuth(this) {
				if (authMethodType == AuthMethodType.SSS
					|| authMethodType == AuthMethodType.TECDSA
				) {

//				Log.d("###1 vaultName", this.walletNameEditText.text.toString())
//				Log.d("###1 coinTypeIdx", coinTypeIdx.toString())
//				Log.d("###1 keyDistTypeIdx", keyDistributionTypeidx.toString())
//				selectedAdminList.forEach {
//					Log.d("###1 admin", it.id.toString() + ":" + it.name)
//				}
//				selectedUserList.forEach {
//					Log.d("###1 user", it.id.toString() + ":" + it.name)
//				}

					val json = JSONObject()
					json.put("name", this.walletNameEditText.text.toString())
					json.put("coinTypeIdx", coinTypeIdx.toString())
					json.put("keyDistributionTypeIdx", keyDistributionTypeidx.toString())
					val adminsJson = JSONArray()
					selectedAdminList.forEach {
						val admin = JSONObject()
						admin.put("userIdx", it.id.toString())
						adminsJson.put(admin)
					}
					json.put("vaultAdmins", adminsJson)
					val usersJson = JSONArray()
					selectedUserList.forEach {
						val user = JSONObject()
						user.put("userIdx", it.id.toString())
						usersJson.put(user)
					}
					json.put("vaultUsers", usersJson)
//				val gson = GsonBuilder().create()
//				val jsonString = gson.toJson(json)
//				Log.d("###1 jsonString", jsonString)

					postVault(this, {
						val vaultIdx = Stash.getInt("VAULT_ID")
						fetchVault(this, {
							val intent = Intent(this, MainActivity::class.java)
							intent.putExtra("selectTab", "WALLET")
							startActivity(intent)
						}, vaultIdx)
					}, json)

				} else {
					// NOT USED NOW
					val intent = Intent(this, X_AuthenticateTECDSAWalletActivity::class.java)
					startActivity(intent)
				}
			}
		}
	}

	private fun showCoinTypeChoiceDialog(array: MutableList<CoinTypeModel>, editText: EditText) {
		val adapter =
			BottomDialogAdapter<CoinTypeModel>(
				BottomDialogType.COIN
			) {
				this.coinTypeBottomDialogFragment.dismiss()
				editText.setText(it.name)
				coinTypeIdx = it.id
//				if (it.id == 2) {
//					this.showERC20WalletDialog();
//				}
			}
		adapter.setItem(array)
		this.coinTypeBottomDialogFragment = BottomDialogFragment(adapter)
		this.coinTypeBottomDialogFragment.show(supportFragmentManager, "TAG")
	}

	private fun showAuthMethodChoiceDialog(array: MutableList<AuthMethodModel>, editText: EditText) {
		val adapter =
			BottomDialogAdapter<AuthMethodModel>(
				BottomDialogType.METHOD
			) {
				this.authMethodBottomDialogFragment.dismiss()
				editText.setText(it.name)
				keyDistributionTypeidx = it.id
				authMethodType = it.type
				if (authMethodType == AuthMethodType.TECDSA) {
					this.tECDSALayout.visibility = View.VISIBLE
				} else {
					this.tECDSALayout.visibility = View.GONE
				}
			}
		adapter.setItem(array)
		this.authMethodBottomDialogFragment = BottomDialogFragment(adapter)
		this.authMethodBottomDialogFragment.show(supportFragmentManager, "TAG")
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

	private fun showUserChoiceDialog(array: MutableList<UserModel>, editText: EditText) {
		this.userBottomDialogFragment = BottomMultiSelectDialogFragment<UserModel>(array, selectedUserList) {
			selectedUserList = it
			editText.setText(it.joinToString { it.name })
		}
		this.userBottomDialogFragment.show(supportFragmentManager, "TAG")
	}

	private fun showAdminChoiceDialog(array: MutableList<UserModel>, editText: EditText) {
		this.adminBottomDialogFragment = BottomMultiSelectDialogFragment<UserModel>(array, selectedAdminList) {
			selectedAdminList = it
			editText.setText(it.joinToString { it.name })
		}
		this.adminBottomDialogFragment.show(supportFragmentManager, "TAG")
	}

	private fun showERC20WalletDialog() {
		val mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_default, null)
		val mBuilder = AlertDialog.Builder(this).setView(mDialogView).create()

		mBuilder.show()

		mDialogView.confirmTextView.text = "ERC20 계좌를 만들기 위해서\n이더리움 계좌가 반드시 필요합니다."
		mDialogView.confirmButton.setOnClickListener {
			mBuilder.dismiss()
		}
	}
}
