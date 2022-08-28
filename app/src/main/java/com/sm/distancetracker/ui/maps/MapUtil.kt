package com.sm.distancetracker.ui.maps

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import java.text.DecimalFormat

object MapUtil {

    fun getCameraPosition(location:LatLng) : CameraPosition{
        return CameraPosition.Builder()
            .target(location)
            .zoom(17f)
            .build()
    }

    fun calculateElapsedTime(startTime:Long,stopTime:Long) : String{
        val elapsedTime = stopTime - startTime
        val seconds = (elapsedTime / 1000).toInt() % 60
        val minutes = (elapsedTime / (1000 * 60)) % 60
        val hours = (elapsedTime / (1000 * 60 * 60)) % 24

        return "$hours:$minutes:$seconds"
    }

    fun calculateDistance(locationList : MutableList<LatLng>) : String{
        if (locationList.size > 1){
            val meters = SphericalUtil.computeDistanceBetween(locationList.first() , locationList.last())
            val kilometers = meters / 1000
            return DecimalFormat("#.##").format(kilometers)
        }
        return "0.00"
    }

    fun unLockMapsUiComponents(map:GoogleMap){
        map.uiSettings.apply {
            isZoomGesturesEnabled = true
            isZoomControlsEnabled = true
            isScrollGesturesEnabled = true
            isTiltGesturesEnabled = true
            isRotateGesturesEnabled = true
            isMapToolbarEnabled = true
            isCompassEnabled = false
        }
    }

    fun lockMapsUiComponents(map:GoogleMap){
        map.uiSettings.apply {
            isZoomGesturesEnabled = false
            isZoomControlsEnabled = false
            isScrollGesturesEnabled = false
            isTiltGesturesEnabled = false
            isRotateGesturesEnabled = false
            isMapToolbarEnabled = false
            isCompassEnabled = false
        }
    }

}