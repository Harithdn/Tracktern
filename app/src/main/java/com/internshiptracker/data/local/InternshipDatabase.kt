package com.internshiptracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.internshiptracker.data.local.dao.ApplicationDao
import com.internshiptracker.data.local.entities.ApplicationEntity
import com.internshiptracker.data.local.entities.Converters

/**
 * Room database definition.
 * Single source of truth for all local persistence.
 *
 * Version history:
 *   1 — initial schema with applications table
 */
@Database(
    entities = [ApplicationEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class InternshipDatabase : RoomDatabase() {
    abstract fun applicationDao(): ApplicationDao

    companion object {
        const val DATABASE_NAME = "internship_tracker.db"
    }
}
