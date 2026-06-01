package cr.una.expressfood.ui.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.firebase.auth.FirebaseUser
import cr.una.expressfood.data.repository.AuthRepository
import cr.una.expressfood.domain.model.User
import cr.una.expressfood.domain.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule val instantTaskRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: LoginViewModel

    private val fakeUser = User(
        uid = "uid-123", email = "test@gmail.com",
        displayName = "Test", photoUrl = null, role = UserRole.CLIENTE
    )
    private val mockFirebaseUser: FirebaseUser = mock()

    @Before fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mock()
        viewModel = LoginViewModel(authRepository)
    }

    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun `estado inicial es Idle`() {
        assertTrue(viewModel.loginState.value is LoginViewModel.LoginState.Idle)
    }

    @Test fun `onSignedIn exitoso emite Loading luego Success`() = runTest {
        whenever(authRepository.upsertAndFetch(any())).thenReturn(fakeUser)
        viewModel.onSignedIn(mockFirebaseUser)
        assertEquals(LoginViewModel.LoginState.Loading, viewModel.loginState.value)
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.loginState.value
        assertTrue(state is LoginViewModel.LoginState.Success)
        assertEquals(fakeUser, (state as LoginViewModel.LoginState.Success).user)
    }

    @Test fun `onSignedIn admin emite Success con rol ADMIN`() = runTest {
        whenever(authRepository.upsertAndFetch(any())).thenReturn(fakeUser.copy(role = UserRole.ADMIN))
        viewModel.onSignedIn(mockFirebaseUser)
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.loginState.value as LoginViewModel.LoginState.Success
        assertEquals(UserRole.ADMIN, state.user.role)
    }

    @Test fun `onSignedIn cuando falla el repository emite Error`() = runTest {
        whenever(authRepository.upsertAndFetch(any())).thenThrow(RuntimeException("fallo"))
        viewModel.onSignedIn(mockFirebaseUser)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.loginState.value is LoginViewModel.LoginState.Error)
    }

    @Test fun `onSignInError emite Error`() {
        viewModel.onSignInError(Exception("algo falló"))
        assertTrue(viewModel.loginState.value is LoginViewModel.LoginState.Error)
    }

    @Test fun `onSignInError con cancelacion muestra texto de cancelado`() {
        viewModel.onSignInError(Exception("User cancelled"))
        val msg = (viewModel.loginState.value as LoginViewModel.LoginState.Error).message
        assertTrue(msg.contains("cancelado", ignoreCase = true))
    }

    @Test fun `resetError desde Error vuelve a Idle`() {
        viewModel.onSignInError(Exception("error"))
        viewModel.resetError()
        assertTrue(viewModel.loginState.value is LoginViewModel.LoginState.Idle)
    }

    @Test fun `resetError desde Success no cambia nada`() = runTest {
        whenever(authRepository.upsertAndFetch(any())).thenReturn(fakeUser)
        viewModel.onSignedIn(mockFirebaseUser)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.resetError()
        assertTrue(viewModel.loginState.value is LoginViewModel.LoginState.Success)
    }
}