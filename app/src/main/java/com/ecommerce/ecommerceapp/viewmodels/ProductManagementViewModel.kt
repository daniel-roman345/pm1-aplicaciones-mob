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

class ProductManagementViewModel(private val sessionManager: SessionManager) : ViewModel() {

    // Lista de productos y categorías
    var products by mutableStateOf<List<Product>>(emptyList())
    var categories by mutableStateOf<List<Category>>(emptyList())
    var selectedProduct by mutableStateOf<Product?>(null)

    // Paginación y filtros
    var currentPage by mutableStateOf(1)
    var totalPages by mutableStateOf(1)
    var searchQuery by mutableStateOf("")
    var selectedCategoryFilter by mutableStateOf<Category?>(null)
    var minPrice by mutableStateOf("")
    var maxPrice by mutableStateOf("")
    var showOnlyInStock by mutableStateOf(false)

    // Estados de carga
    var isLoadingProducts by mutableStateOf(false)
    var isLoadingCategories by mutableStateOf(false)
    var isCreatingProduct by mutableStateOf(false)
    var isUpdatingProduct by mutableStateOf(false)
    var isDeletingProduct by mutableStateOf(false)
    var isAddingImage by mutableStateOf(false)

    // Formularios
    var showCreateDialog by mutableStateOf(false)
    var showEditDialog by mutableStateOf(false)
    var showImageDialog by mutableStateOf(false)

    // Crear/Editar producto
    var productName by mutableStateOf("")
    var productPrice by mutableStateOf("")
    var productStock by mutableStateOf("")
    var selectedCategories by mutableStateOf<Set<Int>>(emptySet())

    // Agregar imagen
    var imagePath by mutableStateOf("")
    var imageAltText by mutableStateOf("")
    var isMainImage by mutableStateOf(false)
    var selectedImageCategory by mutableStateOf<Category?>(null)

    // Estados de error y éxito
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    init {
        loadProducts()
        loadCategories()
    }

    fun loadProducts(
        page: Int = 1,
        search: String = "",
        categoryId: Int? = null,
        minPriceValue: Double? = null,
        maxPriceValue: Double? = null,
        inStock: Boolean? = null
    ) {
        isLoadingProducts = true
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.getProducts(
                    page = page,
                    perPage = 10,
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
                    Log.d("PRODUCT_DEBUG", "Productos cargados: ${products.size}")
                } else {
                    errorMessage = "Error al cargar productos"
                    Log.e("PRODUCT_DEBUG", "Error response: ${response.code()}")
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
                Log.e("PRODUCT_DEBUG", "Exception loading products", e)
            } finally {
                isLoadingProducts = false
            }
        }
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

    fun searchProducts() {
        val minPriceValue = if (minPrice.isNotBlank()) minPrice.toDoubleOrNull() else null
        val maxPriceValue = if (maxPrice.isNotBlank()) maxPrice.toDoubleOrNull() else null
        val inStock = if (showOnlyInStock) true else null

        loadProducts(
            page = 1,
            search = searchQuery,
            categoryId = selectedCategoryFilter?.id_Category,
            minPriceValue = minPriceValue,
            maxPriceValue = maxPriceValue,
            inStock = inStock
        )
    }

    fun loadPage(page: Int) {
        val minPriceValue = if (minPrice.isNotBlank()) minPrice.toDoubleOrNull() else null
        val maxPriceValue = if (maxPrice.isNotBlank()) maxPrice.toDoubleOrNull() else null
        val inStock = if (showOnlyInStock) true else null

        loadProducts(
            page = page,
            search = searchQuery,
            categoryId = selectedCategoryFilter?.id_Category,
            minPriceValue = minPriceValue,
            maxPriceValue = maxPriceValue,
            inStock = inStock
        )
    }

    fun showCreateProductDialog() {
        clearProductForm()
        showCreateDialog = true
    }

    fun showEditProductDialog(product: Product) {
        selectedProduct = product
        productName = product.ProductName
        productPrice = product.Price.toString()
        productStock = product.Stock.toString()
        selectedCategories = product.categories.map { it.id_Category }.toSet()
        showEditDialog = true
    }

    fun createProduct() {
        if (!validateProductForm()) return

        isCreatingProduct = true
        viewModelScope.launch {
            try {
                val token = sessionManager.token.first()
                if (token != null) {
                    val request = CreateProductRequest(
                        ProductName = productName.trim(),
                        Price = productPrice.toDouble(),
                        Stock = productStock.toInt(),
                        categories = selectedCategories.toList()
                    )

                    val response = ApiClient.apiService.createProduct("Bearer $token", request)

                    if (response.isSuccessful && response.body() != null) {
                        successMessage = "Producto creado exitosamente"
                        showCreateDialog = false
                        clearProductForm()
                        loadProducts(currentPage, searchQuery) // Recargar lista actual
                        Log.d("PRODUCT_DEBUG", "Producto creado: ${response.body()}")
                    } else {
                        errorMessage = when (response.code()) {
                            409 -> "Ya existe un producto con ese nombre"
                            400 -> "Datos inválidos"
                            else -> "Error al crear el producto"
                        }
                        Log.e("PRODUCT_DEBUG", "Error creating product: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
                Log.e("PRODUCT_DEBUG", "Exception creating product", e)
            } finally {
                isCreatingProduct = false
            }
        }
    }

    fun updateProduct() {
        val product = selectedProduct ?: return
        if (!validateProductForm()) return

        isUpdatingProduct = true
        viewModelScope.launch {
            try {
                val token = sessionManager.token.first()
                if (token != null) {
                    val request = UpdateProductRequest(
                        ProductName = productName.trim(),
                        Price = productPrice.toDouble(),
                        Stock = productStock.toInt(),
                        categories = selectedCategories.toList()
                    )

                    val response = ApiClient.apiService.updateProduct(
                        token = "Bearer $token",
                        id = product.id_Product,
                        product = request
                    )

                    if (response.isSuccessful && response.body() != null) {
                        successMessage = "Producto actualizado exitosamente"
                        showEditDialog = false
                        selectedProduct = null
                        clearProductForm()
                        loadProducts(currentPage, searchQuery) // Recargar lista actual
                        Log.d("PRODUCT_DEBUG", "Producto actualizado: ${response.body()}")
                    } else {
                        errorMessage = when (response.code()) {
                            409 -> "Ya existe un producto con ese nombre"
                            404 -> "Producto no encontrado"
                            400 -> "Datos inválidos"
                            else -> "Error al actualizar el producto"
                        }
                        Log.e("PRODUCT_DEBUG", "Error updating product: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
                Log.e("PRODUCT_DEBUG", "Exception updating product", e)
            } finally {
                isUpdatingProduct = false
            }
        }
    }

    fun deleteProduct(product: Product) {
        isDeletingProduct = true
        viewModelScope.launch {
            try {
                val token = sessionManager.token.first()
                if (token != null) {
                    val response = ApiClient.apiService.deleteProduct(
                        token = "Bearer $token",
                        id = product.id_Product
                    )

                    if (response.isSuccessful) {
                        successMessage = "Producto eliminado exitosamente"
                        loadProducts(currentPage, searchQuery) // Recargar lista actual
                        Log.d("PRODUCT_DEBUG", "Producto eliminado: ${product.id_Product}")
                    } else {
                        errorMessage = when (response.code()) {
                            404 -> "Producto no encontrado"
                            409 -> "No se puede eliminar: el producto tiene ventas asociadas"
                            else -> "Error al eliminar el producto"
                        }
                        Log.e("PRODUCT_DEBUG", "Error deleting product: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
                Log.e("PRODUCT_DEBUG", "Exception deleting product", e)
            } finally {
                isDeletingProduct = false
            }
        }
    }

    fun showAddImageDialog(product: Product) {
        selectedProduct = product
        clearImageForm()
        showImageDialog = true
    }

    fun addProductImage() {
        val product = selectedProduct ?: return

        if (imagePath.isBlank()) {
            errorMessage = "La ruta de la imagen es requerida"
            return
        }

        isAddingImage = true
        viewModelScope.launch {
            try {
                val token = sessionManager.token.first()
                if (token != null) {
                    val request = AddImageRequest(
                        pathimage = imagePath.trim(),
                        alt_text = imageAltText.trim().ifBlank { null },
                        is_main_image = isMainImage,
                        id_Category = selectedImageCategory?.id_Category
                    )

                    val response = ApiClient.apiService.addProductImage(
                        token = "Bearer $token",
                        productId = product.id_Product,
                        image = request
                    )

                    if (response.isSuccessful && response.body() != null) {
                        successMessage = "Imagen agregada exitosamente"
                        showImageDialog = false
                        clearImageForm()
                        loadProducts(currentPage, searchQuery) // Recargar para mostrar nuevas imágenes
                        Log.d("PRODUCT_DEBUG", "Imagen agregada: ${response.body()}")
                    } else {
                        errorMessage = "Error al agregar la imagen"
                        Log.e("PRODUCT_DEBUG", "Error adding image: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
                Log.e("PRODUCT_DEBUG", "Exception adding image", e)
            } finally {
                isAddingImage = false
            }
        }
    }

    fun deleteProductImage(product: Product, image: ProductImage) {
        viewModelScope.launch {
            try {
                val token = sessionManager.token.first()
                if (token != null) {
                    val response = ApiClient.apiService.deleteProductImage(
                        token = "Bearer $token",
                        productId = product.id_Product,
                        imageId = image.id_image
                    )

                    if (response.isSuccessful) {
                        successMessage = "Imagen eliminada exitosamente"
                        loadProducts(currentPage, searchQuery) // Recargar lista
                        Log.d("PRODUCT_DEBUG", "Imagen eliminada: ${image.id_image}")
                    } else {
                        errorMessage = "Error al eliminar la imagen"
                        Log.e("PRODUCT_DEBUG", "Error deleting image: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
                Log.e("PRODUCT_DEBUG", "Exception deleting image", e)
            }
        }
    }

    private fun validateProductForm(): Boolean {
        if (productName.isBlank()) {
            errorMessage = "El nombre del producto es requerido"
            return false
        }

        if (productPrice.isBlank()) {
            errorMessage = "El precio es requerido"
            return false
        }

        val price = productPrice.toDoubleOrNull()
        if (price == null || price <= 0) {
            errorMessage = "El precio debe ser un número mayor a 0"
            return false
        }

        if (productStock.isBlank()) {
            errorMessage = "El stock es requerido"
            return false
        }

        val stock = productStock.toIntOrNull()
        if (stock == null || stock < 0) {
            errorMessage = "El stock debe ser un número mayor o igual a 0"
            return false
        }

        if (selectedCategories.isEmpty()) {
            errorMessage = "Debe seleccionar al menos una categoría"
            return false
        }

        return true
    }

    private fun clearProductForm() {
        productName = ""
        productPrice = ""
        productStock = ""
        selectedCategories = emptySet()
    }

    private fun clearImageForm() {
        imagePath = ""
        imageAltText = ""
        isMainImage = false
        selectedImageCategory = null
    }

    // Funciones de actualización para la UI
    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    fun updateCategoryFilter(category: Category?) {
        selectedCategoryFilter = category
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

    fun updateProductName(name: String) {
        productName = name
        clearMessages()
    }

    fun updateProductPrice(price: String) {
        productPrice = price
        clearMessages()
    }

    fun updateProductStock(stock: String) {
        productStock = stock
        clearMessages()
    }

    fun toggleCategorySelection(categoryId: Int) {
        selectedCategories = if (selectedCategories.contains(categoryId)) {
            selectedCategories - categoryId
        } else {
            selectedCategories + categoryId
        }
        clearMessages()
    }

    fun updateImagePath(path: String) {
        imagePath = path
        clearMessages()
    }

    fun updateImageAltText(text: String) {
        imageAltText = text
        clearMessages()
    }

    fun updateIsMainImage(isMain: Boolean) {
        isMainImage = isMain
        clearMessages()
    }

    fun updateSelectedImageCategory(category: Category?) {
        selectedImageCategory = category
        clearMessages()
    }

    fun cancelCreateProduct() {
        showCreateDialog = false
        clearProductForm()
        clearMessages()
    }

    fun cancelEditProduct() {
        showEditDialog = false
        selectedProduct = null
        clearProductForm()
        clearMessages()
    }

    fun cancelAddImage() {
        showImageDialog = false
        selectedProduct = null
        clearImageForm()
        clearMessages()
    }

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }
}