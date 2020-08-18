package com.jetec.usbmonitor.Model.Utils

import android.app.Activity
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import com.jetec.usbmonitor.Controller.SettingActivity


class MyInputFilter:InputFilter {
    private var activity:Activity
    private var filterType = 0//1為濾掉byte長度用,0則是濾小數點
    private var dp = 0

    constructor(activity: Activity,dp:Int){
        this.dp = dp
        filterType = 0
        this.activity = activity
    }
    constructor(activity: Activity){
        filterType = 1
        this.activity = activity
    }

    val TAG = SettingActivity::class.java.simpleName + "My"
    override fun filter(
        source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
        /* source:偵測輸入內容
         start:偵測輸入字的開始點
         end:偵測输入字的结束點
         dest：顯示當前顯示的內容
         dstart:控制光標的開始位置
         dent:控制光標的結束位置*/
        when(filterType){
            0->{
                filterDP(dest,source)
            }
            1->{
                val dValue = dest.toString()
                var getLenght = dValue.toByteArray().size
                if (getLenght>15 ||source.toString().toByteArray().size>15){
                    return ""
                    
                }

            }
        }

        return filterDP(dest, source)
    }

    private fun filterDP(dest: Spanned, source: CharSequence): String? {
        try {

            val dValue = dest.toString()
            if (dest.isEmpty() && source == ".") {
                return "0."
            }
            val splitArray = dValue.split(".").toTypedArray()
            if (splitArray.size > 1) {
                val dotValue = splitArray[1]
                if (dotValue.length == dp) {
                    return ""
                }
            }
            return null
        } catch (nfe: NumberFormatException) {
            Log.d(TAG, ":$nfe ");
        }
        return ""
    }
}