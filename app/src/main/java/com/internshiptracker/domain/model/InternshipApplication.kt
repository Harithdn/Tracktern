package com.internshiptracker.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Core domain model for a single internship / job application.
 * This is what every screen and ViewModel works with — never the DB entity directly.
 */
data class InternshipApplication(
    val id: Long = 0,
    val companyName: String,
    val role: String,
    val status: ApplicationStatus,
    val dateApplied: LocalDate,
    val notes: String = "",
    val jobDescription: String = "",
    val resumeText: String = "",
    val matchScore: Int? = null,
    val salaryRange: String = "",
    val location: String = "",
    val applicationUrl: String = "",
    val contactName: String = "",
    val contactEmail: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

/**
 * The four stages of an application lifecycle.
 * The order here determines display order in the Kanban board.
 */
enum class ApplicationStatus(val displayName: String) {
    APPLIED("Applied"),
    INTERVIEW("Interview"),
    OFFER("Offer"),
    REJECTED("Rejected")
}

/**
 * Aggregated stats shown on the Dashboard.
 */
data class ApplicationStats(
    val total: Int,
    val applied: Int,
    val interview: Int,
    val offer: Int,
    val rejected: Int,
    val successRate: Float    // offers / (offers + rejected) * 100
)
