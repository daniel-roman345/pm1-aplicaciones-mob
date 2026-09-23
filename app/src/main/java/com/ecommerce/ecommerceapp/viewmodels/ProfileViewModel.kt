package com.ecommerce.ecommerceapp.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecommerce.ecommerceapp.models.*
import com.ecommerce.ecommerceapp.network.ApiClient
import com.ecommerce.ecommerceapp.utils.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProfileViewModel(private val sessionManager: SessionManager) : ViewModel() {

    var userProfile by mutableStateOf<UserProfile?>(null)
    var userName by mutableStateOf("")
    var selectedCityId by mutableStateOf(1)
    var cities by mutableStateOf<List<City>>(emptyList())

    // Estados de carga
    var isLoadingProfile by mutableStateOf(false)
    var isUpdatingProfile by mutableStateOf(false)
    var isChangingPassword by mutableStateOf(false)
    var isLoadingCities by mutableStateOf(false)

    // Estados de error y éxito
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    // Cambio de contraseña
    var currentPassword by mutableStateOf("")
    var newPassword by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    init {
        loadProfile()
        loadCities()
    }

    fun loadProfile() {
        isLoadingProfile = true
        viewModelScope.launch {
            try {
                val token = sessionManager.token.first()
                if (token != null) {
                    val response = ApiClient.apiService.getProfile("Bearer $token")

                    if (response.isSuccessful && response.body() != null) {
                        userProfile = response.body()
                        userName = userProfile?.UserName ?: ""
                        selectedCityId = userProfile?.iD_City ?: 1
                    } else {
                        errorMessage = "Error al cargar el perfil"
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
                Log.e("PROFILE_DEBUG", "Error loading profile", e)
            } finally {
                isLoadingProfile = false
            }
        }
    }

    fun updateProfile() {
        if (userName.isBlank()) {
            errorMessage = "El nombre de usuario es requerido"
            return
        }

        isUpdatingProfile = true
        viewModelScope.launch {
            try {
                val token = sessionManager.token.first()
                if (token != null) {
                    val updateRequest = UpdateProfileRequest(
                        UserName = userName,
                        iD_City = selectedCityId
                    )

                    val response = ApiClient.apiService.updateProfile("Bearer $token", updateRequest)

                    if (response.isSuccessful && response.body() != null) {
                        userProfile = response.body()

                        // Actualizar sesión con nuevo nombre
                        sessionManager.saveUserSession(
                            token = token,
                            userName = userName,
                            userEmail = userProfile?.Email ?: "",
                            isAdmin = userProfile?.roles?.any { it.TypeRole == "Administrador" } ?: false,
                            roles = userProfile?.roles?.map { it.TypeRole } ?: emptyList()
                        )

                        successMessage = "Perfil actualizado exitosamente"
                    } else {
                        errorMessage = "Error al actualizar el perfil"
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
                Log.e("PROFILE_DEBUG", "Error updating profile", e)
            } finally {
                isUpdatingProfile = false
            }
        }
    }

    fun changePassword() {
        // Validaciones
        if (currentPassword.isBlank()) {
            errorMessage = "La contraseña actual es requerida"
            return
        }

        if (newPassword.isBlank()) {
            errorMessage = "La nueva contraseña es requerida"
            return
        }

        if (newPassword.length < 6) {
            errorMessage = "La nueva contraseña debe tener al menos 6 caracteres"
            return
        }

        if (newPassword != confirmPassword) {
            errorMessage = "Las contraseñas no coinciden"
            return
        }

        isChangingPassword = true
        viewModelScope.launch {
            try {
                val token = sessionManager.token.first()
                if (token != null) {
                    val passwordRequest = ChangePasswordRequest(
                        current_password = currentPassword,
                        new_password = newPassword
                    )

                    val response = ApiClient.apiService.changePassword("Bearer $token", passwordRequest)

                    if (response.isSuccessful) {
                        successMessage = "Contraseña cambiada exitosamente"
                        // Limpiar campos
                        currentPassword = ""
                        newPassword = ""
                        confirmPassword = ""
                    } else {
                        errorMessage = when (response.code()) {
                            401 -> "Contraseña actual incorrecta"
                            else -> "Error al cambiar la contraseña"
                        }
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
                Log.e("PROFILE_DEBUG", "Error changing password", e)
            } finally {
                isChangingPassword = false
            }
        }
    }

    private fun loadCities() {
        isLoadingCities = true
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getCities()
                if (response.isSuccessful && response.body() != null) {
                    cities = response.body()!!
                }
            } catch (e: Exception) {
                Log.e("PROFILE_DEBUG", "Error loading cities", e)
            } finally {
                isLoadingCities = false
            }
        }
    }

    fun updateUserName(newName: String) {
        userName = newName
        clearMessages()
    }

    fun updateSelectedCity(cityId: Int) {
        selectedCityId = cityId
        clearMessages()
    }

    fun updateCurrentPassword(password: String) {
        currentPassword = password
        clearMessages()
    }

    fun updateNewPassword(password: String) {
        newPassword = password
        clearMessages()
    }

    fun updateConfirmPassword(password: String) {
        confirmPassword = password
        clearMessages()
    }

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }
}