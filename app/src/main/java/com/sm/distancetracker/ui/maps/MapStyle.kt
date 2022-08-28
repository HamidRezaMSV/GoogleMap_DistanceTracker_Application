package com.sm.distancetracker.ui.maps

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MapStyleOptions
import com.sm.distancetracker.R

class MapStyle {

    companion object{
        const val MAP_NIGHT_STYLE_CODE = R.raw.map_night_style
        const val MAP_RETRO_STYLE_CODE = R.raw.map_retro_style
        const val MAP_STANDARD_STYLE_CODE = R.raw.map_standard_style

        fun setMapStyle(context: Context,googleMap: GoogleMap , style : Int){
            try {
                val success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context,style))
                if (!success){
                    Toast.makeText(context, "fail to load map style", Toast.LENGTH_SHORT).show()
                }
            }catch (e:Exception){
                Log.d("map_style_exception", e.toString())
            }
        }
    }

}