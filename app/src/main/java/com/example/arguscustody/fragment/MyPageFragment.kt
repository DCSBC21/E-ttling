package com.example.arguscustody.fragment

import User
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.arguscustody.BuildConfig
import com.example.arguscustody.R
import com.example.arguscustody.adapters.HomeAccountListAdapter
import com.example.arguscustody.model.*
import com.example.arguscustody.token.*
import com.example.arguscustody.view.LoginActivity
import com.example.arguscustody.view.MPE_HOST
import com.example.arguscustody.view.MainActivity
import com.example.arguscustody.view.TestActivity
import com.fasterxml.jackson.databind.node.BigIntegerNode
import kotlinx.android.synthetic.main.fragment_my_page.view.*
import okio.ByteString.Companion.encodeUtf8
import org.jetbrains.anko.doAsync
import org.web3j.crypto.Credentials
import java.math.BigInteger
import kotlin.math.log


class MyPageFragment: Fragment() {
	private lateinit var user: User

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val rootView = inflater.inflate(R.layout.fragment_my_page, container, false)
		user = popUser()
		this.configureLogOutButton(rootView.logOutButton, rootView)
		this.setUiItems(rootView)
		return rootView
	}

	private fun configureLogOutButton(logOutButton: Button, rootView: View) {
		logOutButton.setOnClickListener {
				val intent = Intent(activity, LoginActivity::class.java)
				startActivity(intent)
		}
	}

	private fun setUiItems(rootView: View) {
		rootView.versionTextView.setText(BuildConfig.VERSION_NAME)
		rootView.nickNameTextView.setText(user.email)
		rootView.nameTextView.setText(user.name)
	}

}