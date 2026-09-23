package com.ecommerce.ecommerceapp.models

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @SerializedName("message")
    val message: String? = null,

    @SerializedName("user")
    val user: User? = null,

    @SerializedName("id")
    val id: Int? = null
) {
    fun isSuccess(): Boolean {
        return message?.contains("exitoso", ignoreCase = true) == true ||
                message?.contains("success", ignoreCase = true) == true ||
                user != null
    }
}