package com.hafsaIlyas.expensetracker.di

// di/AiModule.kt

import com.hafsaIlyas.expensetracker.data.ai.AiInsightService
import com.hafsaIlyas.expensetracker.data.ai.MockAiInsightService
// import com.yourpackage.expensetracker.data.ai.GeminiAiService  ← swap here
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {

    @Binds
    @Singleton
    abstract fun bindAiInsightService(
        impl: MockAiInsightService
        // impl: GeminiAiService        ← swap here for production
    ): AiInsightService
}