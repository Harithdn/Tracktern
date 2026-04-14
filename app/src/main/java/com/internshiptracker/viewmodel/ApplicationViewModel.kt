package com.internshiptracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.internshiptracker.data.repository.ApplicationRepository
import com.internshiptracker.domain.model.ApplicationStats
import com.internshiptracker.domain.model.ApplicationStatus
import com.internshiptracker.domain.model.InternshipApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Shared ViewModel used by the Dashboard, Applications list, Kanban, and Search screens.
 * All state is exposed as StateFlow — the UI never polls, it just collects.
 */
@HiltViewModel
class ApplicationViewModel @Inject constructor(
    private val repository: ApplicationRepository
) : ViewModel() {

    // ── Search / filter state ─────────────────────────────────────────────
    private val _searchQuery  = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterStatus = MutableStateFlow<ApplicationStatus?>(null)
    val filterStatus: StateFlow<ApplicationStatus?> = _filterStatus.asStateFlow()

    // ── Dashboard stats ───────────────────────────────────────────────────
    val stats: StateFlow<ApplicationStats?> = repository.getStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // ── Filtered application list ─────────────────────────────────────────
    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredApplications: StateFlow<List<InternshipApplication>> =
        combine(_searchQuery, _filterStatus) { q, s -> Pair(q, s) }
            .flatMapLatest { (query, status) ->
                if (query.isEmpty() && status == null)
                    repository.getAllApplications()
                else
                    repository.search(query, status)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Kanban grouped view ───────────────────────────────────────────────
    val applicationsByStatus: StateFlow<Map<ApplicationStatus, List<InternshipApplication>>> =
        repository.getAllApplications()
            .map { apps -> apps.groupBy { it.status } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    // ── Actions ───────────────────────────────────────────────────────────

    fun setSearchQuery(query: String)           { _searchQuery.value  = query  }
    fun setFilterStatus(status: ApplicationStatus?) { _filterStatus.value = status }

    fun updateStatus(application: InternshipApplication, newStatus: ApplicationStatus) {
        viewModelScope.launch {
            repository.updateApplication(
                application.copy(status = newStatus, updatedAt = LocalDateTime.now())
            )
        }
    }

    fun deleteApplication(application: InternshipApplication) {
        viewModelScope.launch { repository.deleteApplication(application) }
    }

    /**
     * Seeds 6 realistic demo applications on first launch so the app
     * looks populated right away during a demo or college presentation.
     */
    fun seedDemoData() {
        viewModelScope.launch {
            val demos = listOf(
                InternshipApplication(
                    companyName = "Google",
                    role        = "Software Engineering Intern",
                    status      = ApplicationStatus.INTERVIEW,
                    dateApplied = LocalDate.now().minusDays(14),
                    notes       = "Phone screen done. Technical round next week.",
                    location    = "Bangalore, India",
                    salaryRange = "₹80,000/month"
                ),
                InternshipApplication(
                    companyName = "Microsoft",
                    role        = "Product Management Intern",
                    status      = ApplicationStatus.APPLIED,
                    dateApplied = LocalDate.now().minusDays(7),
                    notes       = "Applied via LinkedIn. Awaiting HR response.",
                    location    = "Hyderabad, India"
                ),
                InternshipApplication(
                    companyName = "Flipkart",
                    role        = "Data Science Intern",
                    status      = ApplicationStatus.OFFER,
                    dateApplied = LocalDate.now().minusDays(30),
                    notes       = "Offer received! Stipend ₹60,000/month.",
                    salaryRange = "₹60,000/month",
                    location    = "Bangalore, India"
                ),
                InternshipApplication(
                    companyName = "Amazon",
                    role        = "SDE Intern",
                    status      = ApplicationStatus.REJECTED,
                    dateApplied = LocalDate.now().minusDays(21),
                    notes       = "Rejected after online assessment stage.",
                    location    = "Hyderabad, India"
                ),
                InternshipApplication(
                    companyName = "Swiggy",
                    role        = "Backend Engineering Intern",
                    status      = ApplicationStatus.APPLIED,
                    dateApplied = LocalDate.now().minusDays(3),
                    notes       = "Referral from college senior.",
                    location    = "Bangalore, India"
                ),
                InternshipApplication(
                    companyName = "Razorpay",
                    role        = "Frontend Developer Intern",
                    status      = ApplicationStatus.INTERVIEW,
                    dateApplied = LocalDate.now().minusDays(10),
                    notes       = "Technical assignment submitted, awaiting review.",
                    location    = "Bangalore, India"
                )
            )
            demos.forEach { repository.addApplication(it) }
        }
    }
}
