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
import com.ecommerce.ecommerceapp.ui.components.LoadingButton
import com.ecommerce.ecommerceapp.viewmodels.LocationManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: LocationManagementViewModel = viewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableStateOf(0) }

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
                title = { Text("Gestión de Ubicaciones") },
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
        ) {
            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Países") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        viewModel.loadStates()
                    },
                    text = { Text("Estados") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        viewModel.loadCities()
                    },
                    text = { Text("Ciudades") }
                )
            }

            // Contenido según tab seleccionado
            when (selectedTab) {
                0 -> CountriesTab(viewModel)
                1 -> StatesTab(viewModel)
                2 -> CitiesTab(viewModel)
            }
        }
    }
}

@Composable
fun CountriesTab(viewModel: LocationManagementViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Formulario para crear país
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Crear Nuevo País",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = viewModel.newCountryName,
                    onValueChange = viewModel::updateNewCountryName,
                    label = { Text("Nombre del País") },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                LoadingButton(
                    text = "Crear País",
                    isLoading = viewModel.isCreating,
                    onClick = viewModel::createCountry,
                    enabled = viewModel.newCountryName.isNotBlank()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de países
        Text(
            text = "Países Existentes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (viewModel.isLoadingCountries) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.countries) { country ->
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
                            Text(
                                text = country.CountryName,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            IconButton(
                                onClick = { viewModel.loadStates(country.iD_Country) }
                            ) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Ver estados")  }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatesTab(viewModel: LocationManagementViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Formulario para crear estado
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Crear Nuevo Estado",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Selector de país
                var expandedCountries by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedCountries,
                    onExpandedChange = { expandedCountries = !expandedCountries }
                ) {
                    OutlinedTextField(
                        value = viewModel.selectedCountryForState?.CountryName ?: "Seleccionar país",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("País") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCountries) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedCountries,
                        onDismissRequest = { expandedCountries = false }
                    ) {
                        viewModel.countries.forEach { country ->
                            DropdownMenuItem(
                                text = { Text(country.CountryName) },
                                onClick = {
                                    viewModel.updateSelectedCountryForState(country)
                                    expandedCountries = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = viewModel.newStateName,
                    onValueChange = viewModel::updateNewStateName,
                    label = { Text("Nombre del Estado") },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                LoadingButton(
                    text = "Crear Estado",
                    isLoading = viewModel.isCreating,
                    onClick = viewModel::createState,
                    enabled = viewModel.newStateName.isNotBlank() && viewModel.selectedCountryForState != null
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de estados
        Text(
            text = "Estados Existentes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (viewModel.isLoadingStates) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.states) { state ->
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
                            Column {
                                Text(
                                    text = state.StatesName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = state.country?.CountryName ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = { viewModel.loadCities(state.iD_States) }
                            ) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Ver Ciudades")   }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitiesTab(viewModel: LocationManagementViewModel) {
    // Cargar estados al inicializar la tab
    LaunchedEffect(Unit) {
        viewModel.loadStates()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Formulario para crear ciudad
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Crear Nueva Ciudad",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Selector de estado
                var expandedStates by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedStates,
                    onExpandedChange = { expandedStates = !expandedStates }
                ) {
                    OutlinedTextField(
                        value = viewModel.selectedStateForCity?.StatesName ?: "Seleccionar estado",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Estado") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStates) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedStates,
                        onDismissRequest = { expandedStates = false }
                    ) {
                        viewModel.states.forEach { state ->
                            DropdownMenuItem(
                                text = { Text("${state.StatesName} - ${state.country?.CountryName}") },
                                onClick = {
                                    viewModel.updateSelectedStateForCity(state)
                                    expandedStates = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = viewModel.newCityName,
                    onValueChange = viewModel::updateNewCityName,
                    label = { Text("Nombre de la Ciudad") },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                LoadingButton(
                    text = "Crear Ciudad",
                    isLoading = viewModel.isCreating,
                    onClick = viewModel::createCity,
                    enabled = viewModel.newCityName.isNotBlank() && viewModel.selectedStateForCity != null
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de ciudades
        Text(
            text = "Ciudades Existentes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (viewModel.isLoadingCities) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.cities) { city ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = city.CityName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${city.state}, ${city.country}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}