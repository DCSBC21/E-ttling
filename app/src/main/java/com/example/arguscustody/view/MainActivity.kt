package com.example.arguscustody.view

import Tx
import Vault
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.arguscustody.R
import com.example.arguscustody.fragment.HomeFragment
import com.example.arguscustody.fragment.MyPageFragment
import com.example.arguscustody.fragment.TransactionFragment
import com.example.arguscustody.fragment.WalletFragment
import com.example.arguscustody.model.Stash
import com.example.arguscustody.model.popUser
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.fragment_transaction.view.*
import kotlinx.android.synthetic.main.fragment_wallet.*
import kotlinx.android.synthetic.main.fragment_wallet.view.*
import java.util.ArrayList

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
	companion object {
		lateinit private var mainActivity : MainActivity
		val Instance: MainActivity
			get() {
				return mainActivity
			}
	}


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		mainActivity = this

		// Load libmpe.so
		System.loadLibrary("mpe")
		Log.d("###Rust###", hello("World"))

		this.bottomNavigationView.setOnNavigationItemSelectedListener(this)
		supportFragmentManager.beginTransaction().add(R.id.linearLayout, HomeFragment()).commit()

		this.configureUserTitle()
		this.configureWalletBadge()
		this.configureTransactionBadge()
		this.presentWalletFragment()
	}

	override fun onResume() {
		super.onResume()
		//Toast.makeText(this, "MainActivity onResume()", Toast.LENGTH_SHORT).show()

		// 주기적으로 vault, tx 정보 fetch & refresh 하면 좋을 듯

		this.configureWalletBadge()
		this.configureTransactionBadge()
		//WalletFragment().walletRecyclerView?.adapter?.notifyDataSetChanged()
	}

	private fun presentWalletFragment() {
		when(intent.getStringExtra("selectTab")){
			"WALLET" -> {
				this.bottomNavigationView.selectedItemId = R.id.pageWallet
				this.supportFragmentManager.beginTransaction().replace(R.id.linearLayout , WalletFragment()).commitAllowingStateLoss()
			}
			"TRANSACTION" -> {
				this.bottomNavigationView.selectedItemId = R.id.pageTransaction
				this.supportFragmentManager.beginTransaction().replace(R.id.linearLayout , TransactionFragment()).commitAllowingStateLoss()
			}
		}
	}

	private fun configureUserTitle() {
		val user = popUser()
		this.mainToolbarTitle.setText("Stone Wallet / " + user.name + "(" + user.userIdx.toString() + ")")
	}

	private fun configureWalletBadge() {
		val pending_vaults: ArrayList<Vault>? = Stash.getArrayList("PENDING_VAULT_LIST", Vault::class.java)
		Log.d("###Main1", pending_vaults?.count().toString())
		val count = pending_vaults?.count()!!
		if (count > 0) {
			val badge: BadgeDrawable = this.bottomNavigationView.getOrCreateBadge(R.id.pageWallet)
			badge.number = count
			badge.isVisible = true
		}
	}
	private fun configureTransactionBadge() {
		val pending_txs: ArrayList<Tx>? = Stash.getArrayList("PENDING_TX_LIST", Tx::class.java)
		Log.d("###Main2", pending_txs?.count().toString())
		val count = pending_txs?.count()!!
		if (count > 0) {
			val badge: BadgeDrawable = this.bottomNavigationView.getOrCreateBadge(R.id.pageTransaction)
			badge.number = count
			badge.isVisible = true
		}
	}

	override fun onNavigationItemSelected(item: MenuItem): Boolean {
		when(item.itemId) {
			R.id.pageHome -> {
				fetchAll(this) {
					HomeFragment.Instance.configureRecyclerView(HomeFragment.RootView.homeRecyclerView)
					this.configureTransactionBadge()
					this.configureWalletBadge()
					// will be automatically done on loading txFrag or walletFrag
					//TransactionFragment.Instance.configureRecyclerView(TransactionFragment.RootView.transactionRecyclerView)
					//WalletFragment.Instance.configureRecyclerView(WalletFragment.RootView.walletRecyclerView)
				}
				this.supportFragmentManager.beginTransaction().replace(R.id.linearLayout , HomeFragment()).commitAllowingStateLoss()
				return true
			}
			R.id.pageWallet -> {
				this.supportFragmentManager.beginTransaction().replace(R.id.linearLayout , WalletFragment()).commitAllowingStateLoss()
				return true
			}
			R.id.pageTransaction -> {
				this.supportFragmentManager.beginTransaction().replace(R.id.linearLayout , TransactionFragment()).commitAllowingStateLoss()
				return true
			}
			R.id.pageMyPage -> {
				this.supportFragmentManager.beginTransaction().replace(R.id.linearLayout , MyPageFragment()).commitAllowingStateLoss()
				return true
			}
		}
		return false
	}

	external fun hello(to: String): String
	external fun helloCommon(to: String): String
	external fun helloGg20Keygen(to: String): String
	external fun gg20keygen(url: String, parties: String, threshold: String, vaultidx: String, useridx: String): String
	external fun gg20sign(url: String, parties: String, threshold: String, keystore: String, msg: String, txidx: String, useridx: String): String
	external fun gg18keygen(url: String, parties: String, threshold: String, vaultidx: String, useridx: String): String
	external fun gg18sign(url: String, parties: String, threshold: String, keystore: String, msg: String, txidx: String, useridx: String): String
}
