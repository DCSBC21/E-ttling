package com.example.arguscustody.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.arguscustody.R
import com.example.arguscustody.adapters.TransactionSectionAdapter
import com.example.arguscustody.model.MethodType
import com.example.arguscustody.model.TransactionSectionModel
import com.example.arguscustody.model.TransactionStatus
import com.example.arguscustody.view.TransactionDetailActivity
import com.example.arguscustody.view.CreateTransactionActivity
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.android.synthetic.main.fragment_transaction.view.*
import kotlinx.android.synthetic.main.fragment_wallet.view.*

class TransactionFragment: Fragment() {
	companion object {
		lateinit private var txFragment: TransactionFragment
		val Instance: TransactionFragment
			get() {
				return txFragment
			}
		lateinit private var rootView: View
		val RootView: View
			get() {
				return rootView
			}
	}

	private var mActivity: Activity? = null
	private var mContext: Context? = null
	private val dummyDataList = ArrayList<TransactionSectionModel>()

	override fun onAttach(context: Context) {
		super.onAttach(context)
		this.mContext = context
//		this.configureRecyclerView(rootView.transactionRecyclerView)
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
		txFragment = this
		rootView = inflater.inflate(R.layout.fragment_transaction, container, false)
		this.configureRecyclerView(rootView.transactionRecyclerView)
		// createTransaction FloatingButton
		rootView.createTransactionButton.hide()
//		this.configureFloatingButton(rootView.createTransactionButton)
//		this.configureFloatingButtonAnimation(rootView.transactionRecyclerView, rootView.createTransactionButton)
		return rootView
	}

	private fun configureData() {
		dummyDataList.clear()
		dummyDataList.add(TransactionSectionModel(
			status = TransactionStatus.Pending,
			dummyTransactionList = ArrayList<String>()
		))
		dummyDataList.add(TransactionSectionModel(
			status = TransactionStatus.Completed,
			dummyTransactionList = ArrayList<String>()
		))
	}

	fun configureRecyclerView(recyclerView: RecyclerView) {
		this.configureData()
		recyclerView.apply {
			setHasFixedSize(true)
			layoutManager = LinearLayoutManager(mContext!!)
			adapter = TransactionSectionAdapter(
				dummyDataList,
				mContext!!
			) {
				presentTransactionDetail()
			}
		}
	}

	private fun configureFloatingButton(floatingButton: ExtendedFloatingActionButton) {
		floatingButton.setOnClickListener {
			this.presentCreateTransaction()
		}
	}

	private fun configureFloatingButtonAnimation(
		recyclerView: RecyclerView,
		floatingButton: ExtendedFloatingActionButton
	) {
		recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
			override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
				if (dy<0 && !floatingButton.isShown())
					floatingButton.show()
				else if(dy>0 && floatingButton.isShown())
					floatingButton.hide()
			}
		})
	}

	private fun presentTransactionDetail() {
//		val intent = Intent(activity, TransactionDetailActivity()::class.java)
		val intent = Intent(activity, TransactionDetailActivity::class.java)
		intent.putExtra("methodType", MethodType.SSS2OF3) // read from VAULT_ID // MethodType.TECDSA ????
		startActivity(intent)
	}

	private fun presentCreateTransaction() {
		val intent = Intent(activity, CreateTransactionActivity()::class.java)
		startActivity(intent)
	}
}