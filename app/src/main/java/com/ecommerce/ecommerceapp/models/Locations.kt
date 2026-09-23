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