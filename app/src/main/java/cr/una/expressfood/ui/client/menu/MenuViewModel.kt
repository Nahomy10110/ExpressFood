package cr.una.expressfood.ui.client.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cr.una.expressfood.data.local.AppDatabase
import cr.una.expressfood.data.repository.ProductRepository
import cr.una.expressfood.domain.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.app.Application

class MenuViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    sealed class MenuState {
        object Loading : MenuState()
        data class Success(val products: List<Product>) : MenuState()
        data class Error(val message: String) : MenuState()
    }

    private val _menuState = MutableStateFlow<MenuState>(MenuState.Loading)
    val menuState: StateFlow<MenuState> = _menuState.asStateFlow()

    // Resultado de la sincronización con Firestore
    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    init {
        observeProducts()
        syncFromFirestore()
    }

    private fun observeProducts() {
        viewModelScope.launch {
            productRepository.observeAvailable().collect { products ->
                _menuState.value = if (products.isEmpty()) MenuState.Loading
                else MenuState.Success(products)
            }
        }
    }

    /**
     * Descarga productos de Firestore y los guarda en Room.
     * Falla silenciosamente si no hay red;Room ya tiene los datos del sync anterior.
     */
    fun syncFromFirestore() {
        viewModelScope.launch {
            productRepository.syncFromFirestore()
                .onFailure {
                    // Sin red, Room ya tiene los productos cacheados, no es error crítico
                    if (_menuState.value is MenuState.Loading) {
                        _menuState.value = MenuState.Error("Sin conexión — mostrando datos locales")
                    }
                }
        }
    }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db   = AppDatabase.getInstance(app)
            val repo = ProductRepository.default(db.productDao())
            return MenuViewModel(repo) as T
        }
    }
}