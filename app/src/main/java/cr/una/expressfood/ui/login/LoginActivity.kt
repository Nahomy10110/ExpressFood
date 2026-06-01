package cr.una.expressfood.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import cr.una.expressfood.R
import cr.una.expressfood.data.local.AppDatabase
import cr.una.expressfood.data.repository.AuthRepository
import cr.una.expressfood.databinding.ActivityLoginBinding
import cr.una.expressfood.domain.model.UserRole
import cr.una.expressfood.ui.admin.AdminMainActivity
import cr.una.expressfood.ui.client.ClientMainActivity
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val credentialManager by lazy { CredentialManager.create(this) }

    private val viewModel: LoginViewModel by viewModels {
        val db   = AppDatabase.getInstance(applicationContext)
        val repo = AuthRepository.default(db.userDao())
        LoginViewModel.Factory(repo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkExistingSession()
        setupClickListeners()
        observeViewModel()
    }

    // Si ya hay sesión activa en Firebase Y en Room, saltarse el login
    private fun checkExistingSession() {
        val currentUser = auth.currentUser ?: return
        lifecycleScope.launch {
            val local = AppDatabase.getInstance(applicationContext)
                .userDao()
                .getByUid(currentUser.uid)
            if (local != null) navigateByRole(local.role)
        }
    }

    private fun setupClickListeners() {
        binding.btnGoogleSignIn.setOnClickListener { launchGoogleSignIn() }
    }

    private fun observeViewModel() {
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginViewModel.LoginState.Idle -> {
                    showLoading(false)
                    binding.btnGoogleSignIn.isEnabled = true
                }
                is LoginViewModel.LoginState.Loading -> {
                    showLoading(true)
                    binding.btnGoogleSignIn.isEnabled = false
                }
                is LoginViewModel.LoginState.Success -> {
                    showLoading(false)
                    navigateByRole(state.user.role.name)
                }
                is LoginViewModel.LoginState.Error -> {
                    showLoading(false)
                    binding.btnGoogleSignIn.isEnabled = true
                    showErrorDialog(state.message)
                }
            }
        }
    }

    private fun launchGoogleSignIn() {
        // Evitar doble click
        if (viewModel.loginState.value is LoginViewModel.LoginState.Loading) return

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .setAutoSelectEnabled(false)
                    .build()
            ).build()

        lifecycleScope.launch {
            runCatching {
                // 1. Selector de cuentas Google
                val result = credentialManager.getCredential(this@LoginActivity, request)

                // 2. Extraer ID Token
                val googleToken = GoogleIdTokenCredential.createFrom(result.credential.data)

                // 3. Autenticar en Firebase
                val firebaseCred = GoogleAuthProvider.getCredential(googleToken.idToken, null)
                val authResult   = auth.signInWithCredential(firebaseCred).await()

                // 4. Notificar al ViewModel
                authResult.user?.let { viewModel.onSignedIn(it) }
                    ?: throw Exception("Firebase no devolvió un usuario válido")

            }.onFailure { error ->
                if (error is GetCredentialCancellationException) {
                    // El usuario cerró el selector — no es un error, no mostrar diálogo
                    return@onFailure
                }
                viewModel.onSignInError(error)
            }
        }
    }

    private fun navigateByRole(role: String) {
        val target = if (role == UserRole.ADMIN.name) AdminMainActivity::class.java
        else ClientMainActivity::class.java
        startActivity(Intent(this, target).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun showLoading(show: Boolean) {
        binding.progressOverlay.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.login_error_title))
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok)) { _, _ -> viewModel.resetError() }
            .show()
    }
}