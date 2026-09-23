package com.ecommerce.ecommerceapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ecommerce.ecommerceapp.models.Category
import com.ecommerce.ecommerceapp.ui.components.LoadingButton
import com.ecommerce.ecommerceapp.utils.SessionManager
import com.ecommerce.ecommerceapp.viewmodels.CategoryManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    sessionManager: SessionManager,
    onNavigateBack: () -> Unit,
    viewModel: CategoryManagementViewModel = viewModel { CategoryManagementViewModel(sessionManager) }
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
                title = { Text("Gestión de Categorías") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
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
            // Formulario para crear categoría
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Nueva Categoría",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = viewModel.newCategoryName,
                        onValueChange = viewModel::updateNewCategoryName,
                        label = { Text("Nombre de la Categoría") },
                        leadingIcon = {
                            Icon(Icons.Default.List, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    LoadingButton(
                        text = "Crear Categoría",
                        isLoading = viewModel.isCreatingCategory,
                        onClick = viewModel::createCategory,
                        enabled = viewModel.newCategoryName.isNotBlank()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de categorías
            Text(
                text = "Categorías Existentes (${viewModel.categories.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (viewModel.isLoadingCategories) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (viewModel.categories.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No hay categorías creadas aún",
                        modifier = Modifier.padding(32.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.categories) { category ->
                        CategoryCard(
                            category = category,
                            isDeleting = viewModel.isDeletingCategory,
                            onEdit = { viewModel.selectCategoryForEdit(category) },
                            onDelete = { viewModel.deleteCategory(category) }
                        )
                    }
                }
            }
        }
    }

    // Dialog para editar categoría
    if (viewModel.showEditDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelEdit() },
            title = { Text("Editar Categoría") },
            text = {
                OutlinedTextField(
                    value = viewModel.editCategoryName,
                    onValueChange = viewModel::updateEditCategoryName,
                    label = { Text("Nombre de la Categoría") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                LoadingButton(
                    text = "Actualizar",
                    isLoading = viewModel.isUpdatingCategory,
                    onClick = viewModel::updateCategory,
                    enabled = viewModel.editCategoryName.isNotBlank(),
                    modifier = Modifier.width(120.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelEdit() }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun CategoryCard(
    category: Category,
    isDeleting: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.List,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = category.CategoryName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "ID: ${category.id_Category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row {
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
    }

    // Dialog de confirmación para eliminar
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Categoría") },
            text = {
                Text("¿Estás seguro de que deseas eliminar la categoría \"${category.CategoryName}\"?\n\nEsta acción no se puede deshacer.")
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