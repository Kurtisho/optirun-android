package com.kurtis.optirun.data.location

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.kurtis.optirun.domain.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class LocationProvider @Inject constructor(@ApplicationContext context: Context) {
    private val client = LocationServices.getFusedLocationProviderClient(context)

    // Returns null if permission was denied, there's no fix, or it takes over 10s
    @SuppressLint("MissingPermission")
    suspend fun current(): LatLng? = try {
        withTimeoutOrNull(10_000.milliseconds) {
            suspendCancellableCoroutine<LatLng?> { cont ->
                client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { loc -> cont.resume(loc?.let { LatLng(it.latitude, it.longitude) }) }
                    .addOnFailureListener { cont.resume(null) }
            }
        }
    } catch (e: SecurityException) {
        null
    }
}