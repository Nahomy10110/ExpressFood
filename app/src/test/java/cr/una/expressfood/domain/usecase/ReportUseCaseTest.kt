package cr.una.expressfood.domain.usecase

import cr.una.expressfood.domain.model.Order
import cr.una.expressfood.domain.model.OrderStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportUseCaseTest {

    private val useCase = ReportUseCase()

    @Test
    fun `lista vacia retorna reporte con ceros`() {
        val report = useCase.buildMonthReport(emptyList())
        assertEquals(0, report.totalOrders)
        assertEquals(0.0, report.totalAmount, 0.01)
        assertTrue(report.days.isEmpty())
    }

    @Test
    fun `ordenes del mismo dia se agrupan correctamente`() {
        val orders = listOf(
            makeOrder("1", 5085.0, 1748650000000L),
            makeOrder("2", 10170.0, 1748650000000L)
        )
        val report = useCase.buildMonthReport(orders)
        assertEquals(1, report.days.size)
        assertEquals(2, report.days.first().orderCount)
        assertEquals(15255.0, report.days.first().total, 0.01)
    }

    @Test
    fun `ordenes de dias distintos generan registros separados`() {
        val orders = listOf(
            makeOrder("1", 5085.0, 1748650000000L),
            makeOrder("2", 10170.0, 1748736400000L)  // +1 dia
        )
        val report = useCase.buildMonthReport(orders)
        assertEquals(2, report.days.size)
    }

    @Test
    fun `total acumulado es la suma de todas las ordenes`() {
        val orders = listOf(
            makeOrder("1", 5085.0,  1748650000000L),
            makeOrder("2", 10170.0, 1748650000000L),
            makeOrder("3", 3000.0,  1748736400000L)
        )
        val report = useCase.buildMonthReport(orders)
        assertEquals(18255.0, report.totalAmount, 0.01)
        assertEquals(3, report.totalOrders)
    }

    @Test
    fun `currentMonthRange retorna from menor que to`() {
        val (from, to) = useCase.currentMonthRange()
        assertTrue(from < to)
    }

    @Test
    fun `dias se ordenan del mas reciente al mas antiguo`() {
        val orders = listOf(
            makeOrder("1", 5085.0, 1748650000000L),
            makeOrder("2", 5085.0, 1748736400000L)
        )
        val report = useCase.buildMonthReport(orders)
        assertTrue(report.days.first().date >= report.days.last().date)
    }

    private fun makeOrder(id: String, total: Double, createdAt: Long) = Order(
        id = id, orderNumber = id.toInt(), clientUid = "uid",
        clientName = "Test", status = OrderStatus.ENTREGADA,
        subtotal = total / 1.13, taxes = total - (total / 1.13),
        total = total, taxRate = 0.13,
        createdAt = createdAt, updatedAt = createdAt,
        deliveryAddress = "Test", synced = true
    )
}