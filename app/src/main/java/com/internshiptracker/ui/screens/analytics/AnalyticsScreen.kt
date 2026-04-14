package com.internshiptracker.ui.screens.analytics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.internshiptracker.domain.model.ApplicationStatus
import com.internshiptracker.ui.components.toColor
import com.internshiptracker.viewmodel.ApplicationViewModel

/**
 * Analytics screen featuring:
 *   - Donut chart for status distribution
 *   - Animated progress bars per status
 *   - Key metrics cards
 *   - Follow-up email generator
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: ApplicationViewModel = hiltViewModel()
) {
    val stats by viewModel.stats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Analytics") })
        }
    ) { padding ->
        if (stats == null || stats!!.total == 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("📊", style = MaterialTheme.typography.displayMedium)
                    Text("No data yet", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Add applications to see your analytics",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            return@Scaffold
        }

        val s = stats!!

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ── Key metrics row ─────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    label = "Total",
                    value = "${s.total}",
                    icon = Icons.Default.Work,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "Success Rate",
                    value = "${s.successRate.toInt()}%",
                    icon = Icons.Default.TrendingUp,
                    color = ApplicationStatus.OFFER.toColor(),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    label = "In Progress",
                    value = "${s.applied + s.interview}",
                    icon = Icons.Default.HourglassEmpty,
                    color = ApplicationStatus.APPLIED.toColor(),
                    modifier = Modifier.weight(1f)
                )
            }

            // ── Donut chart ─────────────────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Status Distribution",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Donut chart
                        Box(
                            modifier = Modifier.size(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            DonutChart(
                                segments = listOf(
                                    Pair(s.applied.toFloat(), ApplicationStatus.APPLIED.toColor()),
                                    Pair(s.interview.toFloat(), ApplicationStatus.INTERVIEW.toColor()),
                                    Pair(s.offer.toFloat(), ApplicationStatus.OFFER.toColor()),
                                    Pair(s.rejected.toFloat(), ApplicationStatus.REJECTED.toColor())
                                ),
                                total = s.total.toFloat()
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "${s.total}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Total",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }

                        Spacer(Modifier.width(24.dp))

                        // Legend
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            LegendItem("Applied", s.applied, s.total, ApplicationStatus.APPLIED.toColor())
                            LegendItem("Interview", s.interview, s.total, ApplicationStatus.INTERVIEW.toColor())
                            LegendItem("Offer", s.offer, s.total, ApplicationStatus.OFFER.toColor())
                            LegendItem("Rejected", s.rejected, s.total, ApplicationStatus.REJECTED.toColor())
                        }
                    }
                }
            }

            // ── Progress bars ───────────────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Application Funnel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    ApplicationStatus.entries.forEach { status ->
                        val count = when (status) {
                            ApplicationStatus.APPLIED -> s.applied
                            ApplicationStatus.INTERVIEW -> s.interview
                            ApplicationStatus.OFFER -> s.offer
                            ApplicationStatus.REJECTED -> s.rejected
                        }
                        FunnelBar(
                            label = status.displayName,
                            count = count,
                            total = s.total,
                            color = status.toColor()
                        )
                    }
                }
            }

            // ── Conversion rates ────────────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Conversion Rates",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    val interviewRate = if (s.total > 0) (s.interview.toFloat() / s.total) * 100 else 0f
                    val offerRate = if (s.interview > 0) (s.offer.toFloat() / s.interview) * 100 else 0f

                    ConversionRow(
                        label = "Applied → Interview",
                        rate = interviewRate,
                        color = ApplicationStatus.INTERVIEW.toColor()
                    )
                    ConversionRow(
                        label = "Interview → Offer",
                        rate = offerRate,
                        color = ApplicationStatus.OFFER.toColor()
                    )
                    ConversionRow(
                        label = "Overall Success",
                        rate = s.successRate,
                        color = ApplicationStatus.OFFER.toColor()
                    )
                }
            }

            // ── Follow-up email generator ───────────────────────────────────
            FollowUpEmailCard()

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun DonutChart(segments: List<Pair<Float, Color>>, total: Float) {
    var animated by remember { mutableStateOf(false) }
    val animProgress by animateFloatAsState(
        targetValue = if (animated) 1f else 0f,
        animationSpec = tween(1000),
        label = "donut"
    )
    LaunchedEffect(Unit) { animated = true }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 28.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)
        var startAngle = -90f

        segments.forEach { (value, color) ->
            val sweepAngle = if (total > 0) (value / total) * 360f * animProgress else 0f
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle - 2f,   // small gap between segments
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
private fun LegendItem(label: String, count: Int, total: Int, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(60.dp))
        Text(
            "$count",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FunnelBar(label: String, count: Int, total: Int, color: Color) {
    val fraction = if (total > 0) count.toFloat() / total else 0f
    var animated by remember { mutableStateOf(false) }
    val animFraction by animateFloatAsState(
        targetValue = if (animated) fraction else 0f,
        animationSpec = tween(800),
        label = "bar"
    )
    LaunchedEffect(Unit) { animated = true }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text(
                "$count (${(fraction * 100).toInt()}%)",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
        LinearProgressIndicator(
            progress = { animFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}

@Composable
private fun ConversionRow(label: String, rate: Float, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        Text(
            "${rate.toInt()}%",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * Generates a professional follow-up email template based on user input.
 */
@Composable
private fun FollowUpEmailCard() {
    var companyName by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var yourName by remember { mutableStateOf("") }
    var generatedEmail by remember { mutableStateOf("") }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Email,
                    null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Follow-Up Email Generator",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                "Generate a professional follow-up email template instantly.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            OutlinedTextField(
                value = yourName,
                onValueChange = { yourName = it },
                label = { Text("Your Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = companyName,
                onValueChange = { companyName = it },
                label = { Text("Company Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = role,
                onValueChange = { role = it },
                label = { Text("Position Applied For") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = {
                    generatedEmail = generateFollowUpEmail(yourName, companyName, role)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = companyName.isNotBlank() && role.isNotBlank() && yourName.isNotBlank()
            ) {
                Icon(Icons.Default.AutoAwesome, null)
                Spacer(Modifier.width(8.dp))
                Text("Generate Email")
            }

            if (generatedEmail.isNotBlank()) {
                HorizontalDivider()
                Text(
                    "Generated Email:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        generatedEmail,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                        lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                    )
                }
            }
        }
    }
}

/** Generates a professional follow-up email template. */
private fun generateFollowUpEmail(name: String, company: String, role: String): String {
    return """Subject: Follow-Up: $role Application – $name

Dear Hiring Team,

I hope this message finds you well. I am writing to follow up on my recent application for the $role position at $company.

I remain very enthusiastic about the opportunity to contribute to your team and believe my skills align well with the requirements for this role. I would welcome the chance to discuss how my background and experience can add value to $company.

Please let me know if there is any additional information I can provide to support my application. I look forward to hearing from you and am available for an interview at your earliest convenience.

Thank you for your time and consideration.

Warm regards,
$name
""".trimIndent()
}
