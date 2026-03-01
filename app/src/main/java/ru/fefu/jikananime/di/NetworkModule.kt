package ru.fefu.jikananime.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import ru.fefu.jikananime.data.api.JikanApi
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://api.jikan.moe/v4/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor { chain ->
                var attempt = 0
                var response: okhttp3.Response? = null
                val maxAttempts = 5

                while (attempt < maxAttempts) {
                    try {
                        response = chain.proceed(chain.request())
                        if (response.isSuccessful) {
                            break
                        }
                    } catch (e: Exception) {
                    }

                    attempt++
                    if (attempt < maxAttempts) {
                        try {
                            Thread.sleep(2000)
                        } catch (e: InterruptedException) {
                            Thread.currentThread().interrupt()
                            break
                        }
                    }
                }

                response ?: throw java.net.SocketTimeoutException("Connection error")
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        val contentType = "application/json".toMediaType()
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideJikanApi(retrofit: Retrofit): JikanApi {
        return retrofit.create(JikanApi::class.java)
    }
}