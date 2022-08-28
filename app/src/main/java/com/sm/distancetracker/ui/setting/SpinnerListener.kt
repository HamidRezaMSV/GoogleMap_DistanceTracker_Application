package com.sm.distancetracker.ui.setting

import android.graphics.Color
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.sm.distancetracker.R

object SpinnerListener {
    val listener : AdapterView.OnItemSelectedListener = object : AdapterView.OnItemSelectedListener{
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            when(position){
                0 -> { (parent?.getChildAt(0) as TextView).setTextColor(Color.GREEN) }
                1 -> { (parent?.getChildAt(0) as TextView).setTextColor(Color.BLACK) }
                2 -> { (parent?.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(view!!.context, R.color.orange)) }
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }
}