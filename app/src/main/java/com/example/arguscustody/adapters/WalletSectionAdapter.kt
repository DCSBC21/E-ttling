package com.example.arguscustody.adapters

import Vault
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.arguscustody.R
import com.example.arguscustody.model.WalletSectionModel
import com.example.arguscustody.model.WalletStatus

class WalletSectionAdapter(
	private val walletList: List<WalletSectionModel>,
	private val context: Context,
	private val showPendingDialog: () -> Unit,
	private val presentWalletDetail: () -> Unit,
	private val pendingVaultList: ArrayList<Vault>,
	private val vaultList: ArrayList<Vault>
): RecyclerView.Adapter<WalletSectionAdapter.ViewHolder>() {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_wallet_section, parent, false)
		return ViewHolder(view)
	}

	override fun getItemCount(): Int {
		return walletList.size
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(walletList[position], context)
	}

	inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
		private val titleTextView = itemView.findViewById<TextView>(R.id.titleTextView)
		private val recyclerView = itemView.findViewById<RecyclerView>(R.id.walletListRecyclerView)
		fun bind(
			sectionModel: WalletSectionModel,
			context: Context
		) {
			if (sectionModel.status == WalletStatus.Pending) {
				this.titleTextView.text = "Pending"
				recyclerView.apply {
					setHasFixedSize(true)
					layoutManager = LinearLayoutManager(context)
					adapter =
						PendingWalletListAdapter(context, pendingVaultList) {
//							showPendingDialog()
							presentWalletDetail()
						}
				}
			} else {
				this.titleTextView.text = "Completed"
				recyclerView.apply {
					setHasFixedSize(true)
					layoutManager = LinearLayoutManager(context)
					adapter =
						CompletedWalletListAdapter(context, vaultList) {
							presentWalletDetail()
						}
				}
			}
		}
	}
}