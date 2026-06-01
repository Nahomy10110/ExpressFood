package cr.una.expressfood.domain.usecase

import cr.una.expressfood.domain.model.Order
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DayReport(
    val date: String,           // "31/05/2026"
    val orderCount: Int,
    val total: Double
)

data class MonthReport(
    val days: List<DayReport>,
    val totalOrders: Int,
    val totalAmount: Double,
    val monthLabel: String      // "Mayo 2026"
)

class ReportUseCase {

    /**
     * Agrupa una lista de órdenes por día y calcula totales.
     */
    fun buildMonthReport(orders: List<Order>): MonthReport {
        val sdfDay   = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val sdfMonth = SimpleDateFormat("MMMM yyyy", Locale("es", "CR"))

        val grouped = orders
            .groupBy { sdfDay.format(Date(it.createdAt)) }
            .map { (date, dayOrders) ->
                DayReport(
                    date       = date,
                    orderCount = dayOrders.size,
                    total      = dayOrders.sumOf { it.total }
                )
            }
            .sortedByDescending { it.date }

        val monthLabel = if (orders.isEmpty()) {
            sdfMonth.format(Date()).replaceFirstChar { it.uppercaseChar() }
        } else {
            sdfMonth.format(Date(orders.first().createdAt))
                .replaceFirstChar { it.uppercaseChar() }
        }

        return MonthReport(
            days         = grouped,
            totalOrders  = orders.size,
            totalAmount  = orders.sumOf { it.total },
            monthLabel   = monthLabel
        )
    }

    /**
     * Retorna el rango de timestamps del mes actual.
     */
    fun currentMonthRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val from = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val to = cal.timeInMillis

        return Pair(from, to)
    }
}