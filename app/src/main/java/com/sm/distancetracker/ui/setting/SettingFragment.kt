package com.sm.distancetracker.ui.setting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.sm.distancetracker.databinding.FragmentSettingBinding
import com.sm.distancetracker.helper.SpManager
import com.sm.distancetracker.util.Constants.MAP_STYLE_NIGHT
import com.sm.distancetracker.util.Constants.MAP_STYLE_RETRO
import com.sm.distancetracker.util.Constants.MAP_STYLE_STANDARD
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SettingFragment : Fragment() {

    private var _binding : FragmentSettingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingBinding.inflate(inflater,container,false)

        binding.backTextView.setOnClickListener { onBackButtonClicked() }
        binding.saveTextView.setOnClickListener { onSaveButtonClicked() }

        initialMapStyleSpinner()
        binding.mapStyleSpinner.onItemSelectedListener = SpinnerListener.listener

        return binding.root
    }

    private fun initialMapStyleSpinner() {
        when(SpManager.getMapStyleFromSharedPreferences(requireContext())){
            MAP_STYLE_STANDARD -> { binding.mapStyleSpinner.setSelection(0) }
            MAP_STYLE_NIGHT -> { binding.mapStyleSpinner.setSelection(1) }
            MAP_STYLE_RETRO -> { binding.mapStyleSpinner.setSelection(2) }
        }
    }

    private fun onBackButtonClicked() {
        findNavController().navigate(SettingFragmentDirections.actionSettingFragmentToMapsFragment())
    }

    private fun onSaveButtonClicked() {
        val selectedStyle = binding.mapStyleSpinner.selectedItem.toString()
        saveChangesToSharedPreferences(selectedStyle)
        // Navigate back to maps fragment :
        Toast.makeText(requireContext(), "Your changes applied", Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            delay(500)
            findNavController().navigate(SettingFragmentDirections.actionSettingFragmentToMapsFragment())
        }
    }

    private fun saveChangesToSharedPreferences(style:String) {
        SpManager.saveMapStyleInSharedPreferences(requireContext(),style)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}