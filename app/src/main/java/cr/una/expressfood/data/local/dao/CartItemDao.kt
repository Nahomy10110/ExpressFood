package cr.una.expressfood.data.local.dao

import androidx.room.*
import cr.una.expressfood.data.local.entity.CartItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartItemDao {

    // Observa el carrito del usuario, se actualiza en tiempo real
    @Query("SELECT * FROM cart_items WHERE clientUid = :uid ORDER BY addedAt ASC")
    fun observeByClient(uid: String): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE clientUid = :uid ORDER BY addedAt ASC")
    suspend fun getByClient(uid: String): List<CartItemEntity>

    @Query("SELECT * FROM cart_items WHERE clientUid = :uid AND productId = :productId LIMIT 1")
    suspend fun getItem(uid: String, productId: String): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: CartItemEntity)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE id = :id")
    suspend fun updateQuantity(id: String, quantity: Int)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteById(id: String)

    // Vaciar carrito completo,llamado al procesar la orden
    @Query("DELETE FROM cart_items WHERE clientUid = :uid")
    suspend fun clearCart(uid: String)

    // Contar ítems, para el badge del carrito en la toolbar
    @Query("SELECT COALESCE(SUM(quantity), 0) FROM cart_items WHERE clientUid = :uid")
    fun observeItemCount(uid: String): Flow<Int>
}