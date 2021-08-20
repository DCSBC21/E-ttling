package com.example.arguscustody.view

import JsonGG20Key
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.arguscustody.R
import com.example.arguscustody.adapters.ParticipantStatusListAdapter
import com.example.arguscustody.model.Stash
import com.example.arguscustody.token.eth_getAddressFromPublickey
import kotlinx.android.synthetic.main.x_activity_authenticate_tecdsa_wallet.*
import kotlinx.android.synthetic.main.activity_wallet_detail.toolBar
import kotlinx.android.synthetic.main.dialog_confirm_create_wallet.view.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.math.BigInteger

class X_AuthenticateTECDSAWalletActivity : AppCompatActivity() {

	private lateinit var participantStatusListAdapter: ParticipantStatusListAdapter

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.x_activity_authenticate_tecdsa_wallet)
		this.configureToolBar()
		this.configureParticipantStatusRecyclerView(this.participantStatusRecyclerView)

//		this.showConfirmCreateWalletDialog()
//		this.showExpireAuthValidTimeDialog()
		this.configureButtons()
	}

	private fun configureToolBar() {
		this.toolBar.setNavigationIcon(R.drawable.ic_back)
		this.toolBar.setNavigationOnClickListener {
			finish()
		}
	}

	private fun configureParticipantStatusRecyclerView(recyclerView: RecyclerView) {
		participantStatusListAdapter = ParticipantStatusListAdapter(this)
		val linearLayoutManager = LinearLayoutManager(this)
		recyclerView.layoutManager = linearLayoutManager
		recyclerView.setHasFixedSize(true)
		recyclerView.adapter = participantStatusListAdapter
	}

	private fun configureButtons() {
		joinButton.setOnClickListener {
			val mpe_server = MPE_HOST()
			val key = MainActivity.Instance.gg20keygen(mpe_server, "3", "1", "11", "1")
//			val key = (this as MainActivity).gg20keygen(mpe_server, "3", "1")
			Log.d("KEY", key)

			Stash.put("KEYSTORE", key)
			val jsonKey = Json.decodeFromString<JsonGG20Key>(key)
			Log.d("KEY1", jsonKey.shared_keys.y.x)
			Log.d("KEY2", jsonKey.shared_keys.y.y)
			Log.d("KEY3", jsonKey.shared_keys.x_i)

			val pubkey = jsonKey.shared_keys.y.x + jsonKey.shared_keys.y.y
			val bigint = BigInteger(pubkey, 16)
			val binarystring = String(bigint.toByteArray())
			val address = eth_getAddressFromPublickey(binarystring)
			Log.d("ETH", eth_getAddressFromPublickey(binarystring))
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

		mDialogView.confirmTextView.text = "인증 시간 만료"
		mDialogView.confirmButton.setOnClickListener {
			val intent = Intent(this, MainActivity::class.java)
			intent.putExtra("selectTab", "WALLET")
			startActivity(intent)
		}
	}
}