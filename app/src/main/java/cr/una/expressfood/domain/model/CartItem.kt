package cr.una.expressfood.domain.model

import cr.una.expressfood.data.local.entity.CartItemEntity

data class CartItem(
    val id: String,
    val clientUid: String,
    val productId: String,
    val productName: String,
    val productImageUrl: String,
    val unitPrice: Double,
    val quantity: Int,
    val addedAt: Long,
    val subtotal: Double = unitPrice * quantity
)

// Mapeos

fun CartItemEntity.toDomain(): CartItem = CartItem(
    id             = id,
    clientUid      = clientUid,
    productId      = productId,
    productName    = productName,
    productImageUrl = productImageUrl,
    unitPrice      = unitPrice,
    quantity       = quantity,
    addedAt        = addedAt,
    subtotal       = unitPrice * quantity
)

fun CartItem.toEntity(): CartItemEntity = CartItemEntity(
    id             = id,
    clientUid      = clientUid,
    productId      = productId,
    productName    = productName,
    productImageUrl = productImageUrl,
    unitPrice      = unitPrice,
    quantity       = quantity,
    addedAt        = addedAt
)