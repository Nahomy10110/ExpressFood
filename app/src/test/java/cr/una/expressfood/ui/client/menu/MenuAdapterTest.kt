package cr.una.expressfood.ui.client.menu

import android.app.Application
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import cr.una.expressfood.domain.model.Product
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MenuAdapterTest {

    private lateinit var adapter: MenuAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var items: List<Product>

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Application>()

        adapter = MenuAdapter {}

        // Robolectric necesita que el RecyclerView tenga LayoutManager para itemCount
        recyclerView = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = this@MenuAdapterTest.adapter
        }

        items = listOf(
            makeProduct("1", "Hamburguesa Clásica", listOf("lechuga", "tomate", "queso")),
            makeProduct("2", "Pizza Margherita",    listOf("mozzarella", "albahaca", "tomate")),
            makeProduct("3", "Sushi Variado",       listOf("salmón", "arroz", "aguacate")),
            makeProduct("4", "Coca-Cola",           listOf("agua carbonatada", "azúcar"))
        )
        adapter.setItems(items)
    }

    @Test
    fun `setItems carga todos los productos`() {
        assertEquals(4, adapter.itemCount)
    }

    @Test
    fun `filter por nombre retorna coincidencias`() {
        adapter.filter("pizza")
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `filter por ingrediente retorna coincidencias`() {
        adapter.filter("tomate")
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun `filter sin resultados retorna lista vacia`() {
        adapter.filter("ingrediente_inexistente")
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `filter vacio restaura lista completa`() {
        adapter.filter("pizza")
        assertEquals(1, adapter.itemCount)
        adapter.filter("")
        assertEquals(4, adapter.itemCount)
    }

    @Test
    fun `filter es case insensitive`() {
        adapter.filter("SUSHI")
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `filter por ingrediente parcial encuentra producto`() {
        adapter.filter("mozz")
        assertEquals(1, adapter.itemCount)
    }

    private fun makeProduct(id: String, name: String, ingredients: List<String>) = Product(
        id = id, name = name, description = "", ingredients = ingredients,
        price = 1000.0, imageUrl = "", estimatedTimeMinutes = 10,
        category = "TEST", available = true,
        createdAt = 0L, updatedAt = 0L
    )
}