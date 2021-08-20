package com.example.arguscustody.model

data class Account (
	val name: String,
	val type: MethodType
)

enum class MethodType {
	SSS2OF3, SSS3OF4, TECDSA2OF3, TECDSA3OF4
}