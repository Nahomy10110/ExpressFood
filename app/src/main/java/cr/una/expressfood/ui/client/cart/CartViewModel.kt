package cr.una.expressfood.ui.client.cart

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import cr.una.expressfood.data.local.AppDatabase
import cr.una.expressfood.data.repository.CartRepository
import cr.una.expressfood.domain.model.CartItem
import cr.una.expressfood.domain.model.Product
import cr.una.expressfood.domain.usecase.CalculateTotalUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CartViewModel(
    application: Application,
    private val cartRepository: CartRepository
) : AndroidViewModel(application) {

    private val calculateTotal = CalculateTotalUseCase()
    private val clientUid get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val clientName get() = FirebaseAuth.getInstance().currentUser?.displayName ?: ""

    sealed class CartState {
        object Empty : CartState()
        data class WithItems(
            val items: List<CartItem>,
            val subtotal: Double,
            val taxes: Double,
            val total: Double
        ) : CartState()
        object OrderProcessed : CartState()
        data class Error(val message: String) : CartState()
    }

    private val _cartState = MutableStateFlow<CartState>(CartState.Empty)
    val cartState: StateFlow<CartState> = _cartState.asStateFlow()

    private val _itemCount = MutableStateFlow(0)
    val itemCount: StateFlow<Int> = _itemCount.asStateFlow()

    init {
        observeCart()
        observeItemCount()
    }

    private fun observeCart() {
        viewModelScope.launch {
            cartRepository.observeCart(clientUid).collect { items ->
                if (items.isEmpty()) {
                    _cartState.value = CartState.Empty
                } else {
                    val subtotal = items.sumOf { it.subtotal }
                    val totals   = calculateTotal.invoke(subtotal)
                    _cartState.value = CartState.WithItems(
                        items    = items,
                        subtotal = totals.subtotal,
                        taxes    = totals.taxes,
                        total    = totals.total
                    )
                }
            }
        }
    }

    private fun observeItemCount() {
        viewModelScope.launch {
            cartRepository.observeItemCount(clientUid).collect {
                _itemCount.value = it
            }
        }
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            cartRepository.addProduct(clientUid, product)
        }
    }

    fun increaseQuantity(item: CartItem) {
        viewModelScope.launch { cartRepository.increaseQuantity(item) }
    }

    fun decreaseQuantity(item: CartItem) {
        viewModelScope.launch { cartRepository.decreaseQuantity(item) }
    }

    fun processOrder(deliveryAddress: String, notes: String? = null) {
        val state = _cartState.value
        if (state !is CartState.WithItems) return

        viewModelScope.launch {
            runCatching {
                cartRepository.processOrder(
                    clientUid       = clientUid,
                    clientName      = clientName,
                    deliveryAddress = deliveryAddress,
                    notes           = notes,
                    items           = state.items
                )
            }.onSuccess {
                _cartState.value = CartState.OrderProcessed
            }.onFailure {
                _cartState.value = CartState.Error(it.message ?: "Error al procesar la orden")
            }
        }
    }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            val db   = AppDatabase.getInstance(app)
            val repo = CartRepository.default(db.cartItemDao(), db.orderDao(), db.orderItemDao())
            return CartViewModel(app, repo) as T
        }
    }
}