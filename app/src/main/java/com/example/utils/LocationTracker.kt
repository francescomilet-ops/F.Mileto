package com.example.utils

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object LocationTracker {
    suspend fun getCurrentLocation(context: Context): Pair<Double, Double>? = suspendCancellableCoroutine { cont ->
        try {
            // Explizite Laufzeit-Prüfung der Berechtigung, um SecurityExceptions zu verhindern
            val hasFineLocation = androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            
            val hasCoarseLocation = androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            if (!hasFineLocation && !hasCoarseLocation) {
                // Keine Berechtigung vorhanden - breche sicher ab
                cont.resume(null)
                return@suspendCancellableCoroutine
            }

            @SuppressLint("MissingPermission")
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).addOnSuccessListener { result ->
                if (result != null) {
                    cont.resume(Pair(result.latitude, result.longitude))
                } else {
                    cont.resume(null)
                }
            }.addOnFailureListener {
                cont.resume(null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (cont.isActive) {
                cont.resume(null)
            }
        }
    }
}

