package com.example.arguscustody.token

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import one.block.eosiojava.utilities.EOSFormatter
import one.block.eosiojavaandroidkeystoresignatureprovider.EosioAndroidKeyStoreSignatureProvider
import one.block.eosiojavaandroidkeystoresignatureprovider.errors.ErrorString
import one.block.eosiojavaandroidkeystoresignatureprovider.errors.PublicKeyConversionError
import org.bouncycastle.asn1.ASN1Encodable
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers
import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.spec.ECNamedCurveSpec
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemWriter
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric
import java.io.ByteArrayInputStream
import java.io.StringWriter
import java.math.BigInteger
import java.security.*
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECPoint
import java.util.*

val nodeUrl = "https://jungle3.cryptolions.io"

val ANDROID_PUBLIC_KEY_OID_ID: Int = 0
val EC_PUBLICKEY_OID_INDEX: Int = 0
val ANDROID_PRIVATE_KEY_OID_ID: Int = 0
val EC_PRIVATEKEY_OID_INDEX: Int = 0
val SECP256R1_OID_INDEX: Int = 1
val ANDROID_KEYSTORE: String = "AndroidKeyStore"
val ANDROID_ECDSA_SIGNATURE_ALGORITHM: String = "SHA256withECDSA"
val SECP256R1_CURVE_NAME = "secp256r1"
val PEM_OBJECT_TYPE_PUBLIC_KEY = "PUBLIC KEY"
val PEM_OBJECT_TYPE_PRIVATE_KEY = "PRIVATE KEY"

fun test_secp256k1() {
	val privKey =
		BigInteger("1e84cb45d863d98c78c5f023891d6dae2c4d6339da2f1b75079f4adfb643e46b", 16) // secp256k1
//		BigInteger("97ddae0f3a25b92268175400149d65d6887b9cefaf28ea2c078e05cdc15a3c0a", 16) // secp256r1
//		BigInteger("8cdfa4f99ed55f1d17d14fad84f22e88b0c51fc0ff429f8b9c07f2f47e622f2e", 16) // secp256r1
	val pubKey: BigInteger = Sign.publicKeyFromPrivate(privKey)

	val keypair = ECKeyPair(privKey, pubKey)
	Log.d("EOS1", keypair.publicKey.toByteArray().ToHexString())
	Log.d("EOS2", keypair.privateKey.toByteArray().ToHexString())

	val ecPubKey = toEcPublicKey(keypair.publicKey.toByteArray().ToHexString(), "secp256k1")
	val ecPrivKey = toEcPrivateKey(keypair.publicKey.toByteArray().ToHexString(), "secp256k1")

	// 이 아래는 무의미함 --- secp256r1을 위한 것임
	val eosPubKey = convertAndroidKeyStorePublicKeyToEOSFormat(ecPubKey)
	Log.d("EOS3", eosPubKey)
//	val eosPrivKey = convertAndroidKeyStorePrivateKeyToEOSFormat(ecPrivKey) // error
//	Log.d("EOS4", eosPrivKey)
}

fun test_generateKeypair() {
	// 88, 144로 너무 길게 나옴
	val keypair_k1 = generateKeypair("secp256k1")
	Log.d("EOS1", keypair_k1.public.encoded.size.toString()) // 88?
	Log.d("EOS2", keypair_k1.private.encoded.size.toString()) // 144?
	val keypair_r1 = generateKeypair("secp256r1")
	Log.d("EOS3", bytesToEncodedString(keypair_r1.public.encoded))
	Log.d("EOS4", bytesToEncodedString(keypair_r1.private.encoded))
//	val ecPubKey = toEcPublicKey(keypair_r1.public.encoded.toString())
//	val ecPrivKey = toEcPrivateKey(keypair_r1.private.encoded.toString())
//	Log.d("EOS5", bytesToEncodedString(ecPubKey.encoded))
//	Log.d("EOS6", bytesToEncodedString(ecPrivKey.encoded))
//	val ecPubKey2 = toEcPublicKey(bytesToEncodedString(keypair_r1.public.encoded))
//	val ecPrivKey2 = toEcPrivateKey(bytesToEncodedString(keypair_r1.private.encoded))
//	Log.d("EOS7", bytesToEncodedString(ecPubKey2.encoded))
//	Log.d("EOS8", bytesToEncodedString(ecPrivKey2.encoded))
//	val eosPubKey = convertAndroidKeyStorePublicKeyToEOSFormat(ecPubKey)
//	val eosPrivKey = convertAndroidKeyStorePrivateKeyToEOSFormat(ecPrivKey)
//	Log.d("EOS3", eosPubKey)
//	Log.d("EOS4", eosPrivKey)
}

fun eos_createKey(alias: String) {
	test_secp256k1()
	test_generateKeypair()
}

fun generateKeypair(algorithm: String): KeyPair {
	//Security.addProvider(org.bouncycastle.jce.provider.BouncyCastleProvider())
	val keypairGen = KeyPairGenerator.getInstance("ECDSA", "BC")
	keypairGen.initialize(ECGenParameterSpec(algorithm), SecureRandom())
	val keyPair = keypairGen.generateKeyPair()
	return keyPair
}

// eosioandroidkeystoresignatureprovider
fun eosio_createKey(alias: String) {
	val storekey1 = generateAndroidKeyStoreKey("storekey1")
	Log.d("EOS", storekey1)

	val eosioAndroidKeyStoreSignatureProvider: EosioAndroidKeyStoreSignatureProvider =
		EosioAndroidKeyStoreSignatureProvider.Builder().build()
	val allKeysInKeyStore: List<String> = eosioAndroidKeyStoreSignatureProvider.availableKeys
	allKeysInKeyStore.forEach {
		Log.d("EOS", it)
	}
}

fun generateAndroidKeyStoreKey(alias: String): String {
	// Create a default KeyGenParameterSpec
	val keyGenParameterSpec: KeyGenParameterSpec = generateDefaultKeyGenParameterSpecBuilder(alias).build()

	return generateAndroidKeyStoreKey(keyGenParameterSpec)
}

fun generateAndroidKeyStoreKey(keyGenParameterSpec: KeyGenParameterSpec): String {
	val kpg: KeyPairGenerator =
		KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, ANDROID_KEYSTORE)
	kpg.initialize(keyGenParameterSpec)
	val newKeyPair: KeyPair = kpg.generateKeyPair()
//	val newKeyPair: KeyPair = KeyPair()


	Log.d("EOS3", convertAndroidKeyStorePublicKeyToEOSFormat(newKeyPair.public as ECPublicKey))
	val pubkey = newKeyPair.public as ECPublicKey
	Log.d("EOS4", convertAndroidKeyStorePublicKeyToEOSFormat(pubkey as ECPublicKey))

//	Log.d("EOS1", newKeyPair.private.encoded.size.toString())
//	val privkey = toEcPrivateKey(String(newKeyPair.private.encoded))
//	Log.d("EOS2", convertAndroidKeyStorePrivateKeyToEOSFormat(privkey as ECPrivateKey))

	return convertAndroidKeyStorePublicKeyToEOSFormat(
		androidECPublicKey = newKeyPair.public as ECPublicKey
	)
}

fun convertAndroidKeyStorePrivateKeyToEOSFormat(androidECPrivateKey: ECPrivateKey): String {
	// Read the key information using the supported ASN.1 standard.
	val bIn: ASN1InputStream = ASN1InputStream(ByteArrayInputStream(androidECPrivateKey.encoded))
	val asn1Sequence: ASN1Sequence = (bIn.readObject()).toASN1Primitive() as ASN1Sequence

	val stringWriter: StringWriter = StringWriter()
	val pemWriter: PemWriter = PemWriter(stringWriter)
	val pemObject: PemObject = PemObject(PEM_OBJECT_TYPE_PRIVATE_KEY, asn1Sequence.encoded)
	pemWriter.writeObject(pemObject)
	pemWriter.flush()

	val pemFormattedPrivateKey: String = stringWriter.toString()

	return EOSFormatter.convertPEMFormattedPrivateKeyToEOSFormat(pemFormattedPrivateKey)
}

fun convertAndroidKeyStorePublicKeyToEOSFormat(androidECPublicKey: ECPublicKey): String {
	// Read the key information using the supported ASN.1 standard.
	val bIn: ASN1InputStream = ASN1InputStream(ByteArrayInputStream(androidECPublicKey.encoded))
	val asn1Sequence: ASN1Sequence = (bIn.readObject()).toASN1Primitive() as ASN1Sequence

//	// Verify if the key is ECPublicKey and SECP256R1
//	val publicKeyOID: Array<ASN1Encodable> =
//		(asn1Sequence.getObjectAt(ANDROID_PUBLIC_KEY_OID_ID) as ASN1Sequence).toArray()
//	if (X9ObjectIdentifiers.id_ecPublicKey.id != publicKeyOID[EC_PUBLICKEY_OID_INDEX].toString()
//		|| X9ObjectIdentifiers.prime256v1.id != publicKeyOID[SECP256R1_OID_INDEX].toString()
//	) {
//		throw PublicKeyConversionError(ErrorString.CONVERT_EC_TO_EOS_INVALID_INPUT_KEY)
//	}

	val stringWriter: StringWriter = StringWriter()
	val pemWriter: PemWriter = PemWriter(stringWriter)
	val pemObject: PemObject = PemObject(PEM_OBJECT_TYPE_PUBLIC_KEY, asn1Sequence.encoded)
	pemWriter.writeObject(pemObject)
	pemWriter.flush()

	val pemFormattedPublicKey: String = stringWriter.toString()

	return EOSFormatter.convertPEMFormattedPublicKeyToEOSFormat(pemFormattedPublicKey, false)
}

fun generateDefaultKeyGenParameterSpecBuilder(alias: String): KeyGenParameterSpec.Builder {
	return KeyGenParameterSpec.Builder(
		alias,
		KeyProperties.PURPOSE_SIGN
	)
		.setDigests(KeyProperties.DIGEST_SHA256)
		.setAlgorithmParameterSpec(ECGenParameterSpec(SECP256R1_CURVE_NAME))
}

fun toEcPublicKey(publicKey: String, algorithm: String): ECPublicKey {
	val params = ECNamedCurveTable.getParameterSpec(algorithm)
	val curveSpec = ECNamedCurveSpec(algorithm, params.curve, params.g, params.n)

	//This is the part how to generate ECPoint manually from public key string.
	val pubKeyX = publicKey.substring(0, publicKey.length / 2)
	val pubKeyY = publicKey.substring(publicKey.length / 2)
	val ecPoint = ECPoint(BigInteger(pubKeyX, 16), BigInteger(pubKeyY, 16))

	val params2 = EC5Util.convertSpec(curveSpec.curve, params)

	val keySpec = java.security.spec.ECPublicKeySpec(ecPoint, params2)
	val factory = KeyFactory.getInstance("ECDSA")
	return factory.generatePublic(keySpec) as ECPublicKey
}

fun toEcPrivateKey(privateKey: String, algorithm: String): ECPrivateKey {
	val ecKeyPair = ECKeyPair.create(Numeric.hexStringToByteArray(privateKey))

	val params = ECNamedCurveTable.getParameterSpec(algorithm)
	val curveSpec = ECNamedCurveSpec(algorithm, params.curve, params.g, params.n)

	val keySpec = java.security.spec.ECPrivateKeySpec(
		ecKeyPair.privateKey,
		curveSpec)

	val factory = KeyFactory.getInstance("ECDSA")
	return factory.generatePrivate(keySpec) as ECPrivateKey
}

// ######
fun eos_getBalance(account: String) {
	CheckBalanceTask(object :
		CheckBalanceTask.CheckBalanceTaskCallback {
		override fun update(updateContent: String) {
			Log.d("EOS1", updateContent)
		}

		override fun finish(success: Boolean, updateContent: String?, balance: String?) {
			var message: String = updateContent!!
			Log.d("EOS2", message)

			if (success) {
				Log.d("EOS2", balance!!)
			} else {
				Log.d("EOS2", "eos_getBalance error")
			}
		}
	}).execute(nodeUrl, account)
}

fun eos_Transfer(fromAccount: String, toAccount: String, privateKey: String, amount: String, memo: String) {
	TransactionTask(object :
		TransactionTask.TransactionTaskCallback {
		override fun update(updateContent: String) {
			Log.d("EOS1", updateContent)
		}

		override fun finish(success: Boolean, updateContent: String?) {
			var message: String = updateContent!!
			Log.d("EOS2", message)
			if (success) {
				Log.d("EOS2", "eos_Transfer success")
			} else {
				Log.d("EOS2", "eos_Transfer error")
			}
		}
	}).execute(nodeUrl, fromAccount, toAccount, privateKey, amount, memo)
}
