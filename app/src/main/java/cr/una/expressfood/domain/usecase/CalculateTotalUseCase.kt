package cr.una.expressfood.domain.usecase

import cr.una.expressfood.util.Constants

data class OrderTotals(
    val subtotal: Double,
    val taxes: Double,
    val total: Double,
    val taxRate: Double
)

class CalculateTotalUseCase {

    fun invoke(
        subtotal: Double,
        taxRate: Double = Constants.TAX_RATE
    ): OrderTotals {
        val taxes = subtotal * taxRate
        return OrderTotals(
            subtotal = subtotal,
            taxes    = taxes,
            total    = subtotal + taxes,
            taxRate  = taxRate
        )
    }

    // Sobrecarga: calcula desde una lista de ítems
    fun invoke(
        items: List<Pair<Double, Int>>,  // (unitPrice, quantity)
        taxRate: Double = Constants.TAX_RATE
    ): OrderTotals {
        val subtotal = items.sumOf { (price, qty) -> price * qty }
        return invoke(subtotal, taxRate)
    }
}