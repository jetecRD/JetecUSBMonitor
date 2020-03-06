package com.jetec.usbmonitor.Controller

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Typeface
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Vibrator
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.CycleInterpolator
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hoho.android.usbserial.driver.CdcAcmSerialDriver
import com.hoho.android.usbserial.driver.ProbeTable
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.jetec.usbmonitor.Model.AnalysisValueInfo
import com.jetec.usbmonitor.Model.DeviceSetting
import com.jetec.usbmonitor.Model.DeviceValue
import com.jetec.usbmonitor.Model.Tools.MyStatus
import com.jetec.usbmonitor.Model.Tools.Tools
import com.jetec.usbmonitor.R
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.lang.Exception
import java.math.BigInteger
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    val TAG: String = MainActivity::class.java.simpleName + "My"
    val ACTION_USB_PERMISSION: String = "com.jetec.usbmonitor"
    private lateinit var mAdapter: MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initSetValue()//初始設置
        setMenu()//設置標題列
//        //萬一搞壞了這邊手動輸入即可
        var byte = Tools.fromHexString(
            String.format("%02x", 2)//排數
                    + String.format("%02x", 2)//種類
                    + String.format("%02x", 0)//小數點
                    + String.format("%04x", 0)//值
                    + String.format("%02x", 0)//空白
                    + String.format("%02x", 9)//單位
        )
        byte?.let { it1 -> Tools.sendData(it1, 100, this, 0) }

        val filter = IntentFilter()
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        filter.addAction(ACTION_USB_PERMISSION)
        registerReceiver(usbStatus, filter)
        button_Measure.setOnClickListener {
            meansureModel()
            var vibrator: Vibrator =
                getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(100)
        }
        autoAount()//自動偵測(程式內自行判斷)
    }//onCreate
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(usbStatus)
    }

    /**廣播包:偵測是否有拔除、插入以及完成權限確認等行為*/
    private var usbStatus: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val action = intent.action
            if (action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
                if (detectUSBStatus()) {
                    Toast.makeText(
                        context,
                        getString(R.string.sensorPullIn),
                        Toast.LENGTH_SHORT
                    ).show()
                    connectedFunction()
                } else {
                    Toast.makeText(context, "請插入久德電子專用設備", Toast.LENGTH_SHORT).show()
                }
            } else if (action == UsbManager.ACTION_USB_DEVICE_DETACHED) {
                Toast.makeText(
                    context,
                    getString(R.string.sensorHasBeenPullOut),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (action == ACTION_USB_PERMISSION) {
                connectedFunction()
            }
        }
    }//Broadcast

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
                var arrayList: ArrayList<String> = Tools.sendData("Jetec", 20, this, 1)
                for (i in 0 until arrayList.size) {
                    if (arrayList[i]!!.contains("OK")) {
                        meansureModel()
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

    /**取得資訊與介面更動模組*/
    private fun meansureModel() {
        Thread {
            runOnUiThread {

                val sdf = SimpleDateFormat("HH:mm:ss")
                var current = Date()

                textView_timeInfo.text =
                    getString(R.string.timeMeasrue) + "\n" + sdf.format(current)
            }
            val analysisValueInfo = AnalysisValueInfo()
//            Log.d(TAG, ":${Tools.sendData("Request", 200, this, 0)}");
            runOnUiThread {
                val layoutManager = LinearLayoutManager(this)
                layoutManager.orientation = LinearLayoutManager.VERTICAL
                val dataList = findViewById<RecyclerView>(R.id.recyclerView_MainValueDisplay)
                dataList.layoutManager = layoutManager
                mAdapter = MyAdapter(
                    analysisValueInfo
                        .requestValue(this, Tools.sendData("Request", 100, this, 0))
                    , analysisValueInfo.requestSetting(this, Tools.sendData("Get", 100, this, 0))
                )
                dataList.adapter = mAdapter

            }

        }.start()
    }

    /**跳轉後載入介面*/
    private fun initSetValue() {

        var intent = intent
        var infoArrayList = intent.getStringArrayListExtra("DeviceInfo")
        var valueArrayList = intent.getStringArrayListExtra("Value")
//        Log.d(TAG, "$infoArrayList ")
        Log.d(TAG, "$valueArrayList ")
        val sdf = SimpleDateFormat("HH:mm:ss")
        var current = Date()
        textView_timeInfo.text = getString(R.string.timeMeasrue) + "\n" + sdf.format(current)

        val analysisValueInfo = AnalysisValueInfo()
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        val dataList = findViewById<RecyclerView>(R.id.recyclerView_MainValueDisplay)
        dataList.layoutManager = layoutManager
        mAdapter = MyAdapter(
            analysisValueInfo
                .requestValue(this, valueArrayList)
            , analysisValueInfo.requestSetting(this, infoArrayList)
        )
        dataList.adapter = mAdapter

    }

    /**設置標題列*/
    private fun setMenu() {
        val toolBar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolBar)
        toolBar.inflateMenu(R.menu.menu_layout)
        textView_toolBarTitle.typeface = Typeface.createFromAsset(this.assets, "segoe_print.ttf")

        toolBar.menu.findItem(R.id.action_Auto).isChecked = MyStatus.autoMeasurement
        toolBar.setOnMenuItemClickListener {

            var intent: Intent?
            when (it.itemId) {
                R.id.action_recordHistory -> {
                    intent = Intent(this, RecordHistoryActivity::class.java)
                    startActivity(intent)
                }
                R.id.action_Setting -> {
                    intent = Intent(this, SettingActivity::class.java)
                    startActivity(intent)

                }
                R.id.action_Auto -> {
                    it.isChecked = !it.isChecked
                    MyStatus.autoMeasurement = !MyStatus.autoMeasurement
                    if (MyStatus.autoMeasurement){
                        Toast.makeText(this,getString(R.string.autoMeasureOpenLabel),Toast.LENGTH_SHORT).show()
                    }else {
                        Toast.makeText(this,getString(R.string.autoMeasureTurnOff),Toast.LENGTH_SHORT).show()
                    }
                    autoAount()
                }

            }
            false

        }

    }

    /**週期進到onStop時若有開啟自動偵測則關閉之*/
    override fun onStop() {
        super.onStop()
        val toolBar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolBar)
        MyStatus.autoMeasurement = false
        toolBar.menu.findItem(R.id.action_Auto).isChecked = MyStatus.autoMeasurement
        autoAount()
    }

    /**自動偵測*/
    fun autoAount() {
        object : CountDownTimer(1000, 1000) {
            override fun onFinish() {
                if (MyStatus.autoMeasurement) {
                    button_Measure.text = getString(R.string.autoMeasuringButton)
                    meansureModel()
                    autoAount()
                } else {
                    button_Measure.text = getString(R.string.measureButton)
                }
            }

            override fun onTick(p0: Long) {
            }
        }.start()
    }


    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                true
            }
            else -> false
        }
    }

    /**設置按下音量建功能*/
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                meansureModel()
                var vibrator: Vibrator =
                    getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(100)
                true
            }
            KeyEvent.KEYCODE_BACK -> {
                finish()
                true
            }
            else -> false
        }
    }

    /**設置資訊看板(RecyclerView)*/
    private class MyAdapter(
        val mData: MutableList<DeviceValue>,
        val mSetting: MutableList<DeviceSetting>
    ) :
        RecyclerView.Adapter<MyAdapter.ViewHolder>() {
        val TAG = MainActivity::class.java.simpleName + "My"

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val igBell: ImageView = v.findViewById(R.id.imageView_AlarmImage)
            val igSensor: ImageView = v.findViewById(R.id.imageView_SensorTypeImage)
            val tvValue: TextView = v.findViewById(R.id.textView_ValueDisplay)
            val parent = v
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.activity_value_display_item, parent, false)
            return ViewHolder(v)
        }

        override fun getItemCount(): Int {
            return mData.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//            Log.d(TAG, ":${mData[position].getRow()}" +
//                    ", ${mData[position].getType()}" +
//                    ", ${mData[position].getDP()}" +
//                    ", ${mData[position].getmValue()}" +
//                    ", ${mData[position].getLabel()}"+
//                    ", ${mData[position].getUnit()}");
//            Log.d(TAG, ":${mSetting[position].getPV()} ");
//            Log.d(TAG, ":${mSetting[position].getPVValue()} ");
//            Log.d(TAG, ":${mSetting[position].getEH()} ");
//            Log.d(TAG, ":${mSetting[position].getEHValue()} ");
//            Log.d(TAG, ":${mSetting[position].getEL()} ");
//            Log.d(TAG, ":${mSetting[position].getELValue()} ");
//            Log.d(TAG, ":${mSetting[position].getTR()} ");
//            Log.d(TAG, ":${mSetting[position].getTRValue()} ");
            try {
                var displayDouble =
                    mData[position].getmValue().toDouble() + mSetting[position].getPVValue()
                        .toDouble()

                holder.tvValue.text = Tools.getDecDisplay(mData[position].getDP())
                    .format(displayDouble)+ mData[position].getUnit()
                if (displayDouble >= mSetting[position].getEHValue().toDouble()
                    || displayDouble <= mSetting[position].getELValue().toDouble()
                ) {
                    holder.igBell.setColorFilter(Color.RED)
//                var animShack = AnimationUtils.loadAnimation(holder.parent.context, R.anim.shake)
                    var animShack = TranslateAnimation(0f, 10f, 0f, 0f)
                    animShack.interpolator = CycleInterpolator(5f)
                    animShack.duration = 500
                    animShack.repeatCount = Animation.INFINITE
                    holder.igBell.animation = animShack
                    animShack.start()
                    var vibrator: Vibrator =
                        holder.parent.context.getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
                    vibrator.vibrate(1500)

                }
                holder.igSensor.setColorFilter(mData[position].getColor())
                holder.igSensor.setImageResource(mData[position].getIcon())

            } catch (e: Exception) {
                Log.d(TAG, ":${e.message} ");
            }

        }

    }

}

