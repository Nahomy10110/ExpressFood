package cr.una.expressfood.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cr.una.expressfood.data.local.AppDatabase
import cr.una.expressfood.data.repository.OrderRepository
import cr.una.expressfood.data.repository.ProductRepository

/**
 * Worker que sincroniza datos pendientes con Firestore cuando hay red.
 * Se encarga de:
 * 1. Subir órdenes con synced=false a Firestore
 * 2. Descargar productos actualizados de Firestore a Room
 *
 */
class SyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val db             = AppDatabase.getInstance(applicationContext)
            val orderRepo      = OrderRepository.default(db.orderDao(), db.orderItemDao())
            val productRepo    = ProductRepository.default(db.productDao())

            // 1. Subir órdenes pendientes a Firestore
            orderRepo.syncPending()

            // 2. Actualizar productos desde Firestore
            productRepo.syncFromFirestore()

            Result.success()
        } catch (e: Exception) {
            // Si falla reintenta — WorkManager usa backoff exponencial
            Result.retry()
        }
    }
}