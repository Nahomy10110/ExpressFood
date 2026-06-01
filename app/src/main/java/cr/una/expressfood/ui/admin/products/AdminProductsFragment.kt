package cr.una.expressfood.ui.admin.products

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import cr.una.expressfood.R
import cr.una.expressfood.databinding.FragmentAdminProductsBinding
import cr.una.expressfood.domain.model.Product
import kotlinx.coroutines.launch

class AdminProductsFragment : Fragment() {

    private var _binding: FragmentAdminProductsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminProductsViewModel by viewModels {
        AdminProductsViewModel.Factory(requireActivity().application)
    }

    private val adapter = AdminProductsAdapter(
        onEdit   = { product -> showProductForm(product) },
        onToggle = { product ->
            viewModel.toggleAvailability(product)
            val msg = if (product.available) "\"${product.name}\" inhabilitado"
            else "\"${product.name}\" habilitado"
            Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
        }
    )

    // Referencia al dialog abierto para actualizar preview de imagen
    private var currentDialogImageView: ImageView? = null

    // Lanzador para abrir la galería
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data ?: return@registerForActivityResult
            viewModel.setSelectedImage(uri)
            currentDialogImageView?.let { iv ->
                Glide.with(this).load(uri).centerCrop().into(iv)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()

        binding.fabAddProduct.setOnClickListener {
            showProductForm(null)
        }
    }

    private fun setupRecyclerView() {
        binding.rvAdminProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAdminProducts.adapter = adapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.productsState.collect { state ->
                when (state) {
                    is AdminProductsViewModel.ProductsState.Loading -> {
                        binding.progressBarProducts.visibility  = View.VISIBLE
                        binding.rvAdminProducts.visibility      = View.GONE
                        binding.layoutProductsEmpty.visibility  = View.GONE
                    }
                    is AdminProductsViewModel.ProductsState.Success -> {
                        binding.progressBarProducts.visibility  = View.GONE
                        binding.rvAdminProducts.visibility      = View.VISIBLE
                        binding.layoutProductsEmpty.visibility  = View.GONE
                        adapter.submitList(state.products)
                        binding.tvProductCount.text =
                            "${state.products.size} productos en el menú"
                    }
                    is AdminProductsViewModel.ProductsState.Empty -> {
                        binding.progressBarProducts.visibility  = View.GONE
                        binding.rvAdminProducts.visibility      = View.GONE
                        binding.layoutProductsEmpty.visibility  = View.VISIBLE
                        binding.tvProductCount.text             = "Sin productos"
                    }
                }
            }
        }
    }

    private fun showProductForm(product: Product?) {
        viewModel.resetForm()

        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_product_form, null)

        val tvTitle      = dialogView.findViewById<android.widget.TextView>(R.id.tvFormTitle)
        val ivImage      = dialogView.findViewById<ImageView>(R.id.ivFormImage)
        val btnImage     = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSelectImage)
        val etName       = dialogView.findViewById<TextInputEditText>(R.id.etFormName)
        val etDesc       = dialogView.findViewById<TextInputEditText>(R.id.etFormDescription)
        val etIngr       = dialogView.findViewById<TextInputEditText>(R.id.etFormIngredients)
        val etPrice      = dialogView.findViewById<TextInputEditText>(R.id.etFormPrice)
        val etTime       = dialogView.findViewById<TextInputEditText>(R.id.etFormTime)
        val actvCategory = dialogView.findViewById<AutoCompleteTextView>(R.id.actvFormCategory)
        val btnSave      = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnFormSave)
        val btnCancel    = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnFormCancel)

        currentDialogImageView = ivImage

        // Categorías disponibles
        val categories = listOf("HAMBURGUESA", "PIZZA", "SUSHI", "BEBIDA", "POSTRE", "OTRO")
        actvCategory.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        )

        // Si es edición, prellenar los campos
        if (product != null) {
            tvTitle.text      = "Editar producto"
            etName.setText(product.name)
            etDesc.setText(product.description)
            etIngr.setText(product.ingredients.joinToString(", "))
            etPrice.setText(product.price.toInt().toString())
            etTime.setText(product.estimatedTimeMinutes.toString())
            actvCategory.setText(product.category, false)
            viewModel.setCurrentImageUrl(product.imageUrl)
            if (product.imageUrl.isNotBlank()) {
                Glide.with(this).load(product.imageUrl).centerCrop().into(ivImage)
            }
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
        )
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.94).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        btnImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            pickImageLauncher.launch(intent)
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val name     = etName.text.toString()
            val desc     = etDesc.text.toString()
            val ingr     = etIngr.text.toString()
            val price    = etPrice.text.toString().toDoubleOrNull() ?: 0.0
            val time     = etTime.text.toString().toIntOrNull() ?: 0
            val category = actvCategory.text.toString()

            if (product == null) {
                viewModel.createProduct(name, desc, ingr, price, time, category)
            } else {
                viewModel.updateProduct(product, name, desc, ingr, price, time, category)
            }
        }

        // Observar el estado del formulario
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.formState.collect { state ->
                when (state) {
                    is AdminProductsViewModel.FormState.Loading -> {
                        btnSave.isEnabled = false
                        btnSave.text      = "Guardando…"
                    }
                    is AdminProductsViewModel.FormState.Success -> {
                        dialog.dismiss()
                        Snackbar.make(
                            binding.root,
                            if (product == null) "Producto creado ✓" else "Producto actualizado ✓",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    is AdminProductsViewModel.FormState.Error -> {
                        btnSave.isEnabled = true
                        btnSave.text      = "Guardar"
                        Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                    }
                    else -> {
                        btnSave.isEnabled = true
                        btnSave.text      = "Guardar"
                    }
                }
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentDialogImageView = null
        _binding = null
    }
}