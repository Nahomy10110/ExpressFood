package cr.una.expressfood.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val email: String,
    val displayName: String,
    val photoUrl: String?,
    val role: String,        // "CLIENTE" o "ADMIN"
    val phone: String?,
    val address: String?,
    val createdAt: Long,
    val lastLoginAt: Long
)