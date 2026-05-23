package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class Photo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val url: String,
    val title: String,
    val description: String,
    val category: String,
    val dateAdded: Long = System.currentTimeMillis(),
    val cameraModel: String = "Unknown Camera",
    val aperture: String = "f/2.8",
    val iso: String = "ISO 100",
    val isFavorite: Boolean = false,
    val tags: String = "",
    val aiCaption: String? = null,
    val aiLabels: String? = null
)
