package cr.una.expressfood.ui.admin.orders

import cr.una.expressfood.domain.model.Order
import cr.una.expressfood.domain.model.OrderStatus
import cr.una.expressfood.domain.model.toLabel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdminOrdersViewModelTest {

    @Test
    fun `nextStatuses de CREADA incluye PENDIENTE y CANCELADA`() {
        val next = OrderStatus.CREADA.nextStatuses()
        assertTrue(next.contains(OrderStatus.PENDIENTE))
        assertTrue(next.contains(OrderStatus.CANCELADA))
    }

    @Test
    fun `nextStatuses de PENDIENTE incluye EN_CAMINO`() {
        val next = OrderStatus.PENDIENTE.nextStatuses()
        assertTrue(next.contains(OrderStatus.EN_CAMINO))
    }

    @Test
    fun `nextStatuses de EN_CAMINO incluye ENTREGADA`() {
        val next = OrderStatus.EN_CAMINO.nextStatuses()
        assertTrue(next.contains(OrderStatus.ENTREGADA))
    }

    @Test
    fun `nextStatuses de ENTREGADA esta vacio — orden finalizada`() {
        assertTrue(OrderStatus.ENTREGADA.nextStatuses().isEmpty())
    }

    @Test
    fun `nextStatuses de CANCELADA esta vacio — orden finalizada`() {
        assertTrue(OrderStatus.CANCELADA.nextStatuses().isEmpty())
    }

    @Test
    fun `toLabel retorna texto legible en espanol`() {
        assertEquals("En camino", OrderStatus.EN_CAMINO.toLabel())
        assertEquals("Pendiente", OrderStatus.PENDIENTE.toLabel())
    }

    @Test
    fun `filtrar ordenes por estado EN_CAMINO`() {
        val orders = listOf(
            makeOrder("1", OrderStatus.PENDIENTE),
            makeOrder("2", OrderStatus.EN_CAMINO),
            makeOrder("3", OrderStatus.EN_CAMINO),
            makeOrder("4", OrderStatus.ENTREGADA)
        )
        val enCamino = orders.filter { it.status == OrderStatus.EN_CAMINO }
        assertEquals(2, enCamino.size)
    }

    @Test
    fun `ordenes con estado final no permiten cambio`() {
        val entregada = makeOrder("1", OrderStatus.ENTREGADA)
        val cancelada = makeOrder("2", OrderStatus.CANCELADA)
        assertTrue(entregada.status.nextStatuses().isEmpty())
        assertTrue(cancelada.status.nextStatuses().isEmpty())
    }

    @Test
    fun `flujo completo de estados es valido`() {
        var status = OrderStatus.CREADA
        status = status.nextStatuses().first()
        assertEquals(OrderStatus.PENDIENTE, status)
        status = status.nextStatuses().first()
        assertEquals(OrderStatus.EN_CAMINO, status)
        status = status.nextStatuses().first()
        assertEquals(OrderStatus.ENTREGADA, status)
        assertTrue(status.nextStatuses().isEmpty())
    }

    private fun makeOrder(id: String, status: OrderStatus) = Order(
        id = id, orderNumber = id.toInt(), clientUid = "uid",
        clientName = "Cliente Test", status = status,
        subtotal = 9000.0, taxes = 1170.0, total = 10170.0,
        taxRate = 0.13, createdAt = 0L, updatedAt = 0L,
        deliveryAddress = "Dirección test", synced = false
    )
}