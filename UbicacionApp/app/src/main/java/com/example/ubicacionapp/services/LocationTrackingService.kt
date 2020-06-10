package com.example.ubicacionapp.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import com.example.ubicacionapp.model.LocationData
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LocationTrackingService  : Service() {

    var locationManager: LocationManager? = null

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onCreate() {
        if (locationManager == null)
            locationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        try {
            locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, INTERVAL, DISTANCE, locationListeners[1])
        } catch (e: SecurityException) {
            Log.e(TAG, "Fail to request location update", e)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Network provider does not exist", e)
        }

        try {
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, INTERVAL, DISTANCE, locationListeners[0])
        } catch (e: SecurityException) {
            Log.e(TAG, "Fail to request location update", e)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "GPS provider does not exist", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (locationManager != null)
            for (i in 0..locationListeners.size) {
                try {
                    locationManager?.removeUpdates(locationListeners[i])
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to remove location listeners")
                }
            }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        startService(Intent(this, LocationTrackingService::class.java))
    }

    companion object {
        val TAG = "LocationTrackingService"

        val INTERVAL = 3000.toLong() // In milliseconds
        val DISTANCE = 0.toFloat() // In meters

        val locationListeners = arrayOf(
            LTRLocationListener(LocationManager.GPS_PROVIDER),
            LTRLocationListener(LocationManager.NETWORK_PROVIDER)
        )

        class LTRLocationListener(provider: String) : android.location.LocationListener {

            val lastLocation = Location(provider)

            private lateinit var rootNode: FirebaseDatabase
            private lateinit var reference:DatabaseReference

            override fun onLocationChanged(location: Location?) {
                lastLocation.set(location)
                rootNode = FirebaseDatabase.getInstance()
                reference = rootNode.getReference("Locations")
                var locationData = LocationData(location!!.latitude.toString(), location.longitude.toString())
                reference.setValue(locationData)

            }

            override fun onProviderDisabled(provider: String?) {
            }

            override fun onProviderEnabled(provider: String?) {
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }

        }
    }

}