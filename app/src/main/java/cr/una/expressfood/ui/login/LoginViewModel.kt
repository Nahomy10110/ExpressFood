package cr.una.expressfood.ui.login

import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseUser
import cr.una.expressfood.data.repository.AuthRepository
import cr.una.expressfood.domain.model.User
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    sealed class LoginState {
        object Idle    : LoginState()
        object Loading : LoginState()
        data class Success(val user: User) : LoginState()
        data class Error(val message: String) : LoginState()
    }

    private val _loginState = MutableLiveData<LoginState>(LoginState.Idle)
    val loginState: LiveData<LoginState> = _loginState

    fun onSignedIn(firebaseUser: FirebaseUser) {
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            runCatching { authRepository.upsertAndFetch(firebaseUser) }
                .onSuccess { _loginState.value = LoginState.Success(it) }
                .onFailure { _loginState.value = LoginState.Error(
                    it.message ?: "Error al guardar el usuario"
                )}
        }
    }

    fun onSignInError(throwable: Throwable) {
        val message = when {
            throwable.message?.contains("cancel", ignoreCase = true) == true ->
                "Inicio de sesión cancelado"
            throwable.message?.contains("network", ignoreCase = true) == true ->
                "Sin conexión. Verifica tu red e intenta de nuevo."
            else -> "No se pudo iniciar sesión. Intenta de nuevo."
        }
        _loginState.value = LoginState.Error(message)
    }

    fun resetError() {
        if (_loginState.value is LoginState.Error) {
            _loginState.value = LoginState.Idle
        }
    }

    class Factory(private val repo: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LoginViewModel(repo) as T
    }
}