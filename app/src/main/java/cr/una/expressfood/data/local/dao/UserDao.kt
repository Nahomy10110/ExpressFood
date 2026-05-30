package cr.una.expressfood.data.local.dao

import androidx.room.*
import cr.una.expressfood.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity)

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    suspend fun getByUid(uid: String): UserEntity?

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    fun observeByUid(uid: String): Flow<UserEntity?>

    @Query("UPDATE users SET lastLoginAt = :timestamp WHERE uid = :uid")
    suspend fun updateLastLogin(uid: String, timestamp: Long)

    @Query("DELETE FROM users")
    suspend fun clearAll()
}