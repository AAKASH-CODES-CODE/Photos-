package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.AppDatabase
import com.example.data.Photo
import com.example.data.PhotoRepository
import com.example.network.GeminiApiClient
import com.example.network.GeminiContent
import com.example.network.GeminiPart
import com.example.network.GeminiRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PhotosViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = PhotoRepository(db.photoDao())

    val allCategories = listOf("All", "Nature", "Space", "Architecture", "Travel", "Food", "Favorites")

    val selectedCategory = MutableStateFlow("All")
    val searchQuery = MutableStateFlow("")
    val activePhoto = MutableStateFlow<Photo?>(null)

    val isLoadingGemini = MutableStateFlow(false)
    val aiAnalysisError = MutableStateFlow<String?>(null)

    // Reactive stream of photos combined with filters
    val filteredPhotos: StateFlow<List<Photo>> = combine(
        repository.allPhotos,
        selectedCategory,
        searchQuery
    ) { allPhotos, category, query ->
        var result = allPhotos

        // Apply category filter
        if (category == "Favorites") {
            result = result.filter { it.isFavorite }
        } else if (category != "All") {
            result = result.filter { it.category.equals(category, ignoreCase = true) }
        }

        // Apply text query filter
        if (query.isNotEmpty()) {
            result = result.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true) ||
                it.tags.contains(query, ignoreCase = true) ||
                (it.aiCaption?.contains(query, ignoreCase = true) ?: false) ||
                (it.aiLabels?.contains(query, ignoreCase = true) ?: false)
            }
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Preseed initial data if DB is empty
        viewModelScope.launch {
            repository.allPhotos.collect { photosList ->
                if (photosList.isEmpty()) {
                    seedInitialData()
                }
            }
        }
    }

    private suspend fun seedInitialData() = withContext(Dispatchers.IO) {
        val initialPhotos = listOf(
            Photo(
                url = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?q=80&w=800",
                title = "Misty Forest Peak",
                description = "Sunlight piercing through dense pine forest clouds over majestic rolling peaks.",
                category = "Nature",
                cameraModel = "Fujifilm X-T5",
                aperture = "f/4.0",
                iso = "ISO 200",
                tags = "mountains,mist,pine,forest,sunrise"
            ),
            Photo(
                url = "https://images.unsplash.com/photo-1447752875215-b2761acb3c5d?q=80&w=800",
                title = "Sun-drenched Path",
                description = "A peaceful wooden walkway meanders through high summer forest canopies with golden sun flares.",
                category = "Nature",
                cameraModel = "Canon EOS R6 MkII",
                aperture = "f/1.8",
                iso = "ISO 100",
                tags = "canopy,woodland,warmth,sunlight,bridge"
            ),
            Photo(
                url = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=800",
                title = "Nebula Dreams",
                description = "Vibrant clouds of indigo, magenta, and solar gas forming new suns in deep cosmos.",
                category = "Space",
                cameraModel = "JWST (Infrared)",
                aperture = "f/1.2",
                iso = "Scan 2000X",
                tags = "nebula,spacetime,stars,cosmic,galaxy"
            ),
            Photo(
                url = "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?q=80&w=800",
                title = "Planet Horizon",
                description = "Orbital view capture of the delicate crescent curvature of planet Earth enveloped in atmosphere.",
                category = "Space",
                cameraModel = "Custom Orbital Cam",
                aperture = "f/8.0",
                iso = "ISO 250",
                tags = "earth,stars,orbit,iss,space"
            ),
            Photo(
                url = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?q=80&w=800",
                title = "Shard Reflections",
                description = "Sleek obsidian skyscraper geometry reflecting silver sky in geometric architectural balance.",
                category = "Architecture",
                cameraModel = "Sony A7R V",
                aperture = "f/5.6",
                iso = "ISO 100",
                tags = "modernism,skyscraper,steel,geometry,reflection"
            ),
            Photo(
                url = "https://images.unsplash.com/photo-1449034446853-66c86144b0ad?q=80&w=800",
                title = "Golden Gate Fog",
                description = "Dramatically lit red metal piers rising out of mystical morning sea fog.",
                category = "Travel",
                cameraModel = "Leica M11",
                aperture = "f/2.8",
                iso = "ISO 64",
                tags = "bridge,fog,sanfrancisco,travel,mystique"
            ),
            Photo(
                url = "https://images.unsplash.com/photo-1504674900247-0877df9cc836?q=80&w=800",
                title = "Searing Plates",
                description = "Glistening seared ribeye, vibrant herbs, and wine pairing set in cozy tavern dining setting.",
                category = "Food",
                cameraModel = "Hasselblad X2D",
                aperture = "f/2.0",
                iso = "ISO 400",
                tags = "steak,gourmet,dinner,herbs,cozy"
            )
        )
        repository.insertAll(initialPhotos)
    }

    fun toggleFavorite(photo: Photo) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = photo.copy(isFavorite = !photo.isFavorite)
            repository.update(updated)
            // Update active state if opened
            if (activePhoto.value?.id == photo.id) {
                activePhoto.value = updated
            }
        }
    }

    fun updatePhotoTags(photo: Photo, rawTags: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = photo.copy(tags = rawTags)
            repository.update(updated)
            if (activePhoto.value?.id == photo.id) {
                activePhoto.value = updated
            }
        }
    }

    fun deletePhoto(photo: Photo) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(photo)
            if (activePhoto.value?.id == photo.id) {
                activePhoto.value = null
            }
        }
    }

    fun addPhoto(title: String, url: String, category: String, description: String, tags: String, camera: String, aperture: String, iso: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newPhoto = Photo(
                url = url,
                title = title,
                description = description,
                category = category,
                tags = tags,
                cameraModel = camera.ifEmpty { "Unknown Camera" },
                aperture = aperture.ifEmpty { "f/2.8" },
                iso = iso.ifEmpty { "ISO 100" }
            )
            repository.insert(newPhoto)
        }
    }

    fun analyzeWithGemini(photo: Photo) {
        viewModelScope.launch {
            isLoadingGemini.value = true
            aiAnalysisError.value = null
            try {
                val systemPrompt = "You are an art gallery curator. Provide creative interpretations of photographs based on details provided."
                val promptText = """
                    Write a caption and deep artistic review for the photograph:
                    Title: '${photo.title}'
                    User description: '${photo.description}'
                    Category: '${photo.category}'
                    Tags: '${photo.tags}'
                    Camera specs: '${photo.cameraModel}, ${photo.aperture}, ${photo.iso}'
                    
                    Format your response exactly like this:
                    CAPTION: [A majestic one-sentence creative quote or tagline]
                    REVIEW: [A gorgeous 2-3 sentence evocative analysis styled as a high-end gallery placard]
                    MOODS: [Exactly 3-4 comma-separated mood tags, e.g.: Tranquil, Ethereal, Golden, Sylvan]
                """.trimIndent()

                val request = GeminiRequest(
                    contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = promptText)))),
                    systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt)))
                )

                val apiKey = BuildConfig.GEMINI_API_KEY
                val response = GeminiApiClient.service.generateContent(apiKey, request)
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

                if (!responseText.isNullOrEmpty()) {
                    var parsedCaption = "A magnificent showcase of visual photography."
                    var parsedReview = "This beautiful frame captures standard elegance, celebrating raw visual presence."
                    var parsedMoods = "Vivid, Evocative, Aesthetic"

                    // Multi-line parser
                    val lines = responseText.split("\n")
                    for (line in lines) {
                        val trimmed = line.trim()
                        if (trimmed.startsWith("CAPTION:", ignoreCase = true)) {
                            parsedCaption = trimmed.substringAfter("CAPTION:").trim()
                        } else if (trimmed.startsWith("REVIEW:", ignoreCase = true)) {
                            parsedReview = trimmed.substringAfter("REVIEW:").trim()
                        } else if (trimmed.startsWith("MOODS:", ignoreCase = true)) {
                            parsedMoods = trimmed.substringAfter("MOODS:").trim()
                        }
                    }

                    val updatedPhoto = photo.copy(
                        aiCaption = parsedCaption,
                        aiLabels = "$parsedReview|$parsedMoods" // Combine for easy extraction
                    )

                    withContext(Dispatchers.IO) {
                        repository.update(updatedPhoto)
                    }
                    activePhoto.value = updatedPhoto
                } else {
                    aiAnalysisError.value = "Empty response from AI engine."
                }
            } catch (e: Exception) {
                aiAnalysisError.value = "API Call Failed: ${e.localizedMessage ?: "Network Timeout"}"
            } finally {
                isLoadingGemini.value = false
            }
        }
    }
}
