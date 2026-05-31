package cr.una.expressfood.ui.client.orders

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import cr.una.expressfood.databinding.FragmentMyOrdersBinding
import cr.una.expressfood.domain.model.OrderStatus
import cr.una.expressfood.domain.model.toLabel
import kotlinx.coroutines.launch

class MyOrdersFragment : Fragment() {

    private var _binding: FragmentMyOrdersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyOrdersViewModel by viewModels {
        MyOrdersViewModel.Factory(requireActivity().application)
    }

    private val adapter = MyOrdersAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFilters()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.rvOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOrders.adapter = adapter
    }

    private fun setupFilters() {
        // Chip "Todas"
        val allChip = makeChip("Todas", isFirst = true)
        allChip.setOnClickListener {
            uncheckAll()
            allChip.isChecked = true
            viewModel.setFilter(null)
            adapter.filterByStatus(null)
        }
        binding.filterRow.addView(allChip)

        // Chips por estado
        val statuses = listOf(
            OrderStatus.PENDIENTE,
            OrderStatus.EN_CAMINO,
            OrderStatus.ENTREGADA,
            OrderStatus.CANCELADA
        )
        statuses.forEach { status ->
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

    private fun makeChip(label: String, isFirst: Boolean = false): Chip {
        return Chip(requireContext()).apply {
            text        = label
            isClickable = true
            isCheckable = true
            isChecked   = isFirst
            chipBackgroundColor = ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf(-android.R.attr.state_checked)
                ),
                intArrayOf(
                    Color.parseColor("#2E7D32"),
                    Color.parseColor("#F0F0F0")
                )
            )
            setTextColor(
                ColorStateList(
                    arrayOf(
                        intArrayOf(android.R.attr.state_checked),
                        intArrayOf(-android.R.attr.state_checked)
                    ),
                    intArrayOf(Color.WHITE, Color.parseColor("#888888"))
                )
            )
        }
    }

    private fun uncheckAll() {
        binding.filterRow.children.forEach { view ->
            (view as? Chip)?.isChecked = false
        }
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
                        val count = state.orders.size
                        binding.tvOrderCount.text =
                            if (count == 1) "1 orden" else "$count órdenes"
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