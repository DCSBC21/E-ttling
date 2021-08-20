package com.example.arguscustody.view

import Tx
import Vault
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.arguscustody.R
import com.example.arguscustody.adapters.HWKeyStatusListAdapter
import com.example.arguscustody.adapters.RecentTransactionListAdapter
import com.example.arguscustody.model.*
import com.example.arguscustody.token.eth_getBalance
import com.example.arguscustody.token.klay_getBalance
import com.example.arguscustody.token.showWaitingDialog
import kotlinx.android.synthetic.main.activity_wallet_detail.*
import kotlinx.android.synthetic.main.activity_wallet_detail.toolBar
import kotlinx.android.synthetic.main.dialog_address_qr.view.*
import kotlinx.android.synthetic.main.dialog_confirm_create_wallet.view.*
import kotlinx.android.synthetic.main.fragment_wallet.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.ArrayList

class WalletDetailActivity : AppCompatActivity() {

	private lateinit var hwKeyStatusListAdapter: HWKeyStatusListAdapter
	private lateinit var recentTransactionListAdapter: RecentTransactionListAdapter
	private var vault : Vault = popVault()

	private lateinit var coinPriceTextView: TextView

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_wallet_detail)
		coinPriceTextView = this.findViewById<TextView>(R.id.coinPriceTextView)
		this.configureToolBar()

// all wallets are already loaded.
//		fetchTxs(this, txProgressBar, {
//			this.configureRecentTransactionListRecyclerView(this.recentTransactionListRecyclerView)
//		})
		vault = popVault()
		// reload Vault
		reloadVault()

		this.configureAddressQRBButton()
		this.configureTransferButton()
		this.configureDownloadKeyButton() // SSS
		this.configureJoinDismissButton() // T-ECDSA
		this.configureImageViewTouch()
		this.configureHWKeyStatusRecyclerView(this.hwKeyStatusRecyclerView)
		this.configureRecentTransactionListRecyclerView(this.recentTransactionListRecyclerView)
		this.setUiItems()
	}

	private fun reloadVault() {
		pingAuth(this) {
			fetchVault(this, {
				vault = popVault()
				this.configureAddressQRBButton()
				this.configureTransferButton()
				this.configureDownloadKeyButton() // SSS
				this.configureJoinDismissButton() // T-ECDSA
				this.configureImageViewTouch()
				this.configureHWKeyStatusRecyclerView(this.hwKeyStatusRecyclerView)
				this.configureRecentTransactionListRecyclerView(this.recentTransactionListRecyclerView)
				this.setUiItems()
				//WalletFragment.Instance.configureRecyclerView(WalletFragment.RootView.walletRecyclerView)
			}, vault.vaultIdx)
		}
	}

	private fun showPendingDialog() {
		val mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_default, null)
		val mBuilder = android.app.AlertDialog.Builder(this).setView(mDialogView).create()

		mBuilder.show()

		mDialogView.confirmTextView.text = vault.vaultIdx.toString() + ": 생성 대기 중입니다."
		mDialogView.confirmButton.setOnClickListener {
			mDialogView?.apply {
				if (parent != null) {
					(parent as ViewGroup).removeView(this)
				}
			}
			mBuilder.dismiss()
		}
	}
	private fun configureToolBar() {
		this.toolBar.setNavigationIcon(R.drawable.ic_back)
		this.toolBar.setNavigationOnClickListener {
			finish()
		}
	}

	private fun configureImageViewTouch() {
		this.coinImageView.setOnClickListener {
			reloadVault()
		}
	}

	private fun configureAddressQRBButton() {
		this.openAddressQRButton.setOnClickListener {
			if (vault.isComplete()) {
				this.presentAddressQRDialog(this)
				copyToClipboard(this, vault.address)
			} else {
				this.showPendingDialog()
			}
		}
	}

	private fun configureTransferButton() {
		this.transferButton.setOnClickListener {
			if (vault.isComplete()) {
				this.presentCreateTransaction(this)
			} else {
				this.showPendingDialog()
			}
		}
	}

	private fun configureJoinDismissButton() {
		if( vault.keyDistributionType.type == AuthMethodType.TECDSA.ordinal) {
			this.layoutTECDSA.visibility = View.VISIBLE
			this.downloadKeyButton.visibility = View.GONE
			if (vault.isComplete() || vault.status == "CANCEL") {
				this.layoutActions.visibility = View.GONE
			}

			this.joinButton.setOnClickListener {
				pingAuth(this) {
					vault.status = "JOIN"
					val req = MPE_HOST() + " : " + vault.getVaultInfo() + " user:" + Stash.getInt("USER_IDX")
						.toString()
					val progress = showWaitingDialog(this, req)
					putHwKey(this, {
						this.layoutActions.visibility = View.GONE
						Log.d("MPE_GEN0", req)
						val key = MainActivity.Instance.gg18keygen(
							MPE_HOST(),
							vault.keyDistributionType.n.toString(),
							vault.keyDistributionType.k.toString(),
							vault.vaultIdx.toString(),
							Stash.getInt("USER_IDX").toString()
						)
						Log.d("MPE_GEN1", req)
						//Log.d("MPE_GEN2", key)
						progress.cancel()
						if (key.length > 0) {
							Log.d("MPE_GEN3", req)
							vault.status = "COMPLETE"
							vault.key = key // [HWKEY] Temporary saving of HWKEY
							putKeyStoreHWKEY(vault.vaultIdx, key)
							putHwKey(this, {
								fetchVault(this, {
									//progress.cancel()
									this.configureHWKeyStatusRecyclerView(this.hwKeyStatusRecyclerView)
									//WalletFragment.Instance.configureRecyclerView(WalletFragment.RootView.walletRecyclerView)
								}, vault.vaultIdx)
							}, vault)
						}
					}, vault)
				}
			}
			this.dismissButton.setOnClickListener {
				vault.status = "CANCEL"
				putHwKey(this, {
					this.layoutActions.visibility = View.GONE
				}, vault)
			}
		}
	}

	private fun configureDownloadKeyButton() {
		if( vault.keyDistributionType.type != AuthMethodType.TECDSA.ordinal) {
			this.layoutTECDSA.visibility = View.GONE
			this.downloadKeyButton.visibility = View.VISIBLE
			if (vault.isComplete()) {
				this.layoutActions.visibility = View.GONE
			}

			this.downloadKeyButton.setOnClickListener {
				pingAuth(this) {
					vault.status = "COMPLETE"
					val progress = showWaitingDialog(this)
					putHwKey(this, {
						this.layoutActions.visibility = View.GONE
						fetchVault(this, {
							progress.cancel()
							this.configureHWKeyStatusRecyclerView(this.hwKeyStatusRecyclerView)
							//WalletFragment.Instance.configureRecyclerView(WalletFragment.RootView.walletRecyclerView)
						}, vault.vaultIdx)
					}, vault)
				}
			}
		}
	}

	private fun configureHWKeyStatusRecyclerView(recyclerView: RecyclerView) {
		vault = popVault()
		hwKeyStatusListAdapter =
			HWKeyStatusListAdapter(this, vault)
		val linearLayoutManager = LinearLayoutManager(this)
		recyclerView.layoutManager = linearLayoutManager
		recyclerView.setHasFixedSize(true)
		recyclerView.adapter = hwKeyStatusListAdapter
	}

	private fun configureRecentTransactionListRecyclerView(recyclerView: RecyclerView) {
		val txs : ArrayList<Tx> = getVaultTxs(vault.vaultIdx)
		recentTransactionListAdapter =
			RecentTransactionListAdapter(this, txs)
		val linearLayoutManager = LinearLayoutManager(this)
		recyclerView.layoutManager = linearLayoutManager
		recyclerView.setHasFixedSize(true)
		recyclerView.adapter = recentTransactionListAdapter
	}

	private fun presentAddressQRDialog(context: Context) {
		val mDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_address_qr, null)
		val mBuilder = AlertDialog.Builder(context).setView(mDialogView)

		val mAlertDialog = mBuilder.show()

		mDialogView.nameTextView.setText(vault.name)
		mDialogView.addressTextView.setText(vault.address)
		val bitmap = makeQRCode(vault.address)
//		val bitmap = this.makeQRCode("bitcoin:3J98t1WpEZ73CNmQviecrnyiWrnqRhWNLy")
		mDialogView.qrImageView.setImageBitmap(bitmap)

		mDialogView.closeButton.setOnClickListener {
			mAlertDialog.dismiss()
		}
	}

	private fun presentCreateTransaction(context: Context) {
		val intent = Intent(context, CreateTransactionActivity()::class.java)
		startActivity(intent)
	}

	private fun setUiItems() {
//		Log.d("###Wallet", "vault: " + vault.vaultIdx.toString() + "/" + vault.name + "/" + vault.keyDistributionType.name)
		this.findViewById<TextView>(R.id.walletDetailTitle).setText(vault.name+ " (" + vault.vaultIdx + ")")
		if (vault.address.length > 10) {
			this.findViewById<TextView>(R.id.walletNameTextView).setText(vault.name + " (" + vault.address.substring(0,10) + "...)")
		} else {
			this.findViewById<TextView>(R.id.walletNameTextView).setText(vault.name + " (...)")
		}
		this.findViewById<TextView>(R.id.walletTypeTextView).setText(vault.keyDistributionType.name)
		this.findViewById<TextView>(R.id.coinTypeTextView).setText(vault.coinName)
		this.findViewById<TextView>(R.id.wonPriceTextView).setText("")

		this.findViewById<TextView>(R.id.usersTextView).setText(vault.getVaultUsers())
		this.findViewById<TextView>(R.id.adminsTextView).setText(vault.getVaultAdmins())

		this.findViewById<TextView>(R.id.createdTimeTextView).setText(vault.createdTime)
		this.findViewById<TextView>(R.id.usedTimeTextView).setText(vault.usedTime)

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