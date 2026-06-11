package cr.una.expressfood.ui.client.orders

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import cr.una.expressfood.R
import cr.una.expressfood.databinding.FragmentMyOrdersBinding
import cr.una.expressfood.domain.model.Order
import cr.una.expressfood.domain.model.OrderStatus
import cr.una.expressfood.domain.model.toColor
import cr.una.expressfood.domain.model.toLabel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyOrdersFragment : Fragment() {

    private var _binding: FragmentMyOrdersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyOrdersViewModel by viewModels {
        MyOrdersViewModel.Factory(requireActivity().application)
    }

    private lateinit var adapter: MyOrdersAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        setupRecyclerView()
        setupFilters()
        observeViewModel()
    }

    private fun setupAdapter() {
        adapter = MyOrdersAdapter { order ->
            viewModel.refreshOrderItems(order.id)
            viewLifecycleOwner.lifecycleScope.launch {
                // Esperar que Room emita la actualización con los items
                var attempts = 0
                var updatedOrder = order
                while (attempts < 10) {
                    delay(300)
                    val candidate = viewModel.getOrderById(order.id)
                    if (candidate != null && candidate.items.isNotEmpty()) {
                        updatedOrder = candidate
                        break
                    }
                    attempts++
                }
                if (isAdded) showOrderDetail(updatedOrder)
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOrders.adapter = adapter
    }

    private fun setupFilters() {
        val allChip = makeChip("Todas", isFirst = true)
        allChip.setOnClickListener {
            uncheckAll()
            allChip.isChecked = true
            viewModel.setFilter(null)
            adapter.filterByStatus(null)
        }
        binding.filterRow.addView(allChip)

        listOf(
            OrderStatus.PENDIENTE,
            OrderStatus.EN_CAMINO,
            OrderStatus.ENTREGADA,
            OrderStatus.CANCELADA
        ).forEach { status ->
            val chip = makeChip(status.toLabel())
            chip.setOnClickListener {
                uncheckAll()
                chip.isChecked = true
                viewModel.setFilter(status)
                adapter.filterByStatus(status.name)
            }
            binding.filterRow.addView(chip)
        }
    }

    private fun makeChip(label: String, isFirst: Boolean = false) =
        Chip(requireContext()).apply {
            text                 = label
            isClickable          = true
            isCheckable          = true
            isChecked            = isFirst
            isCheckedIconVisible = false
            chipBackgroundColor  = ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf(-android.R.attr.state_checked)
                ),
                intArrayOf(Color.parseColor("#2E7D32"), Color.parseColor("#F0F0F0"))
            )
            setTextColor(ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf(-android.R.attr.state_checked)
                ),
                intArrayOf(Color.WHITE, Color.parseColor("#888888"))
            ))
        }

    private fun uncheckAll() {
        binding.filterRow.children.forEach { (it as? Chip)?.isChecked = false }
    }

    private fun showOrderDetail(order: Order) {
        val ctx        = requireContext()
        val dialogView = LayoutInflater.from(ctx)
            .inflate(R.layout.dialog_order_detail, null)

        // Número y estado
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

        // Items
        val layoutItems = dialogView.findViewById<LinearLayout>(R.id.layoutItems)
        layoutItems.removeAllViews()

        if (order.items.isEmpty()) {
            layoutItems.addView(TextView(ctx).apply {
                text      = "Sin productos"
                textSize  = 12f
                setTextColor(Color.parseColor("#757575"))
            })
        } else {
            order.items.forEach { item ->
                val itemView = LayoutInflater.from(ctx)
                    .inflate(R.layout.item_order_detail, layoutItems, false)

                itemView.findViewById<TextView>(R.id.tvItemName).text =
                    "${item.quantity}x ${item.productName}"
                itemView.findViewById<TextView>(R.id.tvItemPrice).text =
                    "₡${"%,.0f".format(item.subtotal)}"

                Glide.with(ctx)
                    .load(item.productImageUrl)
                    .placeholder(R.drawable.bg_image_placeholder)
                    .transform(RoundedCorners(12))
                    .into(itemView.findViewById(R.id.ivItemImage))

                layoutItems.addView(itemView)
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
        val layoutNotes = dialogView.findViewById<LinearLayout>(R.id.layoutNotes)
        val tvNotes     = dialogView.findViewById<TextView>(R.id.tvDialogNotes)
        if (!order.notes.isNullOrBlank()) {
            layoutNotes.visibility = View.VISIBLE
            tvNotes.text           = order.notes
        } else {
            layoutNotes.visibility = View.GONE
        }

        // Crear y mostrar el dialog
        val dialog = MaterialAlertDialogBuilder(ctx)
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(Color.TRANSPARENT)
        )
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.92).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialogView.findViewById<com.google.android.material.button.MaterialButton>(
            R.id.btnDialogClose
        ).setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.ordersState.collect { state ->
                when (state) {
                    is MyOrdersViewModel.OrdersState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.rvOrders.visibility    = View.GONE
                        binding.layoutEmpty.visibility = View.GONE
                    }
                    is MyOrdersViewModel.OrdersState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.rvOrders.visibility    = View.VISIBLE
                        binding.layoutEmpty.visibility = View.GONE
                        adapter.setItems(state.orders)
                        binding.tvOrderCount.text =
                            if (state.orders.size == 1) "1 orden"
                            else "${state.orders.size} órdenes"
                    }
                    is MyOrdersViewModel.OrdersState.Empty -> {
                        binding.progressBar.visibility = View.GONE
                        binding.rvOrders.visibility    = View.GONE
                        binding.layoutEmpty.visibility = View.VISIBLE
                        binding.tvOrderCount.text      = "Sin órdenes"
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}