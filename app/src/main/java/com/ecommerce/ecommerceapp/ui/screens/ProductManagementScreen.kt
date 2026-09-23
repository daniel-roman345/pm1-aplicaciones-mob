package com.ecommerce.ecommerceapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ecommerce.ecommerceapp.models.Category
import com.ecommerce.ecommerceapp.models.Product
import com.ecommerce.ecommerceapp.models.ProductImage
import com.ecommerce.ecommerceapp.ui.components.LoadingButton
import com.ecommerce.ecommerceapp.utils.SessionManager
import com.ecommerce.ecommerceapp.viewmodels.ProductManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductManagementScreen(
    sessionManager: SessionManager,
    onNavigateBack: () -> Unit,
    viewModel: ProductManagementViewModel = viewModel { ProductManagementViewModel(sessionManager) }
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Manejar mensajes
    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(viewModel.successMessage) {
        viewModel.successMessage?.let { success ->
            snackbarHostState.showSnackbar(success)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Productos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showCreateProductDialog() }) {
                        Icon(Icons.Default.Add, contentDescription = "Crear Producto")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Filtros
            FiltersSection(viewModel)

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de productos
            if (viewModel.isLoadingProducts) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (viewModel.products.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No se encontraron productos",
                        modifier = Modifier.padding(32.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.products) { product ->
                        ProductCard(
                            product = product,
                            isDeleting = viewModel.isDeletingProduct,
                            onEdit = { viewModel.showEditProductDialog(product) },
                            onDelete = { viewModel.deleteProduct(product) },
                            onAddImage = { viewModel.showAddImageDialog(product) },
                            onDeleteImage = { image -> viewModel.deleteProductImage(product, image) }
                        )
                    }

                    // Paginación
                    if (viewModel.totalPages > 1) {
                        item {
                            PaginationSection(viewModel)
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (viewModel.showCreateDialog) {
        ProductDialog(
            title = "Crear Producto",
            viewModel = viewModel,
            onConfirm = viewModel::createProduct,
            onDismiss = viewModel::cancelCreateProduct,
            isLoading = viewModel.isCreatingProduct
        )
    }

    if (viewModel.showEditDialog) {
        ProductDialog(
            title = "Editar Producto",
            viewModel = viewModel,
            onConfirm = viewModel::updateProduct,
            onDismiss = viewModel::cancelEditProduct,
            isLoading = viewModel.isUpdatingProduct
        )
    }

    if (viewModel.showImageDialog) {
        ImageDialog(
            viewModel = viewModel,
            onConfirm = viewModel::addProductImage,
            onDismiss = viewModel::cancelAddImage,
            isLoading = viewModel.isAddingImage
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersSection(viewModel: ProductManagementViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Filtros",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Búsqueda
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                label = { Text("Buscar productos") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Filtro de categoría
                var expandedCategories by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedCategories,
                    onExpandedChange = { expandedCategories = !expandedCategories },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = viewModel.selectedCategoryFilter?.CategoryName ?: "Todas las categorías",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategories) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedCategories,
                        onDismissRequest = { expandedCategories = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todas las categorías") },
                            onClick = {
                                viewModel.updateCategoryFilter(null)
                                expandedCategories = false
                            }
                        )
                        viewModel.categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.CategoryName) },
                                onClick = {
                                    viewModel.updateCategoryFilter(category)
                                    expandedCategories = false
                                }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Precio mínimo
                OutlinedTextField(
                    value = viewModel.minPrice,
                    onValueChange = viewModel::updateMinPrice,
                    label = { Text("Precio min.") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                // Precio máximo
                OutlinedTextField(
                    value = viewModel.maxPrice,
                    onValueChange = viewModel::updateMaxPrice,
                    label = { Text("Precio max.") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Solo en stock
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = viewModel.showOnlyInStock,
                    onCheckedChange = viewModel::updateShowOnlyInStock
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Solo productos en stock")
            }

            // Botón buscar
            Button(
                onClick = viewModel::searchProducts,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Buscar")
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    isDeleting: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddImage: () -> Unit,
    onDeleteImage: (ProductImage) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header del producto
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.ProductName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Precio: $${product.Price}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Stock: ${product.Stock}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (product.Stock > 0) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                    )
                }

                // Acciones
                Row {
                    IconButton(onClick = onAddImage) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Agregar imagen",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        enabled = !isDeleting
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Categorías
            if (product.categories.isNotEmpty()) {
                Text(
                    text = "Categorías:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(product.categories) { category ->
                        AssistChip(
                            onClick = { },
                            label = { Text(category.CategoryName) },
                            modifier = Modifier.height(32.dp)
                        )
                    }
                }
            }

            // Imágenes
            if (product.images.isNotEmpty()) {
                Text(
                    text = "Imágenes (${product.images.size}):",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(product.images) { image ->
                        ImageChip(
                            image = image,
                            onDelete = { onDeleteImage(image) }
                        )
                    }
                }
            } else {
                Text(
                    text = "Sin imágenes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Dialog de confirmación para eliminar
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Producto") },
            text = {
                Text("¿Estás seguro de que deseas eliminar el producto \"${product.ProductName}\"?\n\nEsta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ImageChip(
    image: ProductImage,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    InputChip(
        onClick = { showDeleteDialog = true },
        label = {
            Text(
                text = if (image.is_main_image) "★ Principal" else "Imagen",
                style = MaterialTheme.typography.bodySmall
            )
        },
        selected = false,
        trailingIcon = {
            Icon(
                Icons.Default.Close,
                contentDescription = "Eliminar imagen",
                modifier = Modifier.size(16.dp)
            )
        }
    )

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Imagen") },
            text = { Text("¿Eliminar esta imagen del producto?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun PaginationSection(viewModel: ProductManagementViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { viewModel.loadPage(viewModel.currentPage - 1) },
                enabled = viewModel.currentPage > 1
            ) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null)
                Text("Anterior")
            }

            Text(
                text = "Página ${viewModel.currentPage} de ${viewModel.totalPages}",
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = { viewModel.loadPage(viewModel.currentPage + 1) },
                enabled = viewModel.currentPage < viewModel.totalPages
            ) {
                Text("Siguiente")
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDialog(
    title: String,
    viewModel: ProductManagementViewModel,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Nombre del producto
                OutlinedTextField(
                    value = viewModel.productName,
                    onValueChange = viewModel::updateProductName,
                    label = { Text("Nombre del Producto") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Precio
                    OutlinedTextField(
                        value = viewModel.productPrice,
                        onValueChange = viewModel::updateProductPrice,
                        label = { Text("Precio") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    // Stock
                    OutlinedTextField(
                        value = viewModel.productStock,
                        onValueChange = viewModel::updateProductStock,
                        label = { Text("Stock") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Categorías
                Text(
                    text = "Categorías:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )

                LazyColumn(
                    modifier = Modifier.height(120.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(viewModel.categories) { category ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = viewModel.selectedCategories.contains(category.id_Category),
                                onCheckedChange = {
                                    viewModel.toggleCategorySelection(category.id_Category)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(category.CategoryName)
                        }
                    }
                }
            }
        },
        confirmButton = {
            LoadingButton(
                text = if (title.contains("Crear")) "Crear" else "Actualizar",
                isLoading = isLoading,
                onClick = onConfirm,
                modifier = Modifier.width(120.dp)
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageDialog(
    viewModel: ProductManagementViewModel,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Imagen") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = viewModel.imagePath,
                    onValueChange = viewModel::updateImagePath,
                    label = { Text("Ruta de la imagen") },
                    placeholder = { Text("/images/producto.jpg") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = viewModel.imageAltText,
                    onValueChange = viewModel::updateImageAltText,
                    label = { Text("Texto alternativo (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = viewModel.isMainImage,
                        onCheckedChange = viewModel::updateIsMainImage
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Imagen principal")
                }

                // Categoría de la imagen
                var expandedCategories by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedCategories,
                    onExpandedChange = { expandedCategories = !expandedCategories }
                ) {
                    OutlinedTextField(
                        value = viewModel.selectedImageCategory?.CategoryName ?: "Seleccionar categoría (opcional)",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategories) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedCategories,
                        onDismissRequest = { expandedCategories = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sin categoría") },
                            onClick = {
                                viewModel.updateSelectedImageCategory(null)
                                expandedCategories = false
                            }
                        )
                        viewModel.categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.CategoryName) },
                                onClick = {
                                    viewModel.updateSelectedImageCategory(category)
                                    expandedCategories = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            LoadingButton(
                text = "Agregar",
                isLoading = isLoading,
                onClick = onConfirm,
                enabled = viewModel.imagePath.isNotBlank(),
                modifier = Modifier.width(100.dp)
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}