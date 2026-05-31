package cr.una.expressfood.ui.client.menu

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import cr.una.expressfood.R
import cr.una.expressfood.databinding.ItemProductBinding
import cr.una.expressfood.domain.model.Product

class MenuAdapter(
    private val onAddToCart: (Product) -> Unit
) : ListAdapter<Product, MenuAdapter.ProductViewHolder>(DiffCallback) {

    // Lista original sin filtrar (necesaria para restaurar después de búsqueda)
    private var fullList: List<Product> = emptyList()

    fun setItems(items: List<Product>) {
        fullList = items
        submitList(items)
    }

    /**
     * Filtra por nombre O por cualquier ingrediente.
     * Si query está vacío restaura la lista completa.
     */
    fun filter(query: String) {
        submitList(ProductFilter.filter(query, fullList))
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
            binding.tvPrice.text      = "₡${"%,.0f".format(product.price)}"
            binding.tvTime.text       = "⏱ ${product.estimatedTimeMinutes} min"

            // Cargar imagen con Glide — funciona con cualquier URL (TheMealDB, Cloudinary, etc.)
            Glide.with(binding.ivProduct.context)
                .load(product.imageUrl)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.bg_image_placeholder)
                        .error(R.drawable.bg_image_placeholder)
                        .transform(RoundedCorners(24))
                )
                .into(binding.ivProduct)

            binding.btnAddToCart.setOnClickListener { onAddToCart(product) }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Product>() {
            override fun areItemsTheSame(a: Product, b: Product) = a.id == b.id
            override fun areContentsTheSame(a: Product, b: Product) = a == b
        }
    }
}