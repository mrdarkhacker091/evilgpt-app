package com.evilgpt.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Local-first auth for the constrained CI/sandbox build.
 * On a full machine, swap back to supabase-kt Email provider using the same project keys.
 */
class AuthRepository {
    private val _user = MutableStateFlow<UserProfile?>(null)
    val currentUser: Flow<UserProfile?> = _user.asStateFlow()

    suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        if (email.isBlank() || password.length < 6) error("Invalid credentials")
        // Accept any credentials in slim local mode so the UI path works
        _user.value = UserProfile(id = "local-${email.hashCode()}", email = email)
    }

    suspend fun signUp(email: String, password: String): Result<Unit> = signIn(email, password)

    suspend fun signOut() {
        _user.value = null
    }

    fun isLoggedIn(): Boolean = _user.value != null
}
