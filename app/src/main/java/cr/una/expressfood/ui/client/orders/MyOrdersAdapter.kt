package cr.una.expressfood.ui.client.orders

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cr.una.expressfood.databinding.ItemOrderBinding
import cr.una.expressfood.domain.model.Order
import cr.una.expressfood.domain.model.toColor
import cr.una.expressfood.domain.model.toLabel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyOrdersAdapter(
    private val onOrderClick: (Order) -> Unit
) : ListAdapter<Order, MyOrdersAdapter.OrderViewHolder>(DiffCallback) {

    private var fullList: List<Order> = emptyList()

    fun setItems(items: List<Order>) {
        fullList = items
        submitList(items)
    }

    fun filterByStatus(status: String?) {
        if (status == null) submitList(fullList)
        else submitList(fullList.filter { it.status.name == status })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderViewHolder(
        private val binding: ItemOrderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

            binding.tvOrderNumber.text = "Orden #${order.orderNumber}"
            binding.tvOrderDate.text   = sdf.format(Date(order.createdAt))
            binding.tvOrderItems.text  = when {
                order.items.isEmpty() -> "Toca para ver detalle"
                else -> order.items.joinToString(" · ") { "${it.quantity}x ${it.productName}" }
            }
            binding.tvOrderTotal.text  = "₡${"%,.0f".format(order.total)}"
            binding.tvOrderStatus.text = order.status.toLabel()

            (binding.tvOrderStatus.background.mutate() as? GradientDrawable)
                ?.setColor(order.status.toColor())

            // El popup ahora se maneja desde MyOrdersFragment
            binding.root.setOnClickListener { onOrderClick(order) }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Order>() {
            override fun areItemsTheSame(a: Order, b: Order) = a.id == b.id
            override fun areContentsTheSame(a: Order, b: Order) =
                a.status == b.status && a.total == b.total && a.items.size == b.items.size
        }
    }
}