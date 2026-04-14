package com.internshiptracker.data.local.dao

import androidx.room.*
import com.internshiptracker.data.local.entities.ApplicationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO.  Every query returns a Flow so the UI reacts automatically
 * whenever the database changes — no manual refresh calls needed.
 */
@Dao
interface ApplicationDao {

    // ── Write operations ──────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ApplicationEntity): Long

    @Update
    suspend fun update(entity: ApplicationEntity)

    @Delete
    suspend fun delete(entity: ApplicationEntity)

    @Query("DELETE FROM applications WHERE id = :id")
    suspend fun deleteById(id: Long)

    // ── Read operations ───────────────────────────────────────────────────

    /** All applications, newest first. */
    @Query("SELECT * FROM applications ORDER BY dateApplied DESC, createdAt DESC")
    fun getAllApplications(): Flow<List<ApplicationEntity>>

    /** Single application by primary key. */
    @Query("SELECT * FROM applications WHERE id = :id")
    suspend fun getById(id: Long): ApplicationEntity?

    /** Filter by status string (e.g. "INTERVIEW"). */
    @Query("SELECT * FROM applications WHERE status = :status ORDER BY dateApplied DESC")
    fun getByStatus(status: String): Flow<List<ApplicationEntity>>

    /**
     * Combined search + optional status filter.
     * Pass null for [status] to search across all statuses.
     */
    @Query("""
        SELECT * FROM applications
        WHERE (companyName LIKE '%' || :query || '%'
            OR role        LIKE '%' || :query || '%')
          AND (:status IS NULL OR status = :status)
        ORDER BY dateApplied DESC
    """)
    fun searchWithFilter(query: String, status: String?): Flow<List<ApplicationEntity>>

    /** Total row count. */
    @Query("SELECT COUNT(*) FROM applications")
    fun getTotalCount(): Flow<Int>

    /** Row count for a specific status. */
    @Query("SELECT COUNT(*) FROM applications WHERE status = :status")
    fun getCountByStatus(status: String): Flow<Int>
}
