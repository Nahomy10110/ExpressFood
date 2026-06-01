package cr.una.expressfood.domain.model

import cr.una.expressfood.data.local.entity.ProductEntity

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val ingredients: List<String>,
    val price: Double,
    val imageUrl: String,
    val estimatedTimeMinutes: Int,
    val category: String,
    val available: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

//Mapeos

fun ProductEntity.toDomain(): Product = Product(
    id                   = id,
    name                 = name,
    description          = description,
    ingredients          = if (ingredients.isBlank()) emptyList()
    else ingredients.split("||"),
    price                = price,
    imageUrl             = imageUrl,
    estimatedTimeMinutes = estimatedTimeMinutes,
    category             = category,
    available            = available,
    createdAt            = createdAt,
    updatedAt            = updatedAt
)

fun Product.toEntity(): ProductEntity = ProductEntity(
    id                   = id,
    name                 = name,
    description          = description,
    ingredients          = ingredients.joinToString("||"),
    price                = price,
    imageUrl             = imageUrl,
    estimatedTimeMinutes = estimatedTimeMinutes,
    category             = category,
    available            = available,
    createdAt            = createdAt,
    updatedAt            = updatedAt
)