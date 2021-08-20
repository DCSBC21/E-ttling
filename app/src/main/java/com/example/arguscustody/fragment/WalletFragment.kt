package com.example.arguscustody.fragment

import Vault
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.arguscustody.R
import com.example.arguscustody.model.Stash
import com.example.arguscustody.adapters.WalletSectionAdapter
import com.example.arguscustody.model.WalletSectionModel
import com.example.arguscustody.model.WalletStatus
import com.example.arguscustody.model.popVault
import com.example.arguscustody.view.CreateWalletActivity
import com.example.arguscustody.model.PrintVaultsInStash
import com.example.arguscustody.view.MainActivity
import com.example.arguscustody.view.WalletDetailActivity
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.android.synthetic.main.dialog_confirm_create_wallet.view.*
import kotlinx.android.synthetic.main.fragment_wallet.*
import kotlinx.android.synthetic.main.fragment_wallet.view.*

class WalletFragment: Fragment() {
	companion object {
		lateinit private var walletFragment: WalletFragment
		val Instance: WalletFragment
			get() {
				return walletFragment
			}
		lateinit private var rootView: View
		val RootView: View
			get() {
				return rootView
			}
	}

	private var mActivity: Activity? = null
	private var mContext: Context? = null
	private val dummyDataList = ArrayList<WalletSectionModel>()

	override fun onAttach(context: Context) {
		super.onAttach(context)
		this.mContext = context
//		this.configureRecyclerView(rootView.walletRecyclerView)
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
//		PrintVaultsInStash()
		walletFragment = this
		rootView = inflater.inflate(R.layout.fragment_wallet, container, false)
		this.configureRecyclerView(rootView.walletRecyclerView)
		this.configureFloatingButton(rootView.createWalletButton)
		this.configureFloatingButtonAnimation(rootView.walletRecyclerView, rootView.createWalletButton)
		return rootView
	}

	private fun configureData() {
		dummyDataList.clear()
		dummyDataList.add(WalletSectionModel(
			status = WalletStatus.Pending,
			dummyWalletList = ArrayList<String>()
		))
		dummyDataList.add(WalletSectionModel(
			status = WalletStatus.Completed,
			dummyWalletList = ArrayList<String>()
		))
	}

	fun configureRecyclerView() {
		Log.d("###WalletFragment", "configureRecyclerView() 1")
		val walletRecyclerView = getView()?.findViewById<RecyclerView>(R.id.walletRecyclerView)
		if (walletRecyclerView != null) {
			Log.d("###WalletFragment", "configureRecyclerView() 2")
			walletRecyclerView?.adapter?.notifyDataSetChanged()
		}
	}

	fun configureRecyclerView(recyclerView: RecyclerView) {
		this.configureData()
		recyclerView.apply {
			setHasFixedSize(true)
			layoutManager = LinearLayoutManager(mContext!!)
			adapter = WalletSectionAdapter(
				dummyDataList,
				mContext!!,
				{ showPendingDialog() },
				{ presentWalletDetail() },
				Stash.getArrayList("PENDING_VAULT_LIST", Vault::class.java),
				Stash.getArrayList("VAULT_LIST", Vault::class.java)
			)
		}
	}

	private fun configureFloatingButton(floatingButton: ExtendedFloatingActionButton) {
		floatingButton.setOnClickListener {
			this.presentCreateWallet()
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

	private fun showPendingDialog() {
		val mDialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_default, null)
		val mBuilder = AlertDialog.Builder(mContext).setView(mDialogView).create()

		mBuilder.show()

		val vault : Vault = popVault()

		mDialogView.confirmTextView.text = vault.vaultIdx.toString() + ": 생성 대기 중입니다."
		mDialogView.confirmButton.setOnClickListener {
			mDialogView?.apply {
				if (parent != null) {
					(parent as ViewGroup).removeView(this)
				}
			}
			mBuilder.dismiss()
		}
	}

	private fun presentWalletDetail() {
		val intent = Intent(activity, WalletDetailActivity()::class.java)
//		intent.putExtra("WALLET_ID", wallet.id)
//		Log.d("###WALLET", "vaultIdx="+position.toString())
		startActivity(intent)
	}

	private fun presentCreateWallet() {
		val intent = Intent(activity, CreateWalletActivity::class.java)
		startActivity(intent)
	}
}