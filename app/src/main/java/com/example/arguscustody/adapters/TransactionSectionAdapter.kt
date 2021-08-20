package com.example.arguscustody.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.arguscustody.R
import com.example.arguscustody.model.TransactionSectionModel
import com.example.arguscustody.model.WalletStatus

class TransactionSectionAdapter(
	private val transactionList: List<TransactionSectionModel>,
	private val context: Context,
	private val presentTransactionDetail: () -> Unit
): RecyclerView.Adapter<TransactionSectionAdapter.ViewHolder>() {
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transcation_section, parent, false)
		return ViewHolder(view)
	}

	override fun getItemCount(): Int {
		return transactionList.size
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(transactionList[position], context)
	}

	inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
		private val titleTextView = itemView.findViewById<TextView>(R.id.titleTextView)
		private val recyclerView = itemView.findViewById<RecyclerView>(R.id.transactionListRecyclerView)
		fun bind(
			sectionModel: TransactionSectionModel,
			context: Context
		) {
			if (sectionModel.status == WalletStatus.Pending) {
				this.titleTextView.text = "Pending"
				recyclerView.apply {
					setHasFixedSize(true)
					layoutManager = LinearLayoutManager(context)
					adapter =
						PendingTransactionListAdapter(context) {
							presentTransactionDetail()
						}
				}
			} else {
				this.titleTextView.text = "Completed"
				recyclerView.apply {
					setHasFixedSize(true)
					layoutManager = LinearLayoutManager(context)
					adapter =
						CompletedTransactionListAdapter(context) {
							presentTransactionDetail()
						}
				}
			}
		}
	}
}