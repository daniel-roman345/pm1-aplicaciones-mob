package com.ecommerce.ecommerceapp.viewmodels

import android.util.Log
import android.util.Patterns
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
    var email by mutableStateOf("")
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
                        // CORRECCIÓN PM1 · Reto 2: el perfil viene DENTRO de la
                        // llave "user". Antes se asignaba el envoltorio completo
                        // y todos los campos llegaban vacíos.
                        userProfile = response.body()!!.user
                        userName = userProfile?.UserName ?: ""
                        email = userProfile?.Email ?: ""
                        selectedCityId = userProfile?.iD_City ?: 1
                        Log.d("PROFILE_DEBUG", "Perfil cargado: $userName <$email>")
                    } else {
                        errorMessage = when (response.code()) {
                            401 -> "Tu sesión expiró, vuelve a iniciar sesión"
                            else -> "Error al cargar el perfil (código ${response.code()})"
                        }
                        Log.e("PROFILE_DEBUG", "getProfile HTTP ${response.code()}")
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

    // CORRECCIÓN PM1 · Reto 2: validación de entrada antes de tocar la red.
    // La guía pide campos obligatorios, formato de correo y mensajes claros.
    // Se valida aquí, en el ViewModel, y no en la pantalla, para que la regla
    // sea una sola y la pantalla siga limitándose a dibujar el estado.
    fun validarFormulario(): String? {
        if (userName.isBlank()) return "El nombre de usuario es requerido"
        if (userName.trim().length < 3) return "El nombre de usuario debe tener al menos 3 caracteres"
        if (email.isBlank()) return "El correo es requerido"
        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            return "El correo no tiene un formato válido"
        }
        if (selectedCityId <= 0) return "Debes seleccionar una ciudad"
        return null
    }

    // La pantalla la usa para deshabilitar el botón de guardar mientras el
    // formulario no sea válido.
    val formularioEsValido: Boolean
        get() = validarFormulario() == null

    fun updateProfile() {
        val problema = validarFormulario()
        if (problema != null) {
            errorMessage = problema
            Log.d("PROFILE_DEBUG", "Formulario rechazado: $problema")
            return
        }

        isUpdatingProfile = true
        viewModelScope.launch {
            try {
                val token = sessionManager.token.first()
                if (token != null) {
                    val updateRequest = UpdateProfileRequest(
                        UserName = userName.trim(),
                        iD_City = selectedCityId,
                        Email = email.trim()
                    )

                    val response = ApiClient.apiService.updateProfile("Bearer $token", updateRequest)

                    if (response.isSuccessful && response.body() != null) {
                        // CORRECCIÓN PM1 · Reto 2: mismo envoltorio, ahora con
                        // el mensaje que manda el propio servidor.
                        val cuerpo = response.body()!!
                        userProfile = cuerpo.user
                        email = userProfile?.Email ?: email

                        // Actualizar sesión con nuevo nombre
                        sessionManager.saveUserSession(
                            token = token,
                            userName = userName,
                            userEmail = userProfile?.Email ?: "",
                            isAdmin = userProfile?.roles?.any { it.TypeRole == "Administrador" } ?: false,
                            roles = userProfile?.roles?.map { it.TypeRole } ?: emptyList()
                        )

                        successMessage = cuerpo.message ?: "Perfil actualizado exitosamente"
                        Log.d("PROFILE_DEBUG", "Perfil guardado: $userName, ciudad $selectedCityId")
                    } else {
                        // El 409 lo devuelve el backend cuando el correo ya lo
                        // tiene otro usuario. Decirlo tal cual evita que el
                        // aprendiz crea que la app está rota.
                        errorMessage = when (response.code()) {
                            409 -> "Ese correo ya está en uso por otro usuario"
                            401 -> "Tu sesión expiró, vuelve a iniciar sesión"
                            else -> "Error al actualizar el perfil (código ${response.code()})"
                        }
                        Log.e("PROFILE_DEBUG", "updateProfile HTTP ${response.code()}")
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
                    // La lista viene DENTRO del objeto de respuesta: { "cities": [...] }
                    cities = response.body()!!.cities
                    Log.d("PROFILE_DEBUG", "Ciudades cargadas: ${cities.size}")
                } else {
                    Log.e("PROFILE_DEBUG", "getCities HTTP ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("PROFILE_DEBUG", "Error loading cities", e)
            } finally {
                isLoadingCities = false
            }
        }
    }

    fun updateEmail(newEmail: String) {
        email = newEmail
        clearMessages()
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