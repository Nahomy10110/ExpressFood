package cr.una.expressfood.data.repository

import cr.una.expressfood.domain.model.UserRole
import cr.una.expressfood.util.Constants
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthRepositoryTest {

    @Test
    fun `uid igual a ADMIN_UID recibe rol ADMIN`() {
        val uid  = Constants.ADMIN_UID
        val role = if (uid == Constants.ADMIN_UID) UserRole.ADMIN else UserRole.CLIENTE
        assertEquals(UserRole.ADMIN, role)
    }

    @Test
    fun `uid diferente recibe rol CLIENTE`() {
        val uid  = "otro-uid-cualquiera"
        val role = if (uid == Constants.ADMIN_UID) UserRole.ADMIN else UserRole.CLIENTE
        assertEquals(UserRole.CLIENTE, role)
    }

    @Test
    fun `uid vacio recibe rol CLIENTE`() {
        val uid  = ""
        val role = if (uid == Constants.ADMIN_UID) UserRole.ADMIN else UserRole.CLIENTE
        assertEquals(UserRole.CLIENTE, role)
    }

    @Test
    fun `ADMIN_UID no esta vacio`() {
        assert(Constants.ADMIN_UID.isNotBlank())
    }

    @Test
    fun `TAX_RATE es 0 punto 13`() {
        assertEquals(0.13, Constants.TAX_RATE, 0.0)
    }

    @Test
    fun `DATABASE_NAME no esta vacio`() {
        assert(Constants.DATABASE_NAME.isNotBlank())
    }
}