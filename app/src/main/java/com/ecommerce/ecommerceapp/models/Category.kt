package com.ecommerce.ecommerceapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.gson.annotations.SerializedName

@Parcelize
data class Category(
    @SerializedName("id_Category")
    val id_Category: Int = 0,

    @SerializedName("CategoryName")
    val CategoryName: String = ""
) : Parcelable

data class CreateCategoryRequest(
    @SerializedName("CategoryName")
    val CategoryName: String
)

data class UpdateCategoryRequest(
    @SerializedName("CategoryName")
    val CategoryName: String
)

data class CategoriesResponse(
    @SerializedName("categories")
    val categories: List<Category> = emptyList(),

    @SerializedName("message")
    val message: String? = null
)