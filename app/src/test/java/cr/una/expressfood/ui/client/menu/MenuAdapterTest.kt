package cr.una.expressfood.ui.client.menu

import cr.una.expressfood.domain.model.Product
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests de la lógica de filtrado del menú.
 * Corre en JVM pura sin necesidad de Robolectric.
 */
class MenuAdapterTest {

    private lateinit var items: List<Product>

    @Before
    fun setUp() {
        items = listOf(
            makeProduct("1", "Hamburguesa Clásica", listOf("lechuga", "tomate", "queso")),
            makeProduct("2", "Pizza Margherita",    listOf("mozzarella", "albahaca", "tomate")),
            makeProduct("3", "Sushi Variado",       listOf("salmón", "arroz", "aguacate")),
            makeProduct("4", "Coca-Cola",           listOf("agua carbonatada", "azúcar"))
        )
    }

    @Test
    fun `filter por nombre retorna coincidencias`() {
        val result = ProductFilter.filter("pizza", items)
        assertEquals(1, result.size)
        assertEquals("Pizza Margherita", result.first().name)
    }

    @Test
    fun `filter por ingrediente retorna coincidencias`() {
        val result = ProductFilter.filter("tomate", items)
        assertEquals(2, result.size)
    }

    @Test
    fun `filter sin resultados retorna lista vacia`() {
        val result = ProductFilter.filter("ingrediente_inexistente", items)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `filter vacio restaura lista completa`() {
        val filtered = ProductFilter.filter("pizza", items)
        assertEquals(1, filtered.size)
        val restored = ProductFilter.filter("", items)
        assertEquals(4, restored.size)
    }

    @Test
    fun `filter es case insensitive`() {
        val result = ProductFilter.filter("SUSHI", items)
        assertEquals(1, result.size)
    }

    @Test
    fun `filter por ingrediente parcial encuentra producto`() {
        val result = ProductFilter.filter("mozz", items)
        assertEquals(1, result.size)
    }

    @Test
    fun `filter por nombre parcial encuentra producto`() {
        val result = ProductFilter.filter("burg", items)
        assertEquals(1, result.size)
    }

    @Test
    fun `filter con espacios funciona correctamente`() {
        val result = ProductFilter.filter("  pizza  ", items)
        assertEquals(1, result.size)
    }

    private fun makeProduct(id: String, name: String, ingredients: List<String>) = Product(
        id = id, name = name, description = "", ingredients = ingredients,
        price = 1000.0, imageUrl = "", estimatedTimeMinutes = 10,
        category = "TEST", available = true,
        createdAt = 0L, updatedAt = 0L
    )
}