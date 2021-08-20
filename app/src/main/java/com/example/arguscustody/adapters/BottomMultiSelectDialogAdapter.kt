package com.example.arguscustody.adapters

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.arguscustody.R
import com.example.arguscustody.model.*

class BottomMultiSelectDialogAdapter<T>(
	private val context: Context,
	private val dialogType: BottomDialogType,
	private val onClick: (T, Boolean) -> Unit
): RecyclerView.Adapter<BottomMultiSelectDialogAdapter<T>.Holder>() {
	private var itemList: MutableList<T> = ArrayList()
	private var selectedItemList: MutableList<T> = ArrayList()

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
		val view = LayoutInflater.from(parent.context).inflate(R.layout.bottomsheet_list_item, parent, false)
		return Holder(context ,view)
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

	fun setSelectedItems(items: MutableList<T>) {
		if(!items.isNullOrEmpty()) {
			selectedItemList = items
		}
	}

	inner class Holder(
		val context: Context,
		itemView: View
	) : RecyclerView.ViewHolder(itemView) {
		private val titleTextView = itemView.findViewById<TextView>(R.id.textView)
		fun bind(item: T) {
			when(dialogType) {
				BottomDialogType.USER ->
					if(item is UserModel) {
						titleTextView.text = item.name
					}
				else -> titleTextView.text = ""
			}

			selectedItemList.forEach {
				if (it == item) {
					itemView.isSelected = true
				}
			}
			configureBackground()
			itemView.setOnClickListener {
				onClick(item, itemView.isSelected)
				itemView.isSelected = !itemView.isSelected
				configureBackground()
			}
		}

		private fun configureBackground() {
			val backgroundColor: Int = if(itemView.isSelected) R.color.light_blue else R.color.white
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				itemView.setBackgroundColor(context.getColor(backgroundColor))
			} else {
				itemView.setBackgroundColor(context.resources.getColor(backgroundColor))
			}
		}
	}
}