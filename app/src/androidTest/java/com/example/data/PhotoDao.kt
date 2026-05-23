package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos ORDER BY dateAdded DESC")
    fun getAllPhotos(): Flow<List<Photo>>

    @Query("SELECT * FROM photos WHERE category = :category ORDER BY dateAdded DESC")
    fun getPhotosByCategory(category: String): Flow<List<Photo>>

    @Query("SELECT * FROM photos WHERE isFavorite = 1 ORDER BY dateAdded DESC")
    fun getFavoritePhotos(): Flow<List<Photo>>

    @Query("SELECT * FROM photos WHERE id = :id")
    fun getPhotoById(id: Int): Flow<Photo?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: Photo): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(photos: List<Photo>)

    @Update
    suspend fun updatePhoto(photo: Photo)

    @Delete
    suspend fun deletePhoto(photo: Photo)
}
