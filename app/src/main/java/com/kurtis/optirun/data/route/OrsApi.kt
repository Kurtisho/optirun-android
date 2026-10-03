package com.kurtis.optirun.data.route

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

interface OrsApi {
    @POST("v2/directions/foot-walking/geojson")
    suspend fun roundTrip(@Body body: OrsRequest): OrsResponse
}

//  sending
@Serializable
data class OrsRequest(
    val coordinates: List<List<Double>>,   // [[lon, lat]]: longitude comes first
    val elevation: Boolean = true,
    val options: OrsOptions,
)
@Serializable
data class OrsOptions(@SerialName("round_trip") val roundTrip: RoundTrip)
@Serializable
data class RoundTrip(val length: Int, val points: Int = 3, val seed: Int)

// receiving
@Serializable
data class OrsResponse(val features: List<OrsFeature>)
@Serializable
data class OrsFeature(val geometry: OrsGeometry, val properties: OrsProperties)
@Serializable
data class OrsGeometry(val coordinates: List<List<Double>>)   // [lon, lat, elevation]
@Serializable
data class OrsProperties(
    val summary: OrsSummary,
    val ascent: Double = 0.0,
    val descent: Double = 0.0,
)
@Serializable
data class OrsSummary(val distance: Double = 0.0, val duration: Double = 0.0)