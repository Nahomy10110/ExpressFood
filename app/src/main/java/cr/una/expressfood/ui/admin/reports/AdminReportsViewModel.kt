package cr.una.expressfood.ui.admin.reports

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cr.una.expressfood.data.local.AppDatabase
import cr.una.expressfood.data.repository.OrderRepository
import cr.una.expressfood.domain.usecase.MonthReport
import cr.una.expressfood.domain.usecase.ReportUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminReportsViewModel(
    application: Application,
    private val orderRepository: OrderRepository,
    private val reportUseCase: ReportUseCase = ReportUseCase()
) : AndroidViewModel(application) {

    sealed class ReportState {
        object Loading : ReportState()
        data class Success(val report: MonthReport) : ReportState()
        object Empty : ReportState()
    }

    private val _reportState = MutableStateFlow<ReportState>(ReportState.Loading)
    val reportState: StateFlow<ReportState> = _reportState.asStateFlow()

    init { loadReport() }

    fun loadReport() {
        viewModelScope.launch {
            _reportState.value = ReportState.Loading
            runCatching {
                val (from, to) = reportUseCase.currentMonthRange()
                orderRepository.getAllOrdersByDateRange(from, to)
            }.onSuccess { orders ->
                val report = reportUseCase.buildMonthReport(orders)
                _reportState.value = if (orders.isEmpty()) ReportState.Empty
                else ReportState.Success(report)
            }.onFailure {
                _reportState.value = ReportState.Empty
            }
        }
    }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            val db   = AppDatabase.getInstance(app)
            val repo = OrderRepository.default(db.orderDao(), db.orderItemDao())
            return AdminReportsViewModel(app, repo) as T
        }
    }
}