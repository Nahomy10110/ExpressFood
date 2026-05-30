package cr.una.expressfood.data.repository

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import cr.una.expressfood.data.local.dao.UserDao
import cr.una.expressfood.data.local.entity.UserEntity
import cr.una.expressfood.domain.model.User
import cr.una.expressfood.domain.model.UserRole
import cr.una.expressfood.domain.model.toDomain
import cr.una.expressfood.util.Constants
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    /**
     * Llamado tras un Google Sign-In exitoso.
     * Offline-first: guarda en Room primero, luego intenta subir a Firestore.
     */
    suspend fun upsertAndFetch(firebaseUser: FirebaseUser): User {
        val now = System.currentTimeMillis()

        val role = if (firebaseUser.uid == Constants.ADMIN_UID) UserRole.ADMIN
        else UserRole.CLIENTE

        // Preservar datos previos si el usuario ya existía en Room
        val existing = userDao.getByUid(firebaseUser.uid)

        val entity = UserEntity(
            uid         = firebaseUser.uid,
            email       = firebaseUser.email ?: "",
            displayName = firebaseUser.displayName ?: "",
            photoUrl    = firebaseUser.photoUrl?.toString(),
            role        = role.name,
            phone       = existing?.phone,
            address     = existing?.address,
            createdAt   = existing?.createdAt ?: now,
            lastLoginAt = now
        )

        //Room primero (fuente de verdad, funciona sin red)
        userDao.upsert(entity)

        //Firestore como backup, falla silenciosamente si no hay red
        syncToFirestore(entity)

        return entity.toDomain()
    }

    suspend fun getLocalUser(uid: String): User? =
        userDao.getByUid(uid)?.toDomain()

    suspend fun clearLocalUser() = userDao.clearAll()

    private suspend fun syncToFirestore(entity: UserEntity) {
        runCatching {
            firestore.collection(Constants.Firestore.USERS)
                .document(entity.uid)
                .set(
                    mapOf(
                        "uid"         to entity.uid,
                        "email"       to entity.email,
                        "displayName" to entity.displayName,
                        "photoUrl"    to entity.photoUrl,
                        "role"        to entity.role,
                        "lastLoginAt" to entity.lastLoginAt,
                        "createdAt"   to entity.createdAt
                    ),
                    SetOptions.merge()
                ).await()
        }
        // Error ignorado intencionalmente, el usuario ya está en Room
    }

    companion object {
        fun default(userDao: UserDao) = AuthRepository(userDao = userDao)
    }
}