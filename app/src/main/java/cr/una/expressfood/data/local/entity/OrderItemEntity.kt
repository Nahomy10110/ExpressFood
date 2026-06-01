package cr.una.expressfood.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE   // si se borra la orden, se borran sus ítems
        )
    ],
    indices = [Index("orderId")]
)
data class OrderItemEntity(
    @PrimaryKey val id: String,
    val orderId: String,
    val productId: String,
    val productName: String,       // denormalizado — preserva el nombre aunque cambie
    val productImageUrl: String,   // denormalizado
    val unitPrice: Double,         // precio al momento de compra, no el actual
    val quantity: Int,
    val subtotal: Double           // unitPrice × quantity
)