package com.ecommerce.ecommerceapp.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecommerce.ecommerceapp.models.*
import com.ecommerce.ecommerceapp.network.ApiClient
import kotlinx.coroutines.launch

class LocationManagementViewModel : ViewModel() {

    // Listas de datos
    var countries by mutableStateOf<List<Country>>(emptyList())
    var states by mutableStateOf<List<State>>(emptyList())
    var cities by mutableStateOf<List<City>>(emptyList())

    // Estados de carga
    var isLoadingCountries by mutableStateOf(false)
    var isLoadingStates by mutableStateOf(false)
    var isLoadingCities by mutableStateOf(false)
    var isCreating by mutableStateOf(false)

    // Formularios
    var newCountryName by mutableStateOf("")
    var newStateName by mutableStateOf("")
    var selectedCountryForState by mutableStateOf<Country?>(null)
    var newCityName by mutableStateOf("")
    var selectedStateForCity by mutableStateOf<State?>(null)

    // Estados de error y éxito
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    init {
        loadCountries()
    }

    fun loadCountries() {
        isLoadingCountries = true
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getCountries()
                if (response.isSuccessful && response.body() != null) {
                    countries = response.body()!!
                } else {
                    errorMessage = "Error al cargar países"
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
                Log.e("LOCATION_DEBUG", "Error loading countries", e)
            } finally {
                isLoadingCountries = false
            }
        }
    }

    fun loadStates(countryId: Int? = null) {
        isLoadingStates = true
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getStates(countryId)
                if (response.isSuccessful && response.body() != null) {
                    states = response.body()!!
                } else {
                    errorMessage = "Error al cargar estados"
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
                Log.e("LOCATION_DEBUG", "Error loading states", e)
            } finally {
                isLoadingStates = false
            }
        }
    }

    fun loadCities(stateId: Int? = null) {
        isLoadingCities = true
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getCities(stateId)
                if (response.isSuccessful && response.body() != null) {
                    cities = response.body()!!
                } else {
                    errorMessage = "Error al cargar ciudades"
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
                Log.e("LOCATION_DEBUG", "Error loading cities", e)
            } finally {
                isLoadingCities = false
            }
        }
    }

    fun createCountry() {
        if (newCountryName.isBlank()) {
            errorMessage = "El nombre del país es requerido"
            return
        }

        isCreating = true
        viewModelScope.launch {
            try {
                val request = CreateLocationRequest(CountryName = newCountryName)
                val response = ApiClient.apiService.createCountry(request)

                if (response.isSuccessful) {
                    successMessage = "País creado exitosamente"
                    newCountryName = ""
                    loadCountries() // Recargar lista
                } else {
                    errorMessage = "Error al crear el país"
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
                Log.e("LOCATION_DEBUG", "Error creating country", e)
            } finally {
                isCreating = false
            }
        }
    }

    fun createState() {
        if (newStateName.isBlank()) {
            errorMessage = "El nombre del estado es requerido"
            return
        }

        if (selectedCountryForState == null) {
            errorMessage = "Debe seleccionar un país"
            return
        }

        isCreating = true
        viewModelScope.launch {
            try {
                val request = CreateLocationRequest(
                    StatesName = newStateName,
                    iD_Country = selectedCountryForState!!.iD_Country
                )
                val response = ApiClient.apiService.createState(request)

                if (response.isSuccessful) {
                    successMessage = "Estado creado exitosamente"
                    newStateName = ""
                    selectedCountryForState = null
                    loadStates() // Recargar lista
                } else {
                    errorMessage = "Error al crear el estado"
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
                Log.e("LOCATION_DEBUG", "Error creating state", e)
            } finally {
                isCreating = false
            }
        }
    }

    fun createCity() {
        if (newCityName.isBlank()) {
            errorMessage = "El nombre de la ciudad es requerido"
            return
        }

        if (selectedStateForCity == null) {
            errorMessage = "Debe seleccionar un estado"
            return
        }

        isCreating = true
        viewModelScope.launch {
            try {
                val request = CreateLocationRequest(
                    CityName = newCityName,
                    iD_States = selectedStateForCity!!.iD_States
                )
                val response = ApiClient.apiService.createCity(request)

                if (response.isSuccessful) {
                    successMessage = "Ciudad creada exitosamente"
                    newCityName = ""
                    selectedStateForCity = null
                    loadCities() // Recargar lista
                } else {
                    errorMessage = "Error al crear la ciudad"
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
                Log.e("LOCATION_DEBUG", "Error creating city", e)
            } finally {
                isCreating = false
            }
        }
    }

    // Funciones de actualización
    fun updateNewCountryName(name: String) {
        newCountryName = name
        clearMessages()
    }

    fun updateNewStateName(name: String) {
        newStateName = name
        clearMessages()
    }

    fun updateSelectedCountryForState(country: Country?) {
        selectedCountryForState = country
        clearMessages()
    }

    fun updateNewCityName(name: String) {
        newCityName = name
        clearMessages()
    }

    fun updateSelectedStateForCity(state: State?) {
        selectedStateForCity = state
        clearMessages()
    }

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }
}