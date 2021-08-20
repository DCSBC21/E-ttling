package com.example.arguscustody.adapters

import Tx
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.arguscustody.R
import com.example.arguscustody.model.Stash
import com.example.arguscustody.model.getVaultInStash
import com.example.arguscustody.model.pushTx
import java.util.ArrayList

class PendingTransactionListAdapter(
	private val context: Context,
	private val onClick: () -> Unit
): RecyclerView.Adapter<PendingTransactionListAdapter.ViewHolder>() {
	private val txs : ArrayList<Tx> = Stash.getArrayList("PENDING_TX_LIST", Tx::class.java)

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pending_transaction, parent, false)
		return ViewHolder(view)
	}

	override fun getItemCount(): Int {
		return txs.count()
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(context, position)
	}

	inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
		private val walletTypeTextView = itemView.findViewById<TextView>(R.id.walletTypeTextView)
		private val transactionTitleTextView = itemView.findViewById<TextView>(R.id.transactionTitleTextView)
		private val hashAddressTextView = itemView.findViewById<TextView>(R.id.hashAddressTextView)
		private val coinPriceTextView = itemView.findViewById<TextView>(R.id.coinPriceTextView)
		private val dateTextView = itemView.findViewById<TextView>(R.id.dateTextView)

		fun bind(context: Context, position: Int) {
			val tx = txs.get(position)
			val vault = getVaultInStash(tx.vaultIdx)

			walletTypeTextView.setText(vault.keyDistributionType.name)
			transactionTitleTextView.setText(vault.name + "(" + tx.txIdx + ")")

			hashAddressTextView.setText(tx.toAddress)
			coinPriceTextView.setText(tx.amount.toString())
			dateTextView.setText(tx.createdTime)

			itemView.setOnClickListener {
				pushTx(tx.txIdx)
				onClick()
			}
		}
	}
}