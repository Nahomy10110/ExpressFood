package cr.una.expressfood.ui.client.orders

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import cr.una.expressfood.R
import cr.una.expressfood.databinding.ItemOrderBinding
import cr.una.expressfood.domain.model.Order
import cr.una.expressfood.domain.model.toColor
import cr.una.expressfood.domain.model.toLabel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyOrdersAdapter : ListAdapter<Order, MyOrdersAdapter.OrderViewHolder>(DiffCallback) {

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
            binding.tvOrderNumber.text = "Orden #${order.orderNumber}"

            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            binding.tvOrderDate.text = sdf.format(Date(order.createdAt))

            // Ítems resumidos
            binding.tvOrderItems.text = if (order.items.isEmpty()) {
                "Sin detalles"
            } else {
                order.items.joinToString(" · ") { "${it.quantity}x ${it.productName}" }
            }

            binding.tvOrderTotal.text = "₡${"%,.0f".format(order.total)}"

            // Badge de estado
            binding.tvOrderStatus.text = order.status.toLabel()
            (binding.tvOrderStatus.background.mutate() as? GradientDrawable)
                ?.setColor(order.status.toColor())

            // Click en la card → popup de detalle
            binding.root.setOnClickListener {
                showDetailDialog(order)
            }
        }

        private fun showDetailDialog(order: Order) {
            val context = binding.root.context
            val dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_order_detail, null)

            // Header
            dialogView.findViewById<TextView>(R.id.tvDialogOrderNumber).text =
                "Orden #${order.orderNumber}"

            val tvStatus = dialogView.findViewById<TextView>(R.id.tvDialogStatus)
            tvStatus.text = order.status.toLabel()
            (tvStatus.background.mutate() as? GradientDrawable)
                ?.setColor(order.status.toColor())

            // Fecha
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            dialogView.findViewById<TextView>(R.id.tvDialogDate).text =
                sdf.format(Date(order.createdAt))

            // Ítems
            val layoutItems = dialogView.findViewById<LinearLayout>(R.id.layoutItems)
            if (order.items.isEmpty()) {
                val tv = TextView(context).apply {
                    text      = "Sin detalles de productos"
                    textSize  = 12f
                    setTextColor(Color.parseColor("#757575"))
                }
                layoutItems.addView(tv)
            } else {
                order.items.forEach { item ->
                    val row = LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { bottomMargin = 6 }
                        layoutParams = params
                    }

                    val tvName = TextView(context).apply {
                        text      = "${item.quantity}x ${item.productName}"
                        textSize  = 13f
                        setTextColor(Color.parseColor("#1A1A1A"))
                        layoutParams = LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                        )
                    }

                    val tvSubtotal = TextView(context).apply {
                        text      = "₡${"%,.0f".format(item.subtotal)}"
                        textSize  = 13f
                        setTextColor(Color.parseColor("#2E7D32"))
                    }

                    row.addView(tvName)
                    row.addView(tvSubtotal)
                    layoutItems.addView(row)
                }
            }

            // Totales
            dialogView.findViewById<TextView>(R.id.tvDialogSubtotal).text =
                "₡${"%,.0f".format(order.subtotal)}"
            dialogView.findViewById<TextView>(R.id.tvDialogTaxes).text =
                "₡${"%,.0f".format(order.taxes)}"
            dialogView.findViewById<TextView>(R.id.tvDialogTotal).text =
                "₡${"%,.0f".format(order.total)}"

            // Dirección
            dialogView.findViewById<TextView>(R.id.tvDialogAddress).text =
                order.deliveryAddress

            // Notas
            val tvNotes = dialogView.findViewById<TextView>(R.id.tvDialogNotes)
            if (!order.notes.isNullOrBlank()) {
                tvNotes.visibility = View.VISIBLE
                tvNotes.text       = "📝 ${order.notes}"
            }

            // Crear dialog
            val dialog = MaterialAlertDialogBuilder(context)
                .setView(dialogView)
                .create()
            dialog.window?.setBackgroundDrawable(
                android.graphics.drawable.ColorDrawable(Color.TRANSPARENT)
            )
            dialog.window?.setLayout(
                (context.resources.displayMetrics.widthPixels * 0.92).toInt(),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )

            dialogView.findViewById<com.google.android.material.button.MaterialButton>(
                R.id.btnDialogClose
            ).setOnClickListener { dialog.dismiss() }

            dialog.show()
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