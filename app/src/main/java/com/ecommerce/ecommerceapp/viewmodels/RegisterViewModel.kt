package com.ecommerce.ecommerceapp.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecommerce.ecommerceapp.models.RegisterResponse
import com.ecommerce.ecommerceapp.models.User
import com.ecommerce.ecommerceapp.network.ApiClient
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    var userName by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var registerSuccess by mutableStateOf<RegisterResponse?>(null)

    fun updateUserName(newUserName: String) {
        userName = newUserName
        errorMessage = null
    }

    fun updateEmail(newEmail: String) {
        email = newEmail
        errorMessage = null
    }

    fun updatePassword(newPassword: String) {
        password = newPassword
        errorMessage = null
    }

    fun updateConfirmPassword(newConfirmPassword: String) {
        confirmPassword = newConfirmPassword
        errorMessage = null
    }

    fun register() {
        // Validaciones
        if (userName.isBlank()) {
            errorMessage = "El nombre de usuario es requerido"
            return
        }

        if (email.isBlank()) {
            errorMessage = "El email es requerido"
            return
        }

        if (password.isBlank()) {
            errorMessage = "La contraseña es requerida"
            return
        }

        if (password.length < 6) {
            errorMessage = "La contraseña debe tener al menos 6 caracteres"
            return
        }

        if (password != confirmPassword) {
            errorMessage = "Las contraseñas no coinciden"
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val user = User(
                    UserName = userName,
                    Email = email,
                    PasswoRDkey = password,
                    iD_City = 1
                )

                Log.d("REGISTER_DEBUG", "Enviando user: $user")

                val response = ApiClient.apiService.register(user)

                Log.d("REGISTER_DEBUG", "Response code: ${response.code()}")
                Log.d("REGISTER_DEBUG", "Response successful: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    val registerResponse = response.body()
                    Log.d("REGISTER_DEBUG", "RegisterResponse: $registerResponse")
                    Log.d("REGISTER_DEBUG", "Message: ${registerResponse?.message}")
                    Log.d("REGISTER_DEBUG", "IsSuccess: ${registerResponse?.isSuccess()}")

                    // Considerar exitoso si es 201 o si el mensaje indica éxito
                    if (response.code() == 201 || registerResponse?.isSuccess() == true) {
                        registerSuccess = registerResponse ?: RegisterResponse(
                            message = "Usuario creado exitosamente",
                            user = user
                        )
                        Log.d("REGISTER_DEBUG", "Registro exitoso!")
                    } else {
                        errorMessage = registerResponse?.message ?: "Error al crear la cuenta"
                        Log.e("REGISTER_DEBUG", "Registro no exitoso")
                    }
                } else {
                    errorMessage = when (response.code()) {
                        409 -> "El email ya está registrado"
                        400 -> "Datos inválidos"
                        else -> "Error del servidor: ${response.code()}"
                    }
                    Log.e("REGISTER_DEBUG", "Error response: ${response.code()}")
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
                Log.e("REGISTER_DEBUG", "Exception: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }

    fun clearRegisterSuccess() {
        registerSuccess = null
    }
}