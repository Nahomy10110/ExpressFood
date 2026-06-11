package cr.una.expressfood.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import cr.una.expressfood.data.local.dao.OrderDao
import cr.una.expressfood.data.local.dao.OrderItemDao
import cr.una.expressfood.data.local.entity.OrderEntity
import cr.una.expressfood.domain.model.Order
import cr.una.expressfood.domain.model.OrderStatus
import cr.una.expressfood.domain.model.toDomain
import cr.una.expressfood.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OrderRepository(
    private val orderDao: OrderDao,
    private val orderItemDao: OrderItemDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun observeClientOrders(uid: String): Flow<List<Order>> =
        orderDao.observeByClient(uid).map { entities ->
            entities.map { entity ->
                val items = orderItemDao.getByOrder(entity.id).map { it.toDomain() }
                entity.toDomain(items)
            }
        }

    fun observeAllOrders(): Flow<List<Order>> =
        orderDao.observeAll().map { entities ->
            entities.map { entity ->
                val items = orderItemDao.getByOrder(entity.id).map { it.toDomain() }
                entity.toDomain(items)
            }
        }

    fun listenToAllOrdersFromFirestore() {
        firestore.collection(Constants.Firestore.ORDERS)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                CoroutineScope(Dispatchers.IO).launch {
                    snapshot.documents.forEach { doc ->
                        runCatching {
                            val entity = OrderEntity(
                                id              = doc.getString("id") ?: doc.id,
                                orderNumber     = (doc.getLong("orderNumber") ?: 0L).toInt(),
                                clientUid       = doc.getString("clientUid") ?: "",
                                clientName      = doc.getString("clientName") ?: "",
                                status          = doc.getString("status") ?: "PENDIENTE",
                                subtotal        = doc.getDouble("subtotal") ?: 0.0,
                                taxes           = doc.getDouble("taxes") ?: 0.0,
                                total           = doc.getDouble("total") ?: 0.0,
                                taxRate         = doc.getDouble("taxRate") ?: 0.13,
                                createdAt       = doc.getLong("createdAt") ?: 0L,
                                updatedAt       = doc.getLong("updatedAt") ?: 0L,
                                deliveryAddress = doc.getString("deliveryAddress") ?: "",
                                notes           = doc.getString("notes"),
                                synced          = true,
                                syncAttempts    = 0
                            )
                            orderDao.upsert(entity)

                            // Guardar también los ítems de la orden
                            @Suppress("UNCHECKED_CAST")
                            val itemsList = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
                            itemsList.forEach { itemMap ->
                                runCatching {
                                    val itemEntity = cr.una.expressfood.data.local.entity.OrderItemEntity(
                                        id              = itemMap["id"] as? String ?: "",
                                        orderId         = entity.id,
                                        productId       = itemMap["productId"] as? String ?: "",
                                        productName     = itemMap["productName"] as? String ?: "",
                                        productImageUrl = itemMap["productImageUrl"] as? String ?: "",
                                        unitPrice       = (itemMap["unitPrice"] as? Number)?.toDouble() ?: 0.0,
                                        quantity        = (itemMap["quantity"] as? Number)?.toInt() ?: 1,
                                        subtotal        = (itemMap["subtotal"] as? Number)?.toDouble() ?: 0.0
                                    )
                                    orderItemDao.upsert(itemEntity)
                                }
                            }
                        }
                    }
                }
            }
    }

    suspend fun fetchOrderItemsFromFirestore(orderId: String) {
        runCatching {
            val doc = firestore.collection(Constants.Firestore.ORDERS)
                .document(orderId)
                .get()
                .await()

            @Suppress("UNCHECKED_CAST")
            val itemsList = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
            itemsList.forEach { itemMap ->
                runCatching {
                    val itemEntity = cr.una.expressfood.data.local.entity.OrderItemEntity(
                        id              = itemMap["id"] as? String
                            ?: java.util.UUID.randomUUID().toString(),
                        orderId         = orderId,
                        productId       = itemMap["productId"] as? String ?: "",
                        productName     = itemMap["productName"] as? String ?: "",
                        productImageUrl = itemMap["productImageUrl"] as? String ?: "",
                        unitPrice       = (itemMap["unitPrice"] as? Number)?.toDouble() ?: 0.0,
                        quantity        = (itemMap["quantity"] as? Number)?.toInt() ?: 1,
                        subtotal        = (itemMap["subtotal"] as? Number)?.toDouble() ?: 0.0
                    )
                    orderItemDao.upsert(itemEntity)
                }
            }
        }
    }

    suspend fun updateStatus(orderId: String, newStatus: OrderStatus) {
        val now = System.currentTimeMillis()
        orderDao.updateStatus(orderId, newStatus.name, now)
        runCatching {
            firestore.collection(Constants.Firestore.ORDERS)
                .document(orderId)
                .update(mapOf("status" to newStatus.name, "updatedAt" to now))
                .await()
        }
    }

    suspend fun syncPending(): Result<Int> = runCatching {
        val pending = orderDao.getUnsynced()
        var count   = 0
        pending.forEach { entity ->
            val items = orderItemDao.getByOrder(entity.id).map { it.toDomain() }
            val order = entity.toDomain(items)
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
                orderDao.markSynced(entity.id, System.currentTimeMillis())
                count++
            }
        }
        count
    }

    suspend fun getClientOrdersByDateRange(uid: String, from: Long, to: Long): List<Order> =
        orderDao.getByClientAndDateRange(uid, from, to).map { entity ->
            val items = orderItemDao.getByOrder(entity.id).map { it.toDomain() }
            entity.toDomain(items)
        }

    suspend fun getAllOrdersByDateRange(from: Long, to: Long): List<Order> =
        orderDao.getByDateRange(from, to).map { entity ->
            val items = orderItemDao.getByOrder(entity.id).map { it.toDomain() }
            entity.toDomain(items)
        }

    companion object {
        fun default(orderDao: OrderDao, orderItemDao: OrderItemDao) =
            OrderRepository(orderDao, orderItemDao)
    }
}