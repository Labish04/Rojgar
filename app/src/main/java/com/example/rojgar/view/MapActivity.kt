package com.example.rojgar.view

import android.content.Context
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
            TopAppBar(
                title = { Text("Company Locations") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
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

            // Search Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        showSearchResults = it.isNotEmpty()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(8.dp)),
                    placeholder = { Text("Search companies...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                showSearchResults = false
                                selectedCompany = null
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                // Search Results
                if (showSearchResults && filteredCompanies.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                            .padding(top = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        LazyColumn {
                            items(filteredCompanies) { company ->
                                CompanySearchItem(
                                    company = company,
                                    onClick = {
                                        selectedCompany = company
                                        showSearchResults = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Selected Company Info Card
            selectedCompany?.let { company ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = company.companyName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = company.companyLocation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                                if (company.companyEmail.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = company.companyEmail,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                                if (company.companyContactNumber.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = company.companyContactNumber,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                            IconButton(onClick = { selectedCompany = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }
                    }
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

@Composable
fun CompanySearchItem(
    company: CompanyModel,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(
            text = company.companyName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = company.companyLocation,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        if (company.latitude == 0.0 && company.longitude == 0.0) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Location not available",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Red
            )
        }
    }
    Divider()
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