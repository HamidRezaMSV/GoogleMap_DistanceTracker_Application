package com.sm.distancetracker.bindingadapter

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.sm.distancetracker.R
import com.sm.distancetracker.model.Result

class ResultBindingAdapter {

    companion object{

        @JvmStatic
        @BindingAdapter("bindDistanceValueTextView")
        fun bindDistanceValueTextView(view: TextView , result: Result){
            view.text = view.context.getString(R.string.result , result.distance)
        }

        @JvmStatic
        @BindingAdapter("bindTimeValueTextView")
        fun bindTimeValueTextView(view: TextView , result: Result){
            view.text = result.time
        }

    }

}