package com.hafsaIlyas.expensetracker.di

// di/PreferencesModule.kt

import com.hafsaIlyas.expensetracker.data.preferences.UserPreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// UserPreferencesRepository is @Inject-annotated with @Singleton,
// so Hilt auto-provides it — no manual @Provides needed.
// This module is a placeholder if you add more preferences providers later.
@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule