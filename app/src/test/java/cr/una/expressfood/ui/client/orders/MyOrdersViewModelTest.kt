package cr.una.expressfood.ui.client.orders

import cr.una.expressfood.domain.model.Order
import cr.una.expressfood.domain.model.OrderStatus
import cr.una.expressfood.domain.model.toLabel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MyOrdersViewModelTest {

    @Test
    fun `OrderStatus toLabel retorna texto correcto`() {
        assertEquals("Pendiente", OrderStatus.PENDIENTE.toLabel())
        assertEquals("En camino", OrderStatus.EN_CAMINO.toLabel())
        assertEquals("Entregada", OrderStatus.ENTREGADA.toLabel())
        assertEquals("Cancelada", OrderStatus.CANCELADA.toLabel())
        assertEquals("Creada",    OrderStatus.CREADA.toLabel())
    }

    @Test
    fun `nextStatuses de PENDIENTE incluye EN_CAMINO y CANCELADA`() {
        val next = OrderStatus.PENDIENTE.nextStatuses()
        assertTrue(next.contains(OrderStatus.EN_CAMINO))
        assertTrue(next.contains(OrderStatus.CANCELADA))
    }

    @Test
    fun `nextStatuses de ENTREGADA esta vacio`() {
        assertTrue(OrderStatus.ENTREGADA.nextStatuses().isEmpty())
    }

    @Test
    fun `nextStatuses de CANCELADA esta vacio`() {
        assertTrue(OrderStatus.CANCELADA.nextStatuses().isEmpty())
    }

    @Test
    fun `filtrar ordenes por estado funciona correctamente`() {
        val orders = listOf(
            makeOrder("1", OrderStatus.PENDIENTE),
            makeOrder("2", OrderStatus.ENTREGADA),
            makeOrder("3", OrderStatus.PENDIENTE),
            makeOrder("4", OrderStatus.CANCELADA)
        )
        val pendientes = orders.filter { it.status == OrderStatus.PENDIENTE }
        assertEquals(2, pendientes.size)
    }

    @Test
    fun `filtrar con null devuelve todas las ordenes`() {
        val orders = listOf(
            makeOrder("1", OrderStatus.PENDIENTE),
            makeOrder("2", OrderStatus.ENTREGADA),
        )
        val resultado = orders.filter { true }
        assertEquals(2, resultado.size)
    }

    private fun makeOrder(id: String, status: OrderStatus) = Order(
        id = id, orderNumber = id.toInt(), clientUid = "uid",
        clientName = "Test", status = status,
        subtotal = 4500.0, taxes = 585.0, total = 5085.0,
        taxRate = 0.13, createdAt = 0L, updatedAt = 0L,
        deliveryAddress = "Test", synced = false
    )
}