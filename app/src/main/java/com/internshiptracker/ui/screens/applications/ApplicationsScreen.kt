package com.internshiptracker.ui.screens.applications

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.internshiptracker.domain.model.ApplicationStatus
import com.internshiptracker.ui.components.ApplicationCard
import com.internshiptracker.ui.components.StatusChip
import com.internshiptracker.ui.theme.*
import com.internshiptracker.viewmodel.AddEditViewModel
import com.internshiptracker.viewmodel.ApplicationViewModel
import com.internshiptracker.viewmodel.SaveResult

// ─────────────────────────────────────────────────────────────────────────────
// Applications Screen
// Shows all saved applications + FAB that opens the full Add form sheet.
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationsScreen(
    onAddClick: () -> Unit,          // navigate to standalone AddEdit screen
    onApplicationClick: (Long) -> Unit,
    viewModel: ApplicationViewModel = hiltViewModel()
) {
    val applications by viewModel.filteredApplications.collectAsState()
    val filterStatus by viewModel.filterStatus.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }
    var showAddSheet    by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Applications",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        BadgedBox(
                            badge = { if (filterStatus != null) Badge() }
                        ) {
                            Icon(Icons.Default.FilterList, "Filter")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick  = { showAddSheet = true },
                icon     = { Icon(Icons.Default.Add, null, tint = Color.White) },
                text     = { Text("Add Internship", color = Color.White, fontWeight = FontWeight.SemiBold) },
                containerColor = Brand500,
                contentColor   = Color.White
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Active filter banner ──────────────────────────────────────
            AnimatedVisibility(visible = filterStatus != null) {
                filterStatus?.let { status ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.FilterAlt,
                            null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Filtered by:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        StatusChip(status = status)
                        Spacer(Modifier.weight(1f))
                        TextButton(
                            onClick = { viewModel.setFilterStatus(null) },
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("Clear", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // ── Count row ─────────────────────────────────────────────────
            Text(
                text = "${applications.size} application${if (applications.size != 1) "s" else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // ── List or empty state ───────────────────────────────────────
            if (applications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text("📭", style = MaterialTheme.typography.displayMedium)
                        Text(
                            if (filterStatus != null) "No applications match this filter"
                            else "No applications yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (filterStatus != null) {
                            TextButton(onClick = { viewModel.setFilterStatus(null) }) {
                                Text("Clear filter", color = Brand500)
                            }
                        } else {
                            Button(
                                onClick = { showAddSheet = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Brand500)
                            ) {
                                Icon(Icons.Default.Add, null, tint = Color.White)
                                Spacer(Modifier.width(6.dp))
                                Text("Add your first one", color = Color.White)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp,
                        top = 4.dp, bottom = 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(applications, key = { it.id }) { app ->
                        ApplicationCard(
                            application = app,
                            onClick = { onApplicationClick(app.id) }
                        )
                    }
                }
            }
        }
    }

    // ── Filter sheet ──────────────────────────────────────────────────────
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Filter by Status",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))

                FilterOption("All statuses", filterStatus == null) {
                    viewModel.setFilterStatus(null)
                    showFilterSheet = false
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                ApplicationStatus.entries.forEach { status ->
                    FilterOption(status.displayName, filterStatus == status) {
                        viewModel.setFilterStatus(status)
                        showFilterSheet = false
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    // ── Add Internship sheet ──────────────────────────────────────────────
    if (showAddSheet) {
        AddInternshipSheet(
            onDismiss  = { showAddSheet = false },
            onSaved    = { showAddSheet = false }
        )
    }
}

@Composable
private fun FilterOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Add Internship Sheet
// Full form inside a ModalBottomSheet — scrollable, all fields, saves to Room.
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddInternshipSheet(
    onDismiss: () -> Unit,
    onSaved:   () -> Unit,
    viewModel: AddEditViewModel = hiltViewModel()
) {
    // Always start fresh when the sheet opens
    LaunchedEffect(Unit) { viewModel.resetForm() }

    val saveResult by viewModel.saveResult.collectAsState()
    LaunchedEffect(saveResult) {
        if (saveResult is SaveResult.Success) onSaved()
    }

    // Form state
    val companyName    by viewModel.companyName.collectAsState()
    val role           by viewModel.role.collectAsState()
    val status         by viewModel.status.collectAsState()
    val notes          by viewModel.notes.collectAsState()
    val salaryRange    by viewModel.salaryRange.collectAsState()
    val location       by viewModel.location.collectAsState()
    val applicationUrl by viewModel.applicationUrl.collectAsState()
    val contactName    by viewModel.contactName.collectAsState()
    val contactEmail   by viewModel.contactEmail.collectAsState()

    val errorMsg = (saveResult as? SaveResult.Error)?.message
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(saveResult) {
        if (saveResult !is SaveResult.Error) isSaving = false
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor  = MaterialTheme.colorScheme.surface,
        tonalElevation  = 0.dp,
        // Make the sheet tall enough to show the full form
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)        // occupies 92% of screen height
        ) {
            // ── Fixed header ──────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 4.dp, bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Add Internship / Job",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Fill in the details below",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close, "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(top = 12.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                )
            }

            // ── Scrollable form body ──────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Error banner
                AnimatedVisibility(visible = errorMsg != null) {
                    if (errorMsg != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(StatusRejected.copy(alpha = 0.09f))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, null, tint = StatusRejected, modifier = Modifier.size(18.dp))
                            Text(errorMsg, style = MaterialTheme.typography.bodySmall, color = StatusRejected, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // ── Section: Core Details ─────────────────────────────────
                SheetSectionLabel("Core Details")

                FormField(
                    value = companyName,
                    onValueChange = { viewModel.companyName.value = it },
                    label = "Company Name *",
                    placeholder = "e.g. Google, Infosys, Swiggy…",
                    icon = Icons.Default.Business,
                    isError = errorMsg != null && companyName.isBlank()
                )

                FormField(
                    value = role,
                    onValueChange = { viewModel.role.value = it },
                    label = "Role / Position *",
                    placeholder = "e.g. SDE Intern, Data Analyst…",
                    icon = Icons.Default.Work,
                    isError = errorMsg != null && role.isBlank()
                )

                // Status picker
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Application Status",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ApplicationStatus.entries) { s ->
                            val chipColor = s.toStatusColor()
                            val selected  = status == s
                            FilterChip(
                                selected = selected,
                                onClick  = { viewModel.status.value = s },
                                label    = {
                                    Text(
                                        s.displayName,
                                        style      = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor         = chipColor.copy(alpha = 0.08f),
                                    labelColor             = chipColor,
                                    selectedContainerColor = chipColor,
                                    selectedLabelColor     = Color.White
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled             = true,
                                    selected            = selected,
                                    borderColor         = chipColor.copy(alpha = 0.35f),
                                    selectedBorderColor = chipColor
                                )
                            )
                        }
                    }
                }

                // ── Section: Location & Salary ────────────────────────────
                SheetSectionLabel("Location & Compensation")

                FormField(
                    value = location,
                    onValueChange = { viewModel.location.value = it },
                    label = "Location",
                    placeholder = "e.g. Bangalore, Remote…",
                    icon = Icons.Default.LocationOn
                )

                FormField(
                    value = salaryRange,
                    onValueChange = { viewModel.salaryRange.value = it },
                    label = "Stipend / Salary",
                    placeholder = "e.g. ₹60,000/month",
                    icon = Icons.Default.CurrencyRupee,
                    keyboardType = KeyboardType.Text
                )

                // ── Section: Application Link ─────────────────────────────
                SheetSectionLabel("Job Posting")

                FormField(
                    value = applicationUrl,
                    onValueChange = { viewModel.applicationUrl.value = it },
                    label = "Job URL",
                    placeholder = "https://careers.company.com/…",
                    icon = Icons.Default.Link,
                    keyboardType = KeyboardType.Uri
                )

                // ── Section: Recruiter ────────────────────────────────────
                SheetSectionLabel("Recruiter / Contact")

                FormField(
                    value = contactName,
                    onValueChange = { viewModel.contactName.value = it },
                    label = "Recruiter Name",
                    placeholder = "e.g. Priya Sharma",
                    icon = Icons.Default.Person
                )

                FormField(
                    value = contactEmail,
                    onValueChange = { viewModel.contactEmail.value = it },
                    label = "Recruiter Email",
                    placeholder = "recruiter@company.com",
                    icon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email
                )

                // ── Section: Notes ────────────────────────────────────────
                SheetSectionLabel("Notes")

                OutlinedTextField(
                    value       = notes,
                    onValueChange = { viewModel.notes.value = it },
                    label       = { Text("Additional notes") },
                    placeholder = { Text("Referral, application stage, anything useful…") },
                    modifier    = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    minLines    = 3,
                    maxLines    = 6,
                    colors      = fieldColors()
                )

                Spacer(Modifier.height(8.dp))
            }

            // ── Fixed bottom save bar ─────────────────────────────────────
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick   = onDismiss,
                        modifier  = Modifier.weight(1f),
                        border    = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(
                            "Cancel",
                            color      = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            isSaving = true
                            viewModel.saveApplication()
                        },
                        modifier = Modifier.weight(2f),
                        enabled  = !isSaving,
                        colors   = ButtonDefaults.buttonColors(containerColor = Brand500)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color       = Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Saving…", color = Color.White, fontWeight = FontWeight.SemiBold)
                        } else {
                            Icon(Icons.Default.CheckCircle, null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("Save Application", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Small helpers
// ─────────────────────────────────────────────────────────────────────────────

/** Bold section label with a left-side accent bar. */
@Composable
private fun SheetSectionLabel(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Brand500)
        )
        Text(
            title,
            style      = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color      = Brand500
        )
    }
}

/** Reusable single-line text field used throughout the form. */
@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isError: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label) },
        placeholder   = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
        leadingIcon   = { Icon(icon, null) },
        modifier      = Modifier.fillMaxWidth(),
        singleLine    = true,
        isError       = isError,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            keyboardType   = keyboardType
        ),
        colors = fieldColors()
    )
}

/** Consistent OutlinedTextField colors that use Brand500 when focused. */
@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = Brand500,
    focusedLabelColor       = Brand500,
    focusedLeadingIconColor = Brand500,
    cursorColor             = Brand500
)

/** Map ApplicationStatus → its display colour (avoids importing from components). */
private fun ApplicationStatus.toStatusColor() = when (this) {
    ApplicationStatus.APPLIED   -> StatusApplied
    ApplicationStatus.INTERVIEW -> StatusInterview
    ApplicationStatus.OFFER     -> StatusOffer
    ApplicationStatus.REJECTED  -> StatusRejected
}
