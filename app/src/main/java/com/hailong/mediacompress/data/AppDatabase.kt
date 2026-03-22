package com.hailong.mediacompress.data

import androidx.room.*
import com.hailong.mediacompress.model.CompressionStatus
import com.hailong.mediacompress.model.MediaType
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "media_items")
data class MediaItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uriString: String,
    val name: String,
    val path: String,
    val size: Long,
    val type: MediaType,
    val duration: Long = 0,
    val width: Int = 0,
    val height: Int = 0,
    val compressedSize: Long = 0,
    val compressedPath: String? = null,
    val status: CompressionStatus = CompressionStatus.PENDING,
    val progress: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface MediaItemDao {
    @Query("SELECT * FROM media_items ORDER BY timestamp DESC")
    fun getAllMediaItems(): Flow<List<MediaItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: MediaItemEntity): Long

    @Update
    suspend fun update(item: MediaItemEntity)

    @Delete
    suspend fun delete(item: MediaItemEntity)

    @Query("DELETE FROM media_items")
    suspend fun deleteAll()

    @Query("SELECT * FROM media_items WHERE id = :id")
    suspend fun getById(id: Long): MediaItemEntity?
}

@Database(entities = [MediaItemEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaItemDao(): MediaItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "media_compress_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
