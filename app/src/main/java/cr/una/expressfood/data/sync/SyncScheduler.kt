package cr.una.expressfood.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import cr.una.expressfood.util.Constants
import java.util.concurrent.TimeUnit

/**
 * Centraliza la programación del SyncWorker.
 * Se llama desde cualquier punto de la app cuando se necesita sincronizar.
 *
 */
object SyncScheduler {

    /**
     * Encola una sincronización única que se ejecuta cuando hay red.
     * Si ya hay una en cola con el mismo nombre, la mantiene (KEEP).
     */
    fun enqueueSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30,
                TimeUnit.SECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            Constants.SYNC_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    /**
     * Cancela cualquier sync pendiente.
     * Útil al hacer logout.
     */
    fun cancelSync(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(Constants.SYNC_WORK_NAME)
    }
}