package cr.una.expressfood.data.sync

import cr.una.expressfood.domain.model.Order
import cr.una.expressfood.domain.model.OrderStatus
import cr.una.expressfood.util.Constants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncTest {

    @Test
    fun `orden recien creada tiene synced false`() {
        val order = makeOrder(synced = false)
        assertFalse(order.synced)
    }

    @Test
    fun `orden sincronizada tiene synced true`() {
        val order = makeOrder(synced = true)
        assertTrue(order.synced)
    }

    @Test
    fun `SYNC_WORK_NAME no esta vacio`() {
        assertTrue(Constants.SYNC_WORK_NAME.isNotBlank())
    }

    @Test
    fun `orden con syncAttempts 0 es primer intento`() {
        val order = makeOrder()
        assertEquals(0, order.syncAttempts)
    }

    @Test
    fun `orden pendiente de sync tiene status PENDIENTE`() {
        val order = makeOrder(status = OrderStatus.PENDIENTE, synced = false)
        assertEquals(OrderStatus.PENDIENTE, order.status)
        assertFalse(order.synced)
    }

    @Test
    fun `marcar orden como sincronizada cambia synced a true`() {
        val order   = makeOrder(synced = false)
        val synced  = order.copy(synced = true)
        assertTrue(synced.synced)
    }

    @Test
    fun `filtrar ordenes no sincronizadas`() {
        val orders = listOf(
            makeOrder(id = "1", synced = false),
            makeOrder(id = "2", synced = true),
            makeOrder(id = "3", synced = false)
        )
        val pending = orders.filter { !it.synced }
        assertEquals(2, pending.size)
    }

    @Test
    fun `sin ordenes pendientes la lista de sync esta vacia`() {
        val orders  = listOf(makeOrder(synced = true), makeOrder(synced = true))
        val pending = orders.filter { !it.synced }
        assertTrue(pending.isEmpty())
    }

    private fun makeOrder(
        id: String        = "order-1",
        synced: Boolean   = false,
        syncAttempts: Int = 0,
        status: OrderStatus = OrderStatus.PENDIENTE
    ) = Order(
        id = id, orderNumber = 1, clientUid = "uid",
        clientName = "Test", status = status,
        subtotal = 9000.0, taxes = 1170.0, total = 10170.0,
        taxRate = 0.13, createdAt = 0L, updatedAt = 0L,
        deliveryAddress = "Test", synced = synced,
        syncAttempts = syncAttempts
    )
}