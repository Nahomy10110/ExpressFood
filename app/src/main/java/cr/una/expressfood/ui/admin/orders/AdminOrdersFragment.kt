package cr.una.expressfood.ui.admin.orders

import android.content.Intent
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
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import cr.una.expressfood.databinding.FragmentAdminOrdersBinding
import cr.una.expressfood.domain.model.OrderStatus
import cr.una.expressfood.domain.model.toLabel
import cr.una.expressfood.ui.login.LoginActivity
import kotlinx.coroutines.launch

class AdminOrdersFragment : Fragment() {

    private var _binding: FragmentAdminOrdersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminOrdersViewModel by viewModels {
        AdminOrdersViewModel.Factory(requireActivity().application)
    }

    private val adapter = AdminOrdersAdapter { order, newStatus ->
        viewModel.updateOrderStatus(order.id, newStatus)
        Snackbar.make(
            binding.root,
            "Orden #${order.orderNumber} → ${newStatus.toLabel()}",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFilters()
        setupLogout()
        observeViewModel()
        setupGreeting()
    }

    private fun setupGreeting() {
        val user = FirebaseAuth.getInstance().currentUser
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> "Buenos días"
            hour < 18 -> "Buenas tardes"
            else      -> "Buenas noches"
        }
        binding.tvAdminGreeting.text = greeting
        binding.tvAdminName.text     = user?.displayName ?: "Administrador"

        user?.photoUrl?.let { uri ->
            com.bumptech.glide.Glide.with(this)
                .load(uri)
                .circleCrop()
                .placeholder(cr.una.expressfood.R.drawable.bg_avatar_circle)
                .into(binding.ivAdminAvatar)
        }
    }

    private fun setupRecyclerView() {
        binding.rvAdminOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAdminOrders.adapter = adapter
    }

    private fun setupFilters() {
        val allChip = makeChip("Todas", isFirst = true)
        allChip.setOnClickListener {
            uncheckAll()
            allChip.isChecked = true
            adapter.filterByStatus(null)
        }
        binding.adminFilterRow.addView(allChip)

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
                adapter.filterByStatus(status.name)
            }
            binding.adminFilterRow.addView(chip)
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
                intArrayOf(Color.parseColor("#2E7D32"), Color.parseColor("#F0F0F0"))
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
        binding.adminFilterRow.children.forEach { view ->
            (view as? Chip)?.isChecked = false
        }
    }

    private fun setupLogout() {
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(
                Intent(requireContext(), LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.ordersState.collect { state ->
                when (state) {
                    is AdminOrdersViewModel.AdminOrdersState.Loading -> {
                        binding.progressBarAdmin.visibility  = View.VISIBLE
                        binding.rvAdminOrders.visibility     = View.GONE
                        binding.layoutAdminEmpty.visibility  = View.GONE
                    }
                    is AdminOrdersViewModel.AdminOrdersState.Success -> {
                        binding.progressBarAdmin.visibility  = View.GONE
                        binding.rvAdminOrders.visibility     = View.VISIBLE
                        binding.layoutAdminEmpty.visibility  = View.GONE
                        adapter.setItems(state.orders)
                        val count = state.orders.size
                        binding.tvAdminOrderCount.text =
                            if (count == 1) "1 orden activa" else "$count órdenes activas"
                    }
                    is AdminOrdersViewModel.AdminOrdersState.Empty -> {
                        binding.progressBarAdmin.visibility  = View.GONE
                        binding.rvAdminOrders.visibility     = View.GONE
                        binding.layoutAdminEmpty.visibility  = View.VISIBLE
                        binding.tvAdminOrderCount.text       = "Sin órdenes"
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