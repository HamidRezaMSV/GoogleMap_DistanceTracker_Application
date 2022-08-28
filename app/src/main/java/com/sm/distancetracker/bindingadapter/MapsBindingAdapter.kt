package com.sm.distancetracker.bindingadapter

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.databinding.BindingAdapter

class MapsBindingAdapter {
    companion object{
        @JvmStatic
        @BindingAdapter("observeTracking")
        fun observeTracking(view:View,started:Boolean){
            if (started && view is Button){
                view.visibility = View.VISIBLE
            }else if (started && view is TextView){
                view.visibility = View.INVISIBLE
            }
        }
    }
}