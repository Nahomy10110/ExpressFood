package cr.una.expressfood.ui.client.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import cr.una.expressfood.R
import cr.una.expressfood.databinding.ItemCartBinding
import cr.una.expressfood.domain.model.CartItem

class CartAdapter(
    private val onIncrease: (CartItem) -> Unit,
    private val onDecrease: (CartItem) -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CartViewHolder(
        private val binding: ItemCartBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            binding.tvCartItemName.text     = item.productName
            binding.tvCartItemPrice.text    = "₡${"%,.0f".format(item.unitPrice)} c/u"
            binding.tvQuantity.text         = item.quantity.toString()
            binding.tvCartItemSubtotal.text = "₡${"%,.0f".format(item.subtotal)}"

            Glide.with(binding.ivCartItem.context)
                .load(item.productImageUrl)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.bg_image_placeholder)
                        .error(R.drawable.bg_image_placeholder)
                        .transform(RoundedCorners(16))
                )
                .into(binding.ivCartItem)

            binding.btnIncrease.setOnClickListener { onIncrease(item) }
            binding.btnDecrease.setOnClickListener { onDecrease(item) }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<CartItem>() {
            override fun areItemsTheSame(a: CartItem, b: CartItem) = a.id == b.id
            override fun areContentsTheSame(a: CartItem, b: CartItem) = a == b
        }
    }
}