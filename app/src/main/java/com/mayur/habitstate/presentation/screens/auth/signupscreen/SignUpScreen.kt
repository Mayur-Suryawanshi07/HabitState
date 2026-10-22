package com.mayur.habitstate.presentation.screens.auth.signupscreen

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
import androidx.compose.material.icons.filled.Person
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
fun SignUpScreen(
    viewModel: SignUpViewModel = viewModel(),
    onNavigateToLogin: () -> Unit,
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
            // Header
            AuthHeader(
                title = "Join HabitState",
                subtitle = "Create an account to start building your daily habits."
            )

            // Form Surface
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
                        text = "Sign Up",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Full Name
                    AuthTextField(
                        value = uiState.fullName,
                        onValueChange = viewModel::onFullNameChange,
                        label = "Full Name",
                        leadingIcon = Icons.Default.Person,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email Address
                    AuthTextField(
                        value = uiState.email,
                        onValueChange = viewModel::onEmailChange,
                        label = "Email Address",
                        leadingIcon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password
                    AuthPasswordField(
                        value = uiState.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = "Password (min 6 chars)",
                        leadingIcon = Icons.Default.Lock,
                        isPasswordVisible = uiState.isPasswordVisible,
                        onToggleVisibility = viewModel::togglePasswordVisibility,
                        imeAction = ImeAction.Next
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Confirm Password
                    AuthPasswordField(
                        value = uiState.confirmPassword,
                        onValueChange = viewModel::onConfirmPasswordChange,
                        label = "Confirm Password",
                        leadingIcon = Icons.Default.Lock,
                        isPasswordVisible = uiState.isConfirmPasswordVisible,
                        onToggleVisibility = viewModel::toggleConfirmPasswordVisibility,
                        imeAction = ImeAction.Done,
                        keyboardActions = KeyboardActions(
                            onDone = { viewModel.signup() }
                        )
                    )

                    // Error Banner
                    if (uiState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        AuthErrorCard(errorMessage = uiState.errorMessage ?: "")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Submit Button
                    AuthPrimaryButton(
                        text = "Create Account",
                        onClick = viewModel::signup,
                        isLoading = uiState.isLoading
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer Navigation
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
