package cr.una.expressfood.ui.admin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import cr.una.expressfood.data.sync.ConnectivityObserver
import cr.una.expressfood.data.sync.SyncScheduler
import cr.una.expressfood.databinding.ActivityAdminMainBinding
import cr.una.expressfood.ui.common.ConnectivityBannerHelper
import kotlinx.coroutines.launch

class AdminMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminMainBinding
    private lateinit var connectivityObserver: ConnectivityObserver
    private lateinit var bannerHelper: ConnectivityBannerHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupConnectivity()
    }

    private fun setupNavigation() {
        val navHost = supportFragmentManager
            .findFragmentById(binding.navHostFragmentAdmin.id) as NavHostFragment
        binding.bottomNavAdmin.setupWithNavController(navHost.navController)
    }

    private fun setupConnectivity() {
        connectivityObserver = ConnectivityObserver.default(this)
        bannerHelper = ConnectivityBannerHelper(binding.connectivityBanner.root)

        lifecycleScope.launch {
            connectivityObserver.observe().collect { isConnected ->
                bannerHelper.update(isConnected)
                if (isConnected) {
                    SyncScheduler.enqueueSync(this@AdminMainActivity)
                }
            }
        }
    }
}