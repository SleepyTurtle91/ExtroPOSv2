package com.extrotarget.extroposv2.core.di

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

    private const val SANDBOX_BASE_URL = "https://preprod-api.myinvois.hasil.gov.my/"
    private const val PRODUCTION_BASE_URL = "https://api.myinvois.hasil.gov.my/"
    private const val AUTOCOUNT_BASE_URL = "http://localhost:8080/" // Usually a local server for AutoCount API

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
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
        // Retrofit instances are usually static, but we can use an Interceptor 
        // or a dynamic BaseUrl approach. For simplicity in this POS, 
        // we'll provide the Sandbox one as default and handle the switch in the repository 
        // by creating a temporary Retrofit instance if needed, OR just using a single one if only one environment is used at a time.
        
        return Retrofit.Builder()
            .baseUrl(SANDBOX_BASE_URL) // Default
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
            .baseUrl(AUTOCOUNT_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AutoCountApi::class.java)
    }
}
