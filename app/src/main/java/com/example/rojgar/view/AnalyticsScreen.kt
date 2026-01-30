package com.example.rojgar.view

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rojgar.model.*
import com.example.rojgar.viewmodel.AnalyticsViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit


// Modern Theme Colors (matching JobSeekerDashboard)
object AnalyticsTheme {
    val PrimaryBlue = Color(0xFF3B82F6)
    val LightBlue = Color(0xFF60A5FA)
    val DeepBlue = Color(0xFF2563EB)
    val SkyBlue = Color(0xFFBAE6FD)
    val IceBlue = Color(0xFFE0F2FE)
    val AccentBlue = Color(0xFF0EA5E9)
    val DarkBlue = Color(0xFF1E3A8A)
    val SurfaceLight = Color(0xFFF0F9FF)
    val White = Color(0xFFFFFFFF)
    val TextPrimary = Color(0xFF0F172A)
    val TextSecondary = Color(0xFF64748B)
    val GlassWhite = Color(0xCCFFFFFF)
    val GlassBlue = Color(0x33BFDBFE)
    val SuccessGreen = Color(0xFF10B981)
    val WarningOrange = Color(0xFFF59E0B)
    val InfoCyan = Color(0xFF06B6D4)
    val DangerRed = Color(0xFFEF4444)
}

@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel, companyId: String) {
    val loading = viewModel.loading.observeAsState(false)
    val dashboard = viewModel.dashboard.observeAsState()
    val companyProfile = viewModel.companyProfile.observeAsState()
    val followersCount = viewModel.followersCount.observeAsState(0)
    val conversionMetrics = viewModel.conversionMetrics.observeAsState()
    val topJobs = viewModel.topJobs.observeAsState()
    val categoryPerformance = viewModel.categoryPerformance.observeAsState()
    val errorMessage = viewModel.errorMessage.observeAsState()

    Scaffold { padding ->
        // Handle blank companyId before loading
        if (companyId.isBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(AnalyticsTheme.SurfaceLight, AnalyticsTheme.IceBlue)
                        )
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AnalyticsTheme.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(padding)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Login Required",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AnalyticsTheme.TextPrimary
                        )
                        Text(
                            text = "Please log in to view analytics",
                            fontSize = 14.sp,
                            color = AnalyticsTheme.TextSecondary
                        )
                    }
                }
            }
        }

        LaunchedEffect(companyId) {
            viewModel.loadCompanyDashboard(companyId)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(AnalyticsTheme.SurfaceLight, AnalyticsTheme.IceBlue)
                    )
                )
        ) {
            if (loading.value) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = AnalyticsTheme.PrimaryBlue,
                        strokeWidth = 4.dp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Header
                    item(key = "header") {
                        AnimatedHeaderSection()
                    }

                    // Key Metrics Cards
                    item(key = "metrics_row") {
                        companyProfile.value?.let { profile ->
                            AnimatedKeyMetricsRow(profile, followersCount.value ?: 0)
                        }
                    }

                    // Performance Chart
                    item(key = "performance_chart") {
                        conversionMetrics.value?.let { metrics ->
                            PerformanceLineChart(metrics)
                        }
                    }

                    // Conversion Funnel
                    item(key = "conversion_funnel") {
                        conversionMetrics.value?.let { metrics ->
                            AnimatedConversionFunnel(metrics)
                        }
                    }

                    // Analytics Overview Cards
                    item(key = "overview_grid") {
                        companyProfile.value?.let { profile ->
                            AnalyticsOverviewGrid(profile)
                        }
                    }

                    // Top Performing Jobs Chart
                    item(key = "top_jobs_chart") {
                        val jobs = topJobs.value ?: emptyList()
                        if (jobs.isNotEmpty()) {
                            TopJobsBarChart(jobs = jobs)
                        } else {
                            EmptyDataCard(
                                title = "Top Performing Jobs",
                                hint = "Post jobs and receive applications to see analytics"
                            )
                        }
                    }

                    // Category Performance
                    item(key = "category_performance") {
                        val categories = categoryPerformance.value ?: emptyList()
                        if (categories.isNotEmpty() && categories.any { it.totalApplications > 0 }) {
                            CategoryPerformanceSection(categories)
                        } else {
                            EmptyDataCard(
                                title = "Category Performance",
                                hint = "Post jobs and receive applications to see analytics"
                            )
                        }
                    }

                    // Detailed Job Cards
                    item(key = "detailed_jobs") {
                        val jobs = topJobs.value ?: emptyList()
                        if (jobs.isNotEmpty()) {
                            DetailedJobPerformanceSection(jobs)
                        }
                    }
                    // Bottom spacer
                    item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(82.dp)) }
                }
            }
        }
    }
}

@Composable
fun EmptyDataCard(title: String, hint: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AnalyticsTheme.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AnalyticsTheme.TextPrimary
            )
            Text(
                text = "No data yet",
                fontSize = 14.sp,
                color = AnalyticsTheme.TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = hint,
                fontSize = 12.sp,
                color = AnalyticsTheme.TextSecondary.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun AnimatedHeaderSection() {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(800)) + slideInVertically(tween(800))
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = "Analytics Dashboard",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = AnalyticsTheme.TextPrimary,
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Track your recruitment performance",
                fontSize = 14.sp,
                color = AnalyticsTheme.TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun AnimatedKeyMetricsRow(profile: CompanyProfileAnalytics, followersCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedMetricCard(
            title = "Total Jobs",
            value = profile.totalJobsPosted.toString(),
            icon = "📊",
            color = AnalyticsTheme.PrimaryBlue,
            delay = 0
        )
        AnimatedMetricCard(
            title = "Applications",
            value = profile.totalApplicationsReceived.toString(),
            icon = "📬",
            color = AnalyticsTheme.InfoCyan,
            delay = 100
        )
        AnimatedMetricCard(
            title = "Hires",
            value = profile.totalHires.toString(),
            icon = "✅",
            color = AnalyticsTheme.SuccessGreen,
            delay = 200
        )
        AnimatedMetricCard(
            title = "Followers",
            value = followersCount.toString(),
            icon = "👥",
            color = AnalyticsTheme.AccentBlue,
            delay = 300
        )
    }
}

@Composable
fun AnimatedMetricCard(
    title: String,
    value: String,
    icon: String,
    color: Color,
    delay: Int
) {
    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delay.toLong())
        visible = true
    }

    Card(
        modifier = Modifier
            .width(140.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = color,
                spotColor = color
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AnalyticsTheme.White
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            color.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = icon,
                        fontSize = 20.sp
                    )
                }
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AnalyticsTheme.TextPrimary
                )
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = AnalyticsTheme.TextSecondary
                )
            }
        }
    }
}

@Composable
fun PerformanceLineChart(metrics: ConversionMetrics) {
    var visible by remember { mutableStateOf(false) }
    val animationProgress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(1500, easing = EaseInOutCubic)
    )

    LaunchedEffect(metrics.totalApplications, metrics.totalHired) {
        visible = true
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AnalyticsTheme.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Application Trends",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AnalyticsTheme.TextPrimary
                    )
                    Text(
                        text = "Last 7 days",
                        fontSize = 12.sp,
                        color = AnalyticsTheme.TextSecondary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ChartLegendItem("Applications", AnalyticsTheme.PrimaryBlue)
                    ChartLegendItem("Hired", AnalyticsTheme.SuccessGreen)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Simulated line chart data
            val dataPoints = listOf(
                metrics.totalApplications * 0.7f,
                metrics.totalApplications * 0.8f,
                metrics.totalApplications * 0.6f,
                metrics.totalApplications * 0.9f,
                metrics.totalApplications * 0.85f,
                metrics.totalApplications * 0.95f,
                metrics.totalApplications.toFloat()
            )

            val hiredPoints = listOf(
                metrics.totalHired * 0.6f,
                metrics.totalHired * 0.7f,
                metrics.totalHired * 0.5f,
                metrics.totalHired * 0.8f,
                metrics.totalHired * 0.85f,
                metrics.totalHired * 0.9f,
                metrics.totalHired.toFloat()
            )

            if (metrics.totalApplications <= 0 && metrics.totalHired <= 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data yet",
                        fontSize = 14.sp,
                        color = AnalyticsTheme.TextSecondary
                    )
                }
            } else {
                LineChartCanvas(
                    dataPoints = dataPoints,
                    secondaryDataPoints = hiredPoints,
                    animationProgress = animationProgress,
                    primaryColor = AnalyticsTheme.PrimaryBlue,
                    secondaryColor = AnalyticsTheme.SuccessGreen
                )
            }
        }
    }
}

@Composable
fun LineChartCanvas(
    dataPoints: List<Float>,
    secondaryDataPoints: List<Float>,
    animationProgress: Float,
    primaryColor: Color,
    secondaryColor: Color
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        if (dataPoints.isEmpty() || dataPoints.all { it <= 0f }) {
            return@Canvas
        }
        
        val maxValue = maxOf(
            dataPoints.maxOrNull() ?: 1f,
            secondaryDataPoints.maxOrNull() ?: 1f
        )
        val safeMaxValue = if (maxValue <= 0f) 1f else maxValue * 1.2f
        val spacing = if (dataPoints.size > 1) size.width / (dataPoints.size - 1) else size.width

        // Draw grid lines
        for (i in 0..4) {
            val y = size.height * i / 4
            drawLine(
                color = AnalyticsTheme.TextSecondary.copy(alpha = 0.1f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw primary line with gradient
        val primaryPath = Path()
        val primaryGradientPath = Path()

        dataPoints.forEachIndexed { index, value ->
            val x = index * spacing
            val safeValue = value.coerceAtLeast(0f)
            val y = size.height - (safeValue / safeMaxValue * size.height * animationProgress).coerceIn(0f, size.height)

            if (index == 0) {
                primaryPath.moveTo(x, y)
                primaryGradientPath.moveTo(x, size.height)
                primaryGradientPath.lineTo(x, y)
            } else {
                primaryPath.lineTo(x, y)
                primaryGradientPath.lineTo(x, y)
            }

            // Draw point
            drawCircle(
                color = primaryColor,
                radius = 6.dp.toPx(),
                center = Offset(x, y)
            )
            drawCircle(
                color = primaryColor.copy(alpha = 0.3f),
                radius = 10.dp.toPx(),
                center = Offset(x, y)
            )
        }

        primaryGradientPath.lineTo(size.width, size.height)
        primaryGradientPath.close()

        // Draw gradient fill
        drawPath(
            path = primaryGradientPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.3f),
                    Color.Transparent
                )
            )
        )

        // Draw line
        drawPath(
            path = primaryPath,
            color = primaryColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw secondary line
        val secondaryPath = Path()
        secondaryDataPoints.forEachIndexed { index, value ->
            val x = index * spacing
            val safeValue = value.coerceAtLeast(0f)
            val y = size.height - (safeValue / safeMaxValue * size.height * animationProgress).coerceIn(0f, size.height)

            if (index == 0) {
                secondaryPath.moveTo(x, y)
            } else {
                secondaryPath.lineTo(x, y)
            }

            drawCircle(
                color = secondaryColor,
                radius = 5.dp.toPx(),
                center = Offset(x, y)
            )
        }

        drawPath(
            path = secondaryPath,
            color = secondaryColor,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
            )
        )
    }
}

@Composable
fun ChartLegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = AnalyticsTheme.TextSecondary
        )
    }
}

@Composable
fun AnimatedConversionFunnel(metrics: ConversionMetrics) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AnalyticsTheme.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Conversion Funnel",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AnalyticsTheme.TextPrimary
            )

            FunnelStage(
                label = "Applications",
                value = metrics.totalApplications,
                total = metrics.totalApplications,
                percentage = 100f,
                color = AnalyticsTheme.PrimaryBlue,
                delay = 0
            )

            FunnelStage(
                label = "Shortlisted",
                value = metrics.totalShortlisted,
                total = metrics.totalApplications,
                percentage = metrics.shortlistRate,
                color = AnalyticsTheme.InfoCyan,
                delay = 200
            )

            // Rejected stage
            FunnelStage(
                label = "Rejected",
                value = metrics.totalRejected,
                total = metrics.totalApplications,
                percentage = if (metrics.totalApplications > 0) (metrics.totalRejected.toFloat() / metrics.totalApplications * 100) else 0f,
                color = AnalyticsTheme.DangerRed,
                delay = 300
            )

            FunnelStage(
                label = "Hired",
                value = metrics.totalHired,
                total = metrics.totalApplications,
                percentage = metrics.conversionRate,
                color = AnalyticsTheme.SuccessGreen,
                delay = 400
            )

            Divider(
                color = AnalyticsTheme.TextSecondary.copy(alpha = 0.2f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        ConversionRateCard(
                            modifier = Modifier.fillMaxWidth(),
                            label = "Shortlist Rate",
                            percentage = metrics.shortlistRate,
                            color = AnalyticsTheme.InfoCyan
                        )
                    }
                    Box(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                        ConversionRateCard(
                            modifier = Modifier.fillMaxWidth(),
                            label = "Hire Rate",
                            percentage = metrics.conversionRate,
                            color = AnalyticsTheme.SuccessGreen
                        )
                    }
                    Box(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                        ConversionRateCard(
                            modifier = Modifier.fillMaxWidth(),
                            label = "Rejection Rate",
                            percentage = if (metrics.totalApplications > 0) (metrics.totalRejected.toFloat() / metrics.totalApplications * 100) else 0f,
                            color = AnalyticsTheme.DangerRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FunnelStage(
    label: String,
    value: Int,
    total: Int,
    percentage: Float,
    color: Color,
    delay: Int
) {
    var visible by remember { mutableStateOf(false) }
    val animatedWidth by animateFloatAsState(
        targetValue = if (visible) percentage / 100f else 0f,
        animationSpec = tween(1000, delay, easing = EaseOutCubic)
    )

    LaunchedEffect(Unit) {
        visible = true
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = AnalyticsTheme.TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AnalyticsTheme.TextPrimary
                )
                Text(
                    text = "%.1f%%".format(percentage),
                    fontSize = 12.sp,
                    color = color,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(AnalyticsTheme.IceBlue)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedWidth)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(color, color.copy(alpha = 0.7f))
                        )
                    )
            )
        }
    }
}

@Composable
fun ConversionRateCard(
    modifier: Modifier = Modifier,
    label: String,
    percentage: Float,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AnalyticsTheme.IceBlue)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "%.1f%%".format(percentage),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = AnalyticsTheme.TextSecondary,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
fun AnalyticsOverviewGrid(profile: CompanyProfileAnalytics) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Overview",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = AnalyticsTheme.TextPrimary
        )
    }
}

@Composable
fun OverviewCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    unit: String,
    icon: String,
    color: Color
) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AnalyticsTheme.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            color.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        color = AnalyticsTheme.TextSecondary
                    )
                    Text(text = icon, fontSize = 20.sp)
                }

                Column {
                    Text(
                        text = value,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = AnalyticsTheme.TextPrimary
                    )
                    Text(
                        text = unit,
                        fontSize = 11.sp,
                        color = AnalyticsTheme.TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun TopJobsBarChart(jobs: List<JobAnalyticsMetrics>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AnalyticsTheme.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Text(
                text = "Top Performing Jobs",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AnalyticsTheme.TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            BarChartCanvas(jobs = jobs.take(5))
        }
    }
}

@Composable
fun BarChartCanvas(jobs: List<JobAnalyticsMetrics>) {
    var animationProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(jobs.size) {
        animate(0f, 1f, animationSpec = tween(1200, easing = EaseOutCubic)) { value, _ ->
            animationProgress = value
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        if (jobs.isEmpty()) return@Canvas

        val maxApplications = jobs.maxOfOrNull { it.totalApplications }?.toFloat() ?: 0f
        if (maxApplications <= 0f) return@Canvas

        val barWidth = (size.width / jobs.size) * 0.6f
        val spacing = size.width / jobs.size

        jobs.forEachIndexed { index, job ->
            val safeProgress = (job.totalApplications.toFloat() / maxApplications).coerceIn(0f, 1f)
            val barHeight = safeProgress * size.height * animationProgress
            val x = index * spacing + spacing / 2 - barWidth / 2
            val y = size.height - barHeight

            // Draw bar with gradient
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(AnalyticsTheme.PrimaryBlue, AnalyticsTheme.AccentBlue),
                    startY = y,
                    endY = size.height
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
            )

            // Draw value text
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 12.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText(
                    job.totalApplications.toString(),
                    x + barWidth / 2,
                    y - 8.dp.toPx(),
                    paint
                )
            }
        }
    }
}

@Composable
fun CategoryPerformanceSection(categories: List<CategoryPerformance>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AnalyticsTheme.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Category Performance",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AnalyticsTheme.TextPrimary
            )

            val maxApplications = categories.maxOfOrNull { it.totalApplications }?.toFloat() ?: 0f
            if (maxApplications <= 0f) {
                EmptyDataCard(
                    title = "Category Performance",
                    hint = "Post jobs and receive applications to see analytics"
                )
            } else {
                categories.take(5).forEachIndexed { index, category ->
                    CategoryPerformanceBar(
                        category = category,
                        maxApplications = maxApplications,
                        delay = index * 100
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryPerformanceBar(
    category: CategoryPerformance,
    maxApplications: Float,
    delay: Int
) {
    var visible by remember { mutableStateOf(false) }
    val safeMax = if (maxApplications <= 0f) 1f else maxApplications
    val safeProgress = (category.totalApplications.toFloat() / safeMax).coerceIn(0f, 1f)
    val progress by animateFloatAsState(
        targetValue = if (visible) safeProgress else 0f,
        animationSpec = tween(1000, delay, easing = EaseOutCubic)
    )

    LaunchedEffect(Unit) {
        visible = true
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatCategoryName(category.category),
                fontSize = 14.sp,
                color = AnalyticsTheme.TextPrimary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${category.totalApplications} apps",
                fontSize = 12.sp,
                color = AnalyticsTheme.TextSecondary
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(AnalyticsTheme.IceBlue)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(AnalyticsTheme.InfoCyan, AnalyticsTheme.PrimaryBlue)
                        )
                    )
            )
        }
    }
}

@Composable
fun DetailedJobPerformanceSection(jobs: List<JobAnalyticsMetrics>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Detailed Job Performance",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = AnalyticsTheme.TextPrimary
        )

        jobs.forEach { job ->
            DetailedJobCard(job)
        }
    }
}

@Composable
fun DetailedJobCard(job: JobAnalyticsMetrics) {
    // determine if job deadline expired to tint the card
    val daysLeftCountForCard = parseDaysLeft(job.deadline)
    val isExpiredCard = daysLeftCountForCard != null && daysLeftCountForCard < 0
    val cardContainer = if (isExpiredCard) AnalyticsTheme.DangerRed.copy(alpha = 0.06f) else AnalyticsTheme.White

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.jobTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AnalyticsTheme.TextPrimary
                    )
                    Text(
                        text = formatCategoryName(job.category),
                        fontSize = 12.sp,
                        color = AnalyticsTheme.TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AnalyticsTheme.SuccessGreen.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${job.conversionRate.toInt()}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AnalyticsTheme.SuccessGreen
                    )
                }
            }

            Divider(color = AnalyticsTheme.IceBlue, thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    JobMetricItem(
                        label = "Applications",
                        value = job.totalApplications.toString(),
                        icon = "📨",
                        color = AnalyticsTheme.PrimaryBlue
                    )
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    JobMetricItem(
                        label = "Shortlisted",
                        value = job.shortlisted.toString(),
                        icon = "⭐",
                        color = AnalyticsTheme.InfoCyan
                    )
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    JobMetricItem(
                        label = "Rejected",
                        value = job.rejected.toString(),
                        icon = "❌",
                        color = AnalyticsTheme.DangerRed
                    )
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    JobMetricItem(
                        label = "Hired",
                        value = job.hired.toString(),
                        icon = "✅",
                        color = AnalyticsTheme.SuccessGreen
                    )
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    val daysLeftCount = parseDaysLeft(job.deadline)
                    val daysLeftValue = when {
                        daysLeftCount == null -> "N/A"
                        daysLeftCount < 0 -> "Expired"
                        else -> daysLeftCount.toString()
                    }
                    JobMetricItem(
                        label = "Days left",
                        value = daysLeftValue,
                        icon = "📅",
                        color = AnalyticsTheme.WarningOrange
                    )
                }
            }
        }
    }
}

@Composable
fun JobMetricItem(
    label: String,
    value: String,
    icon: String,
    color: Color,
    subtitle: String? = null
) {
    Column(
        modifier = Modifier.height(72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 16.sp)
        }

        // subtitle shown just below the icon (small, secondary color)
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = AnalyticsTheme.TextSecondary
            )
        }
        // Special compact rendering for time-like values (e.g., "15 days left", "Expired", "No deadline")
        val isTimeValue = value.contains("day", ignoreCase = true) || value.equals("expired", ignoreCase = true) || value.equals("no deadline", ignoreCase = true)

        if (isTimeValue) {
            // Split into primary and secondary parts if possible
            val parts = value.split(" ", limit = 2)
            val primary = parts.getOrNull(0) ?: value
            val secondary = parts.getOrNull(1) ?: ""

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AnalyticsTheme.TextPrimary
                )
                if (secondary.isNotBlank()) {
                    Text(
                        text = secondary,
                        fontSize = 10.sp,
                        color = AnalyticsTheme.TextSecondary
                    )
                }
            }
        } else {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AnalyticsTheme.TextPrimary
            )
        }

        Text(
            text = label,
            fontSize = 11.sp,
            color = AnalyticsTheme.TextSecondary
        )
    }
}

// Circular Progress Indicator for conversion rates
@Composable
fun CircularConversionIndicator(
    percentage: Float,
    color: Color,
    label: String
) {
    var animatedPercentage by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        animate(0f, percentage, animationSpec = tween(1500, easing = EaseOutCubic)) { value, _ ->
            animatedPercentage = value
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(120.dp)) {
                val strokeWidth = 12.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val center = Offset(size.width / 2, size.height / 2)

                // Background circle
                drawCircle(
                    color = AnalyticsTheme.IceBlue,
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeWidth)
                )

                // Progress arc
                val sweepAngle = (animatedPercentage / 100f) * 360f
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(color, color.copy(alpha = 0.5f), color)
                    ),
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "%.1f%%".format(animatedPercentage),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }

        Text(
            text = label,
            fontSize = 14.sp,
            color = AnalyticsTheme.TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

// Donut Chart for category distribution
@Composable
fun CategoryDonutChart(categories: List<CategoryPerformance>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AnalyticsTheme.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Text(
                text = "Category Distribution",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AnalyticsTheme.TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DonutChartCanvas(categories = categories.take(5))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.take(5).forEachIndexed { index, category ->
                        CategoryLegendItem(
                            category = category.category,
                            applications = category.totalApplications,
                            color = getCategoryColor(index)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DonutChartCanvas(categories: List<CategoryPerformance>) {
    var animationProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(categories.size) {
        animate(0f, 1f, animationSpec = tween(1500, easing = EaseOutCubic)) { value, _ ->
            animationProgress = value
        }
    }

    Canvas(modifier = Modifier.size(150.dp)) {
        val total = categories.sumOf { it.totalApplications.toDouble() }.toFloat()
        if (total <= 0f) {
            return@Canvas
        }
        
        var startAngle = -90f
        val strokeWidth = 25.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2

        categories.forEachIndexed { index, category ->
            val safeValue = category.totalApplications.toFloat().coerceAtLeast(0f)
            val sweepAngle = (safeValue / total * 360f) * animationProgress
            val color = getCategoryColor(index)

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            startAngle += sweepAngle
        }

        // Center circle
        drawCircle(
            color = AnalyticsTheme.White,
            radius = radius - strokeWidth / 2
        )
    }
}

@Composable
fun CategoryLegendItem(category: String, applications: Int, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Column {
            Text(
                text = formatCategoryName(category),
                fontSize = 12.sp,
                color = AnalyticsTheme.TextPrimary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$applications apps",
                fontSize = 10.sp,
                color = AnalyticsTheme.TextSecondary
            )
        }
    }
}

fun formatCategoryName(raw: String): String {
    return raw.trim().removePrefix("[").removeSuffix("]")
}

fun getCategoryColor(index: Int): Color {
    val colors = listOf(
        AnalyticsTheme.PrimaryBlue,
        AnalyticsTheme.AccentBlue,
        AnalyticsTheme.SuccessGreen,
        AnalyticsTheme.InfoCyan,
        AnalyticsTheme.WarningOrange,
        AnalyticsTheme.DangerRed
    )
    return colors[index % colors.size]
}

// parse deadline string stored by CompanyUploadPost ("dd/MM/yyyy HH:mm") and return "N days left" or "Expired"
fun getDaysLeft(deadline: String): String {
    if (deadline.isBlank()) return "No deadline"
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = sdf.parse(deadline) ?: return "Invalid date"
        val diff = date.time - System.currentTimeMillis()
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        when {
            diff < 0 -> "Expired"
            days == 0L -> "0 days left"
            days == 1L -> "1 day left"
            else -> "$days days left"
        }
    } catch (e: Exception) {
        "Invalid date"
    }
}

// returns number of days left (can be negative if expired), or null if cannot parse
fun parseDaysLeft(deadline: String): Int? {
    if (deadline.isBlank()) return null
    // try epoch millis
    deadline.toLongOrNull()?.let { epoch ->
        return ((epoch - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
    }

    // try dd/MM/yyyy HH:mm and a couple common formats
    val patterns = listOf("dd/MM/yyyy HH:mm", "dd/MM/yyyy", "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ss")
    for (pattern in patterns) {
        try {
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            val date = sdf.parse(deadline) ?: continue
            return ((date.time - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
        } catch (_: Exception) { /* try next */ }
    }
    return null
}