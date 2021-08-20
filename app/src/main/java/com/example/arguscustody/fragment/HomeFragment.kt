package com.example.arguscustody.fragment

import User
import Vault
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.arguscustody.R
import com.example.arguscustody.adapters.HomeAccountListAdapter
import com.example.arguscustody.model.Stash
import com.example.arguscustody.model.getVaults
import com.example.arguscustody.view.MainActivity
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.fragment_transaction.view.*


class HomeFragment: Fragment() {
	companion object {
		lateinit private var homeFragment: HomeFragment
		val Instance: HomeFragment
			get() {
				return homeFragment
			}
		lateinit private var rootView: View
		val RootView: View
		  get() {
		  	return rootView
		  }
	}

	private var mActivity: Activity? = null
	private var mContext: Context? = null
	private lateinit var adapter: HomeAccountListAdapter

	override fun onAttach(context: Context) {
		super.onAttach(context)
		this.mContext = context
	}

	override fun onDetach() {
		super.onDetach()
		this.mContext = null
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		homeFragment = this
		rootView = inflater.inflate(R.layout.fragment_home, container, false)
		this.configureTotal(rootView)
		this.configureRecyclerView(rootView.homeRecyclerView)
		return rootView
	}

	fun configureRecyclerView(recyclerView: RecyclerView) {
		adapter = HomeAccountListAdapter(mContext!!, getVaults())
		val linearLayoutManager = LinearLayoutManager(mContext!!)
		recyclerView.layoutManager = linearLayoutManager
		recyclerView.setHasFixedSize(true)
		recyclerView.adapter = adapter
	}

	private fun configureTotal(rootView: View) {
		rootView.totalPriceTextView.setText(getVaults().count().toString())
	}

}