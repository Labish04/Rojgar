package com.example.rojgar.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.model.JobModel
import com.example.rojgar.repository.JobRepoImpl
import com.example.rojgar.repository.SavedJobRepoImpl
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.DarkBlue2
import com.example.rojgar.viewmodel.JobViewModel
import com.example.rojgar.viewmodel.JobViewModelFactory
import com.example.rojgar.viewmodel.SavedJobViewModel
import com.example.rojgar.viewmodel.SavedJobViewModelFactory
import kotlinx.coroutines.launch

class SavedJobsActivity : ComponentActivity() {

    private val savedJobViewModel by viewModels<SavedJobViewModel> {
        SavedJobViewModelFactory(SavedJobRepoImpl())
    }

    private val jobViewModel by viewModels<JobViewModel> {
        JobViewModelFactory(JobRepoImpl())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SavedJobsBody(
                savedJobViewModel = savedJobViewModel,
                jobViewModel = jobViewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedJobsBody(savedJobViewModel: SavedJobViewModel,
                  jobViewModel: JobViewModel) {


    val savedJobs by savedJobViewModel.savedJobs.observeAsState(emptyList())
    val allJobs by jobViewModel.allJobs.observeAsState(emptyList())
    val loading by savedJobViewModel.loading.observeAsState(false)

    var savedJobDetails by remember { mutableStateOf<List<JobModel>>(emptyList()) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(savedJobs) {
        if (savedJobs.isNotEmpty()) {
            coroutineScope.launch {
                val jobs = mutableListOf<JobModel>()
                savedJobs.forEach { savedJob ->
                    jobViewModel.getJobPostById(savedJob.jobId)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        savedJobViewModel.loadSavedJobs()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Saved Jobs",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBlue2,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (savedJobs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No Saved Jobs",
                            fontSize = 18.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Save jobs to view them here",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        Text(
                            text = "${savedJobs.size} saved jobs",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // You would need to fetch job details here
                    // For now, showing saved job IDs
                    items(savedJobs) { savedJob ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Job ID: ${savedJob.jobId}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Saved on: ${formatDate(savedJob.savedAt)}",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatsDate(timestamp: Long): String {
    return try {
        val date = java.util.Date(timestamp)
        val formatter = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
        formatter.format(date)
    } catch (e: Exception) {
        ""
    }
}
