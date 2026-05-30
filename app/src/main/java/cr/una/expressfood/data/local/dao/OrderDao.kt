package cr.una.expressfood.data.local.dao

import androidx.room.*
import cr.una.expressfood.data.local.entity.OrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    // Cliente: sus propias órdenes, más recientes primero
    @Query("SELECT * FROM orders WHERE clientUid = :uid ORDER BY createdAt DESC")
    fun observeByClient(uid: String): Flow<List<OrderEntity>>

    // Admin: todas las órdenes
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<OrderEntity>>

    // WorkManager: órdenes pendientes de subir a Firestore
    @Query("SELECT * FROM orders WHERE synced = 0")
    suspend fun getUnsynced(): List<OrderEntity>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(order: OrderEntity)

    // Marcar como sincronizada después de subir a Firestore
    @Query("UPDATE orders SET synced = 1, updatedAt = :now WHERE id = :id")
    suspend fun markSynced(id: String, now: Long = System.currentTimeMillis())

    // Admin: cambiar estado de una orden
    @Query("""
        UPDATE orders 
        SET status = :status, updatedAt = :now, synced = 0 
        WHERE id = :id
    """)
    suspend fun updateStatus(id: String, status: String, now: Long = System.currentTimeMillis())

    // Reporte: órdenes agrupadas por día (createdAt en milisegundos)
    @Query("""
        SELECT * FROM orders 
        WHERE clientUid = :uid 
        AND createdAt BETWEEN :from AND :to
        ORDER BY createdAt DESC
    """)
    suspend fun getByClientAndDateRange(uid: String, from: Long, to: Long): List<OrderEntity>

    @Query("""
        SELECT * FROM orders 
        WHERE createdAt BETWEEN :from AND :to
        ORDER BY createdAt DESC
    """)
    suspend fun getByDateRange(from: Long, to: Long): List<OrderEntity>

    @Query("DELETE FROM orders WHERE id = :id")
    suspend fun deleteById(id: String)
}