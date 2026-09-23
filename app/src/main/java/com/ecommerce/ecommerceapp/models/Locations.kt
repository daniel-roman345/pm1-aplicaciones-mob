package com.ecommerce.ecommerceapp.models

import com.google.gson.annotations.SerializedName

data class Country(
    @SerializedName("iD_Country")
    val iD_Country: Int,

    @SerializedName("CountryName")
    val CountryName: String
)

data class State(
    @SerializedName("iD_States")
    val iD_States: Int,

    @SerializedName("StatesName")
    val StatesName: String,

    @SerializedName("iD_Country")
    val iD_Country: Int,

    @SerializedName("country")
    val country: Country? = null
)

data class CreateLocationRequest(
    @SerializedName("CountryName")
    val CountryName: String? = null,

    @SerializedName("StatesName")
    val StatesName: String? = null,

    @SerializedName("CityName")
    val CityName: String? = null,

    @SerializedName("iD_Country")
    val iD_Country: Int? = null,

    @SerializedName("iD_States")
    val iD_States: Int? = null
)
// ============================================================================
//  CLASES ENVOLTORIO DE LAS RESPUESTAS  (corrección PM1 · Reto 1)
// ============================================================================
// CAUSA DEL ERROR: el backend NO devuelve una lista pelada. Envuelve siempre la
// lista dentro de un objeto con una llave y un contador:
//
//     GET /api/locations/countries  ->  { "countries": [ ... ], "count": 5 }
//
// El ApiService declaraba Response<List<Country>>, o sea que Gson esperaba un
// arreglo "[" y se encontraba un objeto "{". Eso lanza JsonSyntaxException
// (Expected BEGIN_ARRAY but was BEGIN_OBJECT), la excepción cae en el catch del
// ViewModel y el desplegable queda vacío sin explicar por qué.
//
// Estas tres clases representan esa envoltura. El resto del proyecto ya lo hacía
// así para usuarios, categorías y productos (UsersResponse, CategoriesResponse,
// ProductsResponse); ubicaciones y roles eran la excepción.

data class CountriesResponse(
    @SerializedName("countries")
    val countries: List<Country> = emptyList(),

    @SerializedName("count")
    val count: Int = 0
)

data class StatesResponse(
    @SerializedName("states")
    val states: List<State> = emptyList(),

    @SerializedName("count")
    val count: Int = 0
)

data class CitiesResponse(
    @SerializedName("cities")
    val cities: List<City> = emptyList(),

    @SerializedName("count")
    val count: Int = 0
)
