package com.sm.distancetracker.ui.permission

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.sm.distancetracker.util.Permissions.hasLocationPermission
import com.sm.distancetracker.util.Permissions.requestLocationPermission
import com.sm.distancetracker.databinding.FragmentPermissionBinding
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog

class PermissionFragment : Fragment(),EasyPermissions.PermissionCallbacks {

    private var _binding : FragmentPermissionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPermissionBinding.inflate(inflater,container,false)

        binding.continueButton.setOnClickListener { continueButtonClicked() }

        return binding.root
    }

    private fun continueButtonClicked() {
        if (hasLocationPermission(requireContext())){
            findNavController().navigate(PermissionFragmentDirections.actionPermissionFragmentToMapsFragment())
        }else{
            requestLocationPermission(this)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this,perms[0])){
            SettingsDialog.Builder(requireContext())
                .build()
                .show()
        }else{
            requestLocationPermission(this)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        findNavController().navigate(PermissionFragmentDirections.actionPermissionFragmentToMapsFragment())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}