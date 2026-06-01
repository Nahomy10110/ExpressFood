package cr.una.expressfood.domain.model

import cr.una.expressfood.data.local.entity.UserEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UserModelTest {

    private val fakeEntity = UserEntity(
        uid         = "uid-123",
        email       = "test@gmail.com",
        displayName = "Test User",
        photoUrl    = null,
        role        = "CLIENTE",
        phone       = null,
        address     = null,
        createdAt   = 1748000000000L,
        lastLoginAt = 1748000000000L
    )

    @Test fun `toDomain mapea uid`()         { assertEquals("uid-123",       fakeEntity.toDomain().uid) }
    @Test fun `toDomain mapea email`()        { assertEquals("test@gmail.com", fakeEntity.toDomain().email) }
    @Test fun `toDomain mapea displayName`()  { assertEquals("Test User",      fakeEntity.toDomain().displayName) }
    @Test fun `toDomain mapea rol CLIENTE`()  { assertEquals(UserRole.CLIENTE,  fakeEntity.toDomain().role) }
    @Test fun `toDomain photoUrl es null`()   { assertNull(fakeEntity.toDomain().photoUrl) }
    @Test fun `toDomain createdAt correcto`() { assertEquals(1748000000000L,   fakeEntity.toDomain().createdAt) }

    @Test
    fun `toDomain rol invalido cae a CLIENTE`() {
        val entity = fakeEntity.copy(role = "ROL_INVALIDO")
        assertEquals(UserRole.CLIENTE, entity.toDomain().role)
    }

    @Test
    fun `toEntity preserva uid`() {
        val user   = fakeEntity.toDomain()
        val entity = user.toEntity()
        assertEquals("uid-123", entity.uid)
    }

    @Test
    fun `toEntity preserva role como string`() {
        val user   = fakeEntity.toDomain()
        val entity = user.toEntity()
        assertEquals("CLIENTE", entity.role)
    }

    @Test
    fun `toEntity preserva email`() {
        val user   = fakeEntity.toDomain()
        val entity = user.toEntity()
        assertEquals("test@gmail.com", entity.email)
    }

    @Test
    fun `UserRole ADMIN name es ADMIN`() {
        assertEquals("ADMIN", UserRole.ADMIN.name)
    }

    @Test
    fun `UserRole CLIENTE name es CLIENTE`() {
        assertEquals("CLIENTE", UserRole.CLIENTE.name)
    }
}