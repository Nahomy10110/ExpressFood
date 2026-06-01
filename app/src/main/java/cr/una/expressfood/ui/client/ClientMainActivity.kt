package cr.una.expressfood.ui.client

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import cr.una.expressfood.databinding.ActivityClientMainBinding
import cr.una.expressfood.ui.client.cart.CartViewModel
import kotlinx.coroutines.launch

class ClientMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientMainBinding

    private val cartViewModel: CartViewModel by viewModels {
        CartViewModel.Factory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHost = supportFragmentManager
            .findFragmentById(binding.navHostFragment.id) as NavHostFragment
        val navController = navHost.navController

        binding.bottomNav.setupWithNavController(navController)

        observeCartBadge()
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
}