package cr.una.expressfood.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val orderNumber: Int,
    val clientUid: String,
    val clientName: String,
    val status: String,          // OrderStatus.name
    val subtotal: Double,
    val taxes: Double,
    val total: Double,
    val taxRate: Double,
    val createdAt: Long,
    val updatedAt: Long,
    val deliveryAddress: String,
    val notes: String?,
    val synced: Boolean,
    val syncAttempts: Int
)