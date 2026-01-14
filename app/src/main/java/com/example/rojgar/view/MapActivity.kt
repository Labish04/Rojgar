package com.example.rojgar.view

import android.content.Context
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.rojgar.model.CompanyModel
import com.example.rojgar.repository.CompanyRepoImpl
import com.example.rojgar.view.ui.theme.RojgarTheme
import com.example.rojgar.viewmodel.CompanyViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MapActivity : ComponentActivity() {

    private lateinit var viewModel: CompanyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = CompanyViewModel(CompanyRepoImpl())

        setContent {
            RojgarTheme {
                MapScreen(viewModel = viewModel, context = this)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(viewModel: CompanyViewModel, context: Context) {
    val companies by viewModel.allCompanies.observeAsState(initial = emptyList())
    val loading by viewModel.loading.observeAsState(initial = false)

    var companiesWithLocations by remember { mutableStateOf<List<CompanyModel>>(emptyList()) }
    var isGeocoding by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchResults by remember { mutableStateOf(false) }
    var selectedCompany by remember { mutableStateOf<CompanyModel?>(null) }

    val scope = rememberCoroutineScope()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(27.7172, 85.3240), 12f)
    }

    // Fetch companies
    LaunchedEffect(Unit) {
        Log.d("MapActivity", "Fetching companies...")
        viewModel.fetchAllCompaniesForMap()
    }

    // Geocode addresses
    LaunchedEffect(companies) {
        if (companies.isNotEmpty() && !isGeocoding) {
            isGeocoding = true
            Log.d("MapActivity", "Companies received: ${companies.size}")

            scope.launch(Dispatchers.IO) {
                try {
                    val updatedCompanies = companies.map { company ->
                        try {
                            if (company.latitude == 0.0 && company.longitude == 0.0 && company.companyLocation.isNotEmpty()) {
                                Log.d("MapActivity", "Geocoding: ${company.companyName} - ${company.companyLocation}")
                                val latLng = geocodeAddress(context, company.companyLocation)
                                if (latLng != null) {
                                    Log.d("MapActivity", "Success: ${company.companyName} -> $latLng")
                                    company.copy(latitude = latLng.latitude, longitude = latLng.longitude)
                                } else {
                                    Log.w("MapActivity", "Failed to geocode: ${company.companyLocation}")
                                    company
                                }
                            } else {
                                company
                            }
                        } catch (e: Exception) {
                            Log.e("MapActivity", "Error processing: ${company.companyName}", e)
                            company
                        }
                    }

                    withContext(Dispatchers.Main) {
                        companiesWithLocations = updatedCompanies
                        isGeocoding = false
                        Log.d("MapActivity", "Total companies with valid locations: ${updatedCompanies.count { it.latitude != 0.0 && it.longitude != 0.0 }}")
                    }
                } catch (e: Exception) {
                    Log.e("MapActivity", "Geocoding error", e)
                    withContext(Dispatchers.Main) {
                        companiesWithLocations = companies
                        isGeocoding = false
                    }
                }
            }
        }
    }

    // Filter companies based on search
    val filteredCompanies = remember(companiesWithLocations, searchQuery) {
        if (searchQuery.isBlank()) {
            companiesWithLocations
        } else {
            companiesWithLocations.filter { company ->
                company.companyName.contains(searchQuery, ignoreCase = true) ||
                        company.companyLocation.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Move camera to selected company
    LaunchedEffect(selectedCompany) {
        selectedCompany?.let { company ->
            if (company.latitude != 0.0 && company.longitude != 0.0) {
                val position = LatLng(company.latitude, company.longitude)
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(position, 15f),
                    durationMs = 1000
                )
            }
        }
    }

    Scaffold(
        topBar = {
            ModernTopBar()
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Google Map
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = false),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    zoomGesturesEnabled = true,
                    scrollGesturesEnabled = true
                )
            ) {
                filteredCompanies.forEach { company ->
                    if (company.latitude != 0.0 && company.longitude != 0.0) {
                        val position = LatLng(company.latitude, company.longitude)
                        val isSelected = selectedCompany?.companyId == company.companyId

                        Marker(
                            state = MarkerState(position = position),
                            title = company.companyName,
                            snippet = company.companyLocation,
                            icon = BitmapDescriptorFactory.defaultMarker(
                                if (isSelected) BitmapDescriptorFactory.HUE_BLUE
                                else BitmapDescriptorFactory.HUE_RED
                            ),
                            onClick = {
                                selectedCompany = company
                                true
                            }
                        )
                    }
                }
            }

            // Modern Animated Search Bar
            ModernSearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = {
                    searchQuery = it
                    showSearchResults = it.isNotEmpty()
                },
                onClear = {
                    searchQuery = ""
                    showSearchResults = false
                    selectedCompany = null
                },
                showSearchResults = showSearchResults,
                filteredCompanies = filteredCompanies,
                onCompanySelected = { company ->
                    selectedCompany = company
                    showSearchResults = false
                }
            )

            // Modern Selected Company Info Card
            AnimatedVisibility(
                visible = selectedCompany != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                selectedCompany?.let { company ->
                    ModernCompanyInfoCard(
                        company = company,
                        onClose = { selectedCompany = null }
                    )
                }
            }

            // Loading Indicator
            if (loading || isGeocoding) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(32.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (loading) "Loading companies..." else "Geocoding addresses...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // No companies message
            if (!loading && !isGeocoding && companiesWithLocations.isEmpty()) {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = "No companies found",
                        modifier = Modifier.padding(24.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTopBar() {
    // Animated gradient background
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF1E88E5),
                        Color(0xFF2196F3),
                        Color(0xFF42A5F5),
                        Color(0xFF1E88E5)
                    ),
                    startX = animatedOffset,
                    endX = animatedOffset + 1000f
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                // Animated location icon
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )

                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Company Locations",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Discover nearby opportunities",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun ModernSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    showSearchResults: Boolean,
    filteredCompanies: List<CompanyModel>,
    onCompanySelected: (CompanyModel) -> Unit
) {
    // Animation for search bar appearance
    val searchBarScale by animateFloatAsState(
        targetValue = if (searchQuery.isNotEmpty()) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    val searchBarElevation by animateDpAsState(
        targetValue = if (searchQuery.isNotEmpty()) 12.dp else 6.dp,
        animationSpec = tween(300),
        label = "elevation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Modern Search TextField
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(scaleX = searchBarScale, scaleY = searchBarScale)
                .shadow(
                    elevation = searchBarElevation,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = Color(0xFF2196F3).copy(alpha = 0.5f)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animated search icon
                val infiniteTransition = rememberInfiniteTransition(label = "search")
                val iconRotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = if (searchQuery.isNotEmpty()) 360f else 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "rotation"
                )

                Box(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1E88E5),
                                    Color(0xFF2196F3)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer(rotationZ = if (searchQuery.isNotEmpty()) iconRotation else 0f)
                    )
                }

                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    placeholder = {
                        Text(
                            "Search companies or locations...",
                            color = Color.Gray.copy(alpha = 0.6f),
                            fontSize = 15.sp
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = Color(0xFF2196F3)
                    ),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                )

                // Animated clear button
                AnimatedVisibility(
                    visible = searchQuery.isNotEmpty(),
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEF5350).copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = Color(0xFFEF5350),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Animated Search Results
        AnimatedVisibility(
            visible = showSearchResults && filteredCompanies.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    items(filteredCompanies) { company ->
                        CompanySearchItem(
                            company = company,
                            onClick = { onCompanySelected(company) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompanySearchItem(
    company: CompanyModel,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) Color(0xFF2196F3).copy(alpha = 0.1f) else Color.Transparent,
        animationSpec = tween(200),
        label = "background"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable {
                isPressed = true
                onClick()
            }
            .padding(16.dp)
    ) {
        Text(
            text = company.companyName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E88E5)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = company.companyLocation,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        if (company.latitude == 0.0 && company.longitude == 0.0) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "📍 Location not available",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFEF5350),
                fontWeight = FontWeight.Medium
            )
        }
    }
    Divider(color = Color.Gray.copy(alpha = 0.2f))
}

@Composable
fun ModernCompanyInfoCard(
    company: CompanyModel,
    onClose: () -> Unit
) {
    // Animation for card entrance
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    // Shimmer effect for card
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = Color(0xFF2196F3).copy(alpha = 0.5f),
                ambientColor = Color(0xFF1E88E5).copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Cover Photo Section with Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    // Cover Photo or Gradient Background
                    if (company.companyCoverPhoto.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(company.companyCoverPhoto),
                            contentDescription = "Company Cover",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Dark overlay for better text visibility
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.3f),
                                            Color.Black.copy(alpha = 0.6f)
                                        )
                                    )
                                )
                        )
                    } else {
                        // Animated gradient background if no cover photo
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF0D47A1),
                                            Color(0xFF1565C0),
                                            Color(0xFF1976D2),
                                            Color(0xFF1E88E5),
                                            Color(0xFF2196F3)
                                        ),
                                        start = androidx.compose.ui.geometry.Offset(shimmerOffset, shimmerOffset),
                                        end = androidx.compose.ui.geometry.Offset(shimmerOffset + 1000f, shimmerOffset + 1000f)
                                    )
                                )
                        )
                    }

                    // Close Button with Glass Effect
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.25f))
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // Company Details Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        // Company Info Column
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            // Company Name with Fade In Animation
                            Text(
                                text = company.companyName,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0D47A1),
                                maxLines = 2,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.graphicsLayer(alpha = 1f)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Location with Icon
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF2196F3).copy(alpha = 0.08f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFF2196F3),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = company.companyLocation,
                                    fontSize = 13.sp,
                                    color = Color(0xFF1565C0),
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Company Logo with Animated Border
                        val borderRotation by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(3000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "rotation"
                        )

                        Box(
                            modifier = Modifier
                                .size(85.dp)
                                .graphicsLayer(rotationZ = borderRotation)
                                .background(
                                    Brush.sweepGradient(
                                        colors = listOf(
                                            Color(0xFF2196F3),
                                            Color(0xFF1E88E5),
                                            Color(0xFF1976D2),
                                            Color(0xFF1565C0),
                                            Color(0xFF2196F3)
                                        )
                                    ),
                                    shape = RoundedCornerShape(22.dp)
                                )
                                .padding(3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White)
                                    .graphicsLayer(rotationZ = -borderRotation),
                                contentAlignment = Alignment.Center
                            ) {
                                if (company.companyProfileImage.isNotEmpty()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(company.companyProfileImage),
                                        contentDescription = "Company Logo",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(20.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    // Gradient Placeholder
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(
                                                        Color(0xFF1E88E5),
                                                        Color(0xFF2196F3),
                                                        Color(0xFF42A5F5)
                                                    )
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = company.companyName.firstOrNull()?.uppercase() ?: "C",
                                            fontSize = 36.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Decorative Divider with Pulse Animation
                    val dividerAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 0.7f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "divider"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xFF2196F3).copy(alpha = dividerAlpha),
                                        Color(0xFF1E88E5).copy(alpha = dividerAlpha),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    if (company.companyEmail.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF2196F3).copy(alpha = 0.08f),
                                            Color(0xFF1E88E5).copy(alpha = 0.12f)
                                        )
                                    )
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFF2196F3).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Animated Email Icon
                            val iconScale by infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = 1.15f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1200, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "icon_scale"
                            )

                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFF1E88E5),
                                                Color(0xFF2196F3)
                                            )
                                        )
                                    )
                                    .graphicsLayer(scaleX = iconScale, scaleY = iconScale),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Email Address",
                                    fontSize = 11.sp,
                                    color = Color(0xFF1565C0),
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = company.companyEmail,
                                    fontSize = 14.sp,
                                    color = Color(0xFF0D47A1),
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun geocodeAddress(context: Context, address: String): LatLng? {
    return withContext(Dispatchers.IO) {
        try {
            kotlinx.coroutines.delay(300)

            val geocoder = Geocoder(context, Locale.getDefault())

            // Try different address formats
            val addressVariations = listOf(
                "$address, Kathmandu, Nepal",
                "$address, Nepal",
                address
            )

            for (addressVariant in addressVariations) {
                try {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocationName(addressVariant, 1)

                    if (!addresses.isNullOrEmpty()) {
                        val location = addresses[0]
                        Log.d("MapActivity", "Geocoded '$addressVariant' to: ${location.latitude}, ${location.longitude}")
                        return@withContext LatLng(location.latitude, location.longitude)
                    }
                } catch (e: Exception) {
                    Log.w("MapActivity", "Failed to geocode '$addressVariant': ${e.message}")
                }
            }

            Log.w("MapActivity", "All geocoding attempts failed for: $address")
            null
        } catch (e: Exception) {
            Log.e("MapActivity", "Geocoding error for: $address", e)
            null
        }
    }
}