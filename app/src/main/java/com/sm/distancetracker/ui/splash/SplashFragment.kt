package com.sm.distancetracker.ui.splash

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.sm.distancetracker.databinding.FragmentSplashBinding
import com.sm.distancetracker.helper.SpManager.Companion.getStartedState
import com.sm.distancetracker.util.Permissions.hasLocationPermission
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    private var _binding : FragmentSplashBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSplashBinding.inflate(inflater,container,false)

        // Handle opening app from notification content :
        if (getStartedState(requireContext()) && hasLocationPermission(requireContext())){
            findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToMapsFragment())
        }else{
            showSplash()
        }

        return binding.root
    }

    private fun showSplash(){
        binding.splashTextView.alpha = 0f
        lifecycleScope.launch {
            delay(1000)
            binding.splashTextView.animate().alpha(1f).duration = 3000
        }
        lifecycleScope.launch {
            delay(3500)
            navigateToNextFragment()
        }
    }

    private fun navigateToNextFragment() {
        if (hasLocationPermission(requireContext())){
            findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToMapsFragment())
        }else{
            findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToPermissionFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}