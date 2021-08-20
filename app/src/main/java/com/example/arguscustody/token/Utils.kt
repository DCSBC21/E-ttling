package com.example.arguscustody.token

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import com.example.arguscustody.R
import kotlinx.android.synthetic.main.dialog_confirm_create_wallet.view.*
import kotlinx.android.synthetic.main.dialog_default.view.*
import kotlinx.android.synthetic.main.progressbar_with_text.view.*
import java.util.*

fun ByteArray.ToHexString() = joinToString("") { "%02x".format(it) }
//fun ByteArray.toString() = joinToString("") { "%c".format(it) }

fun bytesToString(bytes: ByteArray): String {
	return String(bytes)
}

fun stringToBytes(astring: String): ByteArray {
	return astring.toByteArray()
}

fun bytesToEncodedString(bytes: ByteArray): String {
	return Base64.getEncoder().encodeToString(bytes)
}

fun encodedStringToBytes(encodedString: String): ByteArray {
	return Base64.getDecoder().decode(encodedString)
}

fun String.toHex(): ByteArray {
	require(length % 2 == 0) { "Must have an even length" }

	return chunked(2)
		.map { it.toInt(16).toByte() }
		.toByteArray()
}

fun matchDetails(inputString: String, whatToFind: String, startIndex: Int = 0): Int {
	val matchIndex = inputString.indexOf(whatToFind, startIndex)
	return if (matchIndex >= 0) matchIndex else -1
}

fun showWaitingDialog(context: Context, msgText: String = "Please wait.", closeBtnText: String = "Close", duration: Int = 0) : AlertDialog {
	val mDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_default, null)
	val mBuilder = android.app.AlertDialog.Builder(context).setView(mDialogView).create()
	mBuilder.show()

//		mDialogView.dialogTextView.text = text
	mDialogView.dialogProgressBar.visibility = View.VISIBLE
	mDialogView.dialogProgressBar.setBackgroundColor(Color.argb(255, 255, 255, 255))
	mDialogView.progressBarText.setText(msgText)
	mDialogView.progressBarText.setTextColor(Color.argb(255, 0, 0, 0))
	mDialogView.dialogCloseButton.setText(closeBtnText)
	if(closeBtnText == "") {
		mDialogView.dialogCloseButton.visibility = View.GONE
	}

	mDialogView.dialogCloseButton.setOnClickListener {
		mDialogView.dialogProgressBar.visibility = View.GONE
		mBuilder.cancel()
	}

	if (duration > 0) {
		val title = mDialogView.dialogCloseButton.text.toString()
		var remains = duration
		val countDownTimer = object : CountDownTimer((duration * 1000).toLong(), 1000) {
			override fun onTick(millisUntilFinished: Long) {
				remains -= 1
				mDialogView.dialogCloseButton.setText(title + " (" + remains.toString() + ")" )
			}
			override fun onFinish() {
				mDialogView.dialogProgressBar.visibility = View.GONE
				mBuilder.cancel()
			}
		}.start()
	}

	return mBuilder
}