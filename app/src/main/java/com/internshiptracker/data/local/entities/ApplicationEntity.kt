package com.internshiptracker.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.internshiptracker.domain.model.ApplicationStatus
import com.internshiptracker.domain.model.InternshipApplication
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Room database entity.  The schema is intentionally simple — no reminder
 * columns — keeping the table easy to migrate and query.
 *
 * Database version: 1
 */
@Entity(tableName = "applications")
@TypeConverters(Converters::class)
data class ApplicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val companyName: String,
    val role: String,
    val status: String,           // stored as enum name, e.g. "APPLIED"
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

// ── Mapping functions ─────────────────────────────────────────────────────────

fun ApplicationEntity.toDomain() = InternshipApplication(
    id             = id,
    companyName    = companyName,
    role           = role,
    status         = ApplicationStatus.valueOf(status),
    dateApplied    = dateApplied,
    notes          = notes,
    jobDescription = jobDescription,
    resumeText     = resumeText,
    matchScore     = matchScore,
    salaryRange    = salaryRange,
    location       = location,
    applicationUrl = applicationUrl,
    contactName    = contactName,
    contactEmail   = contactEmail,
    createdAt      = createdAt,
    updatedAt      = updatedAt
)

fun InternshipApplication.toEntity() = ApplicationEntity(
    id             = id,
    companyName    = companyName,
    role           = role,
    status         = status.name,
    dateApplied    = dateApplied,
    notes          = notes,
    jobDescription = jobDescription,
    resumeText     = resumeText,
    matchScore     = matchScore,
    salaryRange    = salaryRange,
    location       = location,
    applicationUrl = applicationUrl,
    contactName    = contactName,
    contactEmail   = contactEmail,
    createdAt      = createdAt,
    updatedAt      = updatedAt
)

// ── Room TypeConverters for java.time ─────────────────────────────────────────

class Converters {
    @TypeConverter fun fromLocalDate(v: LocalDate?): String? = v?.toString()
    @TypeConverter fun toLocalDate(v: String?): LocalDate?   = v?.let { LocalDate.parse(it) }

    @TypeConverter fun fromLocalDateTime(v: LocalDateTime?): String? = v?.toString()
    @TypeConverter fun toLocalDateTime(v: String?): LocalDateTime?   = v?.let { LocalDateTime.parse(it) }
}
