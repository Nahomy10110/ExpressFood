package cr.una.expressfood.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import cr.una.expressfood.data.local.dao.OrderDao
import cr.una.expressfood.data.local.dao.OrderItemDao
import cr.una.expressfood.data.local.entity.OrderItemEntity
import cr.una.expressfood.domain.model.Order
import cr.una.expressfood.domain.model.OrderItem
import cr.una.expressfood.domain.model.OrderStatus
import cr.una.expressfood.domain.model.toDomain
import cr.una.expressfood.domain.model.toEntity
import cr.una.expressfood.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class OrderRepository(
    private val orderDao: OrderDao,
    private val orderItemDao: OrderItemDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // Cliente: observa sus propias órdenes con sus ítems
    fun observeClientOrders(uid: String): Flow<List<Order>> =
        orderDao.observeByClient(uid).map { entities ->
            entities.map { entity ->
                val items = orderItemDao.getByOrder(entity.id).map { it.toDomain() }
                entity.toDomain(items)
            }
        }

    // Admin: observa todas las órdenes
    fun observeAllOrders(): Flow<List<Order>> =
        orderDao.observeAll().map { entities ->
            entities.map { entity ->
                val items = orderItemDao.getByOrder(entity.id).map { it.toDomain() }
                entity.toDomain(items)
            }
        }

    // Admin: cambiar estado de una orden
    suspend fun updateStatus(orderId: String, newStatus: OrderStatus) {
        val now = System.currentTimeMillis()
        // Actualizar en Room primero (offline-first)
        orderDao.updateStatus(orderId, newStatus.name, now)
        // Intentar sincronizar a Firestore silenciosamente
        syncStatusToFirestore(orderId, newStatus, now)
    }

    // Subir órdenes pendientes a Firestore (llamado por SyncWorker)
    suspend fun syncPending(): Result<Int> = runCatching {
        val pending = orderDao.getUnsynced()
        var count = 0
        pending.forEach { entity ->
            val items = orderItemDao.getByOrder(entity.id).map { it.toDomain() }
            val order = entity.toDomain(items)
            uploadOrderToFirestore(order)
            orderDao.markSynced(entity.id, System.currentTimeMillis())
            count++
        }
        count
    }

    // Reporte: órdenes de un cliente por rango de fecha
    suspend fun getClientOrdersByDateRange(
        uid: String, from: Long, to: Long
    ): List<Order> =
        orderDao.getByClientAndDateRange(uid, from, to).map { entity ->
            val items = orderItemDao.getByOrder(entity.id).map { it.toDomain() }
            entity.toDomain(items)
        }

    // Reporte: todas las órdenes por rango de fecha (admin)
    suspend fun getAllOrdersByDateRange(from: Long, to: Long): List<Order> =
        orderDao.getByDateRange(from, to).map { entity ->
            val items = orderItemDao.getByOrder(entity.id).map { it.toDomain() }
            entity.toDomain(items)
        }

    private suspend fun uploadOrderToFirestore(order: Order) {
        runCatching {
            val data = mapOf(
                "id"              to order.id,
                "orderNumber"     to order.orderNumber,
                "clientUid"       to order.clientUid,
                "clientName"      to order.clientName,
                "status"          to order.status.name,
                "subtotal"        to order.subtotal,
                "taxes"           to order.taxes,
                "total"           to order.total,
                "taxRate"         to order.taxRate,
                "createdAt"       to order.createdAt,
                "updatedAt"       to order.updatedAt,
                "deliveryAddress" to order.deliveryAddress,
                "notes"           to order.notes,
                "items"           to order.items.map { item ->
                    mapOf(
                        "id"              to item.id,
                        "productId"       to item.productId,
                        "productName"     to item.productName,
                        "productImageUrl" to item.productImageUrl,
                        "unitPrice"       to item.unitPrice,
                        "quantity"        to item.quantity,
                        "subtotal"        to item.subtotal
                    )
                }
            )
            firestore.collection(Constants.Firestore.ORDERS)
                .document(order.id)
                .set(data)
                .await()
        }
    }

    private suspend fun syncStatusToFirestore(
        orderId: String, status: OrderStatus, updatedAt: Long
    ) {
        runCatching {
            firestore.collection(Constants.Firestore.ORDERS)
                .document(orderId)
                .update(mapOf("status" to status.name, "updatedAt" to updatedAt))
                .await()
        }
    }

    companion object {
        fun default(orderDao: OrderDao, orderItemDao: OrderItemDao) =
            OrderRepository(orderDao, orderItemDao)
    }
}