package cr.una.expressfood.data.repository

import cr.una.expressfood.data.local.dao.CartItemDao
import cr.una.expressfood.data.local.dao.OrderDao
import cr.una.expressfood.data.local.dao.OrderItemDao
import cr.una.expressfood.data.local.entity.CartItemEntity
import cr.una.expressfood.data.local.entity.OrderEntity
import cr.una.expressfood.data.local.entity.OrderItemEntity
import cr.una.expressfood.domain.model.CartItem
import cr.una.expressfood.domain.model.OrderStatus
import cr.una.expressfood.domain.model.Product
import cr.una.expressfood.domain.model.toDomain
import cr.una.expressfood.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class CartRepository(
    private val cartItemDao: CartItemDao,
    private val orderDao: OrderDao,
    private val orderItemDao: OrderItemDao
) {

    fun observeCart(clientUid: String): Flow<List<CartItem>> =
        cartItemDao.observeByClient(clientUid).map { list -> list.map { it.toDomain() } }

    fun observeItemCount(clientUid: String): Flow<Int> =
        cartItemDao.observeItemCount(clientUid)

    /**
     * Agrega un producto al carrito;si ya existe incrementa la cantidad en 1.
     */
    suspend fun addProduct(clientUid: String, product: Product) {
        val existing = cartItemDao.getItem(clientUid, product.id)
        if (existing != null) {
            cartItemDao.updateQuantity(existing.id, existing.quantity + 1)
        } else {
            cartItemDao.upsert(
                CartItemEntity(
                    id             = UUID.randomUUID().toString(),
                    clientUid      = clientUid,
                    productId      = product.id,
                    productName    = product.name,
                    productImageUrl = product.imageUrl,
                    unitPrice      = product.price,
                    quantity       = 1,
                    addedAt        = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun increaseQuantity(item: CartItem) {
        cartItemDao.updateQuantity(item.id, item.quantity + 1)
    }

    suspend fun decreaseQuantity(item: CartItem) {
        if (item.quantity <= 1) {
            cartItemDao.deleteById(item.id)
        } else {
            cartItemDao.updateQuantity(item.id, item.quantity - 1)
        }
    }

    suspend fun removeItem(item: CartItem) {
        cartItemDao.deleteById(item.id)
    }

    suspend fun clearCart(clientUid: String) {
        cartItemDao.clearCart(clientUid)
    }

    /**
     * Convierte el carrito en una Order con synced=false.
     * Offline-first: la orden se guarda en Room y se sube a Firestore después.
     */
    suspend fun processOrder(
        clientUid: String,
        clientName: String,
        deliveryAddress: String,
        notes: String?,
        items: List<CartItem>
    ): String {
        val orderId    = UUID.randomUUID().toString()
        val now        = System.currentTimeMillis()
        val subtotal   = items.sumOf { it.subtotal }
        val taxes      = subtotal * Constants.TAX_RATE
        val total      = subtotal + taxes

        // Número de orden correlativo basado en timestamp
        val orderNumber = (now % 10000).toInt()

        val order = OrderEntity(
            id              = orderId,
            orderNumber     = orderNumber,
            clientUid       = clientUid,
            clientName      = clientName,
            status          = OrderStatus.PENDIENTE.name,
            subtotal        = subtotal,
            taxes           = taxes,
            total           = total,
            taxRate         = Constants.TAX_RATE,
            createdAt       = now,
            updatedAt       = now,
            deliveryAddress = deliveryAddress,
            notes           = notes,
            synced          = false,   // se sube a Firestore después
            syncAttempts    = 0
        )

        orderDao.upsert(order)

        // Guardar los ítems de la orden
        items.forEach { cartItem ->
            orderItemDao.upsert(
                OrderItemEntity(
                    id             = UUID.randomUUID().toString(),
                    orderId        = orderId,
                    productId      = cartItem.productId,
                    productName    = cartItem.productName,
                    productImageUrl = cartItem.productImageUrl,
                    unitPrice      = cartItem.unitPrice,
                    quantity       = cartItem.quantity,
                    subtotal       = cartItem.subtotal
                )
            )
        }

        // Vaciar el carrito después de procesar
        cartItemDao.clearCart(clientUid)

        return orderId
    }

    companion object {
        fun default(
            cartItemDao: CartItemDao,
            orderDao: OrderDao,
            orderItemDao: OrderItemDao
        ) = CartRepository(cartItemDao, orderDao, orderItemDao)
    }
}