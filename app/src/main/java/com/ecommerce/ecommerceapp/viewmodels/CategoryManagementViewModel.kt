package com.ecommerce.ecommerceapp.viewmodels

import android.util.Log
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

class CategoryManagementViewModel(private val sessionManager: SessionManager) : ViewModel() {

    // Lista de categorías
    var categories by mutableStateOf<List<Category>>(emptyList())
    var selectedCategory by mutableStateOf<Category?>(null)

    // Estados de carga
    var isLoadingCategories by mutableStateOf(false)
    var isCreatingCategory by mutableStateOf(false)
    var isUpdatingCategory by mutableStateOf(false)
    var isDeletingCategory by mutableStateOf(false)

    // Formularios
    var newCategoryName by mutableStateOf("")
    var editCategoryName by mutableStateOf("")
    var showEditDialog by mutableStateOf(false)

    // Estados de error y éxito
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    init {
        loadCategories()
    }

    fun loadCategories() {
        isLoadingCategories = true
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getCategories(includeProducts = false)
                if (response.isSuccessful && response.body() != null) {
                    val categoriesResponse = response.body()!!
                    categories = categoriesResponse.categories
                    Log.d("CATEGORY_DEBUG", "Categorías cargadas: ${categories.size}")
                } else {
                    errorMessage = "Error al cargar categorías"
                    Log.e("CATEGORY_DEBUG", "Error response: ${response.code()}")
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
                Log.e("CATEGORY_DEBUG", "Exception loading categories", e)
            } finally {
                isLoadingCategories = false
            }
        }
    }

    fun createCategory() {
        if (newCategoryName.isBlank()) {
            errorMessage = "El nombre de la categoría es requerido"
            return
        }

        isCreatingCategory = true
        viewModelScope.launch {
            try {
                val token = sessionManager.token.first()
                if (token != null) {
                    val request = CreateCategoryRequest(CategoryName = newCategoryName.trim())
                    val response = ApiClient.apiService.createCategory("Bearer $token", request)

                    if (response.isSuccessful && response.body() != null) {
                        successMessage = "Categoría creada exitosamente"
                        newCategoryName = ""
                        loadCategories() // Recargar lista
                        Log.d("CATEGORY_DEBUG", "Categoría creada: ${response.body()}")
                    } else {
                        errorMessage = when (response.code()) {
                            409 -> "Ya existe una categoría con ese nombre"
                            else -> "Error al crear la categoría"
                        }
                        Log.e("CATEGORY_DEBUG", "Error creating category: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
                Log.e("CATEGORY_DEBUG", "Exception creating category", e)
            } finally {
                isCreatingCategory = false
            }
        }
    }

    fun selectCategoryForEdit(category: Category) {
        selectedCategory = category
        editCategoryName = category.CategoryName
        showEditDialog = true
    }

    fun updateCategory() {
        val category = selectedCategory ?: return

        if (editCategoryName.isBlank()) {
            errorMessage = "El nombre de la categoría es requerido"
            return
        }

        isUpdatingCategory = true
        viewModelScope.launch {
            try {
                val token = sessionManager.token.first()
                if (token != null) {
                    val request = UpdateCategoryRequest(CategoryName = editCategoryName.trim())
                    val response = ApiClient.apiService.updateCategory(
                        token = "Bearer $token",
                        id = category.id_Category,
                        category = request
                    )

                    if (response.isSuccessful && response.body() != null) {
                        successMessage = "Categoría actualizada exitosamente"
                        showEditDialog = false
                        selectedCategory = null
                        editCategoryName = ""
                        loadCategories() // Recargar lista
                        Log.d("CATEGORY_DEBUG", "Categoría actualizada: ${response.body()}")
                    } else {
                        errorMessage = when (response.code()) {
                            409 -> "Ya existe una categoría con ese nombre"
                            404 -> "Categoría no encontrada"
                            else -> "Error al actualizar la categoría"
                        }
                        Log.e("CATEGORY_DEBUG", "Error updating category: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
                Log.e("CATEGORY_DEBUG", "Exception updating category", e)
            } finally {
                isUpdatingCategory = false
            }
        }
    }

    fun deleteCategory(category: Category) {
        isDeletingCategory = true
        viewModelScope.launch {
            try {
                val token = sessionManager.token.first()
                if (token != null) {
                    val response = ApiClient.apiService.deleteCategory(
                        token = "Bearer $token",
                        id = category.id_Category
                    )

                    if (response.isSuccessful) {
                        successMessage = "Categoría eliminada exitosamente"
                        loadCategories() // Recargar lista
                        Log.d("CATEGORY_DEBUG", "Categoría eliminada: ${category.id_Category}")
                    } else {
                        errorMessage = when (response.code()) {
                            404 -> "Categoría no encontrada"
                            409 -> "No se puede eliminar: la categoría tiene productos asociados"
                            else -> "Error al eliminar la categoría"
                        }
                        Log.e("CATEGORY_DEBUG", "Error deleting category: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
                Log.e("CATEGORY_DEBUG", "Exception deleting category", e)
            } finally {
                isDeletingCategory = false
            }
        }
    }

    // Funciones de actualización
    fun updateNewCategoryName(name: String) {
        newCategoryName = name
        clearMessages()
    }

    fun updateEditCategoryName(name: String) {
        editCategoryName = name
        clearMessages()
    }

    fun cancelEdit() {
        showEditDialog = false
        selectedCategory = null
        editCategoryName = ""
        clearMessages()
    }

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }
}