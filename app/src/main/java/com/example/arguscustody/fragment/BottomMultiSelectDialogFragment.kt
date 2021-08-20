package com.example.arguscustody.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.arguscustody.R
import com.example.arguscustody.adapters.BottomMultiSelectDialogAdapter
import com.example.arguscustody.model.BottomDialogType
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomMultiSelectDialogFragment<T>(
	val itemList: MutableList<T>,
	val selectedItemList: MutableList<T>,
	val onClickConfirm: (MutableList<T>) -> Unit
): BottomSheetDialogFragment() {
	private lateinit var adapter: BottomMultiSelectDialogAdapter<T>
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.dialog_multi_select_bottomsheet,container,false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		adapter = activity?.applicationContext?.let {
			BottomMultiSelectDialogAdapter<T>(
				it,
				BottomDialogType.USER
			) { model: T, isSelected: Boolean ->
				if (isSelected) {
					selectedItemList.remove(model)
				} else {
					selectedItemList.add(model)
				}
			}
		}!!
		adapter.setSelectedItems(selectedItemList)
		adapter.setItem(itemList)
		view.findViewById<RecyclerView>(R.id.recyclerView).adapter = adapter
		view.findViewById<Button>(R.id.confirmButton).setOnClickListener {
			onClickConfirm(selectedItemList)
			dismiss()
		}
	}
}