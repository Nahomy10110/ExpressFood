package cr.una.expressfood.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import cr.una.expressfood.data.local.dao.ProductDao
import cr.una.expressfood.data.local.entity.ProductEntity
import cr.una.expressfood.domain.model.Product
import cr.una.expressfood.domain.model.toDomain
import cr.una.expressfood.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import cr.una.expressfood.domain.model.toEntity

class ProductRepository(
    private val productDao: ProductDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    //El RecyclerView se actualiza automáticamente
    fun observeAvailable(): Flow<List<Product>> =
        productDao.observeAvailable().map { list -> list.map { it.toDomain() } }

    // Búsqueda por nombre o ingrediente
    fun search(query: String): Flow<List<Product>> =
        productDao.search(query).map { list -> list.map { it.toDomain() } }

    // Admin: todos los productos incluyendo inhabilitados
    fun observeAll(): Flow<List<Product>> =
        productDao.observeAll().map { list -> list.map { it.toDomain() } }

    /**
     * Descarga productos de Firestore y los guarda en Room.
     * Se llama al detectar conexión (online-first para productos).
     * Si falla (sin red), Room ya tiene los productos del sync anterior.
     */
    suspend fun syncFromFirestore(): Result<Int> = runCatching {
        val snapshot = firestore.collection(Constants.Firestore.PRODUCTS)
            .get()
            .await()

        val entities = snapshot.documents.mapNotNull { doc ->
            runCatching {
                ProductEntity(
                    id                   = doc.getString("id") ?: doc.id,
                    name                 = doc.getString("name") ?: "",
                    description          = doc.getString("description") ?: "",
                    ingredients          = doc.getString("ingredients") ?: "",
                    price                = doc.getDouble("price") ?: 0.0,
                    imageUrl             = doc.getString("imageUrl") ?: "",
                    estimatedTimeMinutes = (doc.getLong("estimatedTimeMinutes") ?: 0L).toInt(),
                    category             = doc.getString("category") ?: "",
                    available            = doc.getBoolean("available") ?: true,
                    createdAt            = doc.getLong("createdAt") ?: 0L,
                    updatedAt            = doc.getLong("updatedAt") ?: 0L
                )
            }.getOrNull()
        }

        productDao.upsertAll(entities)
        entities.size
    }

    companion object {
        fun default(productDao: ProductDao) = ProductRepository(productDao)
    }

    // CRUD — solo admin

    suspend fun createProduct(product: Product): Result<Unit> = runCatching {
        val entity = product.toEntity()
        // 1. Room primero (offline-first)
        productDao.upsert(entity)
        // 2. Firestore como backup
        syncProductToFirestore(entity)
    }

    suspend fun updateProduct(product: Product): Result<Unit> = runCatching {
        val entity = product.copy(updatedAt = System.currentTimeMillis()).toEntity()
        productDao.upsert(entity)
        syncProductToFirestore(entity)
    }

    suspend fun setProductAvailability(productId: String, available: Boolean): Result<Unit> = runCatching {
        productDao.setAvailable(productId, available)
        firestore.collection(Constants.Firestore.PRODUCTS)
            .document(productId)
            .update("available", available)
            .await()
    }

    suspend fun deleteProduct(productId: String): Result<Unit> = runCatching {
        productDao.deleteById(productId)
        firestore.collection(Constants.Firestore.PRODUCTS)
            .document(productId)
            .delete()
            .await()
    }

    private suspend fun syncProductToFirestore(entity: ProductEntity) {
        runCatching {
            val data = mapOf(
                "id"                   to entity.id,
                "name"                 to entity.name,
                "description"          to entity.description,
                "ingredients"          to entity.ingredients,
                "price"                to entity.price,
                "imageUrl"             to entity.imageUrl,
                "estimatedTimeMinutes" to entity.estimatedTimeMinutes,
                "category"             to entity.category,
                "available"            to entity.available,
                "createdAt"            to entity.createdAt,
                "updatedAt"            to entity.updatedAt
            )
            firestore.collection(Constants.Firestore.PRODUCTS)
                .document(entity.id)
                .set(data)
                .await()
        }
    }
}