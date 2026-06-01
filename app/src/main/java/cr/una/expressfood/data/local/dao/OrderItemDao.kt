package cr.una.expressfood.data.local.dao

import androidx.room.*
import cr.una.expressfood.data.local.entity.OrderItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderItemDao {

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    fun observeByOrder(orderId: String): Flow<List<OrderItemEntity>>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getByOrder(orderId: String): List<OrderItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: OrderItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<OrderItemEntity>)

    // Se borran en cascada cuando se borra la Order (ForeignKey CASCADE)
    @Query("DELETE FROM order_items WHERE orderId = :orderId")
    suspend fun deleteByOrder(orderId: String)
}