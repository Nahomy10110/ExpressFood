package cr.una.expressfood.ui.client.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import cr.una.expressfood.databinding.FragmentMenuBinding
import cr.una.expressfood.domain.model.Product
import kotlinx.coroutines.launch

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MenuViewModel by viewModels {
        MenuViewModel.Factory(requireActivity().application)
    }

    private val adapter = MenuAdapter { product -> addToCart(product) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.rvMenu.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMenu.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { text ->
            adapter.filter(text.toString())
            // Mostrar estado vacío si no hay resultados
            binding.tvEmpty.visibility =
                if (adapter.itemCount == 0 && !text.isNullOrBlank()) View.VISIBLE
                else View.GONE
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.menuState.collect { state ->
                when (state) {
                    is MenuViewModel.MenuState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.rvMenu.visibility      = View.GONE
                    }
                    is MenuViewModel.MenuState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.rvMenu.visibility      = View.VISIBLE
                        adapter.setItems(state.products)
                    }
                    is MenuViewModel.MenuState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.rvMenu.visibility      = View.VISIBLE
                        Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun addToCart(product: Product) {
        // CartViewModel lo implementamos despues
        Snackbar.make(binding.root, "\"${product.name}\" agregado al carrito ✓", Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}