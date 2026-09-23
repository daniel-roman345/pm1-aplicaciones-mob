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
                    // CORRECCIÓN PM1: la lista viene DENTRO del objeto de respuesta.
                    countries = response.body()!!.countries
                    Log.d("LOCATION_DEBUG", "Países cargados: ${countries.size}")
                    if (countries.isEmpty()) {
                        errorMessage = "El servidor no devolvió ningún país"
                    }
                } else {
                    // Se muestra el código real para poder distinguir un 404 de un 500.
                    errorMessage = "Error al cargar países (código ${response.code()})"
                    Log.e("LOCATION_DEBUG", "getCountries HTTP ${response.code()}")
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
                    // CORRECCIÓN PM1: la lista viene DENTRO del objeto de respuesta.
                    states = response.body()!!.states
                    Log.d("LOCATION_DEBUG", "Estados cargados: ${states.size} (país=$countryId)")
                } else {
                    errorMessage = "Error al cargar estados (código ${response.code()})"
                    Log.e("LOCATION_DEBUG", "getStates HTTP ${response.code()}")
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
                    // CORRECCIÓN PM1: la lista viene DENTRO del objeto de respuesta.
                    cities = response.body()!!.cities
                    Log.d("LOCATION_DEBUG", "Ciudades cargadas: ${cities.size} (estado=$stateId)")
                } else {
                    errorMessage = "Error al cargar ciudades (código ${response.code()})"
                    Log.e("LOCATION_DEBUG", "getCities HTTP ${response.code()}")
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

    // CORRECCIÓN PM1 · Reto 1 — encadenar país -> estados.
    // Antes esta función solo guardaba el país escogido: el segundo desplegable
    // nunca se enteraba y seguía mostrando los estados del país anterior.
    // Ahora, al escoger un país:
    //   1. se limpia la selección dependiente (estado y ciudad), para no dejar
    //      un estado de Colombia seleccionado mientras el país dice México;
    //   2. se dispara la carga de los estados de ESE país.
    fun updateSelectedCountryForState(country: Country?) {
        selectedCountryForState = country
        clearMessages()

        selectedStateForCity = null
        states = emptyList()
        cities = emptyList()

        if (country != null) {
            Log.d("LOCATION_DEBUG", "País seleccionado: ${country.CountryName} (id=${country.iD_Country})")
            loadStates(country.iD_Country)
        }
    }

    fun updateNewCityName(name: String) {
        newCityName = name
        clearMessages()
    }

    // Mismo encadenamiento un nivel más abajo: estado -> ciudades.
    fun updateSelectedStateForCity(state: State?) {
        selectedStateForCity = state
        clearMessages()

        cities = emptyList()

        if (state != null) {
            Log.d("LOCATION_DEBUG", "Estado seleccionado: ${state.StatesName} (id=${state.iD_States})")
            loadCities(state.iD_States)
        }
    }

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }
}