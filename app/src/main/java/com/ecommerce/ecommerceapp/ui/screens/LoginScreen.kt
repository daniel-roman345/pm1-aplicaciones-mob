package com.ecommerce.ecommerceapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ecommerce.ecommerceapp.models.AuthResponse
import com.ecommerce.ecommerceapp.ui.components.LoadingButton
import com.ecommerce.ecommerceapp.ui.theme.Primary
import com.ecommerce.ecommerceapp.viewmodels.LoginViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: (AuthResponse) -> Unit,
    onNavigateToRegister: () -> Unit,
    registeredEmail: String? = null,
    viewModel: LoginViewModel = viewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Pre-llenar email si viene del registro
    LaunchedEffect(registeredEmail) {
        registeredEmail?.let { email ->
            viewModel.updateEmail(email)
        }
    }

    // Observar el estado de éxito del login
    LaunchedEffect(viewModel.loginSuccess) {
        viewModel.loginSuccess?.let { authResponse ->
            onLoginSuccess(authResponse)
            viewModel.clearLoginSuccess()
        }
    }

    // Mostrar errores
    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF5F5F5),
                        Color(0xFFE3F2FD)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Título de la app
            Text(
                text = "E-commerce App",
                style = MaterialTheme.typography.headlineMedium,
                color = Primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Card principal
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Iniciar Sesión",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Campo Email
                    OutlinedTextField(
                        value = viewModel.email,
                        onValueChange = viewModel::updateEmail,
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = viewModel.errorMessage?.contains("email", ignoreCase = true) == true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo Password
                    OutlinedTextField(
                        value = viewModel.password,
                        onValueChange = viewModel::updatePassword,
                        label = { Text("Contraseña") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null)
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = viewModel.errorMessage?.contains("contraseña", ignoreCase = true) == true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón Login
                    LoadingButton(
                        text = "Iniciar Sesión",
                        isLoading = viewModel.isLoading,
                        onClick = viewModel::login,
                        enabled = viewModel.email.isNotBlank() && viewModel.password.isNotBlank()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Link a registro
                    val registerText = buildAnnotatedString {
                        append("¿No tienes cuenta? ")
                        withStyle(style = SpanStyle(color = Primary, fontWeight = FontWeight.Bold)) {
                            append("Regístrate aquí")
                        }
                    }

                    ClickableText(
                        text = registerText,
                        onClick = { onNavigateToRegister() },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }

            // Mostrar mensaje si viene del registro
            registeredEmail?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8F5E8)
                    )
                ) {
                    Text(
                        text = "✅ Cuenta creada exitosamente\nYa puedes iniciar sesión",
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}