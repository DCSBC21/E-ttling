package com.example.arguscustody.adapters

import Vault
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.arguscustody.R
import com.example.arguscustody.token.eth_getBalance
import com.example.arguscustody.token.klay_getBalance
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class HomeAccountListAdapter(val context: Context, val vaults: ArrayList<Vault>) : RecyclerView.Adapter<HomeAccountListAdapter.ViewHolder>() {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_home, parent, false)
		return ViewHolder(view)
	}

	override fun getItemCount(): Int {
		return vaults.count()
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(context, position)
	}

	inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
		private val coinImageView = itemView.findViewById<ImageView>(R.id.coinImageView)
		private val coinNameTextView = itemView.findViewById<TextView>(R.id.coinNameTextView)
		private val coinAddressTextView = itemView.findViewById<TextView>(R.id.coinAddressTextView)
		private val wonPriceTextView = itemView.findViewById<TextView>(R.id.wonPriceTextView)
		private val coinPriceTextView = itemView.findViewById<TextView>(R.id.coinPriceTextView)

		fun bind(context: Context, position: Int) {
			val vault = vaults.get(position)
			val keyType = vault.keyDistributionType.type.toString() + "(" + vault.keyDistributionType.k.toString() + "/" + vault.keyDistributionType.n.toString() + ")"
			coinNameTextView.setText(vault.name + " (" +vault.coinName +")")
			if (vault.address.length > 0) {
				coinAddressTextView.setText(vault.address.substring(0,18)+"...")
			} else {
				coinAddressTextView.setText(vault.address)
			}
			coinPriceTextView.setText("")

			if (vault.coinName == "ETH") {
				doAsync {
					val balance = eth_getBalance(vault.address)
					uiThread {
						wonPriceTextView.setText(balance)
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
}