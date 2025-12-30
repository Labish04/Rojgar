package com.example.rojgar.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.rojgar.R
import com.example.rojgar.repository.JobSeekerRepoImpl
import com.example.rojgar.ui.theme.Blue
import com.example.rojgar.ui.theme.DarkBlue2
import com.example.rojgar.viewmodel.JobSeekerViewModel

class JobSeekerProfileDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val jobSeekerViewModel = remember { JobSeekerViewModel(JobSeekerRepoImpl()) }
            JobSeekerProfileDetailsBody(
                jobSeekerViewModel = jobSeekerViewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSeekerProfileDetailsBody(
    jobSeekerViewModel: JobSeekerViewModel
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var currentJobSeeker by remember { mutableStateOf<com.example.rojgar.model.JobSeekerModel?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val currentUser = jobSeekerViewModel.getCurrentJobSeeker()
        if (currentUser != null) {
            isLoading = true
            jobSeekerViewModel.getJobSeekerById(currentUser.uid) { success, message, jobSeeker ->
                isLoading = false
                if (success && jobSeeker != null) {
                    currentJobSeeker = jobSeeker
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    val collapsed = scrollBehavior.state.collapsedFraction > 0.35f
                    if (!collapsed) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(80.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (currentJobSeeker?.profilePhoto?.isNotEmpty() == true) {
                                    AsyncImage(
                                        model = currentJobSeeker!!.profilePhoto,
                                        contentDescription = "Profile Photo",
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop,
                                        placeholder = painterResource(R.drawable.picture)
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(R.drawable.profileemptypic),
                                        contentDescription = "Default Profile",
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            // Name and Bio in expanded state
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                // Name
                                Text(
                                    text = currentJobSeeker?.fullName?.ifEmpty { "Your Name" }
                                        ?: "Your Name",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )

                                // Bio
                                Text(
                                    text = currentJobSeeker?.bio?.ifEmpty {
                                        "Add your bio in Personal Information"
                                    } ?: "Add your bio in Personal Information",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                    maxLines = 2,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    } else {
                        // Collapsed state - show only profile image and name
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Profile Photo - 40dp in collapsed state
                            Box(
                                modifier = Modifier.size(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (currentJobSeeker?.profilePhoto?.isNotEmpty() == true) {
                                    AsyncImage(
                                        model = currentJobSeeker!!.profilePhoto,
                                        contentDescription = "Profile Photo",
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop,
                                        placeholder = painterResource(R.drawable.picture)
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(R.drawable.picture),
                                        contentDescription = "Default Profile",
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            // Name only in collapsed state
                            Text(
                                text = currentJobSeeker?.fullName?.ifEmpty { "Your Name" }
                                    ?: "Your Name",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            val intent = Intent(context, JobSeekerProfileActivity::class.java)
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(
                            painter = painterResource(R.drawable.outline_more_vert_24),
                            contentDescription = "Menu",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = DarkBlue2,
                    scrolledContainerColor = DarkBlue2
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        if (isLoading) {
            // Loading state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Blue)
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Blue)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OptionPanel(
                        text = "Personal Information",
                        leadingIcon = R.drawable.user
                    ) {
                        context.startActivity(
                            Intent(context, JobSeekerPersonalInformationActivity::class.java)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OptionPanel(
                        text = "Objective",
                        leadingIcon = R.drawable.objectiveicon
                    ) {
                        context.startActivity(
                            Intent(context, JobSeekerObjectiveActivity::class.java)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OptionPanel(
                        text = "Experience",
                        leadingIcon = R.drawable.experienceicon
                    ) {
                        context.startActivity(
                            Intent(context, JobSeekerExperienceActivity::class.java)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OptionPanel(
                        text = "Skill",
                        leadingIcon = R.drawable.skillicon
                    ) {
                        context.startActivity(
                            Intent(context, JobSeekerSkillActivity::class.java)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OptionPanel(
                        text = "Education",
                        leadingIcon = R.drawable.educationicon,

                        ) {
                        context.startActivity(
                            Intent(context, JobSeekerEducationActivity::class.java)
                        )

                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OptionPanel(
                        text = "Training",
                        leadingIcon = R.drawable.trainingicon,
                    ) {
                        context.startActivity(
                            Intent(context, JobSeekerTrainingActivity::class.java)
                        )

                    }

                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OptionPanel(
                        text = "Job Preference",
                        leadingIcon = R.drawable.jobpreferenceicon,
                    ) {
                        context.startActivity(
                            Intent(context, JobSeekerJobPreferenceActivity::class.java)
                        )

                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OptionPanel(
                        text = "Portfolio Accounts",
                        leadingIcon = R.drawable.linkicon,
                    ) {
                        context.startActivity(
                            Intent(context, JobSeekerPortfolioAccountsActivity::class.java)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OptionPanel(
                        text = "Language",
                        leadingIcon = R.drawable.languageicon,
                    ) {
                        context.startActivity(
                            Intent(context, JobSeekerLanguageActivity::class.java)
                        )

                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OptionPanel(
                        text = "Reference",
                        leadingIcon = R.drawable.bioicon,
                    ) {
                        context.startActivity(
                            Intent(context, JobSeekerReferenceActivity::class.java)
                        )

                    }
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun OptionPanel(
    text: String,
    leadingIcon: Int,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .height(62.dp)
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = leadingIcon),
                    contentDescription = null,
                    modifier = Modifier.size(38.dp)
                )

                Text(
                    text,
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    modifier = Modifier.padding(start = 10.dp)
                )
            }

            Icon(
                painter = painterResource(R.drawable.outline_keyboard_arrow_right_24),
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Preview
@Composable
fun JobSeekerProfileDetailsBodyPreview() {
    JobSeekerProfileDetailsBody(
        jobSeekerViewModel = JobSeekerViewModel(JobSeekerRepoImpl())
    )
}