package com.example.arguscustody.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.arguscustody.R
import kotlinx.android.synthetic.main.activity_signup.*
import org.json.JSONObject
import kotlinx.serialization.*
import kotlinx.serialization.json.*

class SignupActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_signup)

		this.configureButtons()

		// init TEST
		this.nameEditText.setText("홍길동")
		this.loginIdEditText.setText("u1@email.com")
		this.loginPasswordEditText.setText("****")
	}

	private fun configureButtons() {

		this.signupButton.setOnClickListener {

			if (this.loginPasswordEditText.text.toString() == "00") {
				val intent = Intent(this, TestActivity::class.java)
				startActivity(intent)
				return@setOnClickListener
			}

			pingAuth(this) {
				llProgressBar.visibility = View.VISIBLE

				val json = JSONObject()
				json.put("loginId", this.loginIdEditText.text)
				json.put("password", this.loginPasswordEditText.text)
				json.put("name", this.loginIdEditText.text)
				VolleySingleton.Instance(this).SendPOST(
					"/api/user",
					json,
					"",
					{ response ->
						// response
						Log.d("API", response.toString())
						val jsonResp = Json.decodeFromString<VolleyJsonReponse>(response.toString())
						if (jsonResp.status == 200) {
							Toast.makeText(this, "User Added", Toast.LENGTH_SHORT).show()
							onBackPressed()
						} else {
							Toast.makeText(
								this,
								"User Add Failed: (" + jsonResp.status.toString() + ")",
								Toast.LENGTH_SHORT
							).show()
						}
						llProgressBar.visibility = View.GONE
					},
					{ error ->
						Log.d("API", "error => $error")
						Toast.makeText(this, "Signup Failed", Toast.LENGTH_SHORT).show()
						llProgressBar.visibility = View.GONE
						onBackPressed()
					}
				) // VolleySingleton.Instance(this).SendPOST
			}
		} // signupButton.setOnClickListener
	} // configureButtons
}
