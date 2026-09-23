package com.ecommerce.ecommerceapp.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecommerce.ecommerceapp.models.AuthResponse
import com.ecommerce.ecommerceapp.models.LoginRequest
import com.ecommerce.ecommerceapp.network.ApiClient
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    var email by mutableStateOf("admin@ecommerce.com")
    var password by mutableStateOf("admin123")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var loginSuccess by mutableStateOf<AuthResponse?>(null)

    fun updateEmail(newEmail: String) {
        email = newEmail
        errorMessage = null
    }

    fun updatePassword(newPassword: String) {
        password = newPassword
        errorMessage = null
    }

    fun login() {
        // Validaciones
        if (email.isBlank()) {
            errorMessage = "El email es requerido"
            return
        }

        if (password.isBlank()) {
            errorMessage = "La contraseña es requerida"
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val loginRequest = LoginRequest(email, password)
                Log.d("LOGIN_DEBUG", "Enviando request: $loginRequest")

                val response = ApiClient.apiService.login(loginRequest)

                Log.d("LOGIN_DEBUG", "Response code: ${response.code()}")
                Log.d("LOGIN_DEBUG", "Response successful: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    Log.d("LOGIN_DEBUG", "AuthResponse: $authResponse")
                    Log.d("LOGIN_DEBUG", "Token: ${authResponse?.token}")
                    Log.d("LOGIN_DEBUG", "User: ${authResponse?.user}")
                    Log.d("LOGIN_DEBUG", "IsSuccess: ${authResponse?.isSuccess()}")

                    if (authResponse?.isSuccess() == true) {
                        loginSuccess = authResponse
                        Log.d("LOGIN_DEBUG", "Login exitoso!")
                    } else {
                        errorMessage = "Error: ${authResponse?.message ?: "Respuesta inválida"}"
                        Log.e("LOGIN_DEBUG", "AuthResponse no es exitosa")
                    }
                } else {
                    errorMessage = when (response.code()) {
                        401 -> "Credenciales incorrectas"
                        404 -> "Usuario no encontrado"
                        else -> "Error del servidor: ${response.code()}"
                    }
                    Log.e("LOGIN_DEBUG", "Error response: ${response.code()}")
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
                Log.e("LOGIN_DEBUG", "Exception: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }

    fun clearLoginSuccess() {
        loginSuccess = null
    }
}