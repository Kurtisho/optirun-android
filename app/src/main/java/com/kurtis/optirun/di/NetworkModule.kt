package com.kurtis.optirun.di

import com.kurtis.optirun.BuildConfig
import com.kurtis.optirun.data.route.OrsApi
import com.kurtis.optirun.data.weather.OpenMeteoApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun json(): Json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    @Provides @Singleton
    fun okHttp(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)   // route generation can be slow
        .build()

    private fun retrofit(baseUrl: String, client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides @Singleton
    fun openMeteoApi(client: OkHttpClient, json: Json): OpenMeteoApi =
        retrofit("https://api.open-meteo.com/", client, json).create(OpenMeteoApi::class.java)

    @Provides @Singleton
    fun orsApi(client: OkHttpClient, json: Json): OrsApi {
        val keyed = client.newBuilder()
            .addInterceptor { chain ->
                chain.proceed(chain.request().newBuilder().header("Authorization", BuildConfig.ORS_API_KEY).build())
            }
            .build()
        return retrofit("https://api.openrouteservice.org/", keyed, json).create(OrsApi::class.java)
    }
}