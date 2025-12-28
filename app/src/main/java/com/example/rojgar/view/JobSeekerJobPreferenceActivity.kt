package com.example.rojgar.view

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.R
import com.example.rojgar.model.PreferenceModel
import com.example.rojgar.repository.JobSeekerRepoImpl
import com.example.rojgar.ui.theme.DarkBlue2
import com.example.rojgar.ui.theme.White
import com.example.rojgar.viewmodel.JobSeekerViewModel
import com.example.rojgar.viewmodel.PreferenceViewModel
import kotlinx.coroutines.launch

class JobSeekerJobPreferenceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JobSeekerJobPreferenceBody()
        }
    }
}

data class PreferenceItem(
    val name: String,
    var isSelected: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSeekerJobPreferenceBody() {

    val context = LocalContext.current
    val activity = context as Activity

    // Initialize ViewModels
    val preferenceViewModel = remember { PreferenceViewModel() }
    val jobSeekerViewModel = remember { JobSeekerViewModel(JobSeekerRepoImpl()) }
    val currentUser = jobSeekerViewModel.getCurrentJobSeeker()

    // Observing ViewModel states
    val preferenceData by preferenceViewModel.preferenceData.observeAsState()
    val isLoading by preferenceViewModel.loading.observeAsState(false)

    // UI states
    var showBottomSheet by remember { mutableStateOf(false) }
    var currentSection by remember { mutableStateOf("category") }
    val scope = rememberCoroutineScope()

    // Job preferences state
    var selectedCategories by remember { mutableStateOf(listOf<String>()) }
    var selectedIndustries by remember { mutableStateOf(listOf<String>()) }
    var selectedJobTitles by remember { mutableStateOf(listOf<String>()) }
    var selectedAvailability by remember { mutableStateOf(listOf<String>()) }
    var locationInput by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Fetch existing preferences when activity starts
    LaunchedEffect(Unit) {
        currentUser?.uid?.let { userId ->
            preferenceViewModel.getPreference(userId)
        }
    }

    // Update UI when preference data changes
    LaunchedEffect(preferenceData) {
        preferenceData?.let { preference ->
            selectedCategories = preference.categories
            selectedIndustries = preference.industries
            selectedJobTitles = preference.titles
            selectedAvailability = preference.availabilities
            locationInput = preference.location
        }
    }

    Scaffold(
        topBar = {
            Card(
                modifier = Modifier
                    .height(140.dp)
                    .padding(top = 55.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(5.dp),
                colors = CardDefaults.cardColors(containerColor = DarkBlue2),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(onClick = {
                        activity.finish()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.outline_arrow_back_ios_24),
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(70.dp))
                    Text(
                        "Job Preference",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(White)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "Define your job preferences to receive more relevant and tailored job matches.",
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Job Category Display
            PreferenceField(
                label = "Job Category",
                selectedItems = selectedCategories,
                onClick = {
                    currentSection = "category"
                    showBottomSheet = true
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Industry Display
            PreferenceField(
                label = "Industry",
                selectedItems = selectedIndustries,
                onClick = {
                    currentSection = "industry"
                    showBottomSheet = true
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Job Title Display
            PreferenceField(
                label = "Job Title",
                selectedItems = selectedJobTitles,
                onClick = {
                    currentSection = "title"
                    showBottomSheet = true
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Available For Display
            PreferenceField(
                label = "Available For",
                selectedItems = selectedAvailability,
                onClick = {
                    currentSection = "availability"
                    showBottomSheet = true
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Location
            Column {
                Text(
                    text = "Job Preference Location",
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = locationInput,
                    onValueChange = { locationInput = it },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.locationicon),
                            contentDescription = "Location",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    placeholder = { Text("Enter job preference location", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = White,
                        unfocusedContainerColor = White,
                        focusedBorderColor = DarkBlue2,
                        unfocusedBorderColor = Color.Gray
                    )
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Bottom Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Back Button
                OutlinedButton(
                    onClick = { activity.finish() },
                    shape = RoundedCornerShape(15.dp),
                    modifier = Modifier
                        .weight(0.7f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.LightGray,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_arrow_back_ios_24),
                        contentDescription = "Back",
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(10.dp))

                Button(
                    onClick = {
                        scope.launch {
                            if (currentUser != null) {
                                val existingPreference = preferenceData

                                val preference = PreferenceModel(
                                    preferenceId = existingPreference?.preferenceId ?: "",
                                    categories = selectedCategories,
                                    industries = selectedIndustries,
                                    titles = selectedJobTitles,
                                    availabilities = selectedAvailability,
                                    location = locationInput,
                                    jobSeekerId = currentUser.uid
                                )

                                if (existingPreference == null) {
                                    // Save new preference
                                    preferenceViewModel.savePreference(preference) { success, message ->
                                        if (success) {
                                            Toast.makeText(context, "Preferences saved successfully!", Toast.LENGTH_SHORT).show()
                                            activity.finish()
                                        } else {
                                            Toast.makeText(context, "Failed to save: $message", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    // Update existing preference
                                    preferenceViewModel.updatePreference(preference) { success, message ->
                                        if (success) {
                                            Toast.makeText(context, "Preferences updated successfully!", Toast.LENGTH_SHORT).show()
                                            activity.finish()
                                        } else {
                                            Toast.makeText(context, "Failed to update: $message", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(context, "No user logged in", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(0.7f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkBlue2,
                        contentColor = Color.White
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = if (preferenceData == null) "Save" else "Update",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Bottom Sheet (Only for category, industry, title, availability)
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = Color.White,
            ) {
                JobPreferenceBottomSheet(
                    initialSection = currentSection,
                    onDismiss = { showBottomSheet = false },
                    onSave = { categories, industries, titles, availability ->
                        selectedCategories = categories
                        selectedIndustries = industries
                        selectedJobTitles = titles
                        selectedAvailability = availability
                        showBottomSheet = false
                    },
                    initialCategories = selectedCategories,
                    initialIndustries = selectedIndustries,
                    initialTitles = selectedJobTitles,
                    initialAvailability = selectedAvailability
                )
            }
        }
    }
}

@Composable
fun PreferenceField(
    label: String,
    selectedItems: List<String>,
    onClick: () -> Unit
) {
    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.DarkGray,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(10.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = ButtonDefaults.outlinedButtonBorder
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedItems.isEmpty()) {
                    Text(
                        text = "Select $label",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text(
                        text = selectedItems.joinToString(", "),
                        color = Color.Black,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 2
                    )
                }
                Icon(
                    painter = painterResource(R.drawable.outline_keyboard_arrow_down_24),
                    contentDescription = "Dropdown",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun JobPreferenceBottomSheet(
    initialSection: String,
    onDismiss: () -> Unit,
    onSave: (List<String>, List<String>, List<String>, List<String>) -> Unit,
    initialCategories: List<String>,
    initialIndustries: List<String>,
    initialTitles: List<String>,
    initialAvailability: List<String>
) {
    var currentSection by remember { mutableStateOf(initialSection) }

    // Job Category list
    val categoryList = remember {
        mutableStateListOf(
            PreferenceItem("Creative / Graphics / Designing", initialCategories.contains("Creative / Graphics / Designing")),
            PreferenceItem("UI / UX Design", initialCategories.contains("UI / UX Design")),
            PreferenceItem("Animation / VFX", initialCategories.contains("Animation / VFX")),
            PreferenceItem("Photography / Videography", initialCategories.contains("Photography / Videography")),
            PreferenceItem("Fashion / Textile Designing", initialCategories.contains("Fashion / Textile Designing")),
            PreferenceItem("Architecture / Interior Designing", initialCategories.contains("Architecture / Interior Designing")),
            PreferenceItem("IT & Telecommunication", initialCategories.contains("IT & Telecommunication")),
            PreferenceItem("Software Development", initialCategories.contains("Software Development")),
            PreferenceItem("Web Development", initialCategories.contains("Web Development")),
            PreferenceItem("Mobile App Development", initialCategories.contains("Mobile App Development")),
            PreferenceItem("Data Science / AI / ML", initialCategories.contains("Data Science / AI / ML")),
            PreferenceItem("Cyber Security", initialCategories.contains("Cyber Security")),
            PreferenceItem("Network / System Administration", initialCategories.contains("Network / System Administration")),
            PreferenceItem("DevOps / Cloud Computing", initialCategories.contains("DevOps / Cloud Computing")),
            PreferenceItem("QA / Software Testing", initialCategories.contains("QA / Software Testing")),
            PreferenceItem("General Management", initialCategories.contains("General Management")),
            PreferenceItem("Project Management", initialCategories.contains("Project Management")),
            PreferenceItem("Operations Management", initialCategories.contains("Operations Management")),
            PreferenceItem("Business Development", initialCategories.contains("Business Development")),
            PreferenceItem("Human Resource / HR", initialCategories.contains("Human Resource / HR")),
            PreferenceItem("Administration / Office Support", initialCategories.contains("Administration / Office Support")),
            PreferenceItem("Accounting / Finance", initialCategories.contains("Accounting / Finance")),
            PreferenceItem("Banking / Insurance / Financial Services", initialCategories.contains("Banking / Insurance / Financial Services")),
            PreferenceItem("Audit / Tax / Compliance", initialCategories.contains("Audit / Tax / Compliance")),
            PreferenceItem("Investment / Wealth Management", initialCategories.contains("Investment / Wealth Management")),
            PreferenceItem("Sales / Public Relations", initialCategories.contains("Sales / Public Relations")),
            PreferenceItem("Marketing / Advertising", initialCategories.contains("Marketing / Advertising")),
            PreferenceItem("Digital Marketing", initialCategories.contains("Digital Marketing")),
            PreferenceItem("Content Writing / Copywriting", initialCategories.contains("Content Writing / Copywriting")),
            PreferenceItem("Media / Journalism", initialCategories.contains("Media / Journalism")),
            PreferenceItem("Customer Service / Call Center", initialCategories.contains("Customer Service / Call Center")),
            PreferenceItem("Construction / Engineering / Architects", initialCategories.contains("Construction / Engineering / Architects")),
            PreferenceItem("Civil Engineering", initialCategories.contains("Civil Engineering")),
            PreferenceItem("Mechanical Engineering", initialCategories.contains("Mechanical Engineering")),
            PreferenceItem("Electrical / Electronics Engineering", initialCategories.contains("Electrical / Electronics Engineering")),
            PreferenceItem("Manufacturing / Production", initialCategories.contains("Manufacturing / Production")),
            PreferenceItem("Maintenance / Technician", initialCategories.contains("Maintenance / Technician")),
            PreferenceItem("Commercial / Logistics / Supply Chain", initialCategories.contains("Commercial / Logistics / Supply Chain")),
            PreferenceItem("Procurement / Purchasing", initialCategories.contains("Procurement / Purchasing")),
            PreferenceItem("Warehouse / Distribution", initialCategories.contains("Warehouse / Distribution")),
            PreferenceItem("Drivers / Delivery", initialCategories.contains("Drivers / Delivery")),
            PreferenceItem("Healthcare / Medical", initialCategories.contains("Healthcare / Medical")),
            PreferenceItem("Nursing / Caregiving", initialCategories.contains("Nursing / Caregiving")),
            PreferenceItem("Pharmacy", initialCategories.contains("Pharmacy")),
            PreferenceItem("Laboratory / Research", initialCategories.contains("Laboratory / Research")),
            PreferenceItem("Public Health", initialCategories.contains("Public Health")),
            PreferenceItem("Teaching / Education", initialCategories.contains("Teaching / Education")),
            PreferenceItem("Training / Coaching", initialCategories.contains("Training / Coaching")),
            PreferenceItem("Academic Research", initialCategories.contains("Academic Research")),
            PreferenceItem("Hotel / Hospitality", initialCategories.contains("Hotel / Hospitality")),
            PreferenceItem("Travel / Tourism", initialCategories.contains("Travel / Tourism")),
            PreferenceItem("Food & Beverage", initialCategories.contains("Food & Beverage")),
            PreferenceItem("Event Management", initialCategories.contains("Event Management")),
            PreferenceItem("Government Jobs", initialCategories.contains("Government Jobs")),
            PreferenceItem("Legal / Law / Compliance", initialCategories.contains("Legal / Law / Compliance")),
            PreferenceItem("NGO / INGO / Social Work", initialCategories.contains("NGO / INGO / Social Work")),
            PreferenceItem("Public Administration / Policy", initialCategories.contains("Public Administration / Policy")),
            PreferenceItem("Skilled Labor / Trades", initialCategories.contains("Skilled Labor / Trades")),
            PreferenceItem("Security Services", initialCategories.contains("Security Services")),
            PreferenceItem("Cleaning / Housekeeping", initialCategories.contains("Cleaning / Housekeeping")),
            PreferenceItem("Agriculture / Farming", initialCategories.contains("Agriculture / Farming"))
        )
    }

    // Industries list
    val industryList = remember {
        mutableStateListOf(
            PreferenceItem("Software Companies", initialIndustries.contains("Software Companies")),
            PreferenceItem("Information / Computer / Technology", initialIndustries.contains("Information / Computer / Technology")),
            PreferenceItem("IT Services / Consulting", initialIndustries.contains("IT Services / Consulting")),
            PreferenceItem("Telecommunication", initialIndustries.contains("Telecommunication")),
            PreferenceItem("AI / Data / Cloud Services", initialIndustries.contains("AI / Data / Cloud Services")),
            PreferenceItem("Cyber Security Services", initialIndustries.contains("Cyber Security Services")),
            PreferenceItem("Manufacturing / Production", initialIndustries.contains("Manufacturing / Production")),
            PreferenceItem("Industrial Production", initialIndustries.contains("Industrial Production")),
            PreferenceItem("Textile / Garment Industry", initialIndustries.contains("Textile / Garment Industry")),
            PreferenceItem("Food & Beverage Manufacturing", initialIndustries.contains("Food & Beverage Manufacturing")),
            PreferenceItem("Pharmaceutical Manufacturing", initialIndustries.contains("Pharmaceutical Manufacturing")),
            PreferenceItem("Construction / Infrastructure", initialIndustries.contains("Construction / Infrastructure")),
            PreferenceItem("Civil Engineering Companies", initialIndustries.contains("Civil Engineering Companies")),
            PreferenceItem("Architecture / Interior Designing", initialIndustries.contains("Architecture / Interior Designing")),
            PreferenceItem("Mechanical / Electrical Engineering", initialIndustries.contains("Mechanical / Electrical Engineering")),
            PreferenceItem("Banking / Financial Institutions", initialIndustries.contains("Banking / Financial Institutions")),
            PreferenceItem("Insurance Companies", initialIndustries.contains("Insurance Companies")),
            PreferenceItem("Audit Firms / Tax Consultant", initialIndustries.contains("Audit Firms / Tax Consultant")),
            PreferenceItem("Microfinance / Cooperative", initialIndustries.contains("Microfinance / Cooperative")),
            PreferenceItem("Investment / Brokerage Firms", initialIndustries.contains("Investment / Brokerage Firms")),
            PreferenceItem("Trading / Wholesale", initialIndustries.contains("Trading / Wholesale")),
            PreferenceItem("Retail Industry", initialIndustries.contains("Retail Industry")),
            PreferenceItem("E-Commerce Companies", initialIndustries.contains("E-Commerce Companies")),
            PreferenceItem("Import / Export", initialIndustries.contains("Import / Export")),
            PreferenceItem("Logistics / Supply Chain", initialIndustries.contains("Logistics / Supply Chain")),
            PreferenceItem("Transportation / Courier Services", initialIndustries.contains("Transportation / Courier Services")),
            PreferenceItem("Warehouse / Distribution", initialIndustries.contains("Warehouse / Distribution")),
            PreferenceItem("Hotel / Resort", initialIndustries.contains("Hotel / Resort")),
            PreferenceItem("Travel / Tourism", initialIndustries.contains("Travel / Tourism")),
            PreferenceItem("Restaurant / Cafe", initialIndustries.contains("Restaurant / Cafe")),
            PreferenceItem("Event Management", initialIndustries.contains("Event Management")),
            PreferenceItem("Marketing / Advertising Agencies", initialIndustries.contains("Marketing / Advertising Agencies")),
            PreferenceItem("Digital Marketing Agencies", initialIndustries.contains("Digital Marketing Agencies")),
            PreferenceItem("Designing / Printing / Publishing", initialIndustries.contains("Designing / Printing / Publishing")),
            PreferenceItem("Media / Broadcasting", initialIndustries.contains("Media / Broadcasting")),
            PreferenceItem("Content / Creative Studios", initialIndustries.contains("Content / Creative Studios")),
            PreferenceItem("Hospitals / Clinics", initialIndustries.contains("Hospitals / Clinics")),
            PreferenceItem("Healthcare Services", initialIndustries.contains("Healthcare Services")),
            PreferenceItem("Pharmaceutical Companies", initialIndustries.contains("Pharmaceutical Companies")),
            PreferenceItem("Medical Equipment Suppliers", initialIndustries.contains("Medical Equipment Suppliers")),
            PreferenceItem("Schools / Colleges", initialIndustries.contains("Schools / Colleges")),
            PreferenceItem("Universities / Academic Institutions", initialIndustries.contains("Universities / Academic Institutions")),
            PreferenceItem("Training / Coaching Institutes", initialIndustries.contains("Training / Coaching Institutes")),
            PreferenceItem("EdTech Companies", initialIndustries.contains("EdTech Companies")),
            PreferenceItem("Government Organizations", initialIndustries.contains("Government Organizations")),
            PreferenceItem("NGO / INGO / Development Projects", initialIndustries.contains("NGO / INGO / Development Projects")),
            PreferenceItem("Legal / Law Firms", initialIndustries.contains("Legal / Law Firms")),
            PreferenceItem("Public Sector Enterprises", initialIndustries.contains("Public Sector Enterprises")),
            PreferenceItem("Associations", initialIndustries.contains("Associations")),
            PreferenceItem("Agriculture / Farming", initialIndustries.contains("Agriculture / Farming")),
            PreferenceItem("Agro-Based Industries", initialIndustries.contains("Agro-Based Industries")),
            PreferenceItem("Dairy / Poultry / Livestock", initialIndustries.contains("Dairy / Poultry / Livestock")),
            PreferenceItem("Renewable Energy / Power", initialIndustries.contains("Renewable Energy / Power")),
            PreferenceItem("Consulting Firms", initialIndustries.contains("Consulting Firms")),
            PreferenceItem("Human Resource / Recruitment Agencies", initialIndustries.contains("Human Resource / Recruitment Agencies")),
            PreferenceItem("Security Services", initialIndustries.contains("Security Services")),
            PreferenceItem("Facility Management / Cleaning Services", initialIndustries.contains("Facility Management / Cleaning Services")),
            PreferenceItem("Startup / Private Companies", initialIndustries.contains("Startup / Private Companies"))
        )
    }


    // Job Titles list
    val jobTitleList = remember {
        mutableStateListOf(
            PreferenceItem("Baker", initialTitles.contains("Baker")),
            PreferenceItem("Pastry Chef", initialTitles.contains("Pastry Chef")),
            PreferenceItem("Sous Chef", initialTitles.contains("Sous Chef")),
            PreferenceItem("Banquet Sous Chef", initialTitles.contains("Banquet Sous Chef")),
            PreferenceItem("Executive Chef", initialTitles.contains("Executive Chef")),
            PreferenceItem("Cook / Line Cook", initialTitles.contains("Cook / Line Cook")),
            PreferenceItem("Restaurant Manager", initialTitles.contains("Restaurant Manager")),
            PreferenceItem("Banquet and Event Manager", initialTitles.contains("Banquet and Event Manager")),
            PreferenceItem("Hotel Manager", initialTitles.contains("Hotel Manager")),
            PreferenceItem("Food & Beverage Supervisor", initialTitles.contains("Food & Beverage Supervisor")),
            PreferenceItem("Event Coordinator", initialTitles.contains("Event Coordinator")),
            PreferenceItem("Event Manager", initialTitles.contains("Event Manager")),
            PreferenceItem("Basketball Coach", initialTitles.contains("Basketball Coach")),
            PreferenceItem("Futsal Coach", initialTitles.contains("Futsal Coach")),
            PreferenceItem("Sports Trainer", initialTitles.contains("Sports Trainer")),
            PreferenceItem("Fitness Instructor", initialTitles.contains("Fitness Instructor")),
            PreferenceItem("Backend Developer", initialTitles.contains("Backend Developer")),
            PreferenceItem("Backend Engineer", initialTitles.contains("Backend Engineer")),
            PreferenceItem("Frontend Developer", initialTitles.contains("Frontend Developer")),
            PreferenceItem("Full Stack Developer", initialTitles.contains("Full Stack Developer")),
            PreferenceItem("Mobile Application Developer", initialTitles.contains("Mobile Application Developer")),
            PreferenceItem("Software Engineer", initialTitles.contains("Software Engineer")),
            PreferenceItem("DevOps Engineer", initialTitles.contains("DevOps Engineer")),
            PreferenceItem("QA Engineer", initialTitles.contains("QA Engineer")),
            PreferenceItem("Automation Test Engineer", initialTitles.contains("Automation Test Engineer")),
            PreferenceItem("Associate Database Administrator", initialTitles.contains("Associate Database Administrator")),
            PreferenceItem("Database Administrator", initialTitles.contains("Database Administrator")),
            PreferenceItem("Data Analyst", initialTitles.contains("Data Analyst")),
            PreferenceItem("Data Engineer", initialTitles.contains("Data Engineer")),
            PreferenceItem("System Administrator", initialTitles.contains("System Administrator")),
            PreferenceItem("Network Engineer", initialTitles.contains("Network Engineer")),
            PreferenceItem("Cloud Engineer", initialTitles.contains("Cloud Engineer")),
            PreferenceItem("UI/UX Designer", initialTitles.contains("UI/UX Designer")),
            PreferenceItem("Graphic Designer", initialTitles.contains("Graphic Designer")),
            PreferenceItem("Motion Graphics Designer", initialTitles.contains("Motion Graphics Designer")),
            PreferenceItem("Video Editor", initialTitles.contains("Video Editor")),
            PreferenceItem("Content Creator", initialTitles.contains("Content Creator")),
            PreferenceItem("Sales Executive", initialTitles.contains("Sales Executive")),
            PreferenceItem("Marketing Officer", initialTitles.contains("Marketing Officer")),
            PreferenceItem("Digital Marketing Specialist", initialTitles.contains("Digital Marketing Specialist")),
            PreferenceItem("Business Development Officer", initialTitles.contains("Business Development Officer")),
            PreferenceItem("Account Manager", initialTitles.contains("Account Manager")),
            PreferenceItem("Customer Service Representative", initialTitles.contains("Customer Service Representative")),
            PreferenceItem("Accountant", initialTitles.contains("Accountant")),
            PreferenceItem("Accounts Officer", initialTitles.contains("Accounts Officer")),
            PreferenceItem("Finance Manager", initialTitles.contains("Finance Manager")),
            PreferenceItem("Audit Associate", initialTitles.contains("Audit Associate")),
            PreferenceItem("Tax Consultant", initialTitles.contains("Tax Consultant")),
            PreferenceItem("Administrative Officer", initialTitles.contains("Administrative Officer")),
            PreferenceItem("Office Assistant", initialTitles.contains("Office Assistant")),
            PreferenceItem("Staff Nurse", initialTitles.contains("Staff Nurse")),
            PreferenceItem("Medical Officer", initialTitles.contains("Medical Officer")),
            PreferenceItem("Pharmacist", initialTitles.contains("Pharmacist")),
            PreferenceItem("Lab Technician", initialTitles.contains("Lab Technician")),
            PreferenceItem("Healthcare Assistant", initialTitles.contains("Healthcare Assistant")),
            PreferenceItem("Civil Engineer", initialTitles.contains("Civil Engineer")),
            PreferenceItem("Site Engineer", initialTitles.contains("Site Engineer")),
            PreferenceItem("Mechanical Engineer", initialTitles.contains("Mechanical Engineer")),
            PreferenceItem("Electrical Engineer", initialTitles.contains("Electrical Engineer")),
            PreferenceItem("Maintenance Technician", initialTitles.contains("Maintenance Technician")),
            PreferenceItem("Intern", initialTitles.contains("Intern")),
            PreferenceItem("Trainee", initialTitles.contains("Trainee")),
            PreferenceItem("Junior Executive", initialTitles.contains("Junior Executive")),
            PreferenceItem("Assistant Manager", initialTitles.contains("Assistant Manager")),
            PreferenceItem("Operations Executive", initialTitles.contains("Operations Executive"))
        )
    }


    // Available For list
    val availabilityList = remember {
        mutableStateListOf(
            PreferenceItem("Full Time", initialAvailability.contains("Full Time")),
            PreferenceItem("Part Time", initialAvailability.contains("Part Time")),
            PreferenceItem("Contract", initialAvailability.contains("Contract")),
            PreferenceItem("Temporary", initialAvailability.contains("Temporary")),
            PreferenceItem("Seasonal", initialAvailability.contains("Seasonal")),
            PreferenceItem("Freelance", initialAvailability.contains("Freelance")),
            PreferenceItem("Remote", initialAvailability.contains("Remote")),
            PreferenceItem("Hybrid", initialAvailability.contains("Hybrid")),
            PreferenceItem("On-site", initialAvailability.contains("On-site")),
            PreferenceItem("Internship", initialAvailability.contains("Internship")),
            PreferenceItem("Traineeship", initialAvailability.contains("Traineeship")),
            PreferenceItem("Apprenticeship", initialAvailability.contains("Apprenticeship")),
            PreferenceItem("Graduate Program", initialAvailability.contains("Graduate Program")),
            PreferenceItem("Volunteer", initialAvailability.contains("Volunteer")),
            PreferenceItem("Shift Based", initialAvailability.contains("Shift Based")),
            PreferenceItem("Project Based", initialAvailability.contains("Project Based"))
        )
    }


    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.6f)
            .padding(horizontal = 20.dp)
    ) {
        // Header with tabs
        when (currentSection) {
            "category" -> SectionHeader(
                title = "Preferred Job Category",
                subtitle = "You can add upto 5 category.",
                count = categoryList.count { it.isSelected },
                maxCount = 5
            )
            "industry" -> SectionHeader(
                title = "Preferred Job Industry",
                subtitle = "You can add upto 5 industry.",
                count = industryList.count { it.isSelected },
                maxCount = 5
            )
            "title" -> SectionHeader(
                title = "Select Job Title",
                subtitle = "You can add upto 5 job title.",
                count = jobTitleList.count { it.isSelected },
                maxCount = 5
            )
            "availability" -> SectionHeader(
                title = "Available For",
                subtitle = "",
                count = availabilityList.count { it.isSelected },
                maxCount = 7
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search bar
        SearchBar(
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            placeholder = when (currentSection) {
                "category" -> "Search preferred job categories"
                "industry" -> "Search preferred job industry"
                "title" -> "Search preferred job title"
                else -> "Search"
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
        // List of items
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            when (currentSection) {
                "category" -> {
                    items(
                        items = categoryList.filter {
                            it.name.contains(searchQuery, ignoreCase = true)
                        },
                        key = { it.name }
                    ) { category ->
                        val index = categoryList.indexOf(category)
                        SelectableItem(
                            name = category.name,
                            isSelected = category.isSelected,
                            onToggle = {
                                val selectedCount = categoryList.count { it.isSelected }
                                if (!category.isSelected && selectedCount >= 5) return@SelectableItem
                                categoryList[index] = category.copy(isSelected = !category.isSelected)
                            }
                        )
                    }
                }
                "industry" -> {
                    items(
                        items = industryList.filter {
                            it.name.contains(searchQuery, ignoreCase = true)
                        },
                        key = { it.name }
                    ) { industry ->
                        val index = industryList.indexOf(industry)
                        SelectableItem(
                            name = industry.name,
                            isSelected = industry.isSelected,
                            onToggle = {
                                val selectedCount = industryList.count { it.isSelected }
                                if (!industry.isSelected && selectedCount >= 5) return@SelectableItem
                                industryList[index] = industry.copy(isSelected = !industry.isSelected)
                            }
                        )
                    }
                }
                "title" -> {
                    items(
                        items = jobTitleList.filter {
                            it.name.contains(searchQuery, ignoreCase = true)
                        },
                        key = { it.name }
                    ) { title ->
                        val index = jobTitleList.indexOf(title)
                        SelectableItem(
                            name = title.name,
                            isSelected = title.isSelected,
                            onToggle = {
                                val selectedCount = jobTitleList.count { it.isSelected }
                                if (!title.isSelected && selectedCount >= 5) return@SelectableItem
                                jobTitleList[index] = title.copy(isSelected = !title.isSelected)
                            }
                        )
                    }
                }
                "availability" -> {
                    items(
                        items = availabilityList,
                        key = { it.name }
                    ) { availability ->
                        val index = availabilityList.indexOf(availability)
                        SelectableItem(
                            name = availability.name,
                            isSelected = availability.isSelected,
                            onToggle = {
                                availabilityList[index] = availability.copy(isSelected = !availability.isSelected)
                            }
                        )
                    }
                }
            }
        }

        // Navigation buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { onDismiss() },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text("Cancel", fontSize = 16.sp)
            }

            Button(
                onClick = {
                    onSave(
                        categoryList.filter { it.isSelected }.map { it.name },
                        industryList.filter { it.isSelected }.map { it.name },
                        jobTitleList.filter { it.isSelected }.map { it.name },
                        availabilityList.filter { it.isSelected }.map { it.name }
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkBlue2)
            ) {
                Text("Done", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String, count: Int, maxCount: Int) {
    Column {
        Text(
            title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        if (subtitle.isNotEmpty()) {
            Text(
                subtitle,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        Text(
            "$count/$maxCount",
            fontSize = 14.sp,
            color = if (count >= maxCount) Color.Red else DarkBlue2,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun SearchBar(searchQuery: String, onSearchChange: (String) -> Unit, placeholder: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text(placeholder, color = Color.Gray) },
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = DarkBlue2,
                unfocusedBorderColor = Color.LightGray
            ),
            singleLine = true
        )
    }
}

@Composable
fun SelectableItem(name: String, isSelected: Boolean, onToggle: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                name,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Icon(
                    painter = painterResource(R.drawable.outline_keyboard_arrow_down_24),
                    contentDescription = "Selected",
                    tint = DarkBlue2,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer(rotationZ = 180f)
                )
            }
        }
    }
}

@Preview
@Composable
fun JobSeekerJobPreferencePreview() {
    JobSeekerJobPreferenceBody()
}