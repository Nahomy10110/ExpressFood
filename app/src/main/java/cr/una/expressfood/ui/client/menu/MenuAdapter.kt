package cr.una.expressfood.ui.client.menu

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import cr.una.expressfood.R
import cr.una.expressfood.databinding.ItemProductBinding
import cr.una.expressfood.domain.model.Product

class MenuAdapter(
    // qty: cantidad seleccionada en el popup (siempre 1 cuando viene del botón +)
    private val onAddToCart: (Product, Int) -> Unit
) : ListAdapter<Product, MenuAdapter.ProductViewHolder>(DiffCallback) {

    private var fullList: List<Product> = emptyList()

    fun setItems(items: List<Product>) {
        fullList = items
        submitList(items)
    }

    /** Filtrado por nombre o ingrediente — usa ProductFilter para ser testeable */
    fun filter(query: String) {
        submitList(ProductFilter.filter(query, fullList))
    }

    /** Filtrado por categoría — "Todos" pasa null */
    fun filterByCategory(category: String?) {
        if (category == null) {
            submitList(fullList)
            return
        }
        submitList(fullList.filter {
            it.category.uppercase().contains(category.uppercase())
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductViewHolder(
        private val binding: ItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.tvProductName.text = product.name
            binding.tvIngredients.text = product.ingredients.joinToString(", ")
            binding.tvPrice.text       = "₡${"%,.0f".format(product.price)}"
            binding.tvTime.text        = "⏱ ${product.estimatedTimeMinutes} min"

            Glide.with(binding.ivProduct.context)
                .load(product.imageUrl)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.bg_image_placeholder)
                        .error(R.drawable.bg_image_placeholder)
                        .transform(RoundedCorners(24))
                )
                .into(binding.ivProduct)

            // Tarjeta completa → popup de detalle
            binding.root.setOnClickListener {
                showDetailDialog(product)
            }

            // Botón + → agrega directo sin popup, con debounce para evitar doble click
            binding.btnAddToCart.setOnClickListener {
                onAddToCart(product, 1)
                it.isEnabled = false
                it.postDelayed({ it.isEnabled = true }, 800)
            }
        }

        private fun showDetailDialog(product: Product) {
            val context = binding.root.context
            val dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_product_detail, null)

            var qty = 1

            // Referencias a las vistas del dialog
            val tvDetailName  = dialogView.findViewById<android.widget.TextView>(R.id.tvDetailName)
            val tvDetailDesc  = dialogView.findViewById<android.widget.TextView>(R.id.tvDetailDescription)
            val tvDetailTime  = dialogView.findViewById<android.widget.TextView>(R.id.tvDetailTime)
            val tvDetailQty   = dialogView.findViewById<android.widget.TextView>(R.id.tvDetailQty)
            val tvDetailPrice = dialogView.findViewById<android.widget.TextView>(R.id.tvDetailPrice)
            val btnIncrease   = dialogView.findViewById<android.widget.TextView>(R.id.btnDetailIncrease)
            val btnDecrease   = dialogView.findViewById<android.widget.TextView>(R.id.btnDetailDecrease)
            val btnAdd        = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDetailAddToCart)
            val ivDetail      = dialogView.findViewById<android.widget.ImageView>(R.id.ivDetailImage)
            val chipGroup     = dialogView.findViewById<ChipGroup>(R.id.chipGroupIngredients)

            // Llenar datos estáticos
            tvDetailName.text = product.name
            tvDetailDesc.text = product.description
            tvDetailTime.text = "⏱ ${product.estimatedTimeMinutes} min"

            // Imagen con Glide
            Glide.with(context)
                .load(product.imageUrl)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.bg_image_placeholder)
                        .error(R.drawable.bg_image_placeholder)
                )
                .into(ivDetail)

            // Chips de ingredientes
            product.ingredients.forEach { ingredient ->
                val chip = Chip(context).apply {
                    text        = ingredient
                    isClickable = false
                    isCheckable = false
                    chipBackgroundColor = ColorStateList.valueOf(
                        Color.parseColor("#EAF3DE")
                    )
                    setTextColor(Color.parseColor("#27500A"))
                }
                chipGroup.addView(chip)
            }

            // Función que actualiza precio y botón según la cantidad
            fun refresh() {
                val total = product.price * qty
                tvDetailQty.text = qty.toString()
                tvDetailPrice.text = "₡${"%,.0f".format(total)}"
                btnAdd.text = "Agregar al carrito"
            }
            refresh()

            // Controles de cantidad
            btnIncrease.setOnClickListener {
                qty++
                refresh()
            }
            btnDecrease.setOnClickListener {
                if (qty > 1) {
                    qty--
                    refresh()
                }
            }

            // Crear y mostrar el dialog
            val dialog = MaterialAlertDialogBuilder(context)
                .setView(dialogView)
                .create()

            dialog.window?.setBackgroundDrawable(
                android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
            )
            dialog.window?.setLayout(
                (binding.root.context.resources.displayMetrics.widthPixels * 0.92).toInt(),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )

            btnAdd.setOnClickListener {
                onAddToCart(product, qty)
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Product>() {
            override fun areItemsTheSame(a: Product, b: Product) = a.id == b.id
            override fun areContentsTheSame(a: Product, b: Product) = a == b
        }
    }
}