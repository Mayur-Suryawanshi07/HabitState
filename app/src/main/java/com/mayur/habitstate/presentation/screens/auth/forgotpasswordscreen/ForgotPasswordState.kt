package com.mayur.habitstate.presentation.screens.auth.forgotpasswordscreen

data class ForgotPasswordState(
    val email: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
