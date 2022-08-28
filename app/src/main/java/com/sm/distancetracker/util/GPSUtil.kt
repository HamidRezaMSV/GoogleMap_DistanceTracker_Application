package com.sm.distancetracker.util

import android.app.Activity
import android.content.Context
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.sm.distancetracker.util.Constants.GPS_CODE

object GPSUtil {

    fun turnOnGPS(context:Context) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val settingClient = LocationServices.getSettingsClient(context)
        val locationRequest = LocationRequest.create()
        locationRequest.priority =  Priority.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 500
        val builder: LocationSettingsRequest.Builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)
        val locationSettingsRequest = builder.build()

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                settingClient.checkLocationSettings(locationSettingsRequest)
                    .addOnSuccessListener(context as Activity) { Log.d("gps_tag", "turnOnGPS: Already Enabled") }
                    .addOnFailureListener { exception ->
                        if ((exception as ApiException).statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                            try {
                                val resolvableApiException = exception as ResolvableApiException
                                resolvableApiException.startResolutionForResult(context , GPS_CODE)
                            } catch (e: Exception) {
                                Log.d("gps_tag", "turnOnGPS: Unable to start default functionality of GPS")
                                Log.d("gps_tag", "turnOnGPS: ${e.message}")
                            }
                        } else {
                            if (exception.statusCode == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
                                val errorMessage = "Location settings are inadequate, and cannot be fixed here. Fix in Settings."
                                Log.e("gps_tag", errorMessage)
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
            }
    }

}