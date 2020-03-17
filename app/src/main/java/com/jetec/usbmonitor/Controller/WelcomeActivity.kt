package com.jetec.usbmonitor.Controller

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.jetec.usbmonitor.Model.CrashHandler
import com.jetec.usbmonitor.Model.Tools.MyStatus
import com.jetec.usbmonitor.Model.Tools.Tools
import com.jetec.usbmonitor.R
import pl.droidsonroids.gif.GifDrawable

class WelcomeActivity : AppCompatActivity() {
    private val TAG: String = WelcomeActivity::class.java.simpleName
    val PERMISSION_WRITE_STORAGE = 100;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        CrashHandler.getInstance().init(this)
        Tools.cleanStatic()
        setContentView(R.layout.activity_welcome)
        setGif()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            detectUSBStatus()
        } else {
            ActivityCompat.requestPermissions(
                this
                , arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_WRITE_STORAGE
            )
        }

    }

    /**取得裝置連接狀態*/
    private fun detectUSBStatus() {
        val manager: UsbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        var deviceList: HashMap<String, UsbDevice> = manager.deviceList
        val deviceIterator: Iterator<UsbDevice> = deviceList.values.iterator()

        Thread {
            while (deviceIterator.hasNext()) {
                val deviceInfo = deviceIterator.next()
                var getS = deviceInfo.productName
                var arrayList: ArrayList<String> = getS?.let { getTypeArray(it, '-') }!!
                try {
                    MyStatus.isOurDevice = arrayList[0]
                    MyStatus.deviceRow = arrayList[1].toInt()
                    MyStatus.deviceType = arrayList[2]
                    MyStatus.usbType = arrayList[3]
                } catch (e: Exception) {
                    Log.d(TAG, "ERROR:$e ");
                }

            }
            SystemClock.sleep(2000)
            runOnUiThread {
                val intent = Intent(this, ConnectStatusActivity::class.java)
                if (MyStatus.deviceType.isNotEmpty() || MyStatus.isOurDevice.contains("UF")) {
                    intent.putExtra("ConnectedStatus", true)
                } else intent.putExtra("ConnectedStatus", false)
                startActivity(intent)
                finish()
            }
        }.start()

    }//detectUSBStatus 偵測USB插入狀態


    /**取得型陣列
     * @param inputString 輸入總陣列(EX:UF-2-TH-CDC)
     * @param charTag 輸入要設為斷點的符號(EX: '-')*/
    private fun getTypeArray(inputString: String, charTag: Char): ArrayList<String> {
        var arrayList = ArrayList<String>()
        var moneyCount = 1
        for (i in inputString.indices) {
            if (inputString[i] == charTag) {
                moneyCount++
            }
        }
        var s = inputString
        for (i in 0 until moneyCount) {
            try {
                arrayList.add(s.substring(0, s.indexOf(charTag)))
                s = s.substring(arrayList[i].length + 1)
            } catch (e: Exception) {
                arrayList.add(s)
            }

        }
        return arrayList
    }//getTypeArray

    /**設置GIF動畫*/
    private fun setGif() {
        val imageView = findViewById<ImageView>(R.id.image)
        try {
            val gif = GifDrawable(
                resources,
                R.drawable.walk_200
            )
            imageView.setImageDrawable(gif)
        } catch (e: Exception) {
            Log.d(TAG, ": $e")

        }
    }//setGif 設置Gif


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, ": $requestCode");
        when (requestCode) {
            PERMISSION_WRITE_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    detectUSBStatus()
                } else {
                    Toast.makeText(this, "煩請賜予權限", Toast.LENGTH_LONG).show()
                    finish()
                }

            }

        }
    }
}
