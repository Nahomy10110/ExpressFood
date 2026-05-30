package cr.una.expressfood.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateTotalUseCaseTest {

    private val useCase = CalculateTotalUseCase()

    @Test
    fun `calcula impuestos del 13 por ciento sobre subtotal`() {
        val result = useCase.invoke(subtotal = 17000.0, taxRate = 0.13)
        assertEquals(2210.0,  result.taxes,   0.01)
        assertEquals(19210.0, result.total,   0.01)
        assertEquals(17000.0, result.subtotal, 0.01)
    }

    @Test
    fun `subtotal cero da total cero`() {
        val result = useCase.invoke(subtotal = 0.0)
        assertEquals(0.0, result.total, 0.0)
        assertEquals(0.0, result.taxes, 0.0)
    }

    @Test
    fun `calcula desde lista de items`() {
        val items = listOf(
            Pair(4500.0, 2),   // Hamburguesa x2 = 9000
            Pair(6500.0, 1),   // Pizza x1       = 6500
            Pair(1500.0, 1)    // Bebida x1       = 1500
        )
        val result = useCase.invoke(items)
        assertEquals(17000.0, result.subtotal, 0.01)
        assertEquals(2210.0,  result.taxes,    0.01)
        assertEquals(19210.0, result.total,    0.01)
    }

    @Test
    fun `tasa personalizada se aplica correctamente`() {
        val result = useCase.invoke(subtotal = 10000.0, taxRate = 0.10)
        assertEquals(1000.0,  result.taxes, 0.01)
        assertEquals(11000.0, result.total, 0.01)
    }

    @Test
    fun `taxRate se preserva en el resultado`() {
        val result = useCase.invoke(subtotal = 5000.0, taxRate = 0.13)
        assertEquals(0.13, result.taxRate, 0.0)
    }
}