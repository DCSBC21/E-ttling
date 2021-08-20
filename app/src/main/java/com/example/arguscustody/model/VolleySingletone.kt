package com.example.arguscustody.view

import android.content.Context
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.arguscustody.model.Stash
import kotlinx.serialization.Serializable
import org.json.JSONObject

@Serializable
class VolleyJsonReponse(val status: Int, val errMsg: String = "")

class VolleySingleton constructor(context: Context) {
	companion object {
		@Volatile
		private var INSTANCE: VolleySingleton? = null
		fun Instance(context: Context) =
			INSTANCE ?: synchronized(this) {
				INSTANCE ?: VolleySingleton(context).also {
					INSTANCE = it
				}
			}
	}
	private val requestQueue: RequestQueue by lazy {
		// applicationContext is key, it keeps you from leaking the
		// Activity or BroadcastReceiver if someone passes one in.
		Volley.newRequestQueue(context.applicationContext)
	}

	fun <T> addToRequestQueue(req: Request<T>) {
		requestQueue.add(req)
	}

	fun SendGET(url: String,
								params: String,
						  	token: String,
								respListner: Response.Listener<String>,
								errorListner: Response.ErrorListener) {
		// Request a string response from the provided URL.
		val final_url : String =
			if (url.contains("http"))
				if (params == "") url else  url + '?' + params
			else
				if (params == "") API_HOST() + url else API_HOST() + url + '?' + params

		val stringReq =
			object: StringRequest(
				Request.Method.GET,
				final_url,
				respListner,
				errorListner
			) {
				@Throws(AuthFailureError::class)
				override fun getHeaders(): Map<String, String> {
					val headers = HashMap<String, String>()
					headers.put("Content-Type", "application/json")
					headers.put("Authorization", "Bearer "+token)
					return headers
				}
//				override fun getParams(): Map<String, String> {
//					val params = HashMap<String, String>()
//					params["period_type"] = "TOTAL"
//					return params
//				}
			}
		addToRequestQueue(stringReq)
	}

	fun SendPOST(url: String,
								 json: JSONObject,
							   token: String,
								 respListner: Response.Listener<JSONObject>,
								 errorListner: Response.ErrorListener) {

		val final_url : String =
			if (url.contains("http"))
				url
			else
				API_HOST() + url

		val jsonRequest : JsonObjectRequest =
			object : JsonObjectRequest(
				Method.POST,
				final_url,
				json,
				respListner,
				errorListner
			){
				@Throws(AuthFailureError::class)
				override fun getHeaders(): Map<String, String> {
					val headers = HashMap<String, String>()
					headers.put("Content-Type", "application/json")
					headers.put("Authorization", "Bearer "+token)
					return headers
				}
				override fun getBody(): ByteArray {
					return json.toString().toByteArray()
				}
			}
		addToRequestQueue(jsonRequest)
	}

	fun SendPUT(url: String,
							 json: JSONObject,
							 token: String,
							 respListner: Response.Listener<JSONObject>,
							 errorListner: Response.ErrorListener) {

		val final_url : String =
			if (url.contains("http"))
				url
			else
				API_HOST() + url

		val jsonRequest : JsonObjectRequest =
			object : JsonObjectRequest(
				Method.PUT,
				final_url,
				json,
				respListner,
				errorListner
			){
				@Throws(AuthFailureError::class)
				override fun getHeaders(): Map<String, String> {
					val headers = HashMap<String, String>()
					headers.put("accept", "application/json")
					headers.put("Authorization", "Bearer "+token)
					headers.put("Content-Type", "application/json")
					return headers
				}
				override fun getBody(): ByteArray {
					return json.toString().toByteArray()
				}
			}
		addToRequestQueue(jsonRequest)
	}

}