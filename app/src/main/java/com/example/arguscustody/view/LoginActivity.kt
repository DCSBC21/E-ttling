package com.example.arguscustody.view

import JsonApiAuth
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.arguscustody.R
import com.example.arguscustody.model.PrintVaultsInStash
import com.example.arguscustody.model.Stash
import com.example.arguscustody.token.showWaitingDialog
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_login)

		this.configureButtons()

		// TEST volley
		// testVolley()

		val loginId = Stash.getString("loginId", "u1@email.com")
		val password = Stash.getString("password", "****")
		this.loginIdEditText.setText(loginId)
		this.loginPasswordEditText.setText(password)
	}

	private fun login() {
		pingAuth(this) {
			val json = JSONObject()
			json.put("loginId", Stash.getString("loginId"))
			json.put("password", Stash.getString("password"))

			VolleySingleton.Instance(this).SendPOST(
				"/api/auth",
				json,
				"",
				{ response ->
					// response
					Log.d("###API", "/api/auth : " + response.toString())
					val jsonResp = Json.decodeFromString<JsonApiAuth>(response.toString())

					if (jsonResp?.status == 200) {
						Stash.put("USER_IDX", jsonResp?.data.userIdx)
						Stash.put("token", jsonResp?.data.token)

						// fetch next
						fetchAll(this) {
							val intent = Intent(this, MainActivity::class.java)
							startActivity(intent)
						}

						Log.d("###API", "(200) /api/auth")
						Toast.makeText(this, "Login Succeeded", Toast.LENGTH_SHORT).show()
					} else {
						Toast.makeText(this, "Login Failed: ("+jsonResp?.status.toString()+")", Toast.LENGTH_SHORT).show()
					}
				},
				{ error ->
					Log.d("###API", "error => $error")
					Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
				}
			) // VolleySingleton.Instance(this).SendPOST
		}
	}

	private fun configureButtons() {
		this.loginButton.setOnClickListener {
			Stash.put("loginId", this.loginIdEditText.text.toString())
			Stash.put("password", this.loginPasswordEditText.text.toString())
			login()
		} // loginButton.setOnClickListener

		this.signupButton.setOnClickListener {
			val intent = Intent(this, SignupActivity::class.java)
			startActivity(intent)
		}
	} // configureButtons

}
