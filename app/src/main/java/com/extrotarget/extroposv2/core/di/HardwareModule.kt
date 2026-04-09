package com.extrotarget.extroposv2.core.di

import com.extrotarget.extroposv2.core.hardware.scale.MockScale
import com.extrotarget.extroposv2.core.hardware.scale.ScaleInterface
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HardwareModule {

    @Binds
    @Singleton
    abstract fun bindScale(
        mockScale: MockScale
    ): ScaleInterface
}
