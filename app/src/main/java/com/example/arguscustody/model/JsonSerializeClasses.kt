import com.example.arguscustody.model.AuthMethodType
import com.example.arguscustody.model.MethodType
import com.example.arguscustody.model.Stash
import kotlinx.serialization.Serializable
import org.web3j.abi.datatypes.Bool

// COMMON
@Serializable
class UserToken(
	val userIdx: Int = 0,
	val token: String = "")

@Serializable
class User(
	val userIdx: Int = 0,
	val name: String = "",
	val login_id: String = "",
	val email: String = "")

@Serializable
class VaultUser(
	val status: String = "",
	val userHwKey: String = "",
	val user: User = User(0, ""))

@Serializable
class Vault(
	var vaultIdx: Int = 0,
	val name: String = "",
	val address: String = "",
	val coinName: String = "",
	val usedTime: String = "",
	val createdTime: String = "",
	var status: String = "", // created, pending, deleted
	var key: String = "",
	val keyDistributionType: KeyDistributionType = KeyDistributionType(0, "", 0, 0, 0),
	var vaultUsers: List<VaultUser> = emptyList(),
	var vaultAdmins: List<VaultUser> = emptyList()) {

	fun getVaultInfo(): String {
		val info = name + " (" + vaultIdx.toString() + " / " + keyDistributionType.name + ")"
		return info
	}

	fun getVaultInfoWithKey(): String {
		val info = getVaultInfo() + " key:" + key
		return info
	}

	fun isComplete(): Boolean {
		return (status == "COMPLETE" || status == "EXPRTED")
//		return !(status == "CREATE" || status == "JOIN" || status == "CANCEL")
	}

	fun getVaultUsers() : String {
		var ret: String = ""
		vaultUsers.forEach {
			ret = ret + "[" + it.user.name + "] "
		}
		return ret
	}

	fun getVaultAdmins() : String {
		var ret: String = ""
		vaultAdmins.forEach {
			ret = ret + "[" + it.user.name + "] "
		}
		return ret
	}
}

@Serializable
class TxUser(
	val status: String = "",
	val updateTime: String = "",
	val user: User = User(0, ""))

@Serializable
class Tx(
	val txIdx: Int = 0,
	var vaultIdx: Int = 0,
	var fromAddress: String = "",
	var toAddress: String = "",
	var amount: Double = 0.0,
	var timeout: Int = 600000,
	var status: String = "",
	var statusUsers: String = "",
	val signTime: String = "",
	val txHash: String = "",
	var gasPrice: Int = 2,
	var gasLimit: Int = 21000,
	var creatorUserIdx: Int = 0,
	val createdTime: String = "",
	val updatedTime: String = "",
	var txUsers: List<TxUser> = emptyList()) {

	fun isCreatedOfUser(userIdx: Int): Boolean {
		if (statusOfUser(userIdx) == "CREATED") {
			return true
		}
		return false
	}

	fun statusOfUser(userIdx: Int): String {
		txUsers.forEach {
			if (it.user.userIdx == userIdx) {
				return it.status
			}
		}
		return "NONE"
	}

	fun isCompleted(): Boolean {
		return (statusUsers == "COMPLETED" || status == "DONE" || status == "SUCCESS")
	}

	fun calcStatus() {
		statusUsers = "NONE"
		txUsers.forEach {
			if (statusUsers != "CREATED") {
				if (it.status == "CREATED") {
					statusUsers = "CREATED"
				} else if (it.status == "READYTOSEND" && statusUsers != "CREATED") {
					statusUsers = "READYTOSEND"
				} else if (it.status == "SENT" && (statusUsers != "CREATED" || statusUsers != "READYTOSEND")) {
					statusUsers = "SENT"
				} else if ((it.status == "COMPLETED" || it.status == "DONE") && (statusUsers != "CREATED" || statusUsers != "READYTOSEND" || statusUsers != "SENT")) {
					statusUsers = "COMPLETED"
				}
			}
		}
	}
}

@Serializable
class HwKey(
	val vaultIdx: Int = 0,
	val userIdx: Int = 0,
	val userHwKey: String = "",
	val status: String = "")

@Serializable
class CoinType(
	val coinTypeIdx: Int = 0,
	val name: String)

@Serializable
class KeyDistributionType(
	val keyDistributionTypeIdx: Int = 0,
	var name: String = "",
	val k: Int,
	val n: Int,
	val type: Int) {
	fun fillName() {
		val kofn = k.toString() + " of " + n.toString()
		if (type == AuthMethodType.SSS.ordinal) {
			name = kofn + " SSS"
		} else if (type == AuthMethodType.TECDSA.ordinal) {
			name = kofn + " T-ECDSA"
		}
	}
}

// /api/auth
@Serializable
class JsonApiAuth(
	val status: Int,
	val data: UserToken = UserToken(0, ""),
	val errMsg: String = "")

// /api/vault
@Serializable
class JsonApiVault(
	val status: Int,
	val data: Vault = Vault(0, "", "", "", "", "", "", "", KeyDistributionType(0, "", 0,0, 0)),
	val errMsg: String = "")

// PUT /api/hwKey
@Serializable
class JsonApiInt(
	val status: Int,
	val data: Int = 0,
	val errMsg: String = "")

// GET /api/hwKey
@Serializable
class JsonApiHwKey(
	val status: Int,
	val data: HwKey,
	val errMsg: String = "")

// GET /api/GENERIC_TYPE // TRYING
@Serializable
class JsonApiT<T>(
	val status: Int,
	val data: List<T> = emptyList(),
	val errMsg: String = "")

// /api/vault/vaults
@Serializable
class JsonApiVaults(
	val status: Int,
	val data: List<Vault> = emptyList(),
	val errMsg: String = "")

// /api/tx/txs
@Serializable
class JsonApiTxs(
	val status: Int,
	val data: List<Tx> = emptyList(),
	val errMsg: String = "")

// get /api/tx
// post /api/tx
@Serializable
class JsonApiTx(
	val status: Int,
	val data: Tx,
	val errMsg: String = "")

// get /api/tx/users
@Serializable
class JsonApiTxUsers(
	val status: Int,
	val data: List<TxUser> = emptyList(),
	val errMsg: String = "")

// /api/user/users
@Serializable
class JsonApiUsers(
	val status: Int,
	val data: List<User> = emptyList(),
	val errMsg: String = "")

// /api/coinType/coinTypes
@Serializable
class JsonApiCoinTypes(
	val status: Int,
	val data: List<CoinType> = emptyList(),
	val errMsg: String = "")

// /api/keyDistributionType/keyDistributionTypes
@Serializable
class JsonApiKeyDistributionTypes(
	val status: Int,
	val data: List<KeyDistributionType> = emptyList(),
	val errMsg: String = "")

// /api/tx/txMsg
@Serializable
class JsonApiTxMsg(
	val status: Int,
	val data: String = "",
	val errMsg: String = "")

//
@Serializable
class JsonGG18Key(
	val party_keys_s: JsonPartyKeys,
	val shared_keys: JsonSharedKeys,
	val party_num_int_s: Int,
	val vss_scheme_vec_s: List<JsonVssSchemeVecS>,
	val paillier_key_vec_s: List<JsonN>,
	val y_sum_s: JsonXy
)

@Serializable
class JsonPartyKeys(
	val u_i: String,
	val y_i: JsonXy,
	val dk: JsonPq,
	val ek: JsonN,
	val party_index: Int
)

@Serializable
class JsonGG20Key(
	val party_keys_s: JsonPartyKeysS,
	val shared_keys: JsonSharedKeys,
	val party_num_int_s: Int,
	val vss_scheme_vec_s: List<JsonVssSchemeVecS>,
	val paillier_key_vec_s: List<JsonN>,
	val y_sum_s: JsonXy,
	val h1_h2_N_tilde_vec_s: List<JsonH1H2NTildeVecS>
)

@Serializable
class JsonPartyKeysS(
	val u_i: String,
	val y_i: JsonXy,
	val dk: JsonPq,
	val ek: JsonN,
	val party_index: Int,
	val N_tilde: String,
	val h1: String,
	val h2: String,
	val xhi: String
)

@Serializable
class JsonSharedKeys(
	val y: JsonXy,
	val x_i: String
)

@Serializable
class JsonVssSchemeVecS(
	val parameters: JsonParameters,
	val commitments: List<JsonXy>
)

@Serializable
class JsonParameters(
	val threshold: Int,
	val share_count: Int
)

@Serializable
class JsonH1H2NTildeVecS(
	val N: String,
	val g: String,
	val ni: String
)

@Serializable
class JsonXy(
	val x: String,
	val y: String
)

@Serializable
class JsonPq(
	val p: String,
	val q: String
)

@Serializable
class JsonN(
	val n: String
)
