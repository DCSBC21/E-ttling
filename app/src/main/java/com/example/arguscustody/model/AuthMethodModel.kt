package com.example.arguscustody.model

data class AuthMethodModel (
	val id: Int,
	val name: String,
	val type: AuthMethodType
)

enum class AuthMethodType {
	SSS, TECDSA
}
