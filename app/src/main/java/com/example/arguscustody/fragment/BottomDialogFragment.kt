package com.example.arguscustody.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.arguscustody.R
import com.example.arguscustody.adapters.BottomDialogAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomDialogFragment<T>(var adapter: BottomDialogAdapter<T>) : BottomSheetDialogFragment() {
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.dialog_bottomsheet,container,false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		view.findViewById<RecyclerView>(R.id.recyclerView).adapter = adapter
	}
}