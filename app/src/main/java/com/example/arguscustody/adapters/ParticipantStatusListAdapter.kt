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
import com.example.arguscustody.model.getTxInStash
import com.example.arguscustody.model.popTx

class ParticipantStatusListAdapter(private val context: Context) : RecyclerView.Adapter<ParticipantStatusListAdapter.ViewHolder>() {
	private val tx : Tx = popTx()

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_participant_status, parent, false)
		return ViewHolder(view)
	}

	override fun getItemCount(): Int {
		return tx.txUsers.count()
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(context, position)
	}

	inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
		private val userNameTextView: TextView = itemView.findViewById(R.id.userNameTextView)
		private val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)

		fun bind(context: Context, position: Int) {
			val txUser = tx.txUsers.get(position)
			userNameTextView.setText(txUser.user.name)
			statusTextView.setText(txUser.status)
		}
	}
}