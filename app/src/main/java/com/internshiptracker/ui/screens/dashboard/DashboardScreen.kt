package com.internshiptracker.ui.screens.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.internshiptracker.domain.model.ApplicationStatus
import com.internshiptracker.ui.components.ApplicationCard
import com.internshiptracker.ui.theme.*
import com.internshiptracker.viewmodel.AddEditViewModel
import com.internshiptracker.viewmodel.ApplicationViewModel
import com.internshiptracker.viewmodel.SaveResult
import java.time.LocalDate

// ─────────────────────────────────────────────────────────────────────────────
// Dashboard Screen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAddClick: () -> Unit,
    onApplicationClick: (Long) -> Unit,
    onViewAllClick: () -> Unit,
    viewModel: ApplicationViewModel = hiltViewModel()
) {
    val stats   by viewModel.stats.collectAsState()
    val allApps by viewModel.filteredApplications.collectAsState()
    var showQuickAdd by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { DashboardTopBar() },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick        = { showQuickAdd = true },
                icon           = { Icon(Icons.Default.Add, null, tint = Color.White) },
                text           = { Text("Add Internship", color = Color.White, fontWeight = FontWeight.SemiBold) },
                containerColor = Brand500,
                contentColor   = Color.White
            )
        }
    ) { padding ->
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(450)) + slideInVertically(tween(450)) { it / 12 }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // ── Hero banner ─────────────────────────────────────────────
                item { HeroBanner(total = stats?.total ?: 0, successRate = stats?.successRate ?: 0f) }

                // ── Status overview cards ───────────────────────────────────
                item { Spacer(Modifier.height(24.dp)) }
                item {
                    SectionTitle(
                        title    = "Overview",
                        subtitle = "Your application pipeline",
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
                item { Spacer(Modifier.height(12.dp)) }
                item {
                    LazyRow(
                        contentPadding       = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item { StatusCard("Applied",   stats?.applied   ?: 0, StatusApplied,   Icons.Default.Send) }
                        item { StatusCard("Interview", stats?.interview ?: 0, StatusInterview, Icons.Default.RecordVoiceOver) }
                        item { StatusCard("Offer",     stats?.offer     ?: 0, StatusOffer,     Icons.Default.CheckCircle) }
                        item { StatusCard("Rejected",  stats?.rejected  ?: 0, StatusRejected,  Icons.Default.Cancel) }
                    }
                }

                // ── Recent applications ─────────────────────────────────────
                item { Spacer(Modifier.height(28.dp)) }
                item {
                    SectionHeader(
                        title    = "Recent Applications",
                        onViewAll = onViewAllClick,
                        modifier  = Modifier.padding(horizontal = 20.dp)
                    )
                }
                item { Spacer(Modifier.height(10.dp)) }

                if (allApps.isEmpty()) {
                    item { EmptyState(onAddClick = { showQuickAdd = true }) }
                } else {
                    items(allApps.take(5), key = { it.id }) { app ->
                        ApplicationCard(
                            application = app,
                            onClick     = { onApplicationClick(app.id) },
                            modifier    = Modifier.padding(horizontal = 20.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                    if (allApps.size > 5) {
                        item {
                            TextButton(
                                onClick  = onViewAllClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                            ) {
                                Text("View all ${allApps.size} applications →", color = Brand500, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showQuickAdd) {
        QuickAddSheet(
            onDismiss = { showQuickAdd = false },
            onFullForm = { showQuickAdd = false; onAddClick() }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top App Bar
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardTopBar() {
    TopAppBar(
        title = {
            Column {
                Text(
                    "Tracktern",
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Track your internship journey",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor         = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Hero Banner — navy gradient, all text hardcoded White
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HeroBanner(total: Int, successRate: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(colors = listOf(Brand900, Brand700, Teal500)))
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        // Decorative ghost circle
        Box(
            modifier = Modifier
                .size(130.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 30.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.04f))
        )

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    "$total",
                    color      = Color.White,         // explicit — never inherits theme
                    fontSize   = 52.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 56.sp
                )
                Text(
                    "Total Applications",
                    color      = Color.White.copy(alpha = 0.88f),
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(56.dp)
                    .background(Color.White.copy(alpha = 0.25f))
            )

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    "${successRate.toInt()}%",
                    color      = Color.White,         // explicit — never inherits theme
                    fontSize   = 40.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 44.sp
                )
                Text(
                    "Success Rate",
                    color      = Color.White.copy(alpha = 0.88f),
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Status Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StatusCard(label: String, count: Int, color: Color, icon: ImageVector) {
    Card(
        modifier = Modifier.size(width = 112.dp, height = 108.dp),
        shape    = RoundedCornerShape(18.dp),
        colors   = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.09f)),
        border   = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.22f))
    ) {
        Column(
            modifier              = Modifier.fillMaxSize().padding(14.dp),
            verticalArrangement   = Arrangement.SpaceBetween
        ) {
            Box(
                modifier          = Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.14f)),
                contentAlignment  = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(17.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text("$count", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
                Text(label,   style = MaterialTheme.typography.labelMedium,   fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Section helpers
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionTitle(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title,    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,   color = MaterialTheme.colorScheme.onBackground)
        Text(subtitle, style = MaterialTheme.typography.bodySmall,                                  color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SectionHeader(title: String, onViewAll: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
        TextButton(onClick = onViewAll) { Text("View All", color = Brand500, fontWeight = FontWeight.Medium) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty State
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyState(onAddClick: () -> Unit) {
    Column(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 32.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier         = Modifier.size(80.dp).clip(CircleShape).background(Brand500.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) { Text("🎯", fontSize = 36.sp) }

        Text("No applications yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text(
            "Start tracking your internship journey by adding your first application.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Button(onClick = onAddClick, colors = ButtonDefaults.buttonColors(containerColor = Brand500)) {
            Icon(Icons.Default.Add, null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("Add First Application", color = Color.White)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Quick-Add Bottom Sheet
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickAddSheet(
    onDismiss: () -> Unit,
    onFullForm: () -> Unit,
    addEditViewModel: AddEditViewModel = hiltViewModel()
) {
    var company        by remember { mutableStateOf("") }
    var role           by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf(ApplicationStatus.APPLIED) }
    var showError      by remember { mutableStateOf(false) }
    var isSaving       by remember { mutableStateOf(false) }

    val saveResult by addEditViewModel.saveResult.collectAsState()

    LaunchedEffect(Unit) { addEditViewModel.resetForm() }
    LaunchedEffect(saveResult) {
        if (saveResult is SaveResult.Success) { isSaving = false; onDismiss() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor   = MaterialTheme.colorScheme.surface,
        tonalElevation   = 0.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Add Internship / Job", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Quick log — fill essentials now", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            // Error banner
            AnimatedVisibility(visible = showError) {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(StatusRejected.copy(alpha = 0.09f)).padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, null, tint = StatusRejected, modifier = Modifier.size(18.dp))
                    Text("Please enter company name and role", style = MaterialTheme.typography.bodySmall, color = StatusRejected, fontWeight = FontWeight.Medium)
                }
            }

            // Company field
            OutlinedTextField(
                value         = company,
                onValueChange = { company = it; if (it.isNotBlank()) showError = false },
                label         = { Text("Company Name") },
                placeholder   = { Text("e.g. Google, Amazon…") },
                leadingIcon   = { Icon(Icons.Default.Business, null) },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                isError       = showError && company.isBlank(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Brand500, focusedLabelColor = Brand500, focusedLeadingIconColor = Brand500)
            )

            // Role field
            OutlinedTextField(
                value         = role,
                onValueChange = { role = it; if (it.isNotBlank()) showError = false },
                label         = { Text("Role / Position") },
                placeholder   = { Text("e.g. SDE Intern, Data Analyst…") },
                leadingIcon   = { Icon(Icons.Default.Work, null) },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                isError       = showError && role.isBlank(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Brand500, focusedLabelColor = Brand500, focusedLeadingIconColor = Brand500)
            )

            // Status chips
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Current Status", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    ApplicationStatus.entries.forEach { status ->
                        val chipColor = when (status) {
                            ApplicationStatus.APPLIED   -> StatusApplied
                            ApplicationStatus.INTERVIEW -> StatusInterview
                            ApplicationStatus.OFFER     -> StatusOffer
                            ApplicationStatus.REJECTED  -> StatusRejected
                        }
                        val isSelected = selectedStatus == status
                        FilterChip(
                            selected = isSelected,
                            onClick  = { selectedStatus = status },
                            label    = { Text(status.displayName, style = MaterialTheme.typography.labelSmall, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors   = FilterChipDefaults.filterChipColors(
                                containerColor = chipColor.copy(alpha = 0.08f), labelColor = chipColor,
                                selectedContainerColor = chipColor, selectedLabelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(enabled = true, selected = isSelected, borderColor = chipColor.copy(alpha = 0.35f), selectedBorderColor = chipColor)
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Action buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick  = onFullForm,
                    modifier = Modifier.weight(1f),
                    border   = androidx.compose.foundation.BorderStroke(1.5.dp, Brand500)
                ) {
                    Text("Full Form", color = Brand500, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.OpenInNew, null, tint = Brand500, modifier = Modifier.size(14.dp))
                }

                Button(
                    onClick  = {
                        if (company.isBlank() || role.isBlank()) { showError = true }
                        else {
                            isSaving = true; showError = false
                            addEditViewModel.companyName.value  = company.trim()
                            addEditViewModel.role.value         = role.trim()
                            addEditViewModel.status.value       = selectedStatus
                            addEditViewModel.dateApplied.value  = LocalDate.now()
                            addEditViewModel.saveApplication()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors   = ButtonDefaults.buttonColors(containerColor = Brand500),
                    enabled  = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Icon(Icons.Default.Save, null, tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("Save", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Text(
                "📅 Applied date set to today. Use Full Form to add more details.",
                style     = MaterialTheme.typography.bodySmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth()
            )
        }
    }
}
