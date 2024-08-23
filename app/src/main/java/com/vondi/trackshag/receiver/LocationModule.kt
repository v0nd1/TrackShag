package com.vondi.trackshag.receiver

import android.content.Context
import android.content.Intent
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Provides
    fun provideLocationServiceIntent(@ApplicationContext context: Context): Intent {
        return Intent(context, LocationService::class.java)
    }
}