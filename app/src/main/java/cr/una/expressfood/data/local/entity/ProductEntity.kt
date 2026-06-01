package cr.una.expressfood.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val ingredients: String,   // List<String> serializada con Converters.kt ("||")
    val price: Double,
    val imageUrl: String,
    val estimatedTimeMinutes: Int,
    val category: String,
    val available: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)