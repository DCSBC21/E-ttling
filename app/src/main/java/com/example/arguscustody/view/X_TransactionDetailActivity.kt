package com.example.arguscustody.view

import Tx
import Vault
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.arguscustody.R
import com.example.arguscustody.model.*
import com.example.arguscustody.token.eth_getBalance
import com.example.arguscustody.token.klay_getBalance
import kotlinx.android.synthetic.main.activity_wallet_detail.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class X_TransactionDetailActivity: AppCompatActivity() {
	private var tx : Tx = popTx()
	private var vault : Vault = Vault()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.x_activity_transcation_detail)

		vault = getVaultInStash(tx.vaultIdx)

//		// reload Tx
//		fetchTx(this, txProgressBar, {
//			tx = popTx()
//		}, tx.txIdx)

		this.configureToolBar()
		this.setUiItems()
	}

	private fun configureToolBar() {
		this.toolBar.setNavigationIcon(R.drawable.ic_back)
		this.toolBar.setNavigationOnClickListener {
			finish()
		}
	}

	private fun setUiItems() {
		val transactionIDTextView = this.findViewById<TextView>(R.id.transactionIDTextView)
		val walletTypeTextView = this.findViewById<TextView>(R.id.walletTypeTextView)
		val wonPriceTextView = this.findViewById<TextView>(R.id.wonPriceTextView)
		val coinPriceTextView = this.findViewById<TextView>(R.id.coinPriceTextView)
		val hashAddressTextView = this.findViewById<TextView>(R.id.hashAddressTextView)
		val transactionStatusTextView = this.findViewById<TextView>(R.id.transactionStatusTextView)
		val coinTypeTextView = this.findViewById<TextView>(R.id.coinTypeTextView)
		val usersTextView = this.findViewById<TextView>(R.id.usersTextView)
		val adminsTextView = this.findViewById<TextView>(R.id.adminsTextView)
		val timeStampTextView = this.findViewById<TextView>(R.id.timeStampTextView)
		val fromHashAddressTextView = this.findViewById<TextView>(R.id.fromHashAddressTextView)
		val toHashAddressTextView = this.findViewById<TextView>(R.id.toHashAddressTextView)
		val transactionFeeTextView = this.findViewById<TextView>(R.id.transactionFeeTextView)

		transactionIDTextView.setText(vault.name+ " (" + vault.vaultIdx + ")")
		walletTypeTextView.setText(vault.keyDistributionType.name)
		wonPriceTextView.setText("")
		coinTypeTextView.setText(vault.coinName)
		hashAddressTextView.setText(vault.address)

		usersTextView.setText(vault.getVaultUsers())
		adminsTextView.setText(vault.getVaultAdmins())

		timeStampTextView.setText(vault.createdTime)

		fromHashAddressTextView.setText(tx.fromAddress)
		toHashAddressTextView.setText(tx.toAddress)

		if (vault.coinName == "ETH") {
			doAsync {
				val balance = eth_getBalance(vault.address)
				uiThread {
					coinPriceTextView.setText(balance)
				}
			}
		}
		if (vault.coinName == "KLAY") {
			doAsync {
				val balance = klay_getBalance(vault.address)
				uiThread {
					coinPriceTextView.setText(balance)
				}
			}
		}
	}
}