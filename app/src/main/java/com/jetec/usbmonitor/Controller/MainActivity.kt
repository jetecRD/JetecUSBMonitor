package com.jetec.usbmonitor.Controller

import android.app.PendingIntent
import android.app.ProgressDialog
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.hardware.camera2.params.RecommendedStreamConfigurationMap
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.*
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.CycleInterpolator
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.stetho.Stetho
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import com.google.gson.Gson
import com.hoho.android.usbserial.driver.CdcAcmSerialDriver
import com.hoho.android.usbserial.driver.ProbeTable
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.jetec.usbmonitor.Model.AnalysisValueInfo
import com.jetec.usbmonitor.Model.CrashHandler
import com.jetec.usbmonitor.Model.DeviceSetting
import com.jetec.usbmonitor.Model.DeviceValue
import com.jetec.usbmonitor.Model.RoomDBHelper.DataBase
import com.jetec.usbmonitor.Model.Tools.MyStatus
import com.jetec.usbmonitor.Model.Tools.Tools
import com.jetec.usbmonitor.R
//import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {
    val TAG: String = MainActivity::class.java.simpleName + "My"
    val ACTION_USB_PERMISSION: String = "com.jetec.usbmonitor"
    private lateinit var mAdapter: MyAdapter
    private var mValue: MutableList<DeviceValue> = ArrayList()
    private var mSetting: MutableList<DeviceSetting> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        CrashHandler.getInstance().init(this)
        initSetValue()//初始設置
        setMenu()//設置標題列

        val filter = IntentFilter()
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        filter.addAction(ACTION_USB_PERMISSION)
        registerReceiver(usbStatus, filter)
        val btMeaSure = findViewById<Button>(R.id.button_Measure)
        btMeaSure.setOnClickListener {
            val floatMenu = findViewById<FloatingActionMenu>(R.id.floatingActionMenu_Menu)
            if (floatMenu.isOpened) {
                floatMenu.close(true)
            }
            meansureModel(1)
            var vibrator: Vibrator =
                getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(100)
        }
        autoMeasure()//自動偵測(程式內自行判斷)
        setFloatButton()//設置FloatButton


    }//onCreate

    /**跳轉後載入介面*/
    private fun initSetValue() {

        var intent = intent
        var infoArrayList = intent.getStringArrayListExtra("DeviceInfo")
        var valueArrayList = intent.getStringArrayListExtra("Value")
//        Log.d(TAG, "$infoArrayList ")
//        Log.d(TAG, "$valueArrayList ")
        val sdf = SimpleDateFormat("HH:mm:ss")
        var current = Date()
        val tvTimeinfo = findViewById<TextView>(R.id.textView_timeInfo)
        tvTimeinfo.text = getString(R.string.timeMeasrue) + "\n" + sdf.format(current)

        val analysisValueInfo = AnalysisValueInfo()
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        val dataList = findViewById<RecyclerView>(R.id.recyclerView_MainValueDisplay)
        dataList.layoutManager = layoutManager
        mValue = analysisValueInfo
            .requestValue(this, valueArrayList)
        mSetting = analysisValueInfo.requestSetting(this, infoArrayList)

        mAdapter = MyAdapter(mValue, mSetting)
        dataList.adapter = mAdapter

    }

    /**設置FloatButton*/
    private fun setFloatButton() {
        val fbFlashSave = findViewById<FloatingActionButton>(R.id.floatingActionActionButton_flash)
        val fbSave = findViewById<FloatingActionButton>(R.id.floatingActionActionButton_normalSave)
        val floatMenu = findViewById<FloatingActionMenu>(R.id.floatingActionMenu_Menu)

        fbFlashSave.setOnClickListener {
            floatMenu.close(true)

            var arrayList: ArrayList<String> = Tools.sendData("Jetec", 20, this, 1)
            if (arrayList.size == 0) {
                Toast.makeText(this, "Not available", android.widget.Toast.LENGTH_SHORT).show()
            }
            for (i in 0 until arrayList.size) {
                if (arrayList[i]!!.contains("OK")) {
                    if (mValue.size != 0) {

                        var dialog = ProgressDialog.show(
                            this, ""
                            , getString(R.string.pleaseWait), true
                        )
                        Thread {

                            SystemClock.sleep(300)
                            val bs: ByteArrayOutputStream = ByteArrayOutputStream()
                            runOnUiThread {
                                getScreenShot()?.compress(Bitmap.CompressFormat.JPEG, 100, bs)
                            }


                            val sdfyMd = SimpleDateFormat("yyyy/MM/dd")
                            val now = Date()
                            val tvTimeinfo = findViewById<TextView>(R.id.textView_timeInfo)
                            var arrayArray = returnValueList()
                            var deivceUUID = Tools.sendData("Name", 200, this, 0)//獲取裝置唯一碼
                            var deivceName = Tools.sendData("Name", 200, this, 1)
                            while (deivceName.isEmpty()) {
                                deivceName = Tools.sendData("Name", 200, this, 1)
                            }
                            while (deivceUUID.isEmpty()) {
                                deivceUUID = Tools.sendData("Name", 200, this, 0)//獲取裝置唯一碼
                            }


                            val json = Gson().toJson(arrayArray)
                            val date = sdfyMd.format(now)
                            val time = tvTimeinfo.text.substring(tvTimeinfo.text.indexOf("\n") + 1)
                            var tester:String = if (MyStatus.lock) MyStatus.lockedTester else getString(R.string.unfilled)

                            DataBase.getInstance(this).dataUao.insertData(
                                deivceUUID[0],
                                deivceName[1].substring(4),
                                MyStatus.deviceType,
                                tester,
                                json,
                                bs.toByteArray(),
                                "",
                                getString(R.string.flashSavetoNote),
                                date,
                                time
                            )

                            dialog.dismiss()
                            Looper.prepare()
                            Toast.makeText(
                                this,
                                getString(R.string.succesSaved),
                                Toast.LENGTH_SHORT
                            ).show()
                            Looper.loop()

                        }.start()


                    } else {
                        Toast.makeText(this, "Not available", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Toast.makeText(this, "Not available", Toast.LENGTH_SHORT).show()
                }
            }


        }
        fbSave.setOnClickListener {
            floatMenu.close(true)
            val arrayList: ArrayList<String> = Tools.sendData("Jetec", 20, this, 1)
            if (arrayList.size == 0) {
                Toast.makeText(this, "Not available", android.widget.Toast.LENGTH_SHORT).show()
            }
            for (i in 0 until arrayList.size) {
                if (arrayList[i]!!.contains("OK")) {
                    if (mValue.size != 0) {
                        Thread {
                            SystemClock.sleep(300)
                            val bs: ByteArrayOutputStream = ByteArrayOutputStream()
                            runOnUiThread {
                                getScreenShot()?.compress(Bitmap.CompressFormat.JPEG, 100, bs)
                            }

                            var arrayArray = returnValueList()

                            val sdfyMd = SimpleDateFormat("yyyy/MM/dd")
                            val now = Date()
                            val intent = Intent(this, RecordActivity::class.java)
                            /**@see MyStatus.IntentNowDataArray ...等等是傳送Intent的標籤*/

                            intent.putExtra(RecordActivity.INTENTNOW, arrayArray)
                            intent.putExtra(RecordActivity.IntentMyNowYMd, sdfyMd.format(now))
                            val tvTimeinfo = findViewById<TextView>(R.id.textView_timeInfo)
                            intent.putExtra(
                                RecordActivity.IntentMyNowHms, tvTimeinfo.text
                                    .substring(tvTimeinfo.text.indexOf("\n") + 1)
                            )

                            intent.putExtra(RecordActivity.GetScreenShot, bs.toByteArray())
                            runOnUiThread {
                                startActivity(intent)
                            }
                        }.start()

                    } else {
                        Toast.makeText(this, "Not available", Toast.LENGTH_SHORT).show()
                    }
                }
            }


        }
    }

    /**紀錄時取得值的基本資訊*/
    private fun returnValueList(): ArrayList<HashMap<String, String>> {
        var arrayArray = ArrayList<HashMap<String, String>>()
        for (i in 0 until mValue.size) {
            var hashMap = HashMap<String, String>()
            var displayDouble =
                mValue[i].getmValue().toDouble() + mSetting[i].getPVValue()
                    .toDouble()
            hashMap[RecordActivity.IntentGetTitle] = mValue[i].getLabel()
            hashMap[RecordActivity.IntentGetValue] = Tools.getDecDisplay(mValue[i].getDP())
                .format(displayDouble)
            hashMap[RecordActivity.IntentGetOriginValue] = mValue[i].getOriginValue()

            hashMap[RecordActivity.IntentPVValue] = mSetting[i].getPVValue()
            hashMap[RecordActivity.GetPVOrigin] = mSetting[i].getPVOrigin()

            hashMap[RecordActivity.IntentEHValue] = mSetting[i].getEHValue()
            hashMap[RecordActivity.GetEHOrigin] = mSetting[i].getEHOrigin()

            hashMap[RecordActivity.IntentELValue] = mSetting[i].getELValue()
            hashMap[RecordActivity.GetELOrigin] = mSetting[i].getELValue()

            arrayArray.add(hashMap)
        }
        return arrayArray
    }

    /**螢幕截圖*/
    private fun getScreenShot(): Bitmap? {
        //藉由View來Cache全螢幕畫面後放入Bitmap

        val mView = window.decorView
        mView.isDrawingCacheEnabled = true
        mView.buildDrawingCache()
        val mFullBitmap = mView.drawingCache

        //取得系統狀態列高度
        val mRect = Rect()
        window.decorView.getWindowVisibleDisplayFrame(mRect)
        val mStatusBarHeight: Int = mRect.top

        //取得手機螢幕長寬尺寸
        val mPhoneWidth = windowManager.defaultDisplay.width
        val mPhoneHeight = windowManager.defaultDisplay.height

        //將狀態列的部分移除並建立新的Bitmap
        val mBitmap = Bitmap.createBitmap(
            mFullBitmap, 0, mStatusBarHeight, mPhoneWidth,
            mPhoneHeight - mStatusBarHeight
        )
        //將Cache的畫面清除
        mView.destroyDrawingCache()
        return mBitmap
    }

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
                        meansureModel(0)
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

    /**取得資訊與介面更動模組
     * @param selectChannel 0為創建列表，1為更新列表*/
    private fun meansureModel(selectChannel: Int) {

        Thread {
            runOnUiThread {

                val sdf = SimpleDateFormat("HH:mm:ss")
                var current = Date()
                val tvTimeInfo = findViewById<TextView>(R.id.textView_timeInfo)
                tvTimeInfo.text =
                    getString(R.string.timeMeasrue) + "\n" + sdf.format(current)
            }
            val analysisValueInfo = AnalysisValueInfo()
//            Log.d(TAG, ":${Tools.sendData("Rqs", 200, this, 0)}");

            runOnUiThread {
                try {
                    if (selectChannel == 1/*刷新數值*/) {
                        var rValue = Tools.sendData(
                            "Rqs"
                            , 100, this, 0
                        )
                        var rSetting = Tools.sendData(
                            "Get"
                            , 100, this, 0
                        )
                        if (rValue.isEmpty()) {
                            Toast.makeText(
                                this,
                                getString(R.string.noSensorHint),
                                Toast.LENGTH_SHORT
                            )
                                .show()
//                        mSetting.clear()
//                        mValue.clear()
//                        mAdapter.notifyDataSetChanged()
                        }

                        /**刷新GET的部分*/
                        val mArrayList: ArrayList<ArrayList<String>> = ArrayList()
                        for (i in 0 until MyStatus.deviceRow) {
                            val liArrayList: ArrayList<String> = ArrayList()
                            for (x in 0 until rSetting.size) {
                                if (rSetting[x].substring(0, 2).toInt() - 1 == i) {
                                    liArrayList.add(rSetting[x])
                                }
                            }
                            mArrayList.add(liArrayList)
                        }
                        for (i in 0 until mArrayList.size) {
                            for (x in 0 until mArrayList[i].size) {
                                var row = mArrayList[i][x].substring(0, 4)
                                var type = mArrayList[i][x].substring(2, 4)
                                var dp = mArrayList[i][x].substring(4, 6)
                                var value = mArrayList[i][x].substring(6, 10)
                                var empty = mArrayList[i][x].substring(10, 12)
                                var unit = mArrayList[i][x].substring(12, 14)
                                when (returnType(type.toInt())) {
                                    "PV" -> {
                                        mSetting[i].setPV("PV")
                                        mSetting[i].setPVValue(
                                            Tools.returnValue(
                                                dp.toInt(),
                                                Tools.hextoDecShort(value)
                                            )
                                        )
                                        mSetting[i].setPVOrigin(mArrayList[i][x])
                                    }
                                    "EH" -> {
                                        mSetting[i].setEH("EH")
                                        mSetting[i].setEHValue(
                                            Tools.returnValue(
                                                dp.toInt(),
                                                Tools.hextoDecShort(value)
                                            )
                                        )
                                        mSetting[i].setEHOrigin(mArrayList[i][x])
                                    }
                                    "EL" -> {
                                        mSetting[i].setEL("EL")
                                        mSetting[i].setELValue(
                                            Tools.returnValue(
                                                dp.toInt(),
                                                Tools.hextoDecShort(value)
                                            )
                                        )
                                        mSetting[i].setELOrigin(mArrayList[i][x])
                                    }
                                    "trans℃" -> {
                                        mSetting[i].setTR("TR")
                                        mSetting[i].setTRValue(
                                            Tools.returnValue(
                                                dp.toInt(),
                                                Tools.hextoDecShort(value)
                                            )
                                        )
                                        mSetting[i].setTROrigin(mArrayList[i][x])
                                    }
                                }
                            }
                            mAdapter.notifyDataSetChanged()
                        }

                        /**刷新REQUEST的部分*/
                        for (i in 0 until rValue.size) {
                            var row = rValue[i].substring(0, 2)
                            var type = rValue[i].substring(2, 4)
                            var dp = rValue[i].substring(4, 6)
                            var value = rValue[i].substring(6, 10)
                            var empty = rValue[i].substring(10, 12)
                            var unit = rValue[i].substring(12, 14)

                            mValue[i] = DeviceValue(
                                row.toInt()
                                , returnType(Tools.hex2Dec(type).toInt())
                                , dp.toInt()
                                , Tools.returnValue(dp.toInt(), Tools.hextoDecShort(value))
                                , empty
                                , Tools.setUnit(Tools.hex2Dec(unit).toInt())
                                , Tools.setLabel(Tools.hex2Dec(unit).toInt(), this)
                                , rValue[i]
                                , Tools.setColor(Tools.hex2Dec(unit).toInt())
                                , Tools.setIcon(Tools.hex2Dec(unit).toInt())
                            )
                            mAdapter.notifyDataSetChanged()
                        }

                    } else {
                        /**刷新REQUEST的部分*/
                        val layoutManager = LinearLayoutManager(this)
                        layoutManager.orientation = LinearLayoutManager.VERTICAL
                        val dataList =
                            findViewById<RecyclerView>(R.id.recyclerView_MainValueDisplay)
                        dataList.layoutManager = layoutManager
                        mValue = analysisValueInfo
                            .requestValue(
                                this, Tools.sendData(
                                    "Rqs"
                                    , 100, this, 0
                                )
                            )
                        mSetting = analysisValueInfo
                            .requestSetting(
                                this, Tools.sendData(
                                    "Get"
                                    , 100, this, 0
                                )
                            )
                        mAdapter = MyAdapter(mValue, mSetting)
                        dataList.adapter = mAdapter
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "收資料失誤: $e ");
                }

            }

        }.start()
    }

    /**
     * 將通訊中第二個byte(決定種類:PV/EH/EF...)
     * @param input 輸入種類*/
    private fun returnType(input: Int): String {

        when (input) {
            1 -> {
                return "PV"
            }
            2 -> {
                return "EH"
            }
            3 -> {
                return "EL"
            }
            4 -> {
                return "trans℃"
            }
            else -> {
                return "empty"
            }
        }
    }


    /**設置標題列*/
    private fun setMenu() {
        val toolBar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolBar_RecordActivity)
        toolBar.inflateMenu(R.menu.menu_layout)
        val tvToolBar = findViewById<TextView>(R.id.textView_RecordToolBarTitle)
        tvToolBar.typeface = Typeface.createFromAsset(this.assets, "segoe_print.ttf")

        toolBar.menu.findItem(R.id.action_Auto).isChecked = MyStatus.autoMeasurement
        toolBar.setOnMenuItemClickListener {

            var intent: Intent?
            when (it.itemId) {
                R.id.action_recordHistory -> {
                    intent = Intent(this, RecordHistoryActivity::class.java)
                    startActivity(intent)
                }
                R.id.action_Setting -> {
                    var arrayList: ArrayList<String> = Tools.sendData("Jetec", 20, this, 1)
                    if (arrayList.size == 0) {
                        Toast.makeText(this, "Not available", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    for (i in 0 until arrayList.size) {
                        if (arrayList[i]!!.contains("OK")) {
                            intent = Intent(this, SettingActivity::class.java)
                            startActivity(intent)
                        }
                    }


                }
                R.id.action_Auto -> {
                    it.isChecked = !it.isChecked
                    MyStatus.autoMeasurement = !MyStatus.autoMeasurement
                    if (MyStatus.autoMeasurement) {
                        Toast.makeText(
                            this,
                            getString(R.string.autoMeasureOpenLabel),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.autoMeasureTurnOff),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    autoMeasure()
                }

            }
            false

        }

    }

    /**週期進到onStop時若有開啟自動偵測則關閉之*/
    override fun onStop() {
        super.onStop()
        val toolBar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolBar_RecordActivity)
        MyStatus.autoMeasurement = false
        toolBar.menu.findItem(R.id.action_Auto).isChecked = MyStatus.autoMeasurement
        autoMeasure()
    }

    /**自動偵測*/
    fun autoMeasure() {
        object : CountDownTimer(1000, 1000) {
            override fun onFinish() {
                val btMeaSure = findViewById<Button>(R.id.button_Measure)
                if (MyStatus.autoMeasurement) {
                    btMeaSure.text = getString(R.string.autoMeasuringButton)
                    meansureModel(1)
                    autoMeasure()
                } else {
                    btMeaSure.text = getString(R.string.measureButton)
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
        val floatMenu = findViewById<FloatingActionMenu>(R.id.floatingActionMenu_Menu)
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                meansureModel(1)
                var vibrator: Vibrator =
                    getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(100)
                true
            }
            KeyEvent.KEYCODE_BACK -> {
                if (floatMenu.isOpened) {
                    floatMenu.close(true)
                } else finish()
                true
            }
            else -> false
        }
    }

    /**設置資訊看板(RecyclerView)*/
    private class MyAdapter(
        val mAData: MutableList<DeviceValue>,
        val mASetting: MutableList<DeviceSetting>
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
            return mAData.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            try {
                var displayDouble =
                    mAData[position].getmValue().toDouble() + mASetting[position].getPVValue()
                        .toDouble()

                holder.tvValue.text = Tools.getDecDisplay(mAData[position].getDP())
                    .format(displayDouble) + mAData[position].getUnit()
                if (displayDouble >= mASetting[position].getEHValue().toDouble()
                    || displayDouble <= mASetting[position].getELValue().toDouble()
                ) {
                    holder.igBell.setColorFilter(Color.RED)
                    var animShack = TranslateAnimation(0f, 10f, 0f, 0f)
                    animShack.interpolator = CycleInterpolator(5f)
                    animShack.duration = 500
                    animShack.repeatCount = Animation.INFINITE
                    holder.igBell.animation = animShack
                    animShack.start()
                    var vibrator: Vibrator =
                        holder.parent.context.getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
                    vibrator.vibrate(1500)

                } else {
                    holder.igBell.setColorFilter(Color.BLACK)
                }
                holder.igSensor.setColorFilter(mAData[position].getColor())
                holder.igSensor.setImageResource(mAData[position].getIcon())

            } catch (e: Exception) {
                Log.d(TAG, ":${e.message} ");
            }

        }

    }

    /**設置螢幕觸控事件*/
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val floatMenu = findViewById<FloatingActionMenu>(R.id.floatingActionMenu_Menu)
        if (event!!.action >= 0 && floatMenu.isOpened) {
            floatMenu.close(true)
        }
        return super.onTouchEvent(event)
    }
}

