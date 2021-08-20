package com.example.arguscustody.model

import Tx
import User
import Vault
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.util.ArrayList

fun PrintTxsInStash() {
	Log.d("###API", "+++++ PrintTxsInStash")
	var txlist: ArrayList<Tx>? = arrayListOf()
	txlist = Stash.getArrayList("TX_LIST", Tx::class.java)
	(txlist as ArrayList<Tx>?)?.forEach {
		Log.d("###API",
			"#####TX_LIST " + it.createdTime + "(" + it.status + "/" + it.amount.toString() + ")")
	}
	Log.d("###API", "----- PrintTxsInStash")
}
fun PrintVaultsInStash() {
	Log.d("###API", "+++++ PrintVaultsInStash")
	var VaultList: ArrayList<Vault>? = arrayListOf()
	VaultList = Stash.getArrayList("PENDING_VAULT_LIST", Vault::class.java)
	(VaultList as ArrayList<Vault>?)?.forEach {
		Log.d("###API", "#####PENDING_VAULT_LIST " + it.name + "(" + it.status + "/" + it.key + ")" + it.vaultUsers.count().toString() + "/" + it.vaultAdmins.count().toString())
	}
	VaultList = Stash.getArrayList("VAULT_LIST", Vault::class.java)
	(VaultList as ArrayList<Vault>?)?.forEach {
		Log.d("###API", "#####VAULT_LIST " + it.name + "(" + it.address + "/" + it.status + "/" + it.key + ")")
	}
	Log.d("###API", "----- PrintVaultsInStash")
}

fun putKeyStoreHWKEY(vaultIdx: Int, key: String) {
	Stash.put("KEYSTORE" + vaultIdx.toString(), key)
}

fun getKeyStoreHWKEY(vaultIdx: Int): String {
	return Stash.getString("KEYSTORE" + vaultIdx.toString())
}

fun putSignStore(vaultIdx: Int, sign: String) {
	Stash.put("SIGNSTORE" + vaultIdx.toString(), sign)
}

fun getSignStore(vaultIdx: Int): String {
	return Stash.getString("SIGNSTORE" + vaultIdx.toString())
}

fun popUser(): User {
	return getUserInStash(Stash.getInt("USER_IDX"))
}

fun getUserInStash(useridx: Int): User {
	var userlist = Stash.getArrayList<User>("USER_LIST", User::class.java)
	userlist.forEach {
		if (it.userIdx == useridx) {
			return it
		}
	}
	return User(0, "")
}

fun popVault() : Vault {
	return getVaultInStash(Stash.getInt("VAULT_ID"))
}

fun pushVault(vaultIdx: Int) {
	Stash.put("VAULT_ID", vaultIdx)
}

fun getVaults() : ArrayList<Vault> {
	var vaultList: ArrayList<Vault>? = arrayListOf()
	vaultList = Stash.getArrayList("VAULT_LIST", Vault::class.java)
	return vaultList
}

fun getVaultInStash(vaultIdx: Int) : Vault {
	var pending_vaults: ArrayList<Vault>? = arrayListOf()
	pending_vaults = Stash.getArrayList("PENDING_VAULT_LIST", Vault::class.java)
	pending_vaults.forEach {
		if (it.vaultIdx == vaultIdx) {
			return it
		}
	}
	var vaults: ArrayList<Vault>? = arrayListOf()
	vaults = Stash.getArrayList("VAULT_LIST", Vault::class.java)
	vaults.forEach {
		if (it.vaultIdx == vaultIdx) {
			return it
		}
	}
	return Vault()
}

fun replaceVaultInStash(vault: Vault) {
	var isFound: Boolean = false

	var vaults: ArrayList<Vault>? = arrayListOf()
	vaults = Stash.getArrayList("VAULT_LIST", Vault::class.java)
	var new_vaults : ArrayList<Vault>? = arrayListOf()
	vaults.forEach {
		if (it.vaultIdx == vault.vaultIdx) {
			if (!vault.isComplete()) {
				// vaultIdx가 같고, CREATED이면 VAULT_LIST에서 삭제하고, PENDING_VAULT_LIST에 넣어야 함
				// remove from VAULT_LIST & add to PENDING_VAULT_LIST as below
			} else {
				new_vaults?.add(vault)
				isFound = true
			}
		} else {
			new_vaults?.add(it)
		}
	}
	if (!isFound && vault.isComplete()) {
		// vaultIdx가 같은 것이 없고, CREATED가 아니면 VAULT_LIST에 넣음
		new_vaults?.add(vault)
	}
	Stash.put("VAULT_LIST", new_vaults)

	var pending_vaults: ArrayList<Vault>? = arrayListOf()
	pending_vaults = Stash.getArrayList("PENDING_VAULT_LIST", Vault::class.java)
	var new_pending_vaults : ArrayList<Vault>? = arrayListOf()
	pending_vaults.forEach {
		if (it.vaultIdx == vault.vaultIdx) {
			if (vault.isComplete()) {
				// vaultIdx가 같고, CREATED가 아니면 PENDING_VAULT_LIST에서 삭제하고, VAULT_LIST에 넣어야 함
				// remove from PENDING_VAULT_LIST & add to VAULT_LIST as above
			} else {
				new_pending_vaults?.add(vault)
				isFound = true
			}
		} else {
			new_pending_vaults?.add(it)
		}
	}
	if (!isFound && !vault.isComplete()) {
		// vaultIdx가 같은 것이 없고, CREATED이면 PENDING_VAULT_LIST에 넣음
		new_pending_vaults?.add(vault)
	}
	Stash.put("PENDING_VAULT_LIST", new_pending_vaults)
}

fun popTx() : Tx {
	return getTxInStash(Stash.getInt("TX_ID"))
}

fun pushTx(txIdx: Int) {
	Stash. put("TX_ID", txIdx)
}

fun getTxInStash(txIdx: Int) : Tx {
	var pending_txs: ArrayList<Tx>? = arrayListOf()
	pending_txs = Stash.getArrayList("PENDING_TX_LIST", Tx::class.java)
	pending_txs.forEach {
		if (it.txIdx == txIdx) {
			return it
		}
	}
	var txs: ArrayList<Tx>? = arrayListOf()
	txs = Stash.getArrayList("TX_LIST", Tx::class.java)
	txs.forEach {
		if (it.txIdx == txIdx) {
			return it
		}
	}
	return Tx()
}
fun getVaultTxs(vaultIdx: Int) : ArrayList<Tx> {
	val all_txs : ArrayList<Tx> = Stash.getArrayList("TX_LIST", Tx::class.java)
	var txs: ArrayList<Tx> = arrayListOf()
	all_txs.forEach {
		if (it.vaultIdx == vaultIdx) {
			txs?.add(it)
		}
	}
	return txs
}

fun replaceTxInStash(tx: Tx) {
	var isFound: Boolean = false

	var txs: ArrayList<Tx>? = arrayListOf()
	txs = Stash.getArrayList("TX_LIST", Tx::class.java)
	var new_txs : ArrayList<Tx>? = arrayListOf()
	txs.forEach {
		if (it.txIdx == tx.txIdx) {
			if (!tx.isCompleted()) {
				// txIdx가 같고, COMPLETED가 아니면 TX_LIST에서 삭제하고, PENDING_TX_LIST에 넣어야 함
			} else {
				new_txs?.add(tx)
				isFound = true
			}
		} else {
			new_txs?.add(it)
		}
	}
	if (!isFound && tx.isCompleted()) {
		new_txs?.add(tx)
	}
	Stash.put("TX_LIST", new_txs)

	var pending_txs: ArrayList<Tx>? = arrayListOf()
	pending_txs = Stash.getArrayList("PENDING_TX_LIST", Tx::class.java)
	var new_pending_txs : ArrayList<Tx>? = arrayListOf()
	pending_txs.forEach {
		if (it.txIdx == tx.txIdx) {
			if (tx.isCompleted()) {
				// txIdx가 같고, COMPLETED이면 PENDING_TX_LIST에서 삭제하고, TX_LIST에 넣어야 함
			} else {
				new_pending_txs?.add(tx)
				isFound = true
			}
		} else {
			new_pending_txs?.add(it)
		}
	}
	if (!isFound && !tx.isCompleted()) {
		new_pending_txs?.add(tx)
	}
	Stash.put("PENDING_TX_LIST", new_pending_txs)
}

fun makeQRCode(content: String): Bitmap? {
	val writer = QRCodeWriter()
	val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
	val width = bitMatrix.width
	val height = bitMatrix.height
	val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
	for (x in 0 until width) {
		for (y in 0 until height) {
			bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
		}
	}
	return bitmap
}

fun copyToClipboard(context: Context, str: String) {
	val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
	val clip: ClipData = ClipData.newPlainText("copy", str)
	clipboard.setPrimaryClip(clip)
}