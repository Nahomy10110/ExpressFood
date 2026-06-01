package cr.una.expressfood.data.repository

import cr.una.expressfood.data.local.entity.ProductEntity
import cr.una.expressfood.domain.model.toDomain
import cr.una.expressfood.domain.model.toEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductRepositoryTest {

    private val fakeEntity = ProductEntity(
        id                   = "prod-001",
        name                 = "Hamburguesa Clásica",
        description          = "Jugosa hamburguesa",
        ingredients          = "lechuga||tomate||queso",
        price                = 4500.0,
        imageUrl             = "https://cloudinary.com/img.jpg",
        estimatedTimeMinutes = 25,
        category             = "HAMBURGUESA",
        available            = true,
        createdAt            = 1748000000000L,
        updatedAt            = 1748000000000L
    )

    @Test
    fun `toDomain mapea nombre correctamente`() {
        val product = fakeEntity.toDomain()
        assertEquals("Hamburguesa Clásica", product.name)
    }

    @Test
    fun `toDomain mapea precio correctamente`() {
        val product = fakeEntity.toDomain()
        assertEquals(4500.0, product.price, 0.01)
    }

    @Test
    fun `toDomain deserializa ingredientes separados por pipe`() {
        val product = fakeEntity.toDomain()
        assertEquals(3, product.ingredients.size)
        assertTrue(product.ingredients.contains("lechuga"))
        assertTrue(product.ingredients.contains("tomate"))
        assertTrue(product.ingredients.contains("queso"))
    }

    @Test
    fun `toDomain con ingredientes vacios retorna lista vacia`() {
        val entity  = fakeEntity.copy(ingredients = "")
        val product = entity.toDomain()
        assertTrue(product.ingredients.isEmpty())
    }

    @Test
    fun `toEntity serializa ingredientes con pipe`() {
        val product = fakeEntity.toDomain()
        val entity  = product.toEntity()
        assertEquals("lechuga||tomate||queso", entity.ingredients)
    }

    @Test
    fun `toEntity preserva available`() {
        val product = fakeEntity.toDomain()
        val entity  = product.toEntity()
        assertEquals(true, entity.available)
    }

    @Test
    fun `producto available false se mapea correctamente`() {
        val entity  = fakeEntity.copy(available = false)
        val product = entity.toDomain()
        assertEquals(false, product.available)
    }

    @Test
    fun `toDomain preserva imageUrl`() {
        val product = fakeEntity.toDomain()
        assertEquals("https://cloudinary.com/img.jpg", product.imageUrl)
    }

    @Test
    fun `toDomain preserva estimatedTimeMinutes`() {
        val product = fakeEntity.toDomain()
        assertEquals(25, product.estimatedTimeMinutes)
    }

    @Test
    fun `roundtrip entity to domain to entity preserva todos los campos`() {
        val product = fakeEntity.toDomain()
        val entity  = product.toEntity()
        assertEquals(fakeEntity.id,       entity.id)
        assertEquals(fakeEntity.name,     entity.name)
        assertEquals(fakeEntity.price,    entity.price, 0.01)
        assertEquals(fakeEntity.category, entity.category)
    }
}