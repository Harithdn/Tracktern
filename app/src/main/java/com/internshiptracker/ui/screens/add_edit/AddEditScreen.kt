package com.internshiptracker.ui.screens.add_edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.internshiptracker.domain.model.ApplicationStatus
import com.internshiptracker.ui.components.toColor
import com.internshiptracker.ui.theme.Brand500
import com.internshiptracker.viewmodel.AddEditViewModel
import com.internshiptracker.viewmodel.SaveResult
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Full-screen form for adding a new application or editing an existing one.
 * Navigated to from the Applications list or the dashboard FAB → "Full Form".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    applicationId: Long?,
    onNavigateBack: () -> Unit,
    onResumeMatch: (Long) -> Unit,
    viewModel: AddEditViewModel = hiltViewModel()
) {
    // Pre-load data when editing
    LaunchedEffect(applicationId) {
        if (applicationId != null) viewModel.loadApplication(applicationId)
        else viewModel.resetForm()
    }

    val saveResult by viewModel.saveResult.collectAsState()
    LaunchedEffect(saveResult) {
        when (saveResult) {
            is SaveResult.Success -> onNavigateBack()
            is SaveResult.Deleted -> onNavigateBack()
            else -> {}
        }
    }

    // Form fields
    val companyName    by viewModel.companyName.collectAsState()
    val role           by viewModel.role.collectAsState()
    val status         by viewModel.status.collectAsState()
    val dateApplied    by viewModel.dateApplied.collectAsState()
    val notes          by viewModel.notes.collectAsState()
    val salaryRange    by viewModel.salaryRange.collectAsState()
    val location       by viewModel.location.collectAsState()
    val applicationUrl by viewModel.applicationUrl.collectAsState()
    val contactName    by viewModel.contactName.collectAsState()
    val contactEmail   by viewModel.contactEmail.collectAsState()

    val errorMsg = (saveResult as? SaveResult.Error)?.message
    var showDeleteDialog    by remember { mutableStateOf(false) }
    var showStatusDropdown  by remember { mutableStateOf(false) }
    var showDatePicker      by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dateApplied.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    val isEditing = applicationId != null

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditing) "Edit Application" else "New Application",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    if (isEditing) {
                        // Resume match
                        IconButton(onClick = {
                            applicationId?.let { onResumeMatch(it) }
                        }) {
                            Icon(Icons.Default.Analytics, "Resume Match", tint = Brand500)
                        }
                        // Delete
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { viewModel.saveApplication() },
                        modifier = Modifier.weight(2f),
                        colors = ButtonDefaults.buttonColors(containerColor = Brand500)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isEditing) "Save Changes" else "Add Application",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Error card
            if (errorMsg != null) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error)
                        Text(errorMsg, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            // ── Basic Info ────────────────────────────────────────────────
            FormCard(title = "Basic Information", icon = Icons.Default.Business) {

                OutlinedTextField(
                    value = companyName,
                    onValueChange = { viewModel.companyName.value = it },
                    label = { Text("Company Name *") },
                    leadingIcon = { Icon(Icons.Default.Business, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = errorMsg != null && companyName.isBlank(),
                    colors = focusedColors()
                )

                OutlinedTextField(
                    value = role,
                    onValueChange = { viewModel.role.value = it },
                    label = { Text("Role / Position *") },
                    leadingIcon = { Icon(Icons.Default.Work, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = errorMsg != null && role.isBlank(),
                    colors = focusedColors()
                )

                // Status dropdown
                ExposedDropdownMenuBox(
                    expanded = showStatusDropdown,
                    onExpandedChange = { showStatusDropdown = it }
                ) {
                    OutlinedTextField(
                        value = status.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Circle, null,
                                tint = status.toColor(),
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showStatusDropdown) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = focusedColors()
                    )
                    ExposedDropdownMenu(
                        expanded = showStatusDropdown,
                        onDismissRequest = { showStatusDropdown = false }
                    ) {
                        ApplicationStatus.entries.forEach { s ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Circle, null,
                                            tint = s.toColor(),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(s.displayName)
                                    }
                                },
                                onClick = {
                                    viewModel.status.value = s
                                    showStatusDropdown = false
                                }
                            )
                        }
                    }
                }

                // Date applied (clickable display)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = dateApplied.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Date Applied") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = focusedColors()
                    )
                    // Transparent overlay to capture clicks
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePicker = true }
                    )
                }
            }

            // ── Location & Salary ─────────────────────────────────────────
            FormCard(title = "Location & Compensation", icon = Icons.Default.LocationOn) {

                OutlinedTextField(
                    value = location,
                    onValueChange = { viewModel.location.value = it },
                    label = { Text("Location") },
                    placeholder = { Text("e.g. Bangalore, Remote") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = focusedColors()
                )

                OutlinedTextField(
                    value = salaryRange,
                    onValueChange = { viewModel.salaryRange.value = it },
                    label = { Text("Stipend / Salary") },
                    placeholder = { Text("e.g. ₹60,000/month") },
                    leadingIcon = { Icon(Icons.Default.CurrencyRupee, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = focusedColors()
                )
            }

            // ── Job Posting ───────────────────────────────────────────────
            FormCard(title = "Job Posting", icon = Icons.Default.Link) {
                OutlinedTextField(
                    value = applicationUrl,
                    onValueChange = { viewModel.applicationUrl.value = it },
                    label = { Text("Job URL") },
                    placeholder = { Text("https://careers.company.com/…") },
                    leadingIcon = { Icon(Icons.Default.Link, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = focusedColors()
                )
            }

            // ── Recruiter / Contact ───────────────────────────────────────
            FormCard(title = "Recruiter / Contact", icon = Icons.Default.Person) {

                OutlinedTextField(
                    value = contactName,
                    onValueChange = { viewModel.contactName.value = it },
                    label = { Text("Recruiter Name") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = focusedColors()
                )

                OutlinedTextField(
                    value = contactEmail,
                    onValueChange = { viewModel.contactEmail.value = it },
                    label = { Text("Recruiter Email") },
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = focusedColors()
                )
            }

            // ── Notes ─────────────────────────────────────────────────────
            FormCard(title = "Notes", icon = Icons.Default.Notes) {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { viewModel.notes.value = it },
                    label = { Text("Notes & Comments") },
                    placeholder = { Text("Anything useful — referral, stage, feedback…") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    minLines = 4,
                    maxLines = 8,
                    colors = focusedColors()
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Application?") },
            text  = { Text("This will permanently remove this application and all its data.") },
            confirmButton = {
                Button(
                    onClick = { showDeleteDialog = false; viewModel.delete() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete", color = Color.White) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        viewModel.dateApplied.value = selectedDate
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FormCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, null, tint = Brand500, modifier = Modifier.size(18.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Brand500
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            content()
        }
    }
}

@Composable
private fun focusedColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = Brand500,
    focusedLabelColor       = Brand500,
    focusedLeadingIconColor = Brand500,
    cursorColor             = Brand500
)
