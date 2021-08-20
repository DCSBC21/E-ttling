package com.example.arguscustody.adapters

import Vault
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.arguscustody.R
import com.example.arguscustody.model.Stash
import com.example.arguscustody.token.eth_getBalance
import com.example.arguscustody.model.makeQRCode
import com.example.arguscustody.model.pushVault
import com.example.arguscustody.token.klay_getBalance
import com.example.arguscustody.view.CreateTransactionActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.android.synthetic.main.dialog_address_qr.view.*
import kotlinx.android.synthetic.main.item_completed_wallet.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.ArrayList

class CompletedWalletListAdapter(private val context: Context, val vaults: ArrayList<Vault>, private val onClick: () -> Unit) : RecyclerView.Adapter<CompletedWalletListAdapter.ViewHolder>() {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_completed_wallet, parent, false)
		return ViewHolder(view, onClick)
	}

	override fun getItemCount(): Int {
		return vaults.count()
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(context, position)
	}

	inner class ViewHolder(itemView: View, val onClick: () -> Unit): RecyclerView.ViewHolder(itemView) {
		private val walletNameTextView = itemView.findViewById<TextView>(R.id.walletNameTextView)
		private val walletAddressTextView = itemView.findViewById<TextView>(R.id.walletAddressTextView)
		private val walletTypeTextView = itemView.findViewById<TextView>(R.id.walletTypeTextView)
		private val wonPriceTextView = itemView.findViewById<TextView>(R.id.wonPriceTextView)
		private val coinPriceTextView = itemView.findViewById<TextView>(R.id.coinPriceTextView)
		private val pendingDescTextView = itemView.findViewById<TextView>(R.id.pendingDescTextView)

		fun bind(context: Context, position: Int) {
			val vault = vaults.get(position)

			walletNameTextView.setText(vault.name + "(" + vault.vaultIdx.toString() +")")
			walletAddressTextView.setText(vault.address)
			walletTypeTextView.setText(vault.keyDistributionType.name)
			wonPriceTextView.setText("")

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

			itemView.setOnClickListener {
				pushVault(vault.vaultIdx)
				onClick()
			}
			itemView.transferButton.setOnClickListener {
				this.presentCreateTransaction(context, vault)
			}
			itemView.openAddressQRButton.setOnClickListener {
				this.presentAddressQRDialog(context, vault)
			}
		}

		private fun presentCreateTransaction(context: Context, vault: Vault) {
			pushVault(vault.vaultIdx)
			val intent = Intent(context, CreateTransactionActivity()::class.java)
			context.startActivity(intent)
		}

		private fun presentAddressQRDialog(context: Context, vault: Vault) {
			val mDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_address_qr, null)
			val mBuilder = AlertDialog.Builder(context).setView(mDialogView)

			val mAlertDialog = mBuilder.show()

			mDialogView.nameTextView.setText(vault.name)
			mDialogView.addressTextView.setText(vault.address)
			val bitmap = makeQRCode(vault.address)
//			val bitmap = this.makeQRCode("bitcoin:3J98t1WpEZ73CNmQviecrnyiWrnqRhWNLy")

			mDialogView.qrImageView.setImageBitmap(bitmap)

			mDialogView.closeButton.setOnClickListener {
				mAlertDialog.dismiss()
			}
		}

	}
}