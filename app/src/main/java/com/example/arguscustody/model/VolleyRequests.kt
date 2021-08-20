package com.example.arguscustody.view

import CoinType
import JsonApiCoinTypes
import JsonApiHwKey
import JsonApiInt
import JsonApiKeyDistributionTypes
import JsonApiT
import JsonApiTx
import JsonApiTxMsg
import JsonApiTxUsers
import JsonApiTxs
import JsonApiUsers
import JsonApiVault
import JsonApiVaults
import KeyDistributionType
import Tx
import TxUser
import User
import Vault
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.arguscustody.model.*
import com.example.arguscustody.token.showWaitingDialog
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_create_wallet.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.util.*

//val default_host = "192.168.0.16"
//val default_host = "192.168.0.193"
val default_host = "147.46.240.248"

fun API_HOST() : String {
	var host = Stash.getString("SERVER_IP", default_host)
	Log.d("API_HOST1", host)
	if (host.length < 1) {
		host = default_host
	}
	Log.d("API_HOST2", host)
	return "http://" + host + ":13309"
}

fun MPE_HOST() : String {
	var host = Stash.getString("SERVER_IP", default_host)
	if (host.length < 1) {
		host = default_host
	}
	Log.d("MPE_HOST", host)
	return "http://" + host + ":13309"
}
fun testVolley(context: Context) {
	// call GET (with query params)
	VolleySingleton.Instance(context).SendGET(
		"http://192.168.0.191:22227/users/ranks",
		"period_type=TOTAL",
		"",
		{ response ->
			var strResp = response.toString()
			Log.d("API", strResp)
		},
		{ Log.d("API", "that didn't work") }
	)

	// call GET (with JSON Object)
	VolleySingleton.Instance(context).SendGET(
		"/api/user/1",
		"",
		"",
		{ response ->
			var strResp = response.toString()
			Log.d("API", strResp)
		},
		{ Log.d("API", "that didn't work") }
	)

	// call POST
	val json = JSONObject()
	json.put("loginId", "u2@email.com")
	json.put("password", "1111")
	json.put("name", "홍길동")
	VolleySingleton.Instance(context).SendPOST(
		"/api/user",
		json,
		"",
		{ response ->
			// response
			// Get your json response and convert it to whatever you want.
			Log.d("API", response.toString())
		},
		{ error ->
			Log.d("API", "error => $error")
		}
	)
} // testVolley

fun traceJson(json: JSONObject) {
	val gson = GsonBuilder().create()
	val jsonString = gson.toJson(json)
	Log.d("###traceJson", jsonString)
}

// Function with GenericTypes (FAILED YET)
fun <T1> FetchData(context: Context,
									 url : String,
									 params: String,
									 token: String,
									 progressBar: View,
									 stashStore: String,
									 doSomethingOnSuccess: () -> Unit
) {
	progressBar.visibility = View.VISIBLE
	VolleySingleton.Instance(context).SendGET(
		url,
		params,
		token,
		{ response ->
			// response
			Log.d("###API", url + " : " + response.toString())
			val JsonIgnoreNull = Json { ignoreUnknownKeys = true }
			val jsonResp = JsonIgnoreNull.decodeFromString<JsonApiT<T1>>(response.toString())
			if (jsonResp?.status == 200) {
				var itemlist: ArrayList<T1>? = arrayListOf()
				jsonResp?.data.forEach {
					itemlist?.add(it)
				}
				Stash.put(stashStore, itemlist)
				doSomethingOnSuccess()

				Log.d("###API", url + " Succeeded (200)")
//					Toast.makeText(this, url + " Succeeded", Toast.LENGTH_SHORT).show()
			} else {
				Log.d("###API", url + " Failed: ("+jsonResp?.status.toString()+")")
//					Toast.makeText(this, url + " Failed: ("+jsonResp?.status.toString()+")", Toast.LENGTH_SHORT).show()
			}
			progressBar.visibility = View.GONE
		},
		{ error ->
			Log.d("###API", url + " error => $error")
//				Toast.makeText(this, url + " Failed", Toast.LENGTH_SHORT).show()
			progressBar.visibility = View.GONE
		}
	) // VolleySingleton.Instance(this).SendPOST
}

//
fun fetchAll(context: Context, doSomethingOnSuccess: () -> Unit) {
	val progress = showWaitingDialog(context, "Please wait.", "")
	fetchCoinTypes(context) {
		fetchKeyDistributionTypes(context) {
			fetchVaults(context) {
				fetchUsers(context) {
					fetchTxs(context, {
						progress.cancel()
						Log.d("###API", "##### PrintVaultsInStash()")
						PrintVaultsInStash()
						doSomethingOnSuccess()
					}, -1)
				}
			}
		}
	}
}

fun pingAuth(context: Context, doSomethingOnSuccess: () -> Unit) {
	if (!API_HOST().contains("147.46.240.248")) {
		doSomethingOnSuccess()
	}

	val url = "/api/auth"
	Log.d("###API", url)
	VolleySingleton.Instance(context).SendGET(
		url,
		"",
		Stash.getString("token", ""),
		{ response ->
			// response
			Log.d("###API",url + " : " + response.toString())
			// doSomethingOnResponse (200 or 400)
			doSomethingOnSuccess()
		},
		{ error ->
			Log.d("###API", url + " error => $error")
			Toast.makeText(context, url + " Failed", Toast.LENGTH_SHORT).show()
			doSomethingOnSuccess()
		}
	) // VolleySingleton.Instance(this).SendPOST
}

//
fun fetchUsers(context: Context, doSomethingOnSuccess: () -> Unit) {
	Stash.put("USER_LIST", "") // INIT ONCE

	// GET Vaults
	VolleySingleton.Instance(context).SendGET(
		"/api/user/users",
		"",
		Stash.getString("token", ""),
		{ response ->
			// response
			Log.d("###API", "/api/user/users : " + response.toString())
			val jsonResp = Json.decodeFromString<JsonApiUsers>(response.toString())

			if (jsonResp?.status == 200) {
				var UserList: ArrayList<User>? = arrayListOf()
				jsonResp?.data.forEach {
					UserList?.add(it)
					Log.d("###API", "/api/user/users : " + it.name + "/" + UserList?.count())
				}
				Stash.put("USER_LIST", UserList)
				doSomethingOnSuccess()
//				fetchCoinTypes(this, progressBar, {
//					fetchKeyDistributionTypes(this, loginProgressBar, {
//						val intent = Intent(this, MainActivity::class.java)
//						startActivity(intent)
//					})
//				})

				Log.d("###API", "(200) /api/user/users")
//				Toast.makeText(context, "Read Users Succeeded", Toast.LENGTH_SHORT).show()
			} else {
				Toast.makeText(context, "Read Users Failed: ("+jsonResp?.status.toString()+")", Toast.LENGTH_SHORT).show()
			}
		},
		{ error ->
			Log.d("###API", "error => $error")
			Toast.makeText(context, "Read Users Failed", Toast.LENGTH_SHORT).show()
		}
	) // VolleySingleton.Instance(this).SendPOST
}

fun fetchCoinTypes(context: Context, doSomethingOnSuccess: () -> Unit) {
	Stash.put("COINTYPE_LIST", "") // INIT ONCE

	// GET Vaults
	VolleySingleton.Instance(context).SendGET(
		"/api/coinType/coinTypes",
		"",
		"",
		{ response ->
			// response
			Log.d("###API", "coinTypes : " + response.toString())
			val jsonResp = Json.decodeFromString<JsonApiCoinTypes>(response.toString())

			if (jsonResp?.status == 200) {
				var CoinTypeList: ArrayList<CoinType>? = arrayListOf()
				CoinTypeList?.clear()
				jsonResp?.data.forEach {
					CoinTypeList?.add(it)
				}
				Stash.put("COINTYPE_LIST", CoinTypeList)

				doSomethingOnSuccess()
//				fetchKeyDistributionTypes(context, progressBar, {
//					val intent = Intent(context, MainActivity::class.java);
//					startActivity(intent);
//				})

				Log.d("###API", "(200) /api/coinType/coinTypes")
//				Toast.makeText(context, "Read coinTypes Succeeded", Toast.LENGTH_SHORT).show()
			} else {
				Toast.makeText(context, "Read coinTypes Failed: ("+jsonResp?.status.toString()+")", Toast.LENGTH_SHORT).show()
			}
		},
		{ error ->
			Log.d("###API", "error => $error")
			Toast.makeText(context, "Read coinTypes Failed", Toast.LENGTH_SHORT).show()
		}
	) // VolleySingleton.Instance(this).SendPOST
}

fun fetchKeyDistributionTypes(context: Context, doSomethingOnSuccess: () -> Unit) {
	Stash.put("KEYDISTRIBUTIONTYPE_LIST", "") // INIT ONCE

	// GET Vaults
	VolleySingleton.Instance(context).SendGET(
		"/api/keyDistributionType/keyDistributionTypes",
		"",
		"",
		{ response ->
			// response
			Log.d("###API", "KeyDistributionTypes : " + response.toString())
			val jsonResp = Json.decodeFromString<JsonApiKeyDistributionTypes>(response.toString())

			if (jsonResp?.status == 200) {
				var KeyDistributionTypeList: ArrayList<KeyDistributionType>? = arrayListOf()
				jsonResp?.data.forEach {
					it.fillName()
					KeyDistributionTypeList?.add(it)
				}
				Stash.put("KEYDISTRIBUTIONTYPE_LIST", KeyDistributionTypeList)
				doSomethingOnSuccess()
//					val intent = Intent(context, MainActivity::class.java);
//					startActivity(intent);

				Log.d("###API", "(200) /api/keyDistributionType/keyDistributionTypes")
//				Toast.makeText(context, "Read KeyDistributionTypes Succeeded", Toast.LENGTH_SHORT).show()
			} else {
				Toast.makeText(context, "Read KeyDistributionTypes Failed: ("+jsonResp?.status.toString()+")", Toast.LENGTH_SHORT).show()
			}
		},
		{ error ->
			Log.d("###API", "error => $error")
			Toast.makeText(context, "Read KeyDistributionTypes Failed", Toast.LENGTH_SHORT).show()
		}
	) // VolleySingleton.Instance(this).SendPOST
}

// fetchVaults -> fetchHwKey -> fetchVault
fun fetchVaults(context: Context, doSomethingOnSuccess: () -> Unit) {
	Stash.put("PENDING_VAULT_LIST", "") // INIT ONCE
	Stash.put("VAULT_LIST", "") // INIT ONCE

	// GET Vaults
	VolleySingleton.Instance(context).SendGET(
		"/api/vault/vaults",
		"",
		Stash.getString("token", ""),
		{ response ->
			// response
			Log.d("###API","/api/vault/vaults : " + response.toString())
			val JsonNull = Json { coerceInputValues = true }
			val jsonResp = JsonNull.decodeFromString<JsonApiVaults>(response.toString())

			if (jsonResp?.status == 200) {
				jsonResp?.data.forEach {
					it.keyDistributionType.fillName()
					replaceVaultInStash(it)
					fetchVault(context, {
					}, it.vaultIdx)
				}
				doSomethingOnSuccess()

				Log.d("###API", "(200) /api/vault/vaults")
//				Toast.makeText(context, "Read Vaults Succeeded", Toast.LENGTH_SHORT).show()
			} else {
				Toast.makeText(context, "Read Vaults Failed: ("+jsonResp?.status.toString()+")", Toast.LENGTH_SHORT).show()
			}
		},
		{ error ->
			Log.d("###API", "error => $error")
			Toast.makeText(context, "Read Vaults Failed", Toast.LENGTH_SHORT).show()
		}
	) // VolleySingleton.Instance(this).SendPOST
}

fun fetchHwKey(context: Context, doSomethingOnSuccess: () -> Unit, vaultIdx: Int) {
	val url = "/api/hwKey"
	Log.d("###API",url + " : " + "vaultIdx="+vaultIdx.toString())
	VolleySingleton.Instance(context).SendGET(
		url,
		"vaultIdx="+vaultIdx.toString(),
		Stash.getString("token", ""),
		{ response ->
			// response
			Log.d("###API",url + " : " + response.toString())
			val JsonNull = Json { coerceInputValues = true }
			val jsonResp = JsonNull.decodeFromString<JsonApiHwKey>(response.toString())

			if (jsonResp?.status == 200) {
				var vault = getVaultInStash(vaultIdx)
				vault.status = jsonResp?.data.status
				vault.key = jsonResp?.data.userHwKey
				if (vault.key.length > 0) {
					putKeyStoreHWKEY(vaultIdx, vault.key) // [HWKEY]
				}
				replaceVaultInStash(vault)

				doSomethingOnSuccess()

				Log.d("###API", url + " Success (200)")
//				Toast.makeText(context, "Read HwKey Succeeded", Toast.LENGTH_SHORT).show()
			} else {
				Log.d("###API", url + " Failed: ("+jsonResp?.status.toString()+")")
				Toast.makeText(context, url + " Failed: ("+jsonResp?.status.toString()+")", Toast.LENGTH_SHORT).show()
			}
		},
		{ error ->
			Log.d("###API", url + " error => $error")
			Toast.makeText(context, url + " Failed", Toast.LENGTH_SHORT).show()
		}
	) // VolleySingleton.Instance(this).SendPOST
}

fun fetchVault(context: Context, doSomethingOnSuccess: () -> Unit, vaultIdx: Int) {
	val url = "/api/vault"
	Log.d("###API",url + " : " + "vaultIdx="+vaultIdx.toString())
	VolleySingleton.Instance(context).SendGET(
		url,
		"vaultIdx="+vaultIdx.toString(),
		Stash.getString("token", ""),
		{ response ->
			// response
			Log.d("###API",url + " : " + response.toString())
			val JsonNull = Json { coerceInputValues = true }
			val jsonResp = JsonNull.decodeFromString<JsonApiVault>(response.toString())

			if (jsonResp.status == 200) {
				var vault : Vault = jsonResp.data
				vault.vaultIdx = vaultIdx
				vault.keyDistributionType.fillName()
				replaceVaultInStash(vault)
				fetchHwKey(context, {
					doSomethingOnSuccess()
				}, vault.vaultIdx)

				Log.d("###API", url + " Success (200)")
//				Toast.makeText(context, "Read vault Succeeded", Toast.LENGTH_SHORT).show()
			} else {
				Log.d("###API", url + " Failed: ("+jsonResp.status.toString()+")")
				Toast.makeText(context, url + " Failed: ("+jsonResp.status.toString()+")", Toast.LENGTH_SHORT).show()
			}
		},
		{ error ->
			Log.d("###API", url + " error => $error")
			Toast.makeText(context, url + " Failed", Toast.LENGTH_SHORT).show()
		}
	) // VolleySingleton.Instance(this).SendPOST
}

// fetchTxs -> fetchTxUsers
fun fetchTxs(context: Context, doSomethingOnSuccess: () -> Unit, vaultIdx: Int = -1) {
	if (vaultIdx < 0) {
		Stash.put("PENDING_TX_LIST", "") // INIT ONCE
		Stash.put("TX_LIST", "") // INIT ONCE
	}

	var url = "/api/tx/txs"
	var params = ""
	if (vaultIdx > -1) {
		url = "/api/vault/txs"
		params = "vaultIdx="+vaultIdx.toString()
	}
	val token = Stash.getString("token", "")
	// Function with GenericTypes (FAILED YET) // Tx & JsonApiTxs 때문에 안됨
	// VolleySingleton.Instance(this).FetchData<Tx>(url, params, token, txProgressBar as LinearLayout, stashStore, doSomethingOnSuccess )

	VolleySingleton.Instance(context).SendGET(
		url,
		params,
		token,
		{ response ->
			// response
			Log.d("###API", url + " : " + response.toString())
			val JsonIgnoreNull = Json { ignoreUnknownKeys = true }
			val jsonResp = JsonIgnoreNull.decodeFromString<JsonApiTxs>(response.toString())
			if (jsonResp?.status == 200) {
				// no tx
				if(jsonResp?.data.count() == 0) {
					doSomethingOnSuccess()
				}
				jsonResp?.data.forEach {
					fetchTxUsers(context, {
						doSomethingOnSuccess()
					}, it)
				}
				Log.d("###API", url + " Succeeded (200)")
//				Toast.makeText(context, url + " Succeeded (200)", Toast.LENGTH_SHORT).show()
			} else {
				Log.d("###API", url + " Failed: ("+jsonResp?.status.toString()+")")
				Toast.makeText(context, url + " Failed: ("+jsonResp?.status.toString()+")", Toast.LENGTH_SHORT).show()
			}
		},
		{ error ->
			Log.d("###API", url + " error => $error")
			Toast.makeText(context, url + " Failed", Toast.LENGTH_SHORT).show()
		}
	) // VolleySingleton.Instance(this).SendPOST
}

fun fetchTxUsers(context: Context, doSomethingOnSuccess: () -> Unit, tx: Tx) {
	val url = "/api/tx/users"
	val params = "txIdx="+tx.txIdx.toString()
	val token = Stash.getString("token", "")

	VolleySingleton.Instance(context).SendGET(
		url,
		params,
		token,
		{ response ->
			// response
			Log.d("###API", url + "?"+params+" : " + response.toString())
			val JsonIgnoreNull = Json { ignoreUnknownKeys = true }
			val jsonResp = JsonIgnoreNull.decodeFromString<JsonApiTxUsers>(response.toString())
			if (jsonResp?.status == 200) {
				tx.txUsers = jsonResp?.data
				tx.calcStatus()
				replaceTxInStash(tx)
				doSomethingOnSuccess()

				Log.d("###API", url + " Succeeded (200)")
//				Toast.makeText(context, url + " Succeeded (200)", Toast.LENGTH_SHORT).show()
			} else {
				Log.d("###API", url + " Failed: ("+jsonResp?.status.toString()+")")
				Toast.makeText(context, url + " Failed: ("+jsonResp?.status.toString()+")", Toast.LENGTH_SHORT).show()
			}
		},
		{ error ->
			Log.d("###API", url + " error => $error")
			Toast.makeText(context, url + " Failed", Toast.LENGTH_SHORT).show()
		}
	) // VolleySingleton.Instance(this).SendPOST
}

// fetchTx -> fetchTxUsers
fun fetchTx(context: Context, doSomethingOnSuccess: () -> Unit, txIdx: Int = 0) {
	val url = "/api/tx"
	var tx_idx = txIdx
	if (tx_idx == 0) { tx_idx = Stash.getInt("TX_ID") }
	val params = "txIdx="+tx_idx.toString()
	val token = Stash.getString("token", "")

	VolleySingleton.Instance(context).SendGET(
		url,
		params,
		token,
		{ response ->
			// response
			Log.d("###API", url + " : " + response.toString())
			val JsonIgnoreNull = Json { ignoreUnknownKeys = true }
			val jsonResp = JsonIgnoreNull.decodeFromString<JsonApiTx>(response.toString())
			if (jsonResp?.status == 200) {
				var tx = jsonResp?.data
				fetchTxUsers(context, {
					doSomethingOnSuccess()
				}, tx)

				Log.d("###API", url + " Succeeded (200)")
//				Toast.makeText(context, url + " Succeeded (200)", Toast.LENGTH_SHORT).show()
			} else {
				Log.d("###API", url + " Failed: ("+jsonResp?.status.toString()+")")
				Toast.makeText(context, url + " Failed: ("+jsonResp?.status.toString()+")", Toast.LENGTH_SHORT).show()
			}
		},
		{ error ->
			Log.d("###API", url + " error => $error")
			Toast.makeText(context, url + " Failed", Toast.LENGTH_SHORT).show()
		}
	) // VolleySingleton.Instance(this).SendPOST
}

// fetchTxMsg
fun fetchTxMsg(context: Context, doSomethingOnSuccess: () -> Unit, txIdx: Int = 0) {
	val url = "/api/tx/txMsg"
	var tx_idx = txIdx
	if (tx_idx == 0) { tx_idx = Stash.getInt("TX_ID") }
	val params = "txIdx="+tx_idx.toString()
	val token = Stash.getString("token", "")

	VolleySingleton.Instance(context).SendGET(
		url,
		params,
		token,
		{ response ->
			// response
			Log.d("###API", url + " : " + response.toString())
			val JsonIgnoreNull = Json { ignoreUnknownKeys = true }
			val jsonResp = JsonIgnoreNull.decodeFromString<JsonApiTxMsg>(response.toString())
			if (jsonResp?.status == 200) {
				Log.d("###API", url + " Succeeded (200)")
				var txMsg = jsonResp?.data
				Stash.put("TXMSG", txMsg)
				doSomethingOnSuccess()
			} else {
				Log.d("###API", url + " Failed: ("+jsonResp?.status.toString()+")")
				Toast.makeText(context, url + " Failed: ("+jsonResp?.status.toString()+")", Toast.LENGTH_SHORT).show()
			}
		},
		{ error ->
			Log.d("###API", url + " error => $error")
			Toast.makeText(context, url + " Failed", Toast.LENGTH_SHORT).show()
		}
	) // VolleySingleton.Instance(this).SendPOST
}

fun putHwKey(context: Context, doSomethingOnSuccess: () -> Unit, vault: Vault) {
	// 1. fetchHwKey를 하고, user_hw_key를 localStorage에 저장
	// 2. putHwKey를 하여 COMPLETE로 변경

	val url = "/api/hwKey"
	val json = JSONObject()
	json.put("vaultIdx", vault.vaultIdx)
	json.put("status", vault.status)
	if (vault.key.length > 0) {
		json.put("userHwKey", vault.key)
	}
	val token = Stash.getString("token", "")
//		val doSomethingOnSuccess: () -> Unit = {
//			this.downloadKeyButton.visibility = View.GONE
//		}

	// Function with GenericTypes (FAILED YET) // Tx & JsonApiTxs 때문에 안됨
	// VolleySingleton.Instance(this).FetchData<Tx>(url, params, token, txProgressBar as LinearLayout, stashStore, doSomethingOnSuccess )

	VolleySingleton.Instance(context).SendPUT(
		url,
		json,
		token,
		{ response ->
			// response
			Log.d("###API", url + " : " + response.toString())
			val JsonIgnoreNull = Json { ignoreUnknownKeys = true }
			val jsonResp = JsonIgnoreNull.decodeFromString<JsonApiInt>(response.toString())
			if (jsonResp?.status == 200) {
				doSomethingOnSuccess()

				Log.d("###API", url + " Succeeded (200)")
//				Toast.makeText(context, url + " Succeeded (200)", Toast.LENGTH_SHORT).show()
			} else {
				Log.d("###API", url + " Failed: ("+jsonResp?.status.toString()+")")
				Toast.makeText(context, url + " Failed: ("+jsonResp?.status.toString()+")", Toast.LENGTH_SHORT).show()
			}
		},
		{ error ->
			Log.d("###API", url + " error => $error")
			Toast.makeText(context, url + " Failed", Toast.LENGTH_SHORT).show()
		}
	) // VolleySingleton.Instance(this).SendPOST
}

fun postVault(context: Context, doSomethingOnSuccess: () -> Unit, json: JSONObject) {
	val url = "/api/vault"
	VolleySingleton.Instance(context).SendPOST(
		url,
		json,
		Stash.getString("token", ""),
		{ response ->
			// response
			Log.d("/api/vault 1", response.toString())
			val jsonResp = Json.decodeFromString<JsonApiVault>(response.toString())
			Log.d("/api/vault 2", jsonResp?.status.toString())
			Log.d("/api/vault 2", jsonResp?.data.vaultIdx.toString())

			if (jsonResp?.status == 200) {
				pushVault(jsonResp?.data.vaultIdx)
				doSomethingOnSuccess()

				Log.d("###API", url + " Succeeded (200)")
//				Toast.makeText(context, url + " Succeeded (200)", Toast.LENGTH_SHORT).show()
			} else {
				Log.d("###API", url + " Failed: ("+jsonResp?.status.toString()+")")
				Toast.makeText(context, url + " Failed: ("+jsonResp?.status.toString()+")", Toast.LENGTH_SHORT).show()
			}
		},
		{ error ->
			Log.d("###API", url + " error => $error")
			Toast.makeText(context, url + " Failed", Toast.LENGTH_SHORT).show()
		}
	) // VolleySingleton.Instance(this).SendPOST
}

fun postTx(context: Context, doSomethingOnSuccess: () -> Unit, json: JSONObject) {
	val url = "/api/tx"
	VolleySingleton.Instance(context).SendPOST(
		url,
		json,
		Stash.getString("token", ""),
		{ response ->
			// response
			val jsonResp = Json.decodeFromString<JsonApiTx>(response.toString())
			if (jsonResp?.status == 200) {
				pushTx(jsonResp?.data.txIdx)	// PostTx에서 만들어진 TX_ID
				doSomethingOnSuccess()

				Log.d("###API", url + " Succeeded (200)")
//				Toast.makeText(context, url + " Succeeded (200)", Toast.LENGTH_SHORT).show()
			} else {
				Log.d("###API", url + " Failed: ("+jsonResp?.status.toString()+")")
				Toast.makeText(context, url + " Failed: ("+jsonResp?.status.toString()+")", Toast.LENGTH_SHORT).show()
			}
		},
		{ error ->
			Log.d("###API", url + " error => $error")
			Toast.makeText(context, url + " Failed", Toast.LENGTH_SHORT).show()
		}
	) // VolleySingleton.Instance(this).SendPOST
}

fun postTxConfirm(context: Context, doSomethingOnSuccess: () -> Unit, json: JSONObject) {
	val url = "/api/tx/confirm"
	VolleySingleton.Instance(context).SendPOST(
		url,
		json,
		Stash.getString("token", ""),
		{ response ->
			// response
			val jsonResp = Json.decodeFromString<JsonApiInt>(response.toString())
			if (jsonResp?.status == 200) {
				doSomethingOnSuccess()

				Log.d("###API", url + " Succeeded (200)")
//				Toast.makeText(context, url + " Succeeded (200)", Toast.LENGTH_SHORT).show()
			} else {
				Log.d("###API", url + " Failed: ("+jsonResp?.status.toString()+")")
				Toast.makeText(context, url + " Failed: ("+jsonResp?.status.toString()+")", Toast.LENGTH_SHORT).show()
			}
		},
		{ error ->
			Log.d("###API", url + " error => $error")
			Toast.makeText(context, url + " Failed", Toast.LENGTH_SHORT).show()
		}
	) // VolleySingleton.Instance(this).SendPOST
}