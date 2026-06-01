package cr.una.expressfood.ui.client.menu

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import cr.una.expressfood.databinding.FragmentMenuBinding
import cr.una.expressfood.ui.client.ClientMainActivity
import cr.una.expressfood.ui.client.cart.CartViewModel
import kotlinx.coroutines.launch

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MenuViewModel by viewModels {
        MenuViewModel.Factory(requireActivity().application)
    }

    private val cartViewModel: CartViewModel by activityViewModels {
        CartViewModel.Factory(requireActivity().application)
    }

    private val adapter = MenuAdapter { product, qty ->
        repeat(qty) { cartViewModel.addProduct(product) }
        Snackbar.make(
            binding.root,
            if (qty == 1) "\"${product.name}\" agregado al carrito ✓"
            else "\"${product.name}\" x$qty agregado al carrito ✓",
            Snackbar.LENGTH_SHORT
        ).show()
    }

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
        setupCategories()
        setupGreeting()
        observeViewModel()
    }

    // ─── Saludo personalizado ─────────────────────────────────────────────────

    private fun setupGreeting() {
        val user = FirebaseAuth.getInstance().currentUser
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> "Buenos días 👋"
            hour < 18 -> "Buenas tardes 👋"
            else      -> "Buenas noches 👋"
        }
        binding.tvGreeting.text = greeting
        binding.tvUserName.text = user?.displayName ?: "Usuario"

        user?.photoUrl?.let { uri ->
            Glide.with(this)
                .load(uri)
                .circleCrop()
                .placeholder(cr.una.expressfood.R.drawable.bg_avatar_circle)
                .into(binding.ivUserAvatar)
        }

        // Logout del cliente
        binding.btnClientLogout.setOnClickListener {
            (requireActivity() as? ClientMainActivity)?.logout()
        }
    }

    // ─── RecyclerView ─────────────────────────────────────────────────────────

    private fun setupRecyclerView() {
        binding.rvMenu.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMenu.adapter = adapter
    }

    // ─── Búsqueda ─────────────────────────────────────────────────────────────

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { text ->
            adapter.filter(text.toString())
            binding.layoutEmpty.visibility =
                if (adapter.itemCount == 0 && !text.isNullOrBlank()) View.VISIBLE
                else View.GONE
        }
    }

    // ─── Chips de categorías ──────────────────────────────────────────────────

    private fun setupCategories() {
        val categories = listOf("Todos", "Hamburguesa", "Pizza", "Sushi", "Bebida", "Postre")

        categories.forEach { cat ->
            val chip = Chip(requireContext()).apply {
                text        = cat
                isClickable = true
                isCheckable = true
                isChecked   = cat == "Todos"

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
                        intArrayOf(
                            Color.WHITE,
                            Color.parseColor("#888888")
                        )
                    )
                )

                setOnClickListener {
                    binding.categoryRow.children.forEach { view ->
                        (view as? Chip)?.isChecked = false
                    }
                    isChecked = true
                    if (cat == "Todos") adapter.filterByCategory(null)
                    else adapter.filterByCategory(cat)
                    binding.etSearch.setText("")
                }
            }
            binding.categoryRow.addView(chip)
        }
    }

    // ─── ViewModel ────────────────────────────────────────────────────────────

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.menuState.collect { state ->
                when (state) {
                    is MenuViewModel.MenuState.Loading -> {
                        binding.progressBar.visibility  = View.VISIBLE
                        binding.rvMenu.visibility       = View.GONE
                        binding.layoutEmpty.visibility  = View.GONE
                    }
                    is MenuViewModel.MenuState.Success -> {
                        binding.progressBar.visibility  = View.GONE
                        binding.rvMenu.visibility       = View.VISIBLE
                        binding.layoutEmpty.visibility  = View.GONE
                        adapter.setItems(state.products)
                    }
                    is MenuViewModel.MenuState.Error -> {
                        binding.progressBar.visibility  = View.GONE
                        binding.rvMenu.visibility       = View.VISIBLE
                        Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
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