package cr.una.expressfood.ui.admin.products

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import cr.una.expressfood.R
import cr.una.expressfood.databinding.ItemAdminProductBinding
import cr.una.expressfood.domain.model.Product

class AdminProductsAdapter(
    private val onEdit:   (Product) -> Unit,
    private val onToggle: (Product) -> Unit
) : ListAdapter<Product, AdminProductsAdapter.ProductViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemAdminProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductViewHolder(
        private val binding: ItemAdminProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.tvAdminProductName.text     = product.name
            binding.tvAdminProductPrice.text    = "₡${"%,.0f".format(product.price)}"
            binding.tvAdminProductCategory.text = product.category

            // Badge disponible / inhabilitado
            if (product.available) {
                binding.tvAdminProductStatus.text = "Disponible"
                (binding.tvAdminProductStatus.background.mutate()
                        as? android.graphics.drawable.GradientDrawable)
                    ?.setColor(Color.parseColor("#4CAF50"))
            } else {
                binding.tvAdminProductStatus.text = "Inhabilitado"
                (binding.tvAdminProductStatus.background.mutate()
                        as? android.graphics.drawable.GradientDrawable)
                    ?.setColor(Color.parseColor("#9E9E9E"))
            }

            Glide.with(binding.ivAdminProduct.context)
                .load(product.imageUrl)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.bg_image_placeholder)
                        .error(R.drawable.bg_image_placeholder)
                        .transform(RoundedCorners(16))
                )
                .into(binding.ivAdminProduct)

            binding.btnEditProduct.setOnClickListener   { onEdit(product) }
            binding.btnToggleProduct.setOnClickListener { onToggle(product) }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Product>() {
            override fun areItemsTheSame(a: Product, b: Product) = a.id == b.id
            override fun areContentsTheSame(a: Product, b: Product) =
                a.name == b.name && a.available == b.available && a.price == b.price
        }
    }
}