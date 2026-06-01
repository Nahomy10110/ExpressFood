package cr.una.expressfood.util

object Constants {

    const val ADMIN_UID = "Dz6QK6QyKAhBOatqA7lqyGkVvHi1"

    const val TAX_RATE      = 0.13
    const val DATABASE_NAME = "expressfood_db"
    const val SYNC_WORK_NAME = "sync-orders"

    object Firestore {
        const val USERS    = "users"
        const val PRODUCTS = "products"
        const val ORDERS   = "orders"
    }

    object Cloudinary {
        const val CLOUD_NAME    = "dazbijp71"
        const val API_KEY       = "447589271833274"
        const val UPLOAD_PRESET = "expressfood_products"
        const val UPLOAD_URL    = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"
    }
}