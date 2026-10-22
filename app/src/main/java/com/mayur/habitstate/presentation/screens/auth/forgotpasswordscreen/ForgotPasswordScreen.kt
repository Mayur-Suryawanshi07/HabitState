package com.mayur.habitstate.presentation.screens.auth.forgotpasswordscreen

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.mayur.habitstate.presentation.screens.auth.component.AuthPrimaryButton
import com.mayur.habitstate.presentation.screens.auth.component.AuthSuccessCard
import com.mayur.habitstate.presentation.screens.auth.component.AuthTextField

@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel = viewModel(),
    onNavigateBackToLogin: () -> Unit
) {
    val uiState by viewModel.state.collectAsState()

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
            // Top Back Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = onNavigateBackToLogin) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Login",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Header
            AuthHeader(
                title = "Forgot Password?",
                subtitle = "Enter your registered email address and we'll send you instructions to reset your password.",
                icon = Icons.Default.LockReset
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
                    // Email Field
                    AuthTextField(
                        value = uiState.email,
                        onValueChange = viewModel::onEmailChange,
                        label = "Email Address",
                        leadingIcon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done,
                        keyboardActions = KeyboardActions(
                            onDone = { viewModel.sendPasswordResetEmail() }
                        )
                    )

                    // Error Banner
                    if (uiState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        AuthErrorCard(errorMessage = uiState.errorMessage ?: "")
                    }

                    // Success Banner
                    if (uiState.successMessage != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        AuthSuccessCard(successMessage = uiState.successMessage ?: "")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Reset Button
                    AuthPrimaryButton(
                        text = "Send Reset Link",
                        onClick = viewModel::sendPasswordResetEmail,
                        isLoading = uiState.isLoading
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer Link
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Remembered your password?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = onNavigateBackToLogin) {
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
