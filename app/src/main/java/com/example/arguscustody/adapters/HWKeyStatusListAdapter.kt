package com.example.arguscustody.adapters

import Vault
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.arguscustody.R

class HWKeyStatusListAdapter(private val context: Context, private val vault: Vault) : RecyclerView.Adapter<HWKeyStatusListAdapter.ViewHolder>() {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hw_key_status, parent, false)
		return ViewHolder(view)
	}

	override fun getItemCount(): Int {
		return vault.vaultUsers.count()
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.bind(context, position)
	}

	inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
		private val userNameTextView: TextView = itemView.findViewById(R.id.userNameTextView)
		private val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)

		fun bind(context: Context, position: Int) {
			val vaultUser = vault.vaultUsers.get(position)
			userNameTextView.setText(vaultUser.user.name)
			statusTextView.setText(vaultUser.status)
		}
	}
}