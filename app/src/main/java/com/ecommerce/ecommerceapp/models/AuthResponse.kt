package com.ecommerce.ecommerceapp.models

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("token")
    val token: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("user")
    val user: User? = null
) {
    fun isAdmin(): Boolean {
        return user?.roles?.any { it.TypeRole == "Administrador" } == true
    }

    fun isSuccess(): Boolean {
        return token != null && user != null
    }

    fun getRoleNames(): List<String> {
        return user?.roles?.map { it.TypeRole } ?: emptyList()
    }
}