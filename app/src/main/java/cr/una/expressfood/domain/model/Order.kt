package cr.una.expressfood.domain.model

import cr.una.expressfood.data.local.entity.OrderEntity
import cr.una.expressfood.data.local.entity.OrderItemEntity
import cr.una.expressfood.util.Constants

data class Order(
    val id: String,
    val orderNumber: Int,
    val clientUid: String,
    val clientName: String,
    val status: OrderStatus,
    val subtotal: Double,
    val taxes: Double,
    val total: Double,
    val taxRate: Double = Constants.TAX_RATE,
    val createdAt: Long,
    val updatedAt: Long,
    val deliveryAddress: String,
    val notes: String? = null,
    val items: List<OrderItem> = emptyList(),
    val synced: Boolean = false,
    val syncAttempts: Int = 0
)

enum class OrderStatus {
    CREADA, PENDIENTE, EN_CAMINO, ENTREGADA, CANCELADA;

    // Transiciones válidas,  el admin solo puede avanzar en la máquina de estados
    fun nextStatuses(): List<OrderStatus> = when (this) {
        CREADA    -> listOf(PENDIENTE, CANCELADA)
        PENDIENTE -> listOf(EN_CAMINO, CANCELADA)
        EN_CAMINO -> listOf(ENTREGADA, CANCELADA)
        ENTREGADA -> emptyList()
        CANCELADA -> emptyList()
    }
}

data class OrderItem(
    val id: String,
    val orderId: String,
    val productId: String,
    val productName: String,
    val productImageUrl: String,
    val unitPrice: Double,
    val quantity: Int,
    val subtotal: Double
)

// Mapeos Order

fun OrderEntity.toDomain(items: List<OrderItem> = emptyList()): Order = Order(
    id              = id,
    orderNumber     = orderNumber,
    clientUid       = clientUid,
    clientName      = clientName,
    status          = runCatching { OrderStatus.valueOf(status) }.getOrDefault(OrderStatus.CREADA),
    subtotal        = subtotal,
    taxes           = taxes,
    total           = total,
    taxRate         = taxRate,
    createdAt       = createdAt,
    updatedAt       = updatedAt,
    deliveryAddress = deliveryAddress,
    notes           = notes,
    items           = items,
    synced          = synced,
    syncAttempts    = syncAttempts
)

fun Order.toEntity(synced: Boolean = this.synced): OrderEntity = OrderEntity(
    id              = id,
    orderNumber     = orderNumber,
    clientUid       = clientUid,
    clientName      = clientName,
    status          = status.name,
    subtotal        = subtotal,
    taxes           = taxes,
    total           = total,
    taxRate         = taxRate,
    createdAt       = createdAt,
    updatedAt       = updatedAt,
    deliveryAddress = deliveryAddress,
    notes           = notes,
    synced          = synced,
    syncAttempts    = syncAttempts
)

// Mapeos OrderItem

fun OrderItemEntity.toDomain(): OrderItem = OrderItem(
    id             = id,
    orderId        = orderId,
    productId      = productId,
    productName    = productName,
    productImageUrl = productImageUrl,
    unitPrice      = unitPrice,
    quantity       = quantity,
    subtotal       = subtotal
)

fun OrderItem.toEntity(): OrderItemEntity = OrderItemEntity(
    id             = id,
    orderId        = orderId,
    productId      = productId,
    productName    = productName,
    productImageUrl = productImageUrl,
    unitPrice      = unitPrice,
    quantity       = quantity,
    subtotal       = subtotal
)