package com.sm.distancetracker.ui.maps

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.sm.distancetracker.R
import com.sm.distancetracker.databinding.FragmentMapsBinding
import com.sm.distancetracker.helper.SpManager
import com.sm.distancetracker.model.Result
import com.sm.distancetracker.service.TrackerService
import com.sm.distancetracker.ui.maps.MapStyle.Companion.MAP_NIGHT_STYLE_CODE
import com.sm.distancetracker.ui.maps.MapStyle.Companion.MAP_RETRO_STYLE_CODE
import com.sm.distancetracker.ui.maps.MapStyle.Companion.MAP_STANDARD_STYLE_CODE
import com.sm.distancetracker.ui.maps.MapStyle.Companion.setMapStyle
import com.sm.distancetracker.ui.maps.MapUtil.calculateDistance
import com.sm.distancetracker.ui.maps.MapUtil.calculateElapsedTime
import com.sm.distancetracker.ui.maps.MapUtil.getCameraPosition
import com.sm.distancetracker.ui.maps.MapUtil.lockMapsUiComponents
import com.sm.distancetracker.ui.maps.MapUtil.unLockMapsUiComponents
import com.sm.distancetracker.util.Constants.ACTION_SERVICE_START
import com.sm.distancetracker.util.Constants.ACTION_SERVICE_STOP
import com.sm.distancetracker.util.Constants.MAP_STYLE_NIGHT
import com.sm.distancetracker.util.Constants.MAP_STYLE_RETRO
import com.sm.distancetracker.util.Constants.MAP_STYLE_STANDARD
import com.sm.distancetracker.util.ExtensionFunctions.disable
import com.sm.distancetracker.util.ExtensionFunctions.enable
import com.sm.distancetracker.util.ExtensionFunctions.hide
import com.sm.distancetracker.util.ExtensionFunctions.show
import com.sm.distancetracker.util.GPSUtil.turnOnGPS
import com.sm.distancetracker.util.Permissions.hasBackgroundLocationPermission
import com.sm.distancetracker.util.Permissions.requestBackgroundLocationPermission
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MapsFragment : Fragment(), OnMapReadyCallback,
    GoogleMap.OnMyLocationButtonClickListener, EasyPermissions.PermissionCallbacks ,
GoogleMap.OnMarkerClickListener{

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!
    private lateinit var map: GoogleMap
    private var locationList = mutableListOf<LatLng>()
    private var polylineList = mutableListOf<Polyline>()
    private var markerList = mutableListOf<Marker>()
    private var startTime = 0L
    private var stopTime = 0L
    private var savedMapStyle : String = MAP_STYLE_STANDARD
    var started : Boolean = false
    private var shouldExecuteResultFunctions : Boolean = false
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = this
        binding.tracking = this

        turnOnGPS(requireContext())

        binding.startButton.setOnClickListener { onStartButtonClicked() }
        binding.stopButton.setOnClickListener { onStopButtonClicked() }
        binding.resetButton.setOnClickListener { onResetButtonClicked() }
        binding.settingButton.setOnClickListener { onSettingButtonClicked() }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.isMyLocationEnabled = true
        map.setOnMyLocationButtonClickListener(this)
        map.setOnMarkerClickListener(this)
        map.uiSettings.isMyLocationButtonEnabled = true
        unLockMapsUiComponents(map)

        // Check saved map style and set it :
        savedMapStyle = SpManager.getMapStyleFromSharedPreferences(requireContext())
        setSavedMapStyle(savedMapStyle)
        setHintTextViewColor(savedMapStyle)

        observeTrackerService()
    }

    private fun setSavedMapStyle(style:String) {
        when(style){
            MAP_STYLE_STANDARD -> { setMapStyle(requireContext(),map,MAP_STANDARD_STYLE_CODE) }
            MAP_STYLE_NIGHT -> { setMapStyle(requireContext(),map,MAP_NIGHT_STYLE_CODE) }
            MAP_STYLE_RETRO -> { setMapStyle(requireContext(),map,MAP_RETRO_STYLE_CODE) }
        }
    }

    private fun setHintTextViewColor(style: String) {
        when(style){
            MAP_STYLE_STANDARD -> { binding.hintTextView.setTextColor(Color.BLACK) }
            MAP_STYLE_NIGHT -> { binding.hintTextView.setTextColor(Color.WHITE) }
            MAP_STYLE_RETRO -> { binding.hintTextView.setTextColor(Color.BLACK) }
        }
    }

    private fun observeTrackerService() {
        TrackerService.locationList.observe(viewLifecycleOwner) {
            if (it != null) {
                locationList = it
                drawPolyline()
                followPolyline()
                // Enable stop button when our list has at least one item(location) :
                if (locationList.size > 1){
                    binding.stopButton.enable()
                }
            }
        }
        TrackerService.started.observe(viewLifecycleOwner){ started = it }
        TrackerService.startTime.observe(viewLifecycleOwner){ startTime = it }
        TrackerService.stopTime.observe(viewLifecycleOwner){
            stopTime = it
            if (stopTime != 0L && shouldExecuteResultFunctions){
                showBiggerPicture()
                displayResult()
            }
        }
    }

    private fun showBiggerPicture() {
        val latLngBounds = LatLngBounds.Builder()
        locationList.forEach { latLngBounds.include(it) }
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds.build() , 100), 2000 , null)

        addMarker(locationList.first() , BitmapDescriptorFactory.HUE_GREEN)
        addMarker(locationList.last() , BitmapDescriptorFactory.HUE_RED)
    }

    private fun addMarker(position:LatLng , hue : Float){
        val marker = map.addMarker(
            MarkerOptions().apply {
                position(position)
                icon(BitmapDescriptorFactory.defaultMarker(hue))
            }
        )
        markerList.add(marker!!)
    }

    private fun displayResult(){
        val result = Result(calculateDistance(locationList) , calculateElapsedTime(startTime, stopTime))
        lifecycleScope.launch{
            delay(2000)
            val action = MapsFragmentDirections.actionMapsFragmentToResultFragment(result)
            findNavController().navigate(action)
        }
    }

    private fun drawPolyline() {
        val polylineColor = when(savedMapStyle){
            MAP_STYLE_STANDARD -> { Color.BLUE }
            MAP_STYLE_NIGHT -> { Color.YELLOW }
            MAP_STYLE_RETRO -> { Color.RED }
            else -> { Color.BLUE }
        }
        val polyline = map.addPolyline(
            PolylineOptions().apply {
                width(10f)
                color(polylineColor)
                jointType(JointType.ROUND)
                startCap(RoundCap())
                endCap(RoundCap())
                addAll(locationList)
            }
        )
        polylineList.add(polyline)
    }

    private fun followPolyline(){
        if (locationList.isNotEmpty()){
            map.animateCamera(
                CameraUpdateFactory.newCameraPosition(getCameraPosition(locationList.last())) ,
                1000 , null
            )
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        binding.hintTextView.animate().alpha(0f).duration = 1500
        lifecycleScope.launch {
            delay(2500)
            binding.hintTextView.hide()
            binding.startButton.show()
        }
        return false
    }

    private fun onStartButtonClicked() {
        if (hasBackgroundLocationPermission(requireContext())) {
            binding.startButton.hide()
            binding.startButton.disable()
            binding.stopButton.alpha = 1f
            binding.stopButton.show()
            binding.settingButton.visibility = View.GONE
            startCountDown()
            lockMapsUiComponents(map)
        } else {
            requestBackgroundLocationPermission(this)
        }
    }

    private fun onStopButtonClicked(){
        shouldExecuteResultFunctions = true
        stopForegroundService()
        binding.stopButton.animate().alpha(0f).duration = 1000
        binding.stopButton.hide()
        binding.resetButton.alpha = 0f
        binding.resetButton.show()
        binding.resetButton.animate().alpha(1f).duration = 1000
        binding.resetButton.enable()
        unLockMapsUiComponents(map)
    }

    private fun startCountDown() {
        binding.timerTextView.show()
        binding.stopButton.disable()
        val timer: CountDownTimer = object : CountDownTimer(4000, 1000) {
            override fun onTick(valueInMillis: Long) {
                val valueInSecond = valueInMillis / 1000
                if (valueInSecond.toString() == "0") {
                    binding.timerTextView.text = getString(R.string.go)
                    if (savedMapStyle == MAP_STYLE_NIGHT){
                        binding.timerTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    }else{
                        binding.timerTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                    }
                } else {
                    binding.timerTextView.text = valueInSecond.toString()
                    binding.timerTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                }
            }

            override fun onFinish() {
                sendActionCommandToService(ACTION_SERVICE_START)
                binding.timerTextView.hide()
            }
        }
        timer.start()
    }

    private fun stopForegroundService() {
        binding.startButton.disable()
        sendActionCommandToService(ACTION_SERVICE_STOP)
    }

    private fun sendActionCommandToService(action: String) {
        Intent(requireContext(), TrackerService::class.java).apply {
            this.action = action
            requireContext().startService(this)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms[0])) {
            SettingsDialog.Builder(requireContext()).build().show()
        } else {
            requestBackgroundLocationPermission(this)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        onStartButtonClicked()
    }

    private fun onResetButtonClicked() {
        shouldExecuteResultFunctions = false
        mapReset()
    }

    @SuppressLint("MissingPermission")
    private fun mapReset() {
        fusedLocationProviderClient.lastLocation.addOnCompleteListener {
            val lastKnownLocation = LatLng(it.result.latitude , it.result.longitude)
            map.animateCamera(
                CameraUpdateFactory.newCameraPosition(getCameraPosition(lastKnownLocation)) , 2000 , null
            )
            polylineList.forEach { polyline -> polyline.remove() }
            markerList.forEach { marker -> marker.remove() }
            locationList.clear()
            polylineList.clear()
            markerList.clear()
            binding.resetButton.hide()
            binding.resetButton.disable()
            binding.startButton.enable()
            binding.startButton.show()
            binding.settingButton.visibility = View.VISIBLE
        }
    }

    override fun onMarkerClick(marker:Marker): Boolean {
        // If user click on markers we want to nothing will happen so :
        return true
    }

    private fun onSettingButtonClicked(){
        findNavController().navigate(MapsFragmentDirections.actionMapsFragmentToSettingFragment())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}