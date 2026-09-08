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

data class WalletState(
    val transactions: List<kotlinx.serialization.json.JsonObject> = emptyList(),
    val withdrawals: List<kotlinx.serialization.json.JsonObject> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
)

class AuthViewModel(
    private val repository: RewardsRepository = RewardsRepository(),
) : ViewModel() {
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state
    private val _wallet = MutableStateFlow(WalletState())
    val wallet: StateFlow<WalletState> = _wallet

    fun loadWallet() = viewModelScope.launch {
        _wallet.value = WalletState(loading = true)
        runCatching { repository.rewardTransactions() to repository.withdrawalHistory() }
            .onSuccess { (transactions, withdrawals) -> _wallet.value = WalletState(transactions, withdrawals) }
            .onFailure { _wallet.value = WalletState(error = "Unable to load wallet history") }
    }

    fun signIn(email: String, password: String) = viewModelScope.launch {
        _state.value = AuthState(loading = true)
        runCatching { repository.signIn(email, password) }
            .onSuccess {
                _state.value = AuthState(
                    signedIn = true,
                    email = repository.currentEmail().orEmpty(),
                )
                loadWallet()
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

    fun playGame(gameType: String, amount: Int, onComplete: (Int?, String?) -> Unit) = viewModelScope.launch {
        runCatching { repository.playGame(gameType, amount) }
            .onSuccess { result -> onComplete(result["balance"]?.toString()?.toIntOrNull(), null) }
            .onFailure { error ->
                val raw = error.message.orEmpty().lowercase()
                onComplete(null, if (raw.contains("limit") || raw.contains("daily") || raw.contains("maximum")) "Limit reached for today" else "Unable to claim reward")
            }
    }

    fun requestRedemption(type: String, cost: Int, onComplete: (Int?, String?) -> Unit) = viewModelScope.launch {
        runCatching { repository.requestRedemption(type, cost) }
            .onSuccess { result -> onComplete(result["balance"]?.toString()?.toIntOrNull(), null) }
            .onFailure { error -> onComplete(null, "Unable to request redemption") }
    }
}
