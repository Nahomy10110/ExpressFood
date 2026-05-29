package cr.una.expressfood

import android.app.Application
import androidx.work.Configuration

class ExpressFoodApp : Application(), Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Firebase se inicializa automáticamente vía google-services
    }
}