package com.internshiptracker.ui.screens.kanban

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.internshiptracker.domain.model.ApplicationStatus
import com.internshiptracker.domain.model.InternshipApplication
import com.internshiptracker.ui.components.StatusChip
import com.internshiptracker.ui.components.toColor
import com.internshiptracker.viewmodel.ApplicationViewModel
import java.time.format.DateTimeFormatter

/**
 * Kanban board view — shows applications in status columns.
 * Long-press a card and drag it to another column to update status.
 * Horizontal scroll lets you swipe between all 4 columns.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanbanScreen(
    onApplicationClick: (Long) -> Unit,
    viewModel: ApplicationViewModel = hiltViewModel()
) {
    val applicationsByStatus by viewModel.applicationsByStatus.collectAsState()

    // Track which card is being dragged
    var draggingApp by remember { mutableStateOf<InternshipApplication?>(null) }
    var hoveredStatus by remember { mutableStateOf<ApplicationStatus?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kanban Board") },
                actions = {
                    Icon(
                        Icons.Default.SwapHoriz,
                        null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Scroll →",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .horizontalScroll(rememberScrollState())
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ApplicationStatus.entries.forEach { status ->
                val apps = applicationsByStatus[status] ?: emptyList()
                val isHovered = hoveredStatus == status

                KanbanColumn(
                    status = status,
                    applications = apps,
                    isDropTarget = isHovered,
                    draggingApp = draggingApp,
                    onCardClick = onApplicationClick,
                    onDragStart = { app -> draggingApp = app },
                    onDragEnd = { targetStatus ->
                        draggingApp?.let { app ->
                            if (app.status != targetStatus) {
                                viewModel.updateStatus(app, targetStatus)
                            }
                        }
                        draggingApp = null
                        hoveredStatus = null
                    },
                    onDragHover = { hovered -> hoveredStatus = if (hovered) status else null }
                )
            }
        }
    }
}

@Composable
private fun KanbanColumn(
    status: ApplicationStatus,
    applications: List<InternshipApplication>,
    isDropTarget: Boolean,
    draggingApp: InternshipApplication?,
    onCardClick: (Long) -> Unit,
    onDragStart: (InternshipApplication) -> Unit,
    onDragEnd: (ApplicationStatus) -> Unit,
    onDragHover: (Boolean) -> Unit
) {
    val statusColor = status.toColor()

    Column(
        modifier = Modifier
            .width(260.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isDropTarget)
                    statusColor.copy(alpha = 0.15f)
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .pointerInput(status) {
                // Detect when a dragged card is released over this column
                detectDragGesturesAfterLongPress(
                    onDragStart = { _ -> onDragHover(true) },
                    onDragEnd = { onDragEnd(status) },
                    onDragCancel = {
                        onDragHover(false)
                        onDragEnd(status)
                    },
                    onDrag = { _, _ -> }
                )
            }
    ) {
        // Column header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(statusColor.copy(alpha = 0.2f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                status.displayName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(statusColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${applications.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (applications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Inbox,
                        null,
                        tint = statusColor.copy(alpha = 0.4f),
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        "No applications",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    if (draggingApp != null) {
                        Text(
                            "Drop here",
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxHeight()
            ) {
                items(applications, key = { it.id }) { app ->
                    KanbanCard(
                        application = app,
                        statusColor = statusColor,
                        onClick = { onCardClick(app.id) },
                        onLongPress = { onDragStart(app) }
                    )
                }
            }
        }
    }
}

@Composable
private fun KanbanCard(
    application: InternshipApplication,
    statusColor: Color,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (isDragging) 8.dp else 2.dp, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { _ ->
                        isDragging = true
                        onLongPress()
                    },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false },
                    onDrag = { _, _ -> }
                )
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Company name with colored left border effect via background
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(statusColor)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    application.companyName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                application.role,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (application.location.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        null,
                        modifier = Modifier.size(10.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Text(
                        application.location,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Footer row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    application.dateApplied.format(DateTimeFormatter.ofPattern("MMM dd")),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
                if (isDragging) {
                    Icon(
                        Icons.Default.DragIndicator,
                        null,
                        modifier = Modifier.size(14.dp),
                        tint = statusColor.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
