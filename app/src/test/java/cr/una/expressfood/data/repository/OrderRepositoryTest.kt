package cr.una.expressfood.data.repository

import cr.una.expressfood.data.local.entity.OrderEntity
import cr.una.expressfood.domain.model.OrderStatus
import cr.una.expressfood.domain.model.toDomain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderRepositoryTest {

    private val fakeEntity = OrderEntity(
        id              = "order-1",
        orderNumber     = 1042,
        clientUid       = "uid-123",
        clientName      = "María González",
        status          = OrderStatus.PENDIENTE.name,
        subtotal        = 17000.0,
        taxes           = 2210.0,
        total           = 19210.0,
        taxRate         = 0.13,
        createdAt       = 1748650000000L,
        updatedAt       = 1748650000000L,
        deliveryAddress = "San José, Costa Rica",
        notes           = null,
        synced          = false,
        syncAttempts    = 0
    )

    @Test
    fun `toDomain mapea id correctamente`() {
        val order = fakeEntity.toDomain()
        assertEquals("order-1", order.id)
    }

    @Test
    fun `toDomain mapea status PENDIENTE correctamente`() {
        val order = fakeEntity.toDomain()
        assertEquals(OrderStatus.PENDIENTE, order.status)
    }

    @Test
    fun `toDomain mapea total correctamente`() {
        val order = fakeEntity.toDomain()
        assertEquals(19210.0, order.total, 0.01)
    }

    @Test
    fun `toDomain mapea synced false correctamente`() {
        val order = fakeEntity.toDomain()
        assertEquals(false, order.synced)
    }

    @Test
    fun `toDomain con status invalido cae a CREADA`() {
        val entity = fakeEntity.copy(status = "STATUS_INVALIDO")
        val order  = entity.toDomain()
        assertEquals(OrderStatus.CREADA, order.status)
    }

    @Test
    fun `toDomain preserva clientUid`() {
        val order = fakeEntity.toDomain()
        assertEquals("uid-123", order.clientUid)
    }

    @Test
    fun `toDomain preserva clientName`() {
        val order = fakeEntity.toDomain()
        assertEquals("María González", order.clientName)
    }

    @Test
    fun `toDomain preserva taxRate`() {
        val order = fakeEntity.toDomain()
        assertEquals(0.13, order.taxRate, 0.0)
    }

    @Test
    fun `toDomain preserva deliveryAddress`() {
        val order = fakeEntity.toDomain()
        assertEquals("San José, Costa Rica", order.deliveryAddress)
    }

    @Test
    fun `toDomain preserva orderNumber`() {
        val order = fakeEntity.toDomain()
        assertEquals(1042, order.orderNumber)
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
    fun `orden con synced false indica pendiente de sincronizar`() {
        val order = fakeEntity.toDomain()
        assertTrue(!order.synced)
    }

    @Test
    fun `subtotal mas taxes igual a total`() {
        val order = fakeEntity.toDomain()
        assertEquals(order.total, order.subtotal + order.taxes, 0.01)
    }
}