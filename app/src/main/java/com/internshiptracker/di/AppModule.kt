package com.internshiptracker.di

import android.content.Context
import androidx.room.Room
import com.internshiptracker.data.local.InternshipDatabase
import com.internshiptracker.data.local.dao.ApplicationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing app-wide singletons:
 *   - Room database
 *   - DAO
 * The Repository is @Singleton and @Inject-constructor, so Hilt auto-provides it.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): InternshipDatabase =
        Room.databaseBuilder(
            context,
            InternshipDatabase::class.java,
            InternshipDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()  // For development; use proper migrations in prod
            .build()

    @Provides
    @Singleton
    fun provideApplicationDao(db: InternshipDatabase): ApplicationDao =
        db.applicationDao()
}
