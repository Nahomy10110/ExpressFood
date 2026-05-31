package cr.una.expressfood.ui.admin.orders

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cr.una.expressfood.databinding.ItemAdminOrderBinding
import cr.una.expressfood.domain.model.Order
import cr.una.expressfood.domain.model.OrderStatus
import cr.una.expressfood.domain.model.toColor
import cr.una.expressfood.domain.model.toLabel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminOrdersAdapter(
    private val onStatusChange: (Order, OrderStatus) -> Unit
) : ListAdapter<Order, AdminOrdersAdapter.AdminOrderViewHolder>(DiffCallback) {

    private var fullList: List<Order> = emptyList()

    fun setItems(items: List<Order>) {
        fullList = items
        submitList(items)
    }

    fun filterByStatus(status: String?) {
        if (status == null) submitList(fullList)
        else submitList(fullList.filter { it.status.name == status })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminOrderViewHolder {
        val binding = ItemAdminOrderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AdminOrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdminOrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AdminOrderViewHolder(
        private val binding: ItemAdminOrderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            binding.tvAdminOrderNumber.text = "Orden #${order.orderNumber}"
            binding.tvAdminClientName.text  = order.clientName

            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            binding.tvAdminOrderDate.text = sdf.format(Date(order.createdAt))

            binding.tvAdminOrderItems.text = if (order.items.isEmpty()) {
                "Sin detalles"
            } else {
                order.items.joinToString(" · ") { "${it.quantity}x ${it.productName}" }
            }

            binding.tvAdminOrderTotal.text = "₡${"%,.0f".format(order.total)}"

            // Badge de estado
            binding.tvAdminOrderStatus.text = order.status.toLabel()
            (binding.tvAdminOrderStatus.background.mutate() as? GradientDrawable)
                ?.setColor(order.status.toColor())

            // Botón cambiar estado — muestra solo los estados siguientes válidos
            val nextStatuses = order.status.nextStatuses()
            if (nextStatuses.isEmpty()) {
                binding.btnChangeStatus.isEnabled = false
                binding.btnChangeStatus.alpha     = 0.4f
            } else {
                binding.btnChangeStatus.isEnabled = true
                binding.btnChangeStatus.alpha     = 1f
                binding.btnChangeStatus.setOnClickListener {
                    showStatusDialog(order, nextStatuses)
                }
            }
        }

        private fun showStatusDialog(order: Order, nextStatuses: List<OrderStatus>) {
            val context  = binding.root.context
            val options  = nextStatuses.map { it.toLabel() }.toTypedArray()

            android.app.AlertDialog.Builder(context)
                .setTitle("Cambiar estado — Orden #${order.orderNumber}")
                .setItems(options) { _, which ->
                    onStatusChange(order, nextStatuses[which])
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Order>() {
            override fun areItemsTheSame(a: Order, b: Order) = a.id == b.id
            override fun areContentsTheSame(a: Order, b: Order) =
                a.status == b.status && a.total == b.total
        }
    }
}