package cr.una.expressfood.ui.client.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import cr.una.expressfood.R
import cr.una.expressfood.databinding.FragmentCartBinding
import kotlinx.coroutines.launch

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CartViewModel by activityViewModels {
        CartViewModel.Factory(requireActivity().application)
    }

    private val adapter = CartAdapter(
        onIncrease = { viewModel.increaseQuantity(it) },
        onDecrease = { viewModel.decreaseQuantity(it) }
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        setupButtons()
    }

    private fun setupRecyclerView() {
        binding.rvCart.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCart.adapter = adapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.cartState.collect { state ->
                when (state) {
                    is CartViewModel.CartState.Empty -> {
                        binding.rvCart.visibility      = View.GONE
                        binding.layoutEmpty.visibility = View.VISIBLE
                        binding.layoutTotals.visibility = View.GONE
                    }
                    is CartViewModel.CartState.WithItems -> {
                        binding.rvCart.visibility       = View.VISIBLE
                        binding.layoutEmpty.visibility  = View.GONE
                        binding.layoutTotals.visibility = View.VISIBLE
                        adapter.submitList(state.items)
                        binding.tvSubtotal.text = "₡${"%,.0f".format(state.subtotal)}"
                        binding.tvTaxes.text    = "₡${"%,.0f".format(state.taxes)}"
                        binding.tvTotal.text    = "₡${"%,.0f".format(state.total)}"
                    }
                    is CartViewModel.CartState.OrderProcessed -> {
                        Snackbar.make(binding.root, "¡Orden procesada exitosamente! ✓", Snackbar.LENGTH_LONG).show()
                    }
                    is CartViewModel.CartState.Error -> {
                        Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun setupButtons() {
        binding.btnProcessOrder.setOnClickListener {
            showProcessOrderDialog()
        }
    }

    private fun showProcessOrderDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_process_order, null)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirmar orden")
            .setView(dialogView)
            .setPositiveButton("Confirmar") { _, _ ->
                val address = dialogView.findViewById<TextInputEditText>(R.id.etAddress)
                    .text.toString().trim()
                val notes = dialogView.findViewById<TextInputEditText>(R.id.etNotes)
                    .text.toString().trim()
                if (address.isBlank()) {
                    Snackbar.make(binding.root, "Ingresá una dirección de entrega", Snackbar.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                viewModel.processOrder(address, notes.ifBlank { null })
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}