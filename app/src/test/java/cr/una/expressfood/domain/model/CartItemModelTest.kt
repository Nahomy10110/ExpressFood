package cr.una.expressfood.domain.model

import cr.una.expressfood.data.local.entity.CartItemEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class CartItemModelTest {

    private val fakeEntity = CartItemEntity(
        id              = "cart-1",
        clientUid       = "uid-123",
        productId       = "prod-001",
        productName     = "Hamburguesa Clásica",
        productImageUrl = "https://cloudinary.com/img.jpg",
        unitPrice       = 4500.0,
        quantity        = 2,
        addedAt         = 1748000000000L
    )

    @Test fun `toDomain mapea id`()            { assertEquals("cart-1",             fakeEntity.toDomain().id) }
    @Test fun `toDomain mapea productName`()   { assertEquals("Hamburguesa Clásica", fakeEntity.toDomain().productName) }
    @Test fun `toDomain mapea unitPrice`()     { assertEquals(4500.0,               fakeEntity.toDomain().unitPrice, 0.01) }
    @Test fun `toDomain mapea quantity`()      { assertEquals(2,                    fakeEntity.toDomain().quantity) }
    @Test fun `toDomain mapea clientUid`()     { assertEquals("uid-123",            fakeEntity.toDomain().clientUid) }

    @Test
    fun `subtotal es unitPrice por quantity`() {
        val item = fakeEntity.toDomain()
        assertEquals(9000.0, item.subtotal, 0.01)
    }

    @Test
    fun `toEntity preserva id`() {
        val item   = fakeEntity.toDomain()
        val entity = item.toEntity()
        assertEquals("cart-1", entity.id)
    }

    @Test
    fun `toEntity preserva quantity`() {
        val item   = fakeEntity.toDomain()
        val entity = item.toEntity()
        assertEquals(2, entity.quantity)
    }

    @Test
    fun `toEntity preserva unitPrice`() {
        val item   = fakeEntity.toDomain()
        val entity = item.toEntity()
        assertEquals(4500.0, entity.unitPrice, 0.01)
    }

    @Test
    fun `subtotal cambia con quantity diferente`() {
        val entity = fakeEntity.copy(quantity = 3)
        val item   = entity.toDomain()
        assertEquals(13500.0, item.subtotal, 0.01)
    }
}