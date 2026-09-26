package com.ecommerce.ecommerceapp.models

import com.google.gson.annotations.SerializedName

data class UsersResponse(
    @SerializedName("users")
    val users: List<UserProfile> = emptyList(),

    @SerializedName("pagination")
    val pagination: Pagination? = null,

    @SerializedName("total")
    val total: Int = 0
)

data class Pagination(
    @SerializedName("page")
    val page: Int,

    @SerializedName("per_page")
    val per_page: Int,

    @SerializedName("total_pages")
    val total_pages: Int
)

data class UpdateUserRoleRequest(
    @SerializedName("role_ids")
    val role_ids: List<Int>
)
// ============================================================================
//  CLASE ENVOLTORIO DE LOS ROLES  (corrección PM1 · Reto 2)
// ============================================================================
// GET /api/auth/roles  ->  { "roles": [ { "iDRole": 1, "TypeRole": "Administrador" }, ... ] }
//
// El ApiService pedía Response<List<Role>>, o sea un arreglo. Gson recibía un
// objeto y lanzaba JsonSyntaxException, igual que con los países del Reto 1.
// Sin roles cargados, el desplegable de la pantalla de gestión de usuarios
// quedaba vacío y no había forma de cambiarle el rol a nadie.

data class RolesResponse(
    @SerializedName("roles")
    val roles: List<Role> = emptyList()
)
