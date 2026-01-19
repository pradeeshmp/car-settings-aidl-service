package com.automotive.carsettings.service.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for service-level dependencies.
 * All dependencies are injected via constructor injection in this project.
 */
@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    // All dependencies use constructor injection via @Inject
    // This module is here for future provider methods if needed
}
