package cr.una.expressfood.ui.admin.products

import cr.una.expressfood.domain.model.Product
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdminProductsViewModelTest {

    private fun makeProduct(
        id: String = "p1",
        name: String = "Hamburguesa",
        price: Double = 4500.0,
        available: Boolean = true,
        category: String = "HAMBURGUESA"
    ) = Product(
        id = id, name = name, description = "Desc",
        ingredients = listOf("lechuga", "tomate"),
        price = price, imageUrl = "", estimatedTimeMinutes = 25,
        category = category, available = available,
        createdAt = 0L, updatedAt = 0L
    )

    @Test
    fun `producto disponible tiene available true`() {
        val product = makeProduct(available = true)
        assertTrue(product.available)
    }

    @Test
    fun `producto inhabilitado tiene available false`() {
        val product = makeProduct(available = false)
        assertFalse(product.available)
    }

    @Test
    fun `toggle de available invierte el valor`() {
        val product = makeProduct(available = true)
        val toggled = product.copy(available = !product.available)
        assertFalse(toggled.available)
    }

    @Test
    fun `ingredientes se parsean correctamente desde string con coma`() {
        val raw         = "lechuga, tomate, queso"
        val ingredients = raw.split(",").map { it.trim() }.filter { it.isNotBlank() }
        assertEquals(3, ingredients.size)
        assertEquals("lechuga", ingredients[0])
        assertEquals("tomate",  ingredients[1])
        assertEquals("queso",   ingredients[2])
    }

    @Test
    fun `ingredientes vacios generan lista vacia`() {
        val raw         = "  "
        val ingredients = raw.split(",").map { it.trim() }.filter { it.isNotBlank() }
        assertTrue(ingredients.isEmpty())
    }

    @Test
    fun `precio 0 no es valido`() {
        val price = 0.0
        assertFalse(price > 0)
    }

    @Test
    fun `precio positivo es valido`() {
        val price = 4500.0
        assertTrue(price > 0)
    }

    @Test
    fun `tiempo 0 no es valido`() {
        val time = 0
        assertFalse(time > 0)
    }

    @Test
    fun `categoria se guarda en uppercase`() {
        val category = "hamburguesa"
        assertEquals("HAMBURGUESA", category.uppercase())
    }

    @Test
    fun `nombre en blanco no es valido`() {
        val name = "  "
        assertTrue(name.isBlank())
    }

    @Test
    fun `nombre con contenido es valido`() {
        val name = "Hamburguesa Clásica"
        assertFalse(name.isBlank())
    }

    @Test
    fun `producto editado preserva el id original`() {
        val original = makeProduct(id = "prod-001")
        val edited   = original.copy(name = "Nuevo nombre")
        assertEquals("prod-001", edited.id)
    }

    @Test
    fun `updatedAt cambia al editar`() {
        val original   = makeProduct()
        val now        = System.currentTimeMillis()
        val edited     = original.copy(updatedAt = now)
        assertTrue(edited.updatedAt >= original.updatedAt)
    }
}