package com.ecommerce.ecommerceapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.gson.annotations.SerializedName

@Parcelize
data class User(
    @SerializedName("iD_User")
    val iD_User: Int = 0,

    @SerializedName("UserName")
    val UserName: String = "",

    @SerializedName("Email")
    val Email: String = "",

    @SerializedName("PasswoRDkey")
    val PasswoRDkey: String = "",

    @SerializedName("iD_City")
    val iD_City: Int = 1,

    @SerializedName("city")
    val city: City? = null,

    @SerializedName("roles")
    val roles: List<Role>? = null
) : Parcelable

@Parcelize
data class City(
    @SerializedName("iD_City")
    val iD_City: Int,

    @SerializedName("CityName")
    val CityName: String,

    @SerializedName("iD_States")
    val iD_States: Int,

    @SerializedName("state")
    val state: String,

    @SerializedName("country")
    val country: String
) : Parcelable

@Parcelize
data class Role(
    @SerializedName("iDRole")
    val iDRole: Int,

    @SerializedName("TypeRole")
    val TypeRole: String
) : Parcelable