package com.sm.distancetracker.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.sm.distancetracker.ui.MainActivity
import com.sm.distancetracker.R
import com.sm.distancetracker.helper.SpManager.Companion.saveStartedState
import com.sm.distancetracker.ui.maps.MapUtil.calculateDistance
import com.sm.distancetracker.util.Constants.ACTION_SERVICE_START
import com.sm.distancetracker.util.Constants.ACTION_SERVICE_STOP
import com.sm.distancetracker.util.Constants.FAST_LOCATION_UPDATE_INTERVAL
import com.sm.distancetracker.util.Constants.LOCATION_UPDATE_INTERVAL
import com.sm.distancetracker.util.Constants.NOTIFICATION_CHANNEL_ID
import com.sm.distancetracker.util.Constants.NOTIFICATION_CHANNEL_NAME
import com.sm.distancetracker.util.Constants.NOTIFICATION_ID
import com.sm.distancetracker.util.Constants.PENDING_INTENT_REQUEST_CODE

class TrackerService : LifecycleService() {

    private lateinit var notificationManager : NotificationManager
    private lateinit var notification : NotificationCompat.Builder
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    companion object {
        val started = MutableLiveData<Boolean>()
        val locationList = MutableLiveData<MutableList<LatLng>>()
        val startTime = MutableLiveData<Long>()
        val stopTime = MutableLiveData<Long>()
    }

    private fun setInitialValues() {
        started.postValue(false)
        locationList.postValue(mutableListOf())
        startTime.postValue(0L)
        stopTime.postValue(0L)
    }

    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            result.locations.forEach { location->
                updateLocationList(location)
                updateNotification()
            }
        }
    }

    private fun updateLocationList(location: Location){
        val latLng = LatLng(location.latitude,location.longitude)
        locationList.value?.apply {
            add(latLng)
            locationList.postValue(this)
        }
    }

    override fun onCreate() {
        setInitialValues()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_SERVICE_START -> {
                    started.postValue(true)
                    startForegroundService()
                    startLocationUpdates()
                    // Shared Preferences :
                    saveStartedState(this , true)
                }
                ACTION_SERVICE_STOP -> {
                    started.postValue(false)
                    stopForegroundService()
                    // Shared Preferences :
                    saveStartedState(this , false)
                }
                else -> {}
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService(){
        createNotificationChannel()
        startForeground(NOTIFICATION_ID,notification.build())
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates(){
        val locationRequest = LocationRequest().apply {
            interval = LOCATION_UPDATE_INTERVAL
            fastestInterval = FAST_LOCATION_UPDATE_INTERVAL
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper())
        startTime.postValue(System.currentTimeMillis())
    }

    private fun stopForegroundService() {
        removeLocationUpdates()
        notificationManager.cancel(NOTIFICATION_ID)
        stopForeground(true)
        stopSelf()
        stopTime.postValue(System.currentTimeMillis())
    }

    private fun removeLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun createNotificationChannel() {
        notification = provideNotificationBuilder(this)
        notificationManager = provideNotificationManager(this)

        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun updateNotification() {
        notification.apply {
            setContentTitle("Distance Travelled")
            setContentText("You travelled "+locationList.value?.let { calculateDistance(it) } + " km ")
        }
        notificationManager.notify(NOTIFICATION_ID , notification.build())
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun provideNotificationBuilder(context: Context): NotificationCompat.Builder {
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(
                context,
                PENDING_INTENT_REQUEST_CODE,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getActivity(
                context,
                PENDING_INTENT_REQUEST_CODE,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_run)
            .setContentIntent(pendingIntent)
    }

    private fun provideNotificationManager(context: Context): NotificationManager {
        //notificationManager = getSystemService(NotificationManager::class.java)
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}