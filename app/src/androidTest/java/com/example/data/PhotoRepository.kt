package com.example.data

import kotlinx.coroutines.flow.Flow

class PhotoRepository(private val photoDao: PhotoDao) {
    val allPhotos: Flow<List<Photo>> = photoDao.getAllPhotos()
    val favorites: Flow<List<Photo>> = photoDao.getFavoritePhotos()

    fun getPhotosByCategory(category: String): Flow<List<Photo>> {
        return photoDao.getPhotosByCategory(category)
    }

    fun getPhotoById(id: Int): Flow<Photo?> {
        return photoDao.getPhotoById(id)
    }

    suspend fun insert(photo: Photo): Long {
        return photoDao.insertPhoto(photo)
    }

    suspend fun insertAll(photos: List<Photo>) {
        photoDao.insertAll(photos)
    }

    suspend fun update(photo: Photo) {
        photoDao.updatePhoto(photo)
    }

    suspend fun delete(photo: Photo) {
        photoDao.deletePhoto(photo)
    }
}
