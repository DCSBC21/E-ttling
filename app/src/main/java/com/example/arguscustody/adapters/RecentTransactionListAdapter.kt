package com.example.arguscustody.adapters

import Tx
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.arguscustody.R

class RecentTransactionListAdapter(private val context: Context, private val txs: ArrayList<Tx>) : RecyclerView.Adapter<RecentTransactionListAdapter.ViewHolder>() {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recent_transaction, parent, false)
		return ViewHolder(view)
	}

	override fun getItemCount(): Int {
		return txs.count()
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(context, position)
	}

	inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
		private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
		private val hwKeyStatusTextView: TextView = itemView.findViewById(R.id.hwKeyStatusTextView)
		private val coinAmountTextView: TextView = itemView.findViewById(R.id.coinAmountTextView)

		fun bind(context: Context, position: Int) {
			val tx : Tx = txs.get(position)
			dateTextView.setText(tx.createdTime)
			hwKeyStatusTextView.setText(tx.status)
			coinAmountTextView.setText(tx.amount.toString())
		}
	}
}