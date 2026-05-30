package cr.una.expressfood.data.local.dao

import androidx.room.*
import cr.una.expressfood.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    // Observa todos los productos disponibles — se actualiza automáticamente con Flow
    @Query("SELECT * FROM products WHERE available = 1 ORDER BY name ASC")
    fun observeAvailable(): Flow<List<ProductEntity>>

    // Búsqueda por nombre O por ingredientes (para el RecyclerView con barra de búsqueda)
    @Query("""
        SELECT * FROM products 
        WHERE available = 1 
        AND (
            name LIKE '%' || :query || '%' 
            OR ingredients LIKE '%' || :query || '%'
        )
        ORDER BY name ASC
    """)
    fun search(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ProductEntity?

    // Inserta o actualiza — usado al sincronizar desde Firestore
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(products: List<ProductEntity>)

    @Query("UPDATE products SET available = :available WHERE id = :id")
    suspend fun setAvailable(id: String, available: Boolean)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun observeAll(): Flow<List<ProductEntity>>   // admin: ve también los inhabilitados
}