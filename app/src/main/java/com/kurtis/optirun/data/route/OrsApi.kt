package com.kurtis.optirun.data.route

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Query
interface OrsApi {
    @POST("v2/directions/foot-walking/geojson")
    suspend fun roundTrip(@Body body: OrsRequest): OrsResponse

    @GET("geocode/autocomplete")
    suspend fun autocomplete(
        @Query("text") text: String,
        @Query("focus.point.lon") focusLon: Double? = null,   // biases results near the user
        @Query("focus.point.lat") focusLat: Double? = null,
        @Query("size") size: Int = 5,
    ): GeocodeResponse
}

//  sending
@Serializable
data class OrsRequest(
    val coordinates: List<List<Double>>,   // [[lon, lat]]: longitude comes first
    val elevation: Boolean = true,
    val options: OrsOptions,
)
@Serializable
data class OrsOptions(
    @SerialName("round_trip") val roundTrip: RoundTrip,
    @SerialName("avoid_features") val avoidFeatures: List<String> = listOf("ferries", "fords"),
)
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

@Serializable
data class GeocodeResponse(val features: List<GeocodeFeature> = emptyList())
@Serializable
data class GeocodeFeature(val geometry: PointGeometry, val properties: GeocodeProperties)
@Serializable
data class PointGeometry(val coordinates: List<Double>)   // [lon, lat]
@Serializable
data class GeocodeProperties(val label: String = "")
