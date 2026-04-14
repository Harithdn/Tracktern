package com.internshiptracker.ui.screens.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.internshiptracker.domain.model.ApplicationStatus
import com.internshiptracker.ui.components.ApplicationCard
import com.internshiptracker.ui.components.StatusChip
import com.internshiptracker.viewmodel.ApplicationViewModel

/**
 * Dedicated search screen with:
 *   - Live search-as-you-type
 *   - Status filter chips
 *   - Result count
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onApplicationClick: (Long) -> Unit,
    viewModel: ApplicationViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterStatus by viewModel.filterStatus.collectAsState()
    val applications by viewModel.filteredApplications.collectAsState()

    val focusRequester = remember { FocusRequester() }

    // Auto-focus search field on enter
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        SearchBar(
            query = searchQuery,
            onQueryChange = { viewModel.setSearchQuery(it) },
            onSearch = {},
            active = false,
            onActiveChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .focusRequester(focusRequester),
            placeholder = { Text("Search by company or role…") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                AnimatedVisibility(visible = searchQuery.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Clear, "Clear search")
                    }
                }
            }
        ) {}

        // Status filter chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            item {
                FilterChip(
                    selected = filterStatus == null,
                    onClick = { viewModel.setFilterStatus(null) },
                    label = { Text("All") },
                    leadingIcon = if (filterStatus == null) {
                        { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                    } else null
                )
            }
            items(ApplicationStatus.entries) { status ->
                FilterChip(
                    selected = filterStatus == status,
                    onClick = {
                        viewModel.setFilterStatus(if (filterStatus == status) null else status)
                    },
                    label = { Text(status.displayName) },
                    leadingIcon = if (filterStatus == status) {
                        { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                    } else null
                )
            }
        }

        HorizontalDivider()

        // Results
        if (searchQuery.isEmpty() && filterStatus == null) {
            // Show hint when nothing typed
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🔍", style = MaterialTheme.typography.displayMedium)
                    Text(
                        "Search your applications",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Type a company name or role to get started",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            // Results header
            Text(
                "${applications.size} result${if (applications.size != 1) "s" else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (applications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("😕", style = MaterialTheme.typography.displayMedium)
                        Text("No results found", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Try a different search term or remove filters",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
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
}
