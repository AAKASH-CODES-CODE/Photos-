package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.Photo
import com.example.ui.PhotosViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: PhotosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF1A1C1E) // Elegant Dark background
                ) {
                    PhotosApp(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotosApp(viewModel: PhotosViewModel) {
    val photos by viewModel.filteredPhotos.collectAsStateWithLifecycle()
    val selectedCat by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val activePhoto by viewModel.activePhoto.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF1A1C1E),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Photos App Icon",
                            tint = Color(0xFFD1E4FF),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "EpicPhotos",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color(0xFFE2E2E6)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .testTag("add_photo_button")
                            .clip(CircleShape)
                            .background(Color(0xFF2D2F31))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Custom Photo",
                            tint = Color(0xFFD1E4FF)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1C1E),
                    titleContentColor = Color(0xFFE2E2E6)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF1A1C1E))
        ) {
            // Search Input Block
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("search_input_field"),
                placeholder = { Text("Search title, specs, tags, AI reviews...", color = Color(0xFFC4C6CF)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon", tint = Color(0xFFC4C6CF)) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search", tint = Color(0xFFC4C6CF))
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFFE2E2E6),
                    unfocusedTextColor = Color(0xFFE2E2E6),
                    focusedContainerColor = Color(0xFF2D2F31),
                    unfocusedContainerColor = Color(0xFF2D2F31),
                    focusedBorderColor = Color(0xFFD1E4FF),
                    unfocusedBorderColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )

            // Horizontal album categories list selector
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.allCategories) { category ->
                    val isSelected = category == selectedCat
                    val backgroundBrush = if (isSelected) {
                        Brush.horizontalGradient(listOf(Color(0xFF38495F), Color(0xFF38495F)))
                    } else {
                        Brush.horizontalGradient(listOf(Color(0xFF2D2F31), Color(0xFF2D2F31)))
                    }
                    val borderStrokeColor = if (isSelected) Color(0xFFD1E4FF).copy(alpha = 0.3f) else Color(0x1CFFFFFF)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(backgroundBrush)
                            .border(1.dp, borderStrokeColor, RoundedCornerShape(20.dp))
                            .clickable { viewModel.selectedCategory.value = category }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("category_chip_${category.lowercase()}")
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) Color(0xFFD1E4FF) else Color(0xFFC4C6CF),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Beautiful Featured Memory card container under Elegant Dark theme
            if (query.isEmpty() && selectedCat == "All" && photos.isNotEmpty()) {
                val featuredPhoto = photos.first()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF2D2F31))
                        .clickable { viewModel.activePhoto.value = featuredPhoto }
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(featuredPhoto.url)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Featured Memory",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Visual atmospheric gradients from standard specifications
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color(0xDD1A1C1E))
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0x55311B92), Color(0x111A1C1E), Color(0x334A148C))
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "FEATURED MEMORY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD1E4FF),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = featuredPhoto.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Curated Selection",
                            fontSize = 12.sp,
                            color = Color(0xFFC4C6CF)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Photos",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE2E2E6)
                    )
                }
            }

            // Photos Grid
            if (photos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Empty State Icon",
                            tint = Color(0xFFC4C6CF),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No Photos Found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE2E2E6)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try adjusting your search query, selecting another category, or creating a new photo record.",
                            fontSize = 13.sp,
                            color = Color(0xFFC4C6CF),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(photos, key = { it.id }) { photo ->
                        PhotoCardItem(
                            photo = photo,
                            onItemClick = { viewModel.activePhoto.value = photo },
                            onFavToggle = { viewModel.toggleFavorite(photo) }
                        )
                    }
                }
            }
        }
    }

    // Active full-resolution photo detail workspace view dialog
    activePhoto?.let { photo ->
        PhotoDetailDialog(
            photo = photo,
            viewModel = viewModel,
            onDismiss = { viewModel.activePhoto.value = null }
        )
    }

    // Add Photo dialog screen
    if (showAddDialog) {
        AddPhotoDialog(
            categories = viewModel.allCategories.filter { it != "All" && it != "Favorites" },
            onSave = { title, url, category, desc, tags, camera, aperture, iso ->
                viewModel.addPhoto(title, url, category, desc, tags, camera, aperture, iso)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
fun PhotoCardItem(
    photo: Photo,
    onItemClick: () -> Unit,
    onFavToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onItemClick)
            .testTag("photo_card_${photo.id}"),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D2F31)
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photo.url)
                    .crossfade(true)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .build(),
                contentDescription = photo.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )

            // Category badge
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xCC1A1C1E))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .align(Alignment.TopStart)
            ) {
                Text(
                    text = photo.category.uppercase(),
                    color = Color(0xFFD1E4FF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Favorite button
            IconButton(
                onClick = onFavToggle,
                modifier = Modifier
                    .padding(4.dp)
                    .align(Alignment.TopEnd)
                    .background(Color(0x991A1C1E), CircleShape)
                    .size(32.dp)
                    .testTag("fav_toggle_btn_${photo.id}")
            ) {
                Icon(
                    imageVector = if (photo.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Toggle favorite",
                    tint = if (photo.isFavorite) Color(0xFFF85149) else Color(0xFFC4C6CF),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(
                text = photo.title,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFFE2E2E6),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = photo.description,
                fontFamily = FontFamily.SansSerif,
                fontSize = 12.sp,
                color = Color(0xFFC4C6CF),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PhotoDetailDialog(
    photo: Photo,
    viewModel: PhotosViewModel,
    onDismiss: () -> Unit
) {
    val isLoadingAI by viewModel.isLoadingGemini.collectAsStateWithLifecycle()
    val aiError by viewModel.aiAnalysisError.collectAsStateWithLifecycle()

    var tagInput by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1A1C1E)),
            color = Color(0xFF1A1C1E)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Image and Floating Actions
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(Color.Black)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(photo.url)
                            .crossfade(true)
                            .build(),
                        contentDescription = photo.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Toolbar overlay
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .background(Color(0xAA1A1C1E), CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Close detail page", tint = Color.White)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Favorite toggle
                            IconButton(
                                onClick = { viewModel.toggleFavorite(photo) },
                                modifier = Modifier
                                    .background(Color(0xAA1A1C1E), CircleShape)
                                    .size(40.dp)
                                    .testTag("detail_fav_toggle")
                            ) {
                                Icon(
                                    imageVector = if (photo.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Toggle favorite status",
                                    tint = if (photo.isFavorite) Color(0xFFF85149) else Color.White
                                )
                            }

                            // Delete action
                            IconButton(
                                onClick = {
                                    viewModel.deletePhoto(photo)
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .background(Color(0xAA1A1C1E), CircleShape)
                                    .size(40.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete photo", tint = Color(0xFFF85149))
                            }
                        }
                    }
                }

                // Core details text card area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = photo.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE2E2E6)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF38495F))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = photo.category,
                                color = Color(0xFFD1E4FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Date added: Just Now",
                            fontSize = 12.sp,
                            color = Color(0xFFC4C6CF)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = photo.description,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = Color(0xFFC4C6CF)
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                    Divider(color = Color(0x4D44474E))
                    Spacer(modifier = Modifier.height(18.dp))

                    // Camera Metadata Settings
                    Text(
                        text = "EXIF CAMERA INFO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD1E4FF),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2D2F31))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Star, contentDescription = "Camera Icon", tint = Color(0xFFC4C6CF), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "CAMERA", fontSize = 10.sp, color = Color(0xFFC4C6CF))
                            Text(text = photo.cameraModel, fontSize = 12.sp, color = Color(0xFFE2E2E6), fontWeight = FontWeight.Bold)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Settings, contentDescription = "Aperture Icon", tint = Color(0xFFC4C6CF), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "APERTURE", fontSize = 10.sp, color = Color(0xFFC4C6CF))
                            Text(text = photo.aperture, fontSize = 12.sp, color = Color(0xFFE2E2E6), fontWeight = FontWeight.Bold)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Build, contentDescription = "ISO settings Icon", tint = Color(0xFFC4C6CF), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "SENSITIVITY", fontSize = 10.sp, color = Color(0xFFC4C6CF))
                            Text(text = photo.iso, fontSize = 12.sp, color = Color(0xFFE2E2E6), fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Divider(color = Color(0x4D44474E))
                    Spacer(modifier = Modifier.height(20.dp))

                    // Customizable tag manager
                    Text(
                        text = "MANAGE TAGS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD1E4FF),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Tags flow-chips row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val tagsList = photo.tags.split(",").filter { it.isNotEmpty() }
                        if (tagsList.isEmpty()) {
                            Text("No tags added yet. Enter custom tags below.", color = Color(0xFFC4C6CF), fontSize = 12.sp)
                        } else {
                            tagsList.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF2D2F31))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(text = "#$tag", color = Color(0xFFC4C6CF), fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = tagInput,
                            onValueChange = { tagInput = it },
                            placeholder = { Text("Add tag (comma or single)", fontSize = 12.sp, color = Color(0xFFC4C6CF)) },
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFD1E4FF),
                                unfocusedBorderColor = Color(0x4D44474E),
                                focusedContainerColor = Color(0xFF2D2F31),
                                unfocusedContainerColor = Color(0xFF2D2F31)
                            )
                        )

                        Button(
                            onClick = {
                                if (tagInput.isNotBlank()) {
                                    val currentTags = photo.tags
                                    val formattedInput = tagInput.trim().lowercase().replace(" ", "")
                                    val updatedTags = if (currentTags.isEmpty()) formattedInput else "$currentTags,$formattedInput"
                                    viewModel.updatePhotoTags(photo, updatedTags)
                                    tagInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38495F)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("ADD", color = Color(0xFFD1E4FF), fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Divider(color = Color(0x4D44474E))
                    Spacer(modifier = Modifier.height(20.dp))

                    // GEMINI AI ART GALLERY ASSISTANT CARD
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF1E1330), Color(0xFF0D0D19))
                                )
                            )
                            .border(1.dp, Color(0xFF8957E5), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Gemini Icon",
                                        tint = Color(0xFFBC8CFF),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Gemini Art Curator",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFFE2D1F9)
                                    )
                                }

                                if (photo.aiCaption != null) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0x33BC8CFF))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = "ANALYZED", color = Color(0xFFBC8CFF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Powering up creative art critique summaries and aesthetic mood curation tags generated instantly using the Gemini Generative AI.",
                                fontSize = 12.sp,
                                color = Color(0xFF8B949E),
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // If AI labels exist, display them
                            if (photo.aiCaption != null && photo.aiLabels != null) {
                                val separatorIdx = photo.aiLabels.indexOf("|")
                                val aiReview = if (separatorIdx != -1) photo.aiLabels.substring(0, separatorIdx) else photo.aiLabels
                                val aiMoods = if (separatorIdx != -1) photo.aiLabels.substring(separatorIdx + 1) else ""

                                Column {
                                    // Poetic Caption
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0x11BC8CFF))
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = "\"${photo.aiCaption}\"",
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp,
                                            color = Color(0xFFD3BFFF),
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Gallery Review text
                                    Text(
                                        text = "CURATOR'S PLACARD REVIEW:",
                                        fontSize = 10.sp,
                                        color = Color(0xFFBC8CFF),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = aiReview,
                                        fontSize = 13.sp,
                                        color = Color(0xFFECE5F9),
                                        lineHeight = 18.sp
                                    )

                                    if (aiMoods.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "AESTHETIC MOOD TAGS:",
                                            fontSize = 10.sp,
                                            color = Color(0xFFBC8CFF),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            aiMoods.split(",").map { it.trim() }.forEach { mood ->
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(Color(0x228957E5))
                                                        .border(1.dp, Color(0x66BC8CFF), RoundedCornerShape(12.dp))
                                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                                ) {
                                                    Text(text = mood, color = Color(0xFFE2D1F9), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                            }

                            // Error block
                            aiError?.let { err ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0x22FF5A5A))
                                        .padding(8.dp)
                                ) {
                                    Text(text = err, color = Color(0xFFFF8B8B), fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            Button(
                                onClick = { viewModel.analyzeWithGemini(photo) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("gemini_analyze_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF8957E5)
                                ),
                                enabled = !isLoadingAI,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                if (isLoadingAI) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Analyzing photo...", color = Color.White)
                                } else {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (photo.aiCaption != null) "Refresh AI Analysis" else "Generate Creative AI Review",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

@Composable
fun AddPhotoDialog(
    categories: List<String>,
    onSave: (String, String, String, String, String, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(categories.firstOrNull() ?: "Nature") }
    var description by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var camera by remember { mutableStateOf("") }
    var aperture by remember { mutableStateOf("") }
    var iso by remember { mutableStateOf("") }

    var errorText by remember { mutableStateOf<String?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("add_photo_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2F31))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Add Photo Record",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE2E2E6)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Error Msg
                errorText?.let { err ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x33FF5A5A))
                            .padding(8.dp)
                    ) {
                        Text(text = err, color = Color(0xFFFF8B8B), fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Fields
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Photo Title *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("photo_title_input"),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFD1E4FF),
                        unfocusedBorderColor = Color(0x4D44474E),
                        focusedLabelColor = Color(0xFFD1E4FF),
                        unfocusedLabelColor = Color(0xFFC4C6CF)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Image URL *") },
                    placeholder = { Text("e.g. https://images.unsplash.com/...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("photo_url_input"),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFD1E4FF),
                        unfocusedBorderColor = Color(0x4D44474E),
                        focusedLabelColor = Color(0xFFD1E4FF),
                        unfocusedLabelColor = Color(0xFFC4C6CF)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Dropdown Category Selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { dropdownExpanded = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("category_dropdown_btn"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFD1E4FF)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder(true).copy()
                    ) {
                        Text("Category: $category")
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.background(Color(0xFF2D2F31))
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, color = Color.White) },
                                onClick = {
                                    category = cat
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFD1E4FF),
                        unfocusedBorderColor = Color(0x4D44474E),
                        focusedLabelColor = Color(0xFFD1E4FF),
                        unfocusedLabelColor = Color(0xFFC4C6CF)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (comma separated)") },
                    placeholder = { Text("e.g. sunset, beach, dramatic") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFD1E4FF),
                        unfocusedBorderColor = Color(0x4D44474E),
                        focusedLabelColor = Color(0xFFD1E4FF),
                        unfocusedLabelColor = Color(0xFFC4C6CF)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "EXIF CAM SPECS (OPTIONAL)", color = Color(0xFFD1E4FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = camera,
                    onValueChange = { camera = it },
                    label = { Text("Camera Model") },
                    placeholder = { Text("e.g. Leica M11") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFD1E4FF),
                        unfocusedBorderColor = Color(0x4D44474E),
                        focusedLabelColor = Color(0xFFD1E4FF),
                        unfocusedLabelColor = Color(0xFFC4C6CF)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = aperture,
                        onValueChange = { aperture = it },
                        label = { Text("Aperture") },
                        placeholder = { Text("f/1.4") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFD1E4FF),
                            unfocusedBorderColor = Color(0x4D44474E),
                            focusedLabelColor = Color(0xFFD1E4FF),
                            unfocusedLabelColor = Color(0xFFC4C6CF)
                        )
                    )

                    OutlinedTextField(
                        value = iso,
                        onValueChange = { iso = it },
                        label = { Text("Sensitivity") },
                        placeholder = { Text("ISO 200") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFD1E4FF),
                            unfocusedBorderColor = Color(0x4D44474E),
                            focusedLabelColor = Color(0xFFD1E4FF),
                            unfocusedLabelColor = Color(0xFFC4C6CF)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL", color = Color(0xFFC4C6CF))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isBlank() || url.isBlank()) {
                                errorText = "Title and Image URL fields are mandatory."
                            } else {
                                onSave(title, url, category, description, tags, camera, aperture, iso)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38495F)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("save_dialog_btn")
                    ) {
                        Text("SAVE PHOTO", color = Color(0xFFD1E4FF), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
