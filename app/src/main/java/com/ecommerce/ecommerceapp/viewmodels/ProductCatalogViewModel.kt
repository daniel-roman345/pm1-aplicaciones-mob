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

class ProductCatalogViewModel : ViewModel() {

    // Lista de productos y categorías
    var products by mutableStateOf<List<Product>>(emptyList())
    var categories by mutableStateOf<List<Category>>(emptyList())
    var featuredProducts by mutableStateOf<List<Product>>(emptyList())

    // Paginación y filtros
    var currentPage by mutableStateOf(1)
    var totalPages by mutableStateOf(1)
    var totalProducts by mutableStateOf(0)

    // Filtros
    var searchQuery by mutableStateOf("")
    var selectedCategoryFilter by mutableStateOf<Category?>(null)
    var minPrice by mutableStateOf("")
    var maxPrice by mutableStateOf("")
    var showOnlyInStock by mutableStateOf(true)
    var sortBy by mutableStateOf("name") // name, price_asc, price_desc

    // Estados de carga
    var isLoadingProducts by mutableStateOf(false)
    var isLoadingCategories by mutableStateOf(false)
    var isLoadingFeatured by mutableStateOf(false)

    // Estados de error
    var errorMessage by mutableStateOf<String?>(null)

    // Vista actual (grid o lista)
    var isGridView by mutableStateOf(true)

    init {
        loadCategories()
        loadFeaturedProducts()
        loadProducts()
    }

    fun loadProducts(
        page: Int = 1,
        search: String = searchQuery,
        categoryId: Int? = selectedCategoryFilter?.id_Category,
        minPriceValue: Double? = null,
        maxPriceValue: Double? = null,
        inStock: Boolean? = if (showOnlyInStock) true else null
    ) {
        isLoadingProducts = true
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getProducts(
                    page = page,
                    perPage = 12, // Más productos para el catálogo
                    search = search.ifBlank { null },
                    categoryId = categoryId,
                    minPrice = minPriceValue,
                    maxPrice = maxPriceValue,
                    inStock = inStock
                )

                if (response.isSuccessful && response.body() != null) {
                    val productsResponse = response.body()!!
                    products = productsResponse.products
                    currentPage = page
                    totalPages = productsResponse.pagination?.total_pages ?: 1
                    totalProducts = productsResponse.total
                    Log.d("CATALOG_DEBUG", "Productos cargados: ${products.size}")
                } else {
                    errorMessage = "Error al cargar productos"
                    Log.e("CATALOG_DEBUG", "Error response: ${response.code()}")
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
                Log.e("CATALOG_DEBUG", "Exception loading products", e)
            } finally {
                isLoadingProducts = false
            }
        }
    }

    private fun loadCategories() {
        isLoadingCategories = true
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getCategories(includeProducts = false)
                if (response.isSuccessful && response.body() != null) {
                     val categoriesResponse = response.body()!!
                    categories = categoriesResponse.categories
                    Log.d("CATALOG_DEBUG", "Categorías cargadas: ${categories.size}")
                } else {
                    Log.e("CATALOG_DEBUG", "Error loading categories: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("CATALOG_DEBUG", "Exception loading categories", e)
            } finally {
                isLoadingCategories = false
            }
        }
    }

    private fun loadFeaturedProducts() {
        isLoadingFeatured = true
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getFeaturedProducts()
                if (response.isSuccessful && response.body() != null) {
                    val productsResponse = response.body()!!
                    featuredProducts = productsResponse.products.take(6) // Solo los primeros 6
                    Log.d("CATALOG_DEBUG", "Productos destacados: ${featuredProducts.size}")
                } else {
                    Log.e("CATALOG_DEBUG", "Error loading featured products: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("CATALOG_DEBUG", "Exception loading featured products", e)
            } finally {
                isLoadingFeatured = false
            }
        }
    }

    fun searchProducts() {
        val minPriceValue = if (minPrice.isNotBlank()) minPrice.toDoubleOrNull() else null
        val maxPriceValue = if (maxPrice.isNotBlank()) maxPrice.toDoubleOrNull() else null

        loadProducts(
            page = 1,
            search = searchQuery,
            categoryId = selectedCategoryFilter?.id_Category,
            minPriceValue = minPriceValue,
            maxPriceValue = maxPriceValue,
            inStock = if (showOnlyInStock) true else null
        )
    }

    fun loadPage(page: Int) {
        val minPriceValue = if (minPrice.isNotBlank()) minPrice.toDoubleOrNull() else null
        val maxPriceValue = if (maxPrice.isNotBlank()) maxPrice.toDoubleOrNull() else null

        loadProducts(
            page = page,
            search = searchQuery,
            categoryId = selectedCategoryFilter?.id_Category,
            minPriceValue = minPriceValue,
            maxPriceValue = maxPriceValue,
            inStock = if (showOnlyInStock) true else null
        )
    }

    fun clearFilters() {
        searchQuery = ""
        selectedCategoryFilter = null
        minPrice = ""
        maxPrice = ""
        showOnlyInStock = true
        loadProducts(page = 1)
    }

    fun quickSearch(query: String) {
        searchQuery = query
        loadProducts(page = 1, search = query)
    }

    // Funciones de actualización para la UI
    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    fun updateCategoryFilter(category: Category?) {
        selectedCategoryFilter = category
        searchProducts()
    }

    fun updateMinPrice(price: String) {
        minPrice = price
    }

    fun updateMaxPrice(price: String) {
        maxPrice = price
    }

    fun updateShowOnlyInStock(inStock: Boolean) {
        showOnlyInStock = inStock
    }

    fun toggleViewMode() {
        isGridView = !isGridView
    }

    fun clearError() {
        errorMessage = null
    }

    fun refreshProducts() {
        loadProducts(currentPage)
        loadFeaturedProducts()
    }
}