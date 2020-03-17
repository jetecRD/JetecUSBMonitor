package com.jetec.usbmonitor.Controller

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Typeface
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.hoho.android.usbserial.driver.*
import com.jetec.usbmonitor.Model.BreathInterpolator
import com.jetec.usbmonitor.Model.CrashHandler
import com.jetec.usbmonitor.R
import com.jetec.usbmonitor.Model.Tools.MyStatus
import com.jetec.usbmonitor.Model.Tools.Tools
//import kotlinx.android.synthetic.main.activity_connect_status.*
import java.io.IOException
import java.lang.Exception
import java.sql.RowId

class ConnectStatusActivity : AppCompatActivity() {
    val TAG: String = ConnectStatusActivity::class.java.simpleName + "my"
    val ACTION_USB_PERMISSION: String = "com.jetec.usbmonitor"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        CrashHandler.getInstance().init(this)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_connect_status)
        val filter = IntentFilter()
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        filter.addAction(ACTION_USB_PERMISSION)
        registerReceiver(usbStatus, filter)
        val tvTitle = findViewById<TextView>(R.id.textView_Title)
        tvTitle.setTypeface(Typeface.createFromAsset(this.assets, "segoe_print.ttf"))//設置字形

        val intent = intent
        var status: Boolean = intent.getBooleanExtra("ConnectedStatus", false)
        when (status) {
            true -> {//已經連線到
                isConnectedAction()
                connectedFunction()
            }
            false -> {//尚未連線到
                disConnectedAction()
            }
        }
        breathAnimation()


    }//onCreate

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbStatus)
    }

    /**開啟確認權限以及與裝置進行第一次握手*/
    private fun connectedFunction() {
        val manager: UsbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        var deviceList: HashMap<String, UsbDevice> = manager.deviceList
        val deviceIterator: Iterator<UsbDevice> = deviceList.values.iterator()
        var customTable: ProbeTable = ProbeTable()
        var drivers: List<UsbSerialDriver>? = null
        while (deviceIterator.hasNext()) {
            val device = deviceIterator.next()
            customTable.addProduct(
                device.vendorId,
                device.productId,
                CdcAcmSerialDriver::class.java
            )
            var prober: UsbSerialProber = UsbSerialProber(customTable)
            drivers = prober.findAllDrivers(manager)
        }

        try {
            if (manager.hasPermission(drivers!![0].device)) {
                var arrayList: ArrayList<String> = Tools.sendData("Jetec", 20, this,1)
                for (i in 0 until arrayList.size) {
                    if (arrayList[i]!!.contains("OK")) {
                        Thread {
                            runOnUiThread {
                                isSuccessConnectedAction()

                            }
                        }.start()
                        getDeviceInfo()
                    }
                }
            } else {
                if (PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0) != null) {

                    manager.requestPermission(
                        drivers!![0].device
                        , PendingIntent.getBroadcast(
                            this
                            , 0, Intent(ACTION_USB_PERMISSION), 0
                        )
                    )
                }
            }

        } catch (e: Exception) {
            Log.d(TAG, "(爆): ${e.message}")
        } catch (e: IOException) {
            Log.d(TAG, "(爆2): ${e.message}")
        }
    }//connectedFunction

    /**已插入裝置但未認證時的畫面*/
    private fun isConnectedAction() {
        val constraintBack = findViewById<ConstraintLayout>(R.id.constraintBack)
        val textView_ConnectInfo = findViewById<TextView>(R.id.textView_ConnectInfo)
        constraintBack.setBackgroundResource(R.drawable.background_y)
        textView_ConnectInfo.text = getString(R.string.tryConnect)
    }

    /**未插入裝置畫面*/
    private fun disConnectedAction() {
        val constraintBack = findViewById<ConstraintLayout>(R.id.constraintBack)
        val textView_ConnectInfo = findViewById<TextView>(R.id.textView_ConnectInfo)
        constraintBack.setBackgroundResource(R.drawable.background_r)
        textView_ConnectInfo.text = getString(R.string.pleaseInputDevice)
        rotationYAnumationfield()
    }

    /**已通過認證後的行為*/
    private fun isSuccessConnectedAction() {
        val constraintBack = findViewById<ConstraintLayout>(R.id.constraintBack)
        val textView_ConnectInfo = findViewById<TextView>(R.id.textView_ConnectInfo)
        constraintBack.setBackgroundResource(R.drawable.background_g)
        textView_ConnectInfo.text = getString(R.string.gettingDeviceInfo)
        rotationYAnumationSuccess()
    }

    /**廣播包:偵測是否有拔除、插入以及完成權限確認等行為*/
    private var usbStatus: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val action = intent.action
            if (action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
                if (detectUSBStatus()) {
                    isConnectedAction()
                    connectedFunction()
                } else {
                    disConnectedAction()
                    Toast.makeText(context, "請插入久德電子專用設備", Toast.LENGTH_SHORT).show()
                }
            } else if (action == UsbManager.ACTION_USB_DEVICE_DETACHED) {
                disConnectedAction()
            } else if (action == ACTION_USB_PERMISSION) {
                connectedFunction()
            }
        }
    }//Broadcast

    /**背景呼吸燈動畫*/
    private fun breathAnimation() {
        val constraintBack = findViewById<ConstraintLayout>(R.id.constraintBack)

        val alphaAnimator = ObjectAnimator.ofFloat(constraintBack, "alpha", 0.7f, 1f)
        alphaAnimator.duration = 6000
        alphaAnimator.interpolator = BreathInterpolator()
        alphaAnimator.repeatCount = ValueAnimator.INFINITE
        alphaAnimator.start()
    }

    /**當正確連接到時旋轉icon*/
    private fun rotationYAnumationSuccess() {
        val imageView_AuthIcon = findViewById<ImageView>(R.id.imageView_AuthIcon)
        val rotateAnimator = ObjectAnimator.ofFloat(imageView_AuthIcon, "rotationY", 0f, 180f, 0f)
        imageView_AuthIcon.setImageDrawable(getDrawable(R.drawable.noun_success))
        rotateAnimator.duration = 500
        rotateAnimator.start()
    }

    /**當拔除連線時旋轉icon*/
    private fun rotationYAnumationfield() {
        val imageView_AuthIcon = findViewById<ImageView>(R.id.imageView_AuthIcon)
        val rotateAnimator = ObjectAnimator.ofFloat(imageView_AuthIcon, "rotationY", 0f, 180f, 0f)
        imageView_AuthIcon.setImageDrawable(getDrawable(R.drawable.noun_no_connection))
        rotateAnimator.duration = 500
        rotateAnimator.start()
    }

    /**判斷連接狀態，以及有沒有接入不屬於本公司之開發之產品*/
    private fun detectUSBStatus(): Boolean {
        val manager: UsbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        var deviceList: HashMap<String, UsbDevice> = manager.deviceList
        val deviceIterator: Iterator<UsbDevice> = deviceList.values.iterator()
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
                return false
            }
        }
        return MyStatus.isOurDevice.contains("UF")
    }

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
    }//getTypeArray 取得型號陣列

    /**取得裝置資訊*/
    private fun getDeviceInfo() {
        var arrayList: ArrayList<String> = ArrayList()
        var arrayInfo:ArrayList<String> = ArrayList()
        val textView_ConnectInfo = findViewById<TextView>(R.id.textView_ConnectInfo)
        Thread {
            SystemClock.sleep(1000)
            runOnUiThread {
                arrayList = Tools.sendData("Request", 200, this,0)
                arrayInfo = Tools.sendData("Get",200,this,0)
//                Log.d(TAG, "$arrayList ");
                if (arrayList.size>0){
                    textView_ConnectInfo.text = getString(R.string.successConnected)
                }else{
                    textView_ConnectInfo.text = getString(R.string.pleaseInputDevice)
                }

            }//結束第一個UI線程
            SystemClock.sleep(800)
            if (arrayList.size>0){
                runOnUiThread{
                    var intent = Intent(this,MainActivity::class.java)
                    intent.putExtra("Value",arrayList)//送值
                    intent.putExtra("DeviceInfo",arrayInfo)//送參數
                    startActivity(intent)
                    finish()
                }
            }
        }.start()
    }
}




