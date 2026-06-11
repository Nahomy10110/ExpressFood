package cr.una.expressfood.ui.admin.orders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cr.una.expressfood.data.local.AppDatabase
import cr.una.expressfood.data.repository.OrderRepository
import cr.una.expressfood.domain.model.Order
import cr.una.expressfood.domain.model.OrderStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminOrdersViewModel(
    application: Application,
    private val orderRepository: OrderRepository
) : AndroidViewModel(application) {

    sealed class AdminOrdersState {
        object Loading : AdminOrdersState()
        data class Success(val orders: List<Order>) : AdminOrdersState()
        object Empty : AdminOrdersState()
    }

    private val _ordersState = MutableStateFlow<AdminOrdersState>(AdminOrdersState.Loading)
    val ordersState: StateFlow<AdminOrdersState> = _ordersState.asStateFlow()

    init {
        // Escuchar Firestore en tiempo real — trae órdenes de todos los clientes
        orderRepository.listenToAllOrdersFromFirestore()
        // Observar Room — se actualiza cuando llegan datos de Firestore
        observeAllOrders()
    }

    private fun observeAllOrders() {
        viewModelScope.launch {
            orderRepository.observeAllOrders().collect { orders ->
                _ordersState.value = when {
                    orders.isEmpty() -> AdminOrdersState.Empty
                    else             -> AdminOrdersState.Success(orders)
                }
            }
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        viewModelScope.launch {
            runCatching {
                orderRepository.updateStatus(orderId, newStatus)
            }
        }
    }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            val db   = AppDatabase.getInstance(app)
            val repo = OrderRepository.default(db.orderDao(), db.orderItemDao())
            return AdminOrdersViewModel(app, repo) as T
        }
    }
}