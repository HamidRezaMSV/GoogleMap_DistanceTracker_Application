package com.sm.distancetracker.helper

import android.content.Context
import android.content.SharedPreferences
import com.sm.distancetracker.util.Constants.MAP_STYLE_STANDARD

class SpManager {

    companion object{
        private var sharedPreferences : SharedPreferences? = null
        private var editor : SharedPreferences.Editor? = null
        private const val spName = "SHARED_PREFERENCES_NAME"
        private const val mapStyle = "MAP_STYLE"
        private const val startedState = "STARTED_STATE"

        fun saveMapStyleInSharedPreferences(context:Context,style:String){
            sharedPreferences = context.getSharedPreferences(spName , Context.MODE_PRIVATE)
            editor = sharedPreferences?.edit()
            editor?.putString(mapStyle,style)
            editor?.apply()
        }

        fun getMapStyleFromSharedPreferences(context:Context) : String{
            sharedPreferences = context.getSharedPreferences(spName,Context.MODE_PRIVATE)
            return sharedPreferences?.getString(mapStyle, MAP_STYLE_STANDARD) ?: return MAP_STYLE_STANDARD
        }

        fun saveStartedState(context:Context,started:Boolean){
            sharedPreferences = context.getSharedPreferences(spName , Context.MODE_PRIVATE)
            editor = sharedPreferences?.edit()
            editor?.putBoolean(startedState,started)
            editor?.apply()
        }

        fun getStartedState(context: Context) : Boolean{
            sharedPreferences = context.getSharedPreferences(spName , Context.MODE_PRIVATE)
            return sharedPreferences?.getBoolean(startedState , false) ?: return false
        }
    }

}