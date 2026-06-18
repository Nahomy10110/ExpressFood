package cr.una.expressfood.ui.client

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import cr.una.expressfood.data.sync.ConnectivityObserver
import cr.una.expressfood.data.sync.SyncScheduler
import cr.una.expressfood.databinding.ActivityClientMainBinding
import cr.una.expressfood.ui.client.cart.CartViewModel
import cr.una.expressfood.ui.common.ConnectivityBannerHelper
import cr.una.expressfood.ui.login.LoginActivity
import kotlinx.coroutines.launch

class ClientMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientMainBinding

    private val cartViewModel: CartViewModel by viewModels {
        CartViewModel.Factory(application)
    }

    private lateinit var connectivityObserver: ConnectivityObserver
    private lateinit var bannerHelper: ConnectivityBannerHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupConnectivity()
        setupLogout()
        observeCartBadge()
    }

    private fun setupNavigation() {
        val navHost = supportFragmentManager
            .findFragmentById(binding.navHostFragment.id) as NavHostFragment
        binding.bottomNav.setupWithNavController(navHost.navController)
    }

    private fun setupConnectivity() {
        connectivityObserver = ConnectivityObserver.default(this)
        // Pasar el root view del activity en lugar del banner incluido
        bannerHelper = ConnectivityBannerHelper(binding.root)

        lifecycleScope.launch {
            connectivityObserver.observe().collect { isConnected ->
                android.util.Log.d("ConnectivityBanner", "isConnected=$isConnected")
                bannerHelper.update(isConnected)
                if (isConnected) {
                    SyncScheduler.enqueueSync(this@ClientMainActivity)
                }
            }
        }
    }

    private fun setupLogout() {
        // Botón logout en la toolbar del menú
        // Se maneja desde el menú de opciones
    }

    private fun observeCartBadge() {
        lifecycleScope.launch {
            cartViewModel.itemCount.collect { count ->
                val badge = binding.bottomNav.getOrCreateBadge(
                    cr.una.expressfood.R.id.cartFragment
                )
                if (count > 0) {
                    badge.isVisible = true
                    badge.number   = count
                } else {
                    badge.isVisible = false
                }
            }
        }
    }

    fun logout() {
        SyncScheduler.cancelSync(this)
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}