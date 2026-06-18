package cr.una.expressfood.ui.common

import android.graphics.Color
import android.view.View
import com.google.android.material.snackbar.Snackbar

class ConnectivityBannerHelper(private val rootView: View) {

    private var currentSnackbar: Snackbar? = null

    fun update(isConnected: Boolean) {
        currentSnackbar?.dismiss()

        if (!isConnected) {
            currentSnackbar = Snackbar.make(
                rootView,
                "📵  Sin conexión — modo offline",
                Snackbar.LENGTH_INDEFINITE
            ).apply {
                setBackgroundTint(Color.parseColor("#B71C1C"))
                setTextColor(Color.WHITE)
                show()
            }
        } else {
            currentSnackbar = Snackbar.make(
                rootView,
                "✅  Conectado — sincronizando",
                Snackbar.LENGTH_SHORT
            ).apply {
                setBackgroundTint(Color.parseColor("#2E7D32"))
                setTextColor(Color.WHITE)
                show()
            }
        }
    }
}