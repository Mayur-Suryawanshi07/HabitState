package com.mayur.habitstate.presentation.screens.auth.loginscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mayur.habitstate.presentation.screens.auth.component.AuthErrorCard
import com.mayur.habitstate.presentation.screens.auth.component.AuthHeader
import com.mayur.habitstate.presentation.screens.auth.component.AuthPasswordField
import com.mayur.habitstate.presentation.screens.auth.component.AuthPrimaryButton
import com.mayur.habitstate.presentation.screens.auth.component.AuthTextField

@Composable
fun LogInScreen(
    viewModel: LogInScreenViewModel = viewModel(),
    onNavigateToSignup: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val uiState by viewModel.state.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateToHome()
            viewModel.resetState()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Logo & Branding
            AuthHeader(
                title = "HabitState",
                subtitle = "Welcome back! Sign in to track your habits."
            )

            // Form Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Email Field
                    AuthTextField(
                        value = uiState.email,
                        onValueChange = viewModel::onEmailChange,
                        label = "Email Address",
                        leadingIcon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    AuthPasswordField(
                        value = uiState.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = "Password",
                        leadingIcon = Icons.Default.Lock,
                        isPasswordVisible = uiState.isPasswordVisible,
                        onToggleVisibility = viewModel::togglePasswordVisibility,
                        imeAction = ImeAction.Done,
                        keyboardActions = KeyboardActions(
                            onDone = { viewModel.login() }
                        )
                    )

                    // Forgot Password Link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onNavigateToForgotPassword) {
                            Text(
                                text = "Forgot Password?",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Error Banner
                    if (uiState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        AuthErrorCard(errorMessage = uiState.errorMessage ?: "")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Submit Button
                    AuthPrimaryButton(
                        text = "Login",
                        onClick = viewModel::login,
                        isLoading = uiState.isLoading
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer Navigation
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Don't have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = onNavigateToSignup) {
                    Text(
                        text = "Sign Up",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
