package com.jetec.usbmonitor.Controller

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.jetec.usbmonitor.R

class MainActivity : AppCompatActivity() {
    val TAG: String = MainActivity::class.java.simpleName+"My"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var intent = intent
        var infoArrayList = intent.getStringArrayListExtra("deviceInfo")
        Log.d(TAG, "$infoArrayList ");

    }
}
