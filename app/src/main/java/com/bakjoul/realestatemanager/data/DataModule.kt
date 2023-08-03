package com.bakjoul.realestatemanager.data

import com.bakjoul.realestatemanager.BuildConfig
import com.bakjoul.realestatemanager.data.api.CurrencyApi
import com.bakjoul.realestatemanager.data.api.GoogleApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataModule {

    @Singleton
    @Provides
    fun provideGson(): Gson = GsonBuilder().setLenient().create()

    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    @CurrencyApiHttpClient
    @Singleton
    @Provides
    fun provideCurrencyApiOkHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(httpLoggingInterceptor)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .addInterceptor(
            Interceptor { chain: Interceptor.Chain ->
                chain.proceed(
                    chain.request().let { request ->
                        request
                            .newBuilder()
                            .url(
                                request.url.newBuilder()
                                    .addQueryParameter("api_key", BuildConfig.CURRENCY_API_KEY)
                                    .build()
                            )
                            .build()
                    }
                )
            }
        )
        .build()

    @GoogleApiHttpClient
    @Singleton
    @Provides
    fun provideGoogleApiOkHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(httpLoggingInterceptor)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    @Singleton
    @Provides
    @CurrencyApiRetrofit
    fun provideCurrencyApiRetrofit(@CurrencyApiHttpClient httpClient: OkHttpClient, gson: Gson): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.getgeoapi.com/v2/currency/convert/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(httpClient)
        .build()

    @Singleton
    @Provides
    @GoogleApiRetrofit
    fun provideGoogleApiRetrofit(@GoogleApiHttpClient httpClient: OkHttpClient, gson: Gson): Retrofit = Retrofit.Builder()
        .baseUrl("https://maps.googleapis.com/maps/api/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(httpClient)
        .build()

    @Singleton
    @Provides
    fun provideCurrencyApi(@CurrencyApiRetrofit retrofit: Retrofit): CurrencyApi = retrofit.create(CurrencyApi::class.java)

    @Singleton
    @Provides
    fun provideGoogleApi(@GoogleApiRetrofit retrofit: Retrofit): GoogleApi = retrofit.create(GoogleApi::class.java)

    @Singleton
    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GoogleApiRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GoogleApiHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CurrencyApiRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CurrencyApiHttpClient