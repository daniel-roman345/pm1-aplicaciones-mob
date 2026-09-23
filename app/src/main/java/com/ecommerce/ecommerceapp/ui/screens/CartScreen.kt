package com.ecommerce.ecommerceapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ecommerce.ecommerceapp.models.CartItem
import com.ecommerce.ecommerceapp.ui.components.LoadingButton
import com.ecommerce.ecommerceapp.utils.SessionManager
import com.ecommerce.ecommerceapp.viewmodels.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    sessionManager: SessionManager,
    onNavigateBack: () -> Unit,
    onCheckoutSuccess: () -> Unit,
    viewModel: CartViewModel // Remuevo el valor por defecto para forzar pasar el viewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showClearCartDialog by remember { mutableStateOf(false) }

    // FORZAR REFRESH del carrito al entrar a la pantalla
    LaunchedEffect(Unit) {
        android.util.Log.d("CART_SCREEN_DEBUG", "CartScreen iniciada con ViewModel: ${viewModel.hashCode()}")
        android.util.Log.d("CART_SCREEN_DEBUG", "Estado actual del carrito: ${viewModel.getDebugInfo()}")
        android.util.Log.d("CART_SCREEN_DEBUG", "Refrescando carrito...")
        viewModel.refreshCart()
    }

    // Observar cambios en el carrito
    LaunchedEffect(viewModel.cartItemsCount, viewModel.cart) {
        android.util.Log.d("CART_SCREEN_DEBUG", "CAMBIO DETECTADO en carrito:")
        android.util.Log.d("CART_SCREEN_DEBUG", "- Items count: ${viewModel.cartItemsCount}")
        android.util.Log.d("CART_SCREEN_DEBUG", "- Cart items size: ${viewModel.cart?.items?.size}")
        android.util.Log.d("CART_SCREEN_DEBUG", "- Is empty: ${viewModel.isCartEmpty()}")
    }

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

    // Manejar checkout exitoso
    LaunchedEffect(viewModel.lastCompletedSale) {
        viewModel.lastCompletedSale?.let { sale ->
            onCheckoutSuccess()
            viewModel.clearLastCompletedSale()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Mi Carrito${if (viewModel.cartItemsCount > 0) " (${viewModel.cartItemsCount})" else ""}")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Botón de debug temporal
                    IconButton(
                        onClick = {
                            android.util.Log.d("CART_SCREEN_DEBUG", "=== DEBUG MANUAL DESDE CART SCREEN ===")
                            viewModel.debugCurrentState()
                            viewModel.refreshCart()
                        }
                    ) {
                        Icon(
                            Icons.Default.BugReport,
                            contentDescription = "Debug & Refresh",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (!viewModel.isCartEmpty()) {
                        IconButton(
                            onClick = { showClearCartDialog = true },
                            enabled = !viewModel.isClearingCart
                        ) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Vaciar carrito",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!viewModel.isCartEmpty()) {
                CartBottomBar(
                    total = viewModel.getCartTotal(),
                    isCheckingOut = viewModel.isCheckingOut,
                    onCheckout = { showCheckoutDialog = true }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (viewModel.isLoadingCart) {
                android.util.Log.d("CART_SCREEN_DEBUG", "Carrito cargando...")
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Cargando carrito...")
                    }
                }
            } else if (viewModel.isCartEmpty()) {
                android.util.Log.d("CART_SCREEN_DEBUG", "Carrito vacío - Items: ${viewModel.cart?.items?.size ?: 0}, Total items: ${viewModel.cartItemsCount}")
                EmptyCartState(onNavigateBack = onNavigateBack)
            } else {
                android.util.Log.d("CART_SCREEN_DEBUG", "Mostrando carrito con ${viewModel.cart?.items?.size ?: 0} productos")
                CartContent(
                    cartItems = viewModel.cart?.items ?: emptyList(),
                    isUpdatingCart = viewModel.isUpdatingCart,
                    isRemovingFromCart = viewModel.isRemovingFromCart,
                    onUpdateQuantity = { itemId, quantity ->
                        viewModel.updateCartItemQuantity(itemId, quantity)
                    },
                    onRemoveItem = { itemId ->
                        viewModel.removeFromCart(itemId)
                    }
                )
            }
        }
    }

    // Dialog de confirmación para checkout
    if (showCheckoutDialog) {
        CheckoutDialog(
            checkoutDescription = viewModel.checkoutDescription,
            onDescriptionChange = { viewModel.updateCheckoutDescription(it) },
            isLoading = viewModel.isCheckingOut,
            onConfirm = {
                viewModel.checkout()
                showCheckoutDialog = false
            },
            onDismiss = { showCheckoutDialog = false }
        )
    }

    // Dialog de confirmación para vaciar carrito
    if (showClearCartDialog) {
        AlertDialog(
            onDismissRequest = { showClearCartDialog = false },
            title = { Text("Vaciar Carrito") },
            text = {
                Text("¿Estás seguro de que deseas vaciar el carrito? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearCart()
                        showClearCartDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    enabled = !viewModel.isClearingCart
                ) {
                    if (viewModel.isClearingCart) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onError
                        )
                    } else {
                        Text("Vaciar")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCartDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun CartContent(
    cartItems: List<CartItem>,
    isUpdatingCart: Boolean,
    isRemovingFromCart: Boolean,
    onUpdateQuantity: (Int, Int) -> Unit,
    onRemoveItem: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(cartItems) { cartItem ->
            CartItemCard(
                cartItem = cartItem,
                isUpdating = isUpdatingCart,
                isRemoving = isRemovingFromCart,
                onUpdateQuantity = { quantity ->
                    onUpdateQuantity(cartItem.id_cart_item, quantity)
                },
                onRemove = {
                    onRemoveItem(cartItem.id_cart_item)
                }
            )
        }

        // Espaciado adicional para el bottom bar
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartItemCard(
    cartItem: CartItem,
    isUpdating: Boolean,
    isRemoving: Boolean,
    onUpdateQuantity: (Int) -> Unit,
    onRemove: () -> Unit
) {
    var showRemoveDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen placeholder del producto
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

            // Información del producto
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cartItem.product?.ProductName ?: "Producto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$${cartItem.product?.Price ?: 0.0} c/u",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Subtotal: $${cartItem.subtotal}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Controles de cantidad
                    QuantityControls(
                        quantity = cartItem.quantity,
                        isUpdating = isUpdating,
                        onQuantityChange = onUpdateQuantity
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Botón eliminar
            IconButton(
                onClick = { showRemoveDialog = true },
                enabled = !isRemoving
            ) {
                if (isRemoving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    // Dialog de confirmación para eliminar item
    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("Eliminar Producto") },
            text = {
                Text("¿Eliminar \"${cartItem.product?.ProductName}\" del carrito?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRemove()
                        showRemoveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun QuantityControls(
    quantity: Int,
    isUpdating: Boolean,
    onQuantityChange: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Botón decrementar
        FilledTonalIconButton(
            onClick = { onQuantityChange(quantity - 1) },
            enabled = quantity > 1 && !isUpdating,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.Default.Remove,
                contentDescription = "Disminuir cantidad",
                modifier = Modifier.size(18.dp)
            )
        }

        // Cantidad actual
        if (isUpdating) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = quantity.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(32.dp)
            )
        }

        // Botón incrementar
        FilledTonalIconButton(
            onClick = { onQuantityChange(quantity + 1) },
            enabled = !isUpdating,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Aumentar cantidad",
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun CartBottomBar(
    total: Double,
    isCheckingOut: Boolean,
    onCheckout: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${String.format("%.2f", total)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                LoadingButton(
                    text = "Confirmar Pedido",
                    isLoading = isCheckingOut,
                    onClick = onCheckout,
                    modifier = Modifier.width(180.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyCartState(onNavigateBack: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.ShoppingCartCheckout,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Tu carrito está vacío",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "¡Agrega productos para comenzar tu compra!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Explorar Productos")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutDialog(
    checkoutDescription: String,
    onDescriptionChange: (String) -> Unit,
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar Pedido") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "¿Estás seguro de que deseas confirmar este pedido?",
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = checkoutDescription,
                    onValueChange = onDescriptionChange,
                    label = { Text("Descripción del pedido (opcional)") },
                    placeholder = { Text("Ej: Entrega a domicilio, regalo, etc.") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    singleLine = false
                )

                Text(
                    text = "Una vez confirmado, se procesará tu pedido y se vaciará el carrito.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            LoadingButton(
                text = "Confirmar",
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