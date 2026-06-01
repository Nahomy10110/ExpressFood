package cr.una.expressfood.ui.admin.products

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cr.una.expressfood.data.local.AppDatabase
import cr.una.expressfood.data.repository.CloudinaryRepository
import cr.una.expressfood.data.repository.ProductRepository
import cr.una.expressfood.domain.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AdminProductsViewModel(
    application: Application,
    private val productRepository: ProductRepository,
    private val cloudinaryRepository: CloudinaryRepository
) : AndroidViewModel(application) {

    sealed class ProductsState {
        object Loading : ProductsState()
        data class Success(val products: List<Product>) : ProductsState()
        object Empty : ProductsState()
    }

    sealed class FormState {
        object Idle : FormState()
        object Loading : FormState()
        object Success : FormState()
        data class Error(val message: String) : FormState()
    }

    private val _productsState = MutableStateFlow<ProductsState>(ProductsState.Loading)
    val productsState: StateFlow<ProductsState> = _productsState.asStateFlow()

    private val _formState = MutableStateFlow<FormState>(FormState.Idle)
    val formState: StateFlow<FormState> = _formState.asStateFlow()

    // Imagen seleccionada temporalmente
    private var selectedImageUri: Uri? = null
    private var currentImageUrl: String = ""

    init { observeAllProducts() }

    private fun observeAllProducts() {
        viewModelScope.launch {
            productRepository.observeAll().collect { products ->
                _productsState.value = when {
                    products.isEmpty() -> ProductsState.Empty
                    else               -> ProductsState.Success(products)
                }
            }
        }
    }

    fun setSelectedImage(uri: Uri) {
        selectedImageUri = uri
    }

    fun setCurrentImageUrl(url: String) {
        currentImageUrl = url
    }

    fun resetForm() {
        selectedImageUri  = null
        currentImageUrl   = ""
        _formState.value  = FormState.Idle
    }

    /**
     * Crea un producto nuevo.
     * Si hay imagen seleccionada la sube a Cloudinary primero.
     */
    fun createProduct(
        name: String,
        description: String,
        ingredients: String,
        price: Double,
        timeMinutes: Int,
        category: String
    ) {
        if (!validateFields(name, description, ingredients, price, timeMinutes, category)) return

        _formState.value = FormState.Loading
        viewModelScope.launch {
            runCatching {
                val imageUrl = uploadImageIfNeeded() ?: ""
                val now      = System.currentTimeMillis()
                val product  = Product(
                    id                   = UUID.randomUUID().toString(),
                    name                 = name.trim(),
                    description          = description.trim(),
                    ingredients          = ingredients.split(",").map { it.trim() }.filter { it.isNotBlank() },
                    price                = price,
                    imageUrl             = imageUrl,
                    estimatedTimeMinutes = timeMinutes,
                    category             = category.uppercase(),
                    available            = true,
                    createdAt            = now,
                    updatedAt            = now
                )
                productRepository.createProduct(product).getOrThrow()
            }.onSuccess {
                _formState.value = FormState.Success
            }.onFailure {
                _formState.value = FormState.Error(it.message ?: "Error al crear el producto")
            }
        }
    }

    /**
     * Actualiza un producto existente.
     */
    fun updateProduct(
        existing: Product,
        name: String,
        description: String,
        ingredients: String,
        price: Double,
        timeMinutes: Int,
        category: String
    ) {
        if (!validateFields(name, description, ingredients, price, timeMinutes, category)) return

        _formState.value = FormState.Loading
        viewModelScope.launch {
            runCatching {
                val imageUrl = uploadImageIfNeeded() ?: existing.imageUrl
                val product  = existing.copy(
                    name                 = name.trim(),
                    description          = description.trim(),
                    ingredients          = ingredients.split(",").map { it.trim() }.filter { it.isNotBlank() },
                    price                = price,
                    imageUrl             = imageUrl,
                    estimatedTimeMinutes = timeMinutes,
                    category             = category.uppercase(),
                    updatedAt            = System.currentTimeMillis()
                )
                productRepository.updateProduct(product).getOrThrow()
            }.onSuccess {
                _formState.value = FormState.Success
            }.onFailure {
                _formState.value = FormState.Error(it.message ?: "Error al actualizar el producto")
            }
        }
    }

    fun toggleAvailability(product: Product) {
        viewModelScope.launch {
            productRepository.setProductAvailability(product.id, !product.available)
        }
    }

    private suspend fun uploadImageIfNeeded(): String? {
        val uri = selectedImageUri ?: return null
        val result = cloudinaryRepository.uploadImage(uri)
        return result.getOrThrow()
    }

    private fun validateFields(
        name: String, description: String, ingredients: String,
        price: Double, timeMinutes: Int, category: String
    ): Boolean {
        return when {
            name.isBlank()        -> { _formState.value = FormState.Error("El nombre es requerido"); false }
            description.isBlank() -> { _formState.value = FormState.Error("La descripción es requerida"); false }
            ingredients.isBlank() -> { _formState.value = FormState.Error("Los ingredientes son requeridos"); false }
            price <= 0            -> { _formState.value = FormState.Error("El precio debe ser mayor a 0"); false }
            timeMinutes <= 0      -> { _formState.value = FormState.Error("El tiempo debe ser mayor a 0"); false }
            category.isBlank()    -> { _formState.value = FormState.Error("La categoría es requerida"); false }
            else -> true
        }
    }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            val db          = AppDatabase.getInstance(app)
            val productRepo = ProductRepository.default(db.productDao())
            val cloudRepo   = CloudinaryRepository.default(app)
            return AdminProductsViewModel(app, productRepo, cloudRepo) as T
        }
    }
}