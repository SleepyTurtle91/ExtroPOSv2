package com.extrotarget.extroposv2.core.di

import com.extrotarget.extroposv2.BuildConfig
import com.extrotarget.extroposv2.core.config.AppConfig
import com.extrotarget.extroposv2.core.network.api.autocount.AutoCountApi
import com.google.gson.Gson
import com.extrotarget.extroposv2.core.network.api.lhdn.MyInvoisApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideMyInvoisApi(okHttpClient: OkHttpClient): MyInvoisApi {
        // Defaulting to Sandbox, but LhdnRepository handles the switch dynamically
        return Retrofit.Builder()
            .baseUrl(AppConfig.Network.LHDN_SANDBOX_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MyInvoisApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideAutoCountApi(okHttpClient: OkHttpClient): AutoCountApi {
        return Retrofit.Builder()
            .baseUrl(AppConfig.Network.AUTOCOUNT_DEFAULT_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AutoCountApi::class.java)
    }
}
