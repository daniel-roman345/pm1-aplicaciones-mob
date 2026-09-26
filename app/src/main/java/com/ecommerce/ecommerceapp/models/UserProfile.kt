package com.ecommerce.ecommerceapp.models

import com.google.gson.annotations.SerializedName

data class UserProfile(
    @SerializedName("iD_User")
    val iD_User: Int,

    @SerializedName("UserName")
    val UserName: String,

    @SerializedName("Email")
    val Email: String,

    @SerializedName("iD_City")
    val iD_City: Int,

    @SerializedName("city")
    val city: City? = null,

    @SerializedName("roles")
    val roles: List<Role>? = null
)

data class UpdateProfileRequest(
    @SerializedName("UserName")
    val UserName: String,

    @SerializedName("iD_City")
    val iD_City: Int,

    // CORRECCIÓN PM1 · Reto 2: el backend acepta también el correo y devuelve
    // 409 si ya lo tiene otro usuario. Antes nunca se enviaba, así que el campo
    // era imposible de editar desde la app.
    @SerializedName("Email")
    val Email: String
)

data class ChangePasswordRequest(
    @SerializedName("current_password")
    val current_password: String,

    @SerializedName("new_password")
    val new_password: String
)
// ============================================================================
//  CLASES ENVOLTORIO DEL PERFIL  (corrección PM1 · Reto 2)
// ============================================================================
// Mismo patrón que en el Reto 1. El backend nunca devuelve el objeto pelado:
//
//     GET /api/users/profile  ->  { "user": { ... } }
//     PUT /api/users/profile  ->  { "message": "...", "user": { ... } }
//
// El ApiService declaraba Response<UserProfile>, así que Gson intentaba leer
// iD_User, UserName y Email en el nivel de afuera. Ahí no están: están una capa
// más adentro, dentro de "user". Como todos los campos quedaban en null y el
// data class los pide no nulos, la deserialización fallaba o entregaba un
// objeto inservible, y la pantalla de perfil se quedaba vacía.
//
// Esta misma clase sirve para las tres rutas porque las tres responden igual:
// GET /api/users/profile, PUT /api/users/profile y PUT /api/users/{id}/roles.

data class UserProfileResponse(
    @SerializedName("user")
    val user: UserProfile? = null,

    @SerializedName("message")
    val message: String? = null
)
