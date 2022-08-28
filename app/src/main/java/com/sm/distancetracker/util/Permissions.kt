package com.sm.distancetracker.util

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.fragment.app.Fragment
import com.sm.distancetracker.util.Constants.BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
import com.sm.distancetracker.util.Constants.LOCATION_PERMISSION_REQUEST_CODE
import com.vmadalin.easypermissions.EasyPermissions

object Permissions {

    fun hasLocationPermission(context: Context): Boolean {
        return EasyPermissions.hasPermissions(context, Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun requestLocationPermission(fragment:Fragment){
        EasyPermissions.requestPermissions(
            fragment ,
            "This application cannot work without 'location' permission" ,
            LOCATION_PERMISSION_REQUEST_CODE ,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    fun hasBackgroundLocationPermission(context: Context) : Boolean{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            EasyPermissions.hasPermissions(context,Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            true
        }
    }

    fun requestBackgroundLocationPermission(fragment: Fragment){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                fragment,
                "This application cannot work without 'background location' permission" ,
                BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE ,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

}