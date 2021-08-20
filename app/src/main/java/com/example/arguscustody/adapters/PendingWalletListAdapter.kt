package com.example.arguscustody.adapters

import Vault
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.arguscustody.R
import com.example.arguscustody.model.Stash
import com.example.arguscustody.model.pushVault
import java.util.ArrayList

class PendingWalletListAdapter(private val context: Context, val vaults: ArrayList<Vault>, private val onClick: () -> Unit) : RecyclerView.Adapter<PendingWalletListAdapter.ViewHolder>() {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pending_wallet, parent, false)
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

		fun bind(context: Context, position: Int) {
			val vault = vaults.get(position)

			walletNameTextView.setText(vault.name + "(" + vault.vaultIdx.toString() +")")
			walletAddressTextView.setText(vault.address)
			walletTypeTextView.setText(vault.keyDistributionType.name)
			wonPriceTextView.setText("")
			coinPriceTextView.setText("")

			itemView.setOnClickListener {
				pushVault(vault.vaultIdx)
				onClick()
			}
		}
	}
}