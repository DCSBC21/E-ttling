package com.example.arguscustody.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.arguscustody.R
import com.example.arguscustody.model.*

class BottomDialogAdapter<T>(
	private val dialogType: BottomDialogType,
	private val onClick: (T) -> Unit
): RecyclerView.Adapter<BottomDialogAdapter<T>.Holder>() {
	private var itemList: MutableList<T> = ArrayList()

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.bottomsheet_list_item, parent, false)
		return Holder(view)
	}

	override fun getItemCount(): Int {
		return itemList.size
	}

	override fun onBindViewHolder(holder: Holder, position: Int) {
		val item = itemList[position]
		holder.bind(item)
	}

	fun setItem(items: MutableList<T>) {
		if (!items.isNullOrEmpty()) {
			itemList = items
			notifyDataSetChanged()
		}
	}

	inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val titleTextView = itemView.findViewById<TextView>(R.id.textView)
		fun bind(item: T) {
			when(dialogType) {
				BottomDialogType.COIN ->
					if(item is CoinTypeModel) {
						titleTextView.text = item.name
					}
				BottomDialogType.METHOD ->
					if(item is AuthMethodModel) {
						titleTextView.text = item.name
					}
				BottomDialogType.VALID_TIME ->
					if(item is String) {
						titleTextView.text = item
					}
				BottomDialogType.USER ->
					if(item is UserModel) {
						titleTextView.text = item.name
					}
				BottomDialogType.ADMIN ->
					if(item is UserModel) {
						titleTextView.text = item.name
					}
				BottomDialogType.ACCOUNT ->
					if(item is Account) {
						titleTextView.text = item.name
					}
			}
			itemView.setOnClickListener {
				onClick(item)
			}
		}
	}
}
