package cr.una.expressfood.domain.model

import cr.una.expressfood.data.local.entity.UserEntity

data class User(
    val uid: String,
    val email: String,
    val displayName: String,
    val photoUrl: String?,
    val role: UserRole,
    val phone: String? = null,
    val address: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
)

enum class UserRole { CLIENTE, ADMIN }

// Mapeos entre capas

fun UserEntity.toDomain(): User = User(
    uid         = uid,
    email       = email,
    displayName = displayName,
    photoUrl    = photoUrl,
    role        = runCatching { UserRole.valueOf(role) }.getOrDefault(UserRole.CLIENTE),
    phone       = phone,
    address     = address,
    createdAt   = createdAt,
    lastLoginAt = lastLoginAt
)

fun User.toEntity(): UserEntity = UserEntity(
    uid         = uid,
    email       = email,
    displayName = displayName,
    photoUrl    = photoUrl,
    role        = role.name,
    phone       = phone,
    address     = address,
    createdAt   = createdAt,
    lastLoginAt = lastLoginAt
)