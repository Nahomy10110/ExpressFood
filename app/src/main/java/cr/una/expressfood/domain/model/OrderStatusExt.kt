package cr.una.expressfood.domain.model

import android.graphics.Color

/**
 * Extensiones de OrderStatus para la UI.
 * en cliente (Mis Órdenes) y admin (Panel Admin).
 */
fun OrderStatus.toColor(): Int = when (this) {
    OrderStatus.CREADA    -> Color.parseColor("#9E9E9E")
    OrderStatus.PENDIENTE -> Color.parseColor("#FF9800")
    OrderStatus.EN_CAMINO -> Color.parseColor("#2196F3")
    OrderStatus.ENTREGADA -> Color.parseColor("#4CAF50")
    OrderStatus.CANCELADA -> Color.parseColor("#F44336")
}

fun OrderStatus.toLabel(): String = when (this) {
    OrderStatus.CREADA    -> "Creada"
    OrderStatus.PENDIENTE -> "Pendiente"
    OrderStatus.EN_CAMINO -> "En camino"
    OrderStatus.ENTREGADA -> "Entregada"
    OrderStatus.CANCELADA -> "Cancelada"
}