package cr.una.expressfood.ui.client.orders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import cr.una.expressfood.data.local.AppDatabase
import cr.una.expressfood.data.repository.OrderRepository
import cr.una.expressfood.domain.model.Order
import cr.una.expressfood.domain.model.OrderStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyOrdersViewModel(
    application: Application,
    private val orderRepository: OrderRepository
) : AndroidViewModel(application) {

    private val clientUid get() =
        FirebaseAuth.getInstance().currentUser?.uid ?: ""

    sealed class OrdersState {
        object Loading : OrdersState()
        data class Success(val orders: List<Order>) : OrdersState()
        object Empty : OrdersState()
    }

    private val _ordersState = MutableStateFlow<OrdersState>(OrdersState.Loading)
    val ordersState: StateFlow<OrdersState> = _ordersState.asStateFlow()

    private val _activeFilter = MutableStateFlow<OrderStatus?>(null)
    val activeFilter: StateFlow<OrderStatus?> = _activeFilter.asStateFlow()

    init {
        observeOrders()
    }

    private fun observeOrders() {
        viewModelScope.launch {
            orderRepository.observeClientOrders(clientUid).collect { orders ->
                _ordersState.value = when {
                    orders.isEmpty() -> OrdersState.Empty
                    else             -> OrdersState.Success(orders)
                }
            }
        }
    }

    fun setFilter(status: OrderStatus?) {
        _activeFilter.value = status
    }

    fun refreshOrderItems(orderId: String) {
        viewModelScope.launch {
            runCatching {
                orderRepository.fetchOrderItemsFromFirestore(orderId)
            }
        }
    }

    fun getOrderById(orderId: String): Order? {
        val state = _ordersState.value
        return if (state is OrdersState.Success)
            state.orders.find { it.id == orderId }
        else null
    }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            val db   = AppDatabase.getInstance(app)
            val repo = OrderRepository.default(db.orderDao(), db.orderItemDao())
            return MyOrdersViewModel(app, repo) as T
        }
    }
}