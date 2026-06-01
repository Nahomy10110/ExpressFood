package cr.una.expressfood.ui.client.menu

import cr.una.expressfood.domain.model.Product

/**
 * Lógica de filtrado del menú extraída del adapter para ser testeable en JVM pura.
 */
object ProductFilter {

    fun filter(query: String, fullList: List<Product>): List<Product> {
        if (query.isBlank()) return fullList
        val lower = query.lowercase().trim()
        return fullList.filter { product ->
            product.name.lowercase().contains(lower) ||
                    product.ingredients.any { it.lowercase().contains(lower) }
        }
    }
}