package com.hafsaIlyas.expensetracker.di

// di/AppModule.kt

import android.content.Context
import androidx.room.Room
import com.hafsaIlyas.expensetracker.data.local.ExpenseDao
import com.hafsaIlyas.expensetracker.data.local.ExpenseDatabase
import com.hafsaIlyas.expensetracker.data.repository.ExpenseRepository
import com.hafsaIlyas.expensetracker.data.repository.ExpenseRepositoryImpl

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideExpenseDatabase(
        @ApplicationContext context: Context
    ): ExpenseDatabase = Room.databaseBuilder(
        context,
        ExpenseDatabase::class.java,
        ExpenseDatabase.DATABASE_NAME
    ).build()

    @Provides
    @Singleton
    fun provideExpenseDao(database: ExpenseDatabase): ExpenseDao =
        database.expenseDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(
        impl: ExpenseRepositoryImpl
    ): ExpenseRepository
}