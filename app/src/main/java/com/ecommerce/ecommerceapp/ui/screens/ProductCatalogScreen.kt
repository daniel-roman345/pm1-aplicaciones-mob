package com.ecommerce.ecommerceapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ecommerce.ecommerceapp.models.Category
import com.ecommerce.ecommerceapp.models.Product
import com.ecommerce.ecommerceapp.viewmodels.ProductCatalogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCatalogScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCart: (() -> Unit)? = null,
    onProductClick: ((Product) -> Unit)? = null,
    cartViewModel: com.ecommerce.ecommerceapp.viewmodels.CartViewModel? = null,
    isUserLoggedIn: Boolean = false,
    viewModel: ProductCatalogViewModel = viewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showFilters by remember { mutableStateOf(false) }

    // Manejar errores del catálogo
    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    // Manejar errores del carrito
    LaunchedEffect(cartViewModel?.errorMessage) {
        cartViewModel?.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            cartViewModel.clearMessages()
        }
    }

    // Manejar mensajes de éxito del carrito
    LaunchedEffect(cartViewModel?.successMessage) {
        cartViewModel?.successMessage?.let { success ->
            snackbarHostState.showSnackbar(
                message = success,
                duration = SnackbarDuration.Short
            )
            cartViewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            CatalogTopBar(
                searchQuery = viewModel.searchQuery,
                onSearchChange = { viewModel.updateSearchQuery(it) },
                onSearchSubmit = { viewModel.searchProducts() },
                isGridView = viewModel.isGridView,
                onToggleView = { viewModel.toggleViewMode() },
                onShowFilters = { showFilters = true },
                onNavigateBack = onNavigateBack,
                cartItemCount = cartViewModel?.getCartItemCount() ?: 0,
                onNavigateToCart = onNavigateToCart,
                isUserLoggedIn = isUserLoggedIn,
                onDebugCart = {
                    // Debug temporal
                    cartViewModel?.let { vm ->
                        android.util.Log.d("CART_DEBUG", "=== DEBUG INFO ===")
                        android.util.Log.d("CART_DEBUG", vm.getDebugInfo())
                        android.util.Log.d("CART_DEBUG", "==================")
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
        ) {
            // Productos destacados (solo si no hay filtros activos)
            if (viewModel.searchQuery.isBlank() &&
                viewModel.selectedCategoryFilter == null &&
                viewModel.featuredProducts.isNotEmpty()) {

                FeaturedProductsSection(
                    products = viewModel.featuredProducts,
                    isLoading = viewModel.isLoadingFeatured,
                    onProductClick = onProductClick,
                    cartViewModel = cartViewModel,
                    isUserLoggedIn = isUserLoggedIn
                )
            }

            // Filtros rápidos de categorías
            if (!viewModel.isLoadingCategories && viewModel.categories.isNotEmpty()) {
                QuickCategoryFilters(
                    categories = viewModel.categories,
                    selectedCategory = viewModel.selectedCategoryFilter,
                    onCategorySelect = { viewModel.updateCategoryFilter(it) }
                )
            }

            // Información de resultados
            ResultsHeader(
                totalProducts = viewModel.totalProducts,
                currentPage = viewModel.currentPage,
                totalPages = viewModel.totalPages,
                isLoading = viewModel.isLoadingProducts
            )

            // Lista/Grid de productos
            if (viewModel.isLoadingProducts && viewModel.products.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Cargando productos...")
                    }
                }
            } else if (viewModel.products.isEmpty()) {
                EmptyProductsState(
                    searchQuery = viewModel.searchQuery,
                    hasFilters = viewModel.selectedCategoryFilter != null,
                    onClearFilters = { viewModel.clearFilters() }
                )
            } else {
                ProductsGrid(
                    products = viewModel.products,
                    isGridView = viewModel.isGridView,
                    currentPage = viewModel.currentPage,
                    totalPages = viewModel.totalPages,
                    isLoading = viewModel.isLoadingProducts,
                    onProductClick = onProductClick,
                    onLoadPage = { viewModel.loadPage(it) },
                    cartViewModel = cartViewModel,
                    isUserLoggedIn = isUserLoggedIn
                )
            }
        }
    }

    // Modal Bottom Sheet para filtros (fuera del Scaffold)
    if (showFilters) {
        FiltersBottomSheet(
            viewModel = viewModel,
            onDismiss = { showFilters = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogTopBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    isGridView: Boolean,
    onToggleView: () -> Unit,
    onShowFilters: () -> Unit,
    onNavigateBack: () -> Unit,
    cartItemCount: Int = 0,
    onNavigateToCart: (() -> Unit)? = null,
    isUserLoggedIn: Boolean = false,
    onDebugCart: (() -> Unit)? = null // Función temporal de debug
) {
    TopAppBar(
        title = { Text("Catálogo de Productos") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
        },
        actions = {
            // Botón del carrito (solo para usuarios logueados)
            if (isUserLoggedIn && onNavigateToCart != null) {
                BadgedBox(
                    badge = {
                        if (cartItemCount > 0) {
                            Badge {
                                Text(cartItemCount.toString())
                            }
                        }
                    }
                ) {
                    IconButton(onClick = onNavigateToCart) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = "Ver carrito ($cartItemCount items)"
                        )
                    }
                }
            }

            // Botón de debug temporal
            if (isUserLoggedIn && onDebugCart != null) {
                IconButton(onClick = onDebugCart) {
                    Icon(
                        Icons.Default.BugReport,
                        contentDescription = "Debug cart"
                    )
                }
            }

            IconButton(onClick = onToggleView) {
                Icon(
                    if (isGridView) Icons.Default.List else Icons.Default.GridView,
                    contentDescription = if (isGridView) "Vista Lista" else "Vista Grid"
                )
            }
            IconButton(onClick = onShowFilters) {
                Icon(Icons.Default.FilterList, contentDescription = "Filtros")
            }
        }
    )
}

@Composable
fun FeaturedProductsSection(
    products: List<Product>,
    isLoading: Boolean,
    onProductClick: ((Product) -> Unit)?,
    cartViewModel: com.ecommerce.ecommerceapp.viewmodels.CartViewModel? = null,
    isUserLoggedIn: Boolean = false
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⭐ Productos Destacados",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(products) { product ->
                    FeaturedProductCard(
                        product = product,
                        onClick = { onProductClick?.invoke(product) },
                        cartViewModel = cartViewModel,
                        isUserLoggedIn = isUserLoggedIn
                    )
                }
            }
        }
    }

    Divider(modifier = Modifier.padding(vertical = 8.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeaturedProductCard(
    product: Product,
    onClick: () -> Unit,
    cartViewModel: com.ecommerce.ecommerceapp.viewmodels.CartViewModel? = null,
    isUserLoggedIn: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(160.dp)
            .height(240.dp), // Aumentado para dar espacio al botón
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Placeholder para imagen
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.ProductName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "${product.Price}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Botón agregar al carrito (solo para usuarios logueados)
            if (isUserLoggedIn && cartViewModel != null) {
                Spacer(modifier = Modifier.height(8.dp))

                val isInCart = cartViewModel.hasProductInCart(product.id_Product)

                if (isInCart) {
                    // Mostrar estado en carrito
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "En carrito",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // Botón agregar al carrito
                    FilledTonalButton(
                        onClick = {
                            cartViewModel.addToCart(product.id_Product, 1)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = product.Stock > 0 && !cartViewModel.isAddingToCart,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        if (cartViewModel.isAddingToCart) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 1.5.dp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "...",
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Icon(
                                if (product.Stock > 0) Icons.Default.Add else Icons.Default.Remove,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (product.Stock > 0) "Agregar" else "Sin stock",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickCategoryFilters(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelect: (Category?) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Categorías",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            // Chip para "Todas"
            item {
                FilterChip(
                    onClick = { onCategorySelect(null) },
                    label = { Text("Todas") },
                    selected = selectedCategory == null,
                    leadingIcon = if (selectedCategory == null) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
            }

            items(categories) { category ->
                FilterChip(
                    onClick = { onCategorySelect(category) },
                    label = { Text(category.CategoryName) },
                    selected = selectedCategory?.id_Category == category.id_Category,
                    leadingIcon = if (selectedCategory?.id_Category == category.id_Category) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
            }
        }
    }
}

@Composable
fun ResultsHeader(
    totalProducts: Int,
    currentPage: Int,
    totalPages: Int,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isLoading) {
            Text(
                text = "Cargando...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = "$totalProducts productos encontrados",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (totalPages > 1) {
            Text(
                text = "Página $currentPage de $totalPages",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProductsGrid(
    products: List<Product>,
    isGridView: Boolean,
    currentPage: Int,
    totalPages: Int,
    isLoading: Boolean,
    onProductClick: ((Product) -> Unit)?,
    onLoadPage: (Int) -> Unit,
    cartViewModel: com.ecommerce.ecommerceapp.viewmodels.CartViewModel? = null,
    isUserLoggedIn: Boolean = false
) {
    LazyColumn {
        if (isGridView) {
            // Vista en grid
            val chunkedProducts = products.chunked(2)
            items(chunkedProducts) { productPair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    productPair.forEach { product ->
                        ProductGridCard(
                            product = product,
                            modifier = Modifier.weight(1f),
                            onClick = { onProductClick?.invoke(product) },
                            cartViewModel = cartViewModel,
                            isUserLoggedIn = isUserLoggedIn
                        )
                    }

                    // Relleno si es impar
                    if (productPair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        } else {
            // Vista en lista
            items(products) { product ->
                ProductListCard(
                    product = product,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    onClick = { onProductClick?.invoke(product) },
                    cartViewModel = cartViewModel,
                    isUserLoggedIn = isUserLoggedIn
                )
            }
        }

        // Paginación
        if (totalPages > 1) {
            item {
                PaginationControls(
                    currentPage = currentPage,
                    totalPages = totalPages,
                    isLoading = isLoading,
                    onLoadPage = onLoadPage
                )
            }
        }

        // Espaciado final
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductGridCard(
    product: Product,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    cartViewModel: com.ecommerce.ecommerceapp.viewmodels.CartViewModel? = null,
    isUserLoggedIn: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(260.dp), // Aumentado para dar espacio al botón
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Imagen placeholder
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.ProductName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(40.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${product.Price}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Stock: ${product.Stock}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (product.Stock > 0)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón agregar al carrito (solo para usuarios logueados)
            if (isUserLoggedIn && cartViewModel != null) {
                val isInCart = cartViewModel.hasProductInCart(product.id_Product)
                val quantityInCart = cartViewModel.getProductQuantityInCart(product.id_Product)

                if (isInCart) {
                    // Mostrar cantidad en carrito
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "En carrito ($quantityInCart)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // Botón agregar al carrito
                    Button(
                        onClick = {
                            cartViewModel.addToCart(product.id_Product, 1)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = product.Stock > 0 && !cartViewModel.isAddingToCart,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        if (cartViewModel.isAddingToCart) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Agregando...",
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Icon(
                                Icons.Default.AddShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (product.Stock > 0) "Agregar" else "Sin stock",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListCard(
    product: Product,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    cartViewModel: com.ecommerce.ecommerceapp.viewmodels.CartViewModel? = null,
    isUserLoggedIn: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen placeholder
            Card(
                modifier = Modifier.size(80.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.ProductName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (product.categories.isNotEmpty()) {
                    Text(
                        text = product.categories.joinToString(", ") { it.CategoryName },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${product.Price}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Stock: ${product.Stock}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (product.Stock > 0)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
                }
            }

            // Botón agregar al carrito (solo para usuarios logueados)
            if (isUserLoggedIn && cartViewModel != null) {
                Spacer(modifier = Modifier.width(8.dp))

                val isInCart = cartViewModel.hasProductInCart(product.id_Product)
                val quantityInCart = cartViewModel.getProductQuantityInCart(product.id_Product)

                if (isInCart) {
                    // Mostrar cantidad en carrito
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "En carrito",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "($quantityInCart)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // Botón agregar al carrito
                    FilledTonalIconButton(
                        onClick = {
                            cartViewModel.addToCart(product.id_Product, 1)
                        },
                        enabled = product.Stock > 0 && !cartViewModel.isAddingToCart
                    ) {
                        if (cartViewModel.isAddingToCart) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                if (product.Stock > 0) Icons.Default.AddShoppingCart else Icons.Default.RemoveShoppingCart,
                                contentDescription = if (product.Stock > 0) "Agregar al carrito" else "Sin stock"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyProductsState(
    searchQuery: String,
    hasFilters: Boolean,
    onClearFilters: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (searchQuery.isNotBlank()) {
                    "No se encontraron productos para \"$searchQuery\""
                } else if (hasFilters) {
                    "No se encontraron productos con los filtros aplicados"
                } else {
                    "No hay productos disponibles"
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Intenta con otros términos de búsqueda o filtros",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (hasFilters || searchQuery.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = onClearFilters) {
                    Icon(Icons.Default.Clear, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Limpiar filtros")
                }
            }
        }
    }
}

@Composable
fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    isLoading: Boolean,
    onLoadPage: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { onLoadPage(currentPage - 1) },
                enabled = currentPage > 1 && !isLoading
            ) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null)
                Text("Anterior")
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = "$currentPage / $totalPages",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Button(
                onClick = { onLoadPage(currentPage + 1) },
                enabled = currentPage < totalPages && !isLoading
            ) {
                Text("Siguiente")
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersBottomSheet(
    viewModel: ProductCatalogViewModel,
    onDismiss: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Filtros Avanzados",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Búsqueda
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                label = { Text("Buscar productos") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Selector de categoría
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = viewModel.selectedCategoryFilter?.CategoryName ?: "Todas las categorías",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Categoría") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Todas las categorías") },
                        onClick = {
                            viewModel.updateCategoryFilter(null)
                            expanded = false
                        }
                    )
                    viewModel.categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.CategoryName) },
                            onClick = {
                                viewModel.updateCategoryFilter(category)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Rango de precios
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = viewModel.minPrice,
                    onValueChange = { viewModel.updateMinPrice(it) },
                    label = { Text("Precio mín.") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = viewModel.maxPrice,
                    onValueChange = { viewModel.updateMaxPrice(it) },
                    label = { Text("Precio máx.") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Solo en stock
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = viewModel.showOnlyInStock,
                    onCheckedChange = { viewModel.updateShowOnlyInStock(it) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Solo productos en stock")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.clearFilters()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Limpiar")
                }

                Button(
                    onClick = {
                        viewModel.searchProducts()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Aplicar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}