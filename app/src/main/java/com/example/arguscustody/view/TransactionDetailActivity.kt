package com.example.arguscustody.view

import Tx
import Vault
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.arguscustody.R
import com.example.arguscustody.adapters.ParticipantStatusListAdapter
import com.example.arguscustody.model.*
import com.example.arguscustody.token.eth_getBalance
import com.example.arguscustody.token.klay_getBalance
import com.example.arguscustody.token.showWaitingDialog
import kotlinx.android.synthetic.main.activity_transaction_detail.*
import kotlinx.android.synthetic.main.dialog_confirm_create_transaction.view.*
import kotlinx.android.synthetic.main.fragment_transaction.view.*
import kotlinx.android.synthetic.main.fragment_wallet.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject


class TransactionDetailActivity : AppCompatActivity() {

	private lateinit var methodType: MethodType
	private lateinit var participantStatusListAdapter: ParticipantStatusListAdapter
	private var tx : Tx = Tx() // popTx()
	private var vault : Vault = Vault() // popVault()
	private var userIdx = Stash.getInt("USER_IDX")
	private lateinit var coinPriceTextView : TextView

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_transaction_detail)
		coinPriceTextView = this.findViewById<TextView>(R.id.coinPriceTextView)
		this.configureToolBar()

		tx = popTx()
		Log.d("###TX", tx.txUsers.get(0).status + "/" + tx.txUsers.get(1).status)
		Log.d("###TX", tx.txUsers.get(0).user.name + "/" + tx.txUsers.get(1).user.name)
		vault = getVaultInStash(tx.vaultIdx)

		// reload Tx
		reloadTx()

//		this.showConfirmCreateTransactionDialog()
//		this.showSuccessCreateTransactionDialog()

		this.configureTECDSALayoutVisibility()
		this.configureParticipantStatusRecyclerView(this.participantStatusRecyclerView)
		this.configureButtons()
		this.setUiItems()
	}

	private fun reloadTx() {
		// reload Tx
		pingAuth(this) {
			fetchTx(this, {
				tx = popTx()
				this.configureTECDSALayoutVisibility()
				this.configureParticipantStatusRecyclerView(this.participantStatusRecyclerView)
				this.configureButtons()
				this.setUiItems()
				//TransactionFragment.Instance.configureRecyclerView(TransactionFragment.RootView.transactionRecyclerView)
			}, tx.txIdx)
		}
	}

	private fun configureToolBar() {
		this.toolBar.setNavigationIcon(R.drawable.ic_back)
		this.toolBar.setNavigationOnClickListener {
			finish()
		}
	}

	private fun configureTECDSALayoutVisibility() {
		methodType = intent.getSerializableExtra("methodType") as MethodType

		if (methodType == MethodType.TECDSA2OF3 || methodType == MethodType.TECDSA3OF4) {
			this.tECDSALayout.visibility = View.VISIBLE
		} else {
			this.tECDSALayout.visibility = View.GONE
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
		if (tx.isCreatedOfUser(userIdx)) {
			this.buttonsLayout.visibility = View.VISIBLE
		} else {
			this.buttonsLayout.visibility = View.GONE
		}

		this.coinImageView.setOnClickListener {
			reloadTx()
		}

		this.confirmButton.setOnClickListener {
			pingAuth(this) {
				if (vault.keyDistributionType.type == AuthMethodType.SSS.ordinal) {
					var progress = showWaitingDialog(this)
					val json = JSONObject()
					json.put("txIdx", tx.txIdx)
					json.put("vaultIdx", tx.vaultIdx)
					json.put("userHwKey", vault.key)
					// traceJson(json)
					postTxConfirm(this, {
						fetchTx(this, {
							progress.cancel()
							this.configureTECDSALayoutVisibility()
							this.configureParticipantStatusRecyclerView(this.participantStatusRecyclerView)
							this.configureButtons()
							setUiItems()
							//TransactionFragment.Instance.configureRecyclerView(TransactionFragment.RootView.transactionRecyclerView)
						}, tx.txIdx)
					}, json)
				} else if (vault.keyDistributionType.type == AuthMethodType.TECDSA.ordinal) {
					val req =
						MPE_HOST() + " vault:" + vault.getVaultInfo() + " tx:" + tx.txIdx.toString()
					var progress = showWaitingDialog(this, req)
					Log.d("MPE_SIGN1", req)
					fetchTxMsg(this, {
						Log.d("MPE_SIGN2", Stash.getString("TXMSG"))
						//Log.d("MPE_SIGN3", getKeyStoreHWKEY(vault.vaultIdx))
						val sign = MainActivity.Instance.gg18sign(
							MPE_HOST(),
							vault.keyDistributionType.n.toString(),
							vault.keyDistributionType.k.toString(),
							getKeyStoreHWKEY(vault.vaultIdx), // [HWKEY]
							Stash.getString("TXMSG"),
							tx.txIdx.toString(),
							Stash.getInt("USER_IDX").toString()
						)
						putSignStore(vault.vaultIdx, sign)
						fetchTx(this, {
							progress.cancel()
							this.configureTECDSALayoutVisibility()
							this.configureParticipantStatusRecyclerView(this.participantStatusRecyclerView)
							this.configureButtons()
							setUiItems()
							//TransactionFragment.Instance.configureRecyclerView(TransactionFragment.RootView.transactionRecyclerView)
						}, tx.txIdx) // fetchTx
					}, tx.txIdx) // fetchTxMsg
				}
			}
		}
		this.quitButton.setOnClickListener {
			// do nothing
		}
	}

	private fun showConfirmCreateTransactionDialog() {
		val mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_create_transaction, null)
		val mBuilder = AlertDialog.Builder(this).setView(mDialogView).create()

		mBuilder.show()

		if (methodType == MethodType.TECDSA2OF3 || methodType == MethodType.TECDSA3OF4) {
			mDialogView.tECDSALayout.visibility = View.VISIBLE
		} else {
			mDialogView.tECDSALayout.visibility = View.GONE
		}

		mDialogView.confirmButton.setOnClickListener {
				val intent = Intent(this, MainActivity::class.java)
				intent.putExtra("selectTab","TRANSACTION")
				startActivity(intent)
		}

		mDialogView.cancelButton.setOnClickListener {
			mBuilder.dismiss()
		}
	}

	private fun showSuccessCreateTransactionDialog() {
		val mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_default, null)
		val mBuilder = AlertDialog.Builder(this).setView(mDialogView).create()

		mBuilder.show()

		mDialogView.confirmTextView.text = "트랜잭션 성공"
		mDialogView.confirmButton.setOnClickListener {
			val intent = Intent(this, MainActivity::class.java)
			intent.putExtra("selectTab", "TRANSACTION")
			startActivity(intent)
		}
	}

	private fun setUiItems() {
		this.findViewById<TextView>(R.id.transactionTitleTextView).setText("Transaction "+tx.txIdx)

		if (vault.address.length > 10) {
			this.findViewById<TextView>(R.id.walletNameTextView).setText(vault.name + " (" + vault.address.substring(0, 10) + "...)")
		} else {
			this.findViewById<TextView>(R.id.walletNameTextView).setText(vault.name + " (...)")
		}

		this.findViewById<TextView>(R.id.walletTypeTextView).setText(vault.keyDistributionType.name)
		this.findViewById<TextView>(R.id.coinTypeTextView).setText(vault.coinName)
		this.findViewById<TextView>(R.id.wonPriceTextView).setText("")
		this.findViewById<TextView>(R.id.hashAddressTextView).setText(tx.fromAddress)

		this.findViewById<TextView>(R.id.amountTextView).setText(tx.amount.toString())
		this.findViewById<TextView>(R.id.toHashAddressTextView).setText(tx.toAddress)
		this.findViewById<TextView>(R.id.gasPriceTextView).setText(tx.gasPrice.toString())
		this.findViewById<TextView>(R.id.gasLimitTextView).setText(tx.gasLimit.toString())
		this.findViewById<TextView>(R.id.statusTextView).setText(tx.status + " / " + tx.statusUsers)

		if (vault.coinName == "ETH") {
			coinPriceTextView.setText("? ETH")
			doAsync {
				val balance = eth_getBalance(vault.address)
				uiThread {
					coinPriceTextView.setText(balance)
				}
			}
		}
		if (vault.coinName == "KLAY") {
			coinPriceTextView.setText("? KLAY")
			doAsync {
				val balance = klay_getBalance(vault.address)
				uiThread {
					coinPriceTextView.setText(balance)
				}
			}
		}
	}

}