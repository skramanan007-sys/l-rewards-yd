package com.lrewards.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lrewards.app.data.RewardsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val loading: Boolean = false,
    val signedIn: Boolean = false,
    val email: String = "",
    val error: String? = null,
)

class AuthViewModel(
    private val repository: RewardsRepository = RewardsRepository(),
) : ViewModel() {
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    fun signIn(email: String, password: String) = viewModelScope.launch {
        _state.value = AuthState(loading = true)
        runCatching { repository.signIn(email, password) }
            .onSuccess {
                _state.value = AuthState(
                    signedIn = true,
                    email = repository.currentEmail().orEmpty(),
                )
            }
            .onFailure {
                _state.value = AuthState(
                    error = "Unable to sign in. Check your credentials and try again.",
                )
            }
    }

    fun signUp(email: String, password: String, name: String) = viewModelScope.launch {
        _state.value = AuthState(loading = true)
        runCatching { repository.signUp(email, password, name) }
            .onSuccess {
                _state.value = AuthState(error = "Check your email to confirm your account.")
            }
            .onFailure {
                _state.value = AuthState(error = "Unable to create the account. Please try again.")
            }
    }

    fun signOut() = viewModelScope.launch {
        runCatching { repository.signOut() }
        _state.value = AuthState()
    }
}
