package com.ecommerce.ecommerceapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.gson.annotations.SerializedName

@Parcelize
data class Product(
    @SerializedName("id_Product")
    val id_Product: Int = 0,

    @SerializedName("ProductName")
    val ProductName: String = "",

    @SerializedName("Price")
    val Price: Double = 0.0,

    @SerializedName("Stock")
    val Stock: Int = 0,

    @SerializedName("categories")
    val categories: List<Category> = emptyList(),

    @SerializedName("images")
    val images: List<ProductImage> = emptyList()
) : Parcelable

@Parcelize
data class ProductImage(
    @SerializedName("id_image")
    val id_image: Int = 0,

    @SerializedName("pathimage")
    val pathimage: String = "",

    @SerializedName("alt_text")
    val alt_text: String? = null,

    @SerializedName("is_main_image")
    val is_main_image: Boolean = false,

    @SerializedName("id_Category")
    val id_Category: Int? = null
) : Parcelable

data class CreateProductRequest(
    @SerializedName("ProductName")
    val ProductName: String,

    @SerializedName("Price")
    val Price: Double,

    @SerializedName("Stock")
    val Stock: Int,

    @SerializedName("categories")
    val categories: List<Int> = emptyList()
)

data class UpdateProductRequest(
    @SerializedName("ProductName")
    val ProductName: String,

    @SerializedName("Price")
    val Price: Double,

    @SerializedName("Stock")
    val Stock: Int,

    @SerializedName("categories")
    val categories: List<Int> = emptyList()
)

data class UpdateStockRequest(
    @SerializedName("Stock")
    val Stock: Int
)

data class AddImageRequest(
    @SerializedName("pathimage")
    val pathimage: String,

    @SerializedName("alt_text")
    val alt_text: String?,

    @SerializedName("is_main_image")
    val is_main_image: Boolean = false,

    @SerializedName("id_Category")
    val id_Category: Int?
)

data class ProductsResponse(
    @SerializedName("products")
    val products: List<Product> = emptyList(),

    @SerializedName("pagination")
    val pagination: Pagination? = null,

    @SerializedName("total")
    val total: Int = 0
)