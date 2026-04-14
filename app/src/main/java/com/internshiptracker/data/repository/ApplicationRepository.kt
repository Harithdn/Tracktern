package com.internshiptracker.data.repository

import com.internshiptracker.data.local.dao.ApplicationDao
import com.internshiptracker.data.local.entities.toDomain
import com.internshiptracker.data.local.entities.toEntity
import com.internshiptracker.domain.model.ApplicationStats
import com.internshiptracker.domain.model.ApplicationStatus
import com.internshiptracker.domain.model.InternshipApplication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single repository that abstracts the Room DAO from ViewModels.
 * ViewModels never import DAO, Entity, or Room types directly.
 */
@Singleton
class ApplicationRepository @Inject constructor(
    private val dao: ApplicationDao
) {

    // ── CRUD ─────────────────────────────────────────────────────────────

    suspend fun addApplication(app: InternshipApplication): Long = dao.insert(app.toEntity())

    suspend fun updateApplication(app: InternshipApplication) = dao.update(app.toEntity())

    suspend fun deleteApplication(app: InternshipApplication) = dao.delete(app.toEntity())

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun getById(id: Long): InternshipApplication? = dao.getById(id)?.toDomain()

    // ── Queries ───────────────────────────────────────────────────────────

    fun getAllApplications(): Flow<List<InternshipApplication>> =
        dao.getAllApplications().map { list -> list.map { it.toDomain() } }

    fun getByStatus(status: ApplicationStatus): Flow<List<InternshipApplication>> =
        dao.getByStatus(status.name).map { list -> list.map { it.toDomain() } }

    fun search(query: String, status: ApplicationStatus? = null): Flow<List<InternshipApplication>> =
        dao.searchWithFilter(query, status?.name).map { list -> list.map { it.toDomain() } }

    // ── Dashboard stats ───────────────────────────────────────────────────

    /**
     * Combines the full application list into a single [ApplicationStats] object.
     * Updates reactively whenever the database changes.
     */
    fun getStats(): Flow<ApplicationStats> =
        getAllApplications().map { apps ->
            val applied   = apps.count { it.status == ApplicationStatus.APPLIED }
            val interview = apps.count { it.status == ApplicationStatus.INTERVIEW }
            val offer     = apps.count { it.status == ApplicationStatus.OFFER }
            val rejected  = apps.count { it.status == ApplicationStatus.REJECTED }
            val closed    = offer + rejected
            val successRate = if (closed > 0) (offer.toFloat() / closed) * 100f else 0f

            ApplicationStats(
                total       = apps.size,
                applied     = applied,
                interview   = interview,
                offer       = offer,
                rejected    = rejected,
                successRate = successRate
            )
        }
}
