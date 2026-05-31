package cr.una.expressfood.ui.client

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import cr.una.expressfood.databinding.ActivityClientMainBinding

class ClientMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClientMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHost = supportFragmentManager
            .findFragmentById(binding.navHostFragment.id) as NavHostFragment
        val navController = navHost.navController

        binding.bottomNav.setupWithNavController(navController)
    }
}