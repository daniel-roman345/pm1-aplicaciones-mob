package com.ecommerce.ecommerceapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ecommerce.ecommerceapp.ui.components.LoadingButton
import com.ecommerce.ecommerceapp.utils.SessionManager
import com.ecommerce.ecommerceapp.viewmodels.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    sessionManager: SessionManager,
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel { ProfileViewModel(sessionManager) }
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showChangePasswordDialog by remember { mutableStateOf(false) }

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
                title = { Text("Mi Perfil") },
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Información del perfil
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Información Personal",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    // Campo Nombre
                    OutlinedTextField(
                        value = viewModel.userName,
                        onValueChange = viewModel::updateUserName,
                        label = { Text("Nombre de Usuario") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !viewModel.isUpdatingProfile
                    )

                    // Selector de Ciudad
                    var expandedCities by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedCities,
                        onExpandedChange = { expandedCities = !expandedCities }
                    ) {
                        OutlinedTextField(
                            value = viewModel.cities.find { it.iD_City == viewModel.selectedCityId }?.CityName ?: "Seleccionar ciudad",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Ciudad") },
                            leadingIcon = {
                                Icon(Icons.Default.LocationOn, contentDescription = null)
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCities) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expandedCities,
                            onDismissRequest = { expandedCities = false }
                        ) {
                            viewModel.cities.forEach { city ->
                                DropdownMenuItem(
                                    text = { Text("${city.CityName} - ${city.state}") },
                                    onClick = {
                                        viewModel.updateSelectedCity(city.iD_City)
                                        expandedCities = false
                                    }
                                )
                            }
                        }
                    }

                    // Email (solo lectura)
                    OutlinedTextField(
                        value = viewModel.userProfile?.Email ?: "",
                        onValueChange = { },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false
                    )

                    // Botón actualizar perfil
                    LoadingButton(
                        text = "Actualizar Perfil",
                        isLoading = viewModel.isUpdatingProfile,
                        onClick = viewModel::updateProfile
                    )
                }
            }

            // Sección de contraseña
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Seguridad",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = { showChangePasswordDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cambiar Contraseña")
                    }
                }
            }

            // Información de roles
            viewModel.userProfile?.roles?.let { roles ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Roles",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        roles.forEach { role ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(role.TypeRole)
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog para cambiar contraseña
    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("Cambiar Contraseña") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = viewModel.currentPassword,
                        onValueChange = viewModel::updateCurrentPassword,
                        label = { Text("Contraseña Actual") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = viewModel.newPassword,
                        onValueChange = viewModel::updateNewPassword,
                        label = { Text("Nueva Contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = viewModel.confirmPassword,
                        onValueChange = viewModel::updateConfirmPassword,
                        label = { Text("Confirmar Nueva Contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                LoadingButton(
                    text = "Cambiar",
                    isLoading = viewModel.isChangingPassword,
                    onClick = {
                        viewModel.changePassword()
                        if (viewModel.successMessage != null) {
                            showChangePasswordDialog = false
                        }
                    },
                    modifier = Modifier.width(120.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}