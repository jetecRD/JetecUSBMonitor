package com.jetec.usbmonitor.Controller

import android.app.ProgressDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.SystemClock
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jetec.usbmonitor.Model.AnalysisValueInfo
import com.jetec.usbmonitor.Model.CrashHandler
import com.jetec.usbmonitor.Model.DeviceSetting
import com.jetec.usbmonitor.Model.Initialization
import com.jetec.usbmonitor.Model.Tools.MyInputFilter
import com.jetec.usbmonitor.Model.Tools.MyStatus
import com.jetec.usbmonitor.Model.Tools.Tools
import com.jetec.usbmonitor.R

//import kotlinx.android.synthetic.main.activity_setting.*


class SettingActivity : AppCompatActivity() {
    val TAG = SettingActivity::class.java.simpleName + "My"
    private var mAdapter: MyAdapter? = null
    private var mNormalAdapter: NormalAdapter? = null
    lateinit var settingList: MutableList<DeviceSetting>
    private var dpFilter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        CrashHandler.getInstance().init(this)
        setMenu()//設置Toolbar
        setSensorSetting()//設置設定sensor的RecyclerView部分
        applicationSetting()//設置設定應用程式設定部分

    }

    /**設置設定應用程式設定部分 & 新增APP功能設定*/
    private fun applicationSetting() {
        var norSetting: ArrayList<String> = ArrayList()
        /**若需要新增APP設定就放這邊*/
        norSetting.add(getString(R.string.factoryReset))//恢復原廠
        norSetting.add(getString(R.string.lockTester))//鎖定測試者(解鎖要驗證)
        norSetting.add(getString(R.string.changePassword))//變更密碼(需驗證)
        norSetting.add(getString(R.string.changeTester))//變更測試人員(需驗證)
        norSetting.add(getString(R.string.changeDeviceName))//變更感測器名稱
//        norSetting.add(getString(R.string.nightMode))//夜間模式
        /**若需要新增APP設定就放這邊*/
        val layoutManager = LinearLayoutManager(this@SettingActivity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        val dataList = findViewById<RecyclerView>(R.id.recyclerView_NorSetting)
        dataList.layoutManager = layoutManager
        mNormalAdapter = NormalAdapter(norSetting)
        dataList.adapter = mNormalAdapter
        mNormalAdapter?.setOnClick(object : NormalAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int, string: String) {
                val mBuilder = AlertDialog.Builder(this@SettingActivity)
                when (string) {
                    getString(R.string.factoryReset) -> {
                        resetFactorySetting(mBuilder)
                    }
                    getString(R.string.lockTester) -> {}
                    getString(R.string.changePassword) -> {
                        changePassword(mBuilder, string)
                    }
                    getString(R.string.changeTester) -> {
                        changeTester(mBuilder, string)
                    }
                    getString(R.string.changeDeviceName) -> {
                        setDeviceName(mBuilder, string)
                    }
                }
            }
            /**切換所綁定的測試人員*/
            private fun changeTester(
                mBuilder: AlertDialog.Builder,
                string: String
            ) {
                val view = layoutInflater.inflate(R.layout.change_password_dialog, null)
                mBuilder.setView(view)
                val titleOld = view.findViewById<TextView>(R.id.textView_OldTitle)
                val titleNew = view.findViewById<TextView>(R.id.textView_NewTitle)
                var title = view.findViewById<TextView>(R.id.textView_SettingDialogTitle)
                var edOld = view.findViewById<EditText>(R.id.editText_OldPassword)
                var edNew = view.findViewById<EditText>(R.id.editText_NewPassword)
                var btOK = view.findViewById<Button>(R.id.button_SettingDialogOK)
                var btCancel = view.findViewById<Button>(R.id.button_SettingDialogCancel)
                val dialog = mBuilder.create()
                dialog.show()
                val dm = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(dm)
                dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.window!!.setLayout(dm.widthPixels - 180, ViewGroup.LayoutParams.WRAP_CONTENT)
                edOld.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                edNew.hint = MyStatus.lockedTester
                title.text = string
                titleOld.text = getString(R.string.plzInputPassword)
                titleNew.text = getString(R.string.plzInputChangeTester)
                btCancel.setOnClickListener { dialog.dismiss() }
                btOK.setOnClickListener {
                    val passWord = edOld.text.toString()
                    val tester = edNew.text.toString()
                    if (passWord.isEmpty() || tester.isEmpty()) {
                        Toast.makeText(
                            this@SettingActivity,
                            getString(R.string.dontBlank),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                    if (!passWord.contentEquals(MyStatus.password)) {
                        Toast.makeText(
                            this@SettingActivity
                            , getString(R.string.oldPasswordIsWrong), Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                    MyStatus.lockedTester = tester
                    Toast.makeText(
                        this@SettingActivity
                        , getString(R.string.successModify), Toast.LENGTH_SHORT
                    ).show()
                    dialog.dismiss()
                }
            }

            /**更改企業密碼*/
            private fun changePassword(
                mBuilder: AlertDialog.Builder,
                string: String
            ) {
                val view = layoutInflater.inflate(R.layout.change_password_dialog, null)
                mBuilder.setView(view)
                var title = view.findViewById<TextView>(R.id.textView_SettingDialogTitle)
                var edOld = view.findViewById<EditText>(R.id.editText_OldPassword)
                var edNew = view.findViewById<EditText>(R.id.editText_NewPassword)
                var btOK = view.findViewById<Button>(R.id.button_SettingDialogOK)
                var btCancel = view.findViewById<Button>(R.id.button_SettingDialogCancel)
                val dialog = mBuilder.create()
                dialog.show()
                val dm = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(dm)
                dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.window!!.setLayout(dm.widthPixels - 180, ViewGroup.LayoutParams.WRAP_CONTENT)

                title.text = string
                btCancel.setOnClickListener { dialog.dismiss() }
                btOK.setOnClickListener {
                    val oldW = edOld.text.toString()
                    val newW = edNew.text.toString()
                    if (oldW.isEmpty() || newW.isEmpty()) {
                        Toast.makeText(
                            this@SettingActivity
                            , getString(R.string.dontBlank), Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                    if (oldW != MyStatus.password) {
                        Toast.makeText(
                            this@SettingActivity
                            , getString(R.string.oldPasswordIsWrong), Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                    MyStatus.password = newW
                    Toast.makeText(
                        this@SettingActivity
                        , getString(R.string.successModify), Toast.LENGTH_SHORT
                    ).show()
                    dialog.dismiss()
                }
            }

            /**恢復原廠設定*/
            private fun resetFactorySetting(mBuilder: AlertDialog.Builder) {
                mBuilder.setTitle(getString(R.string.notice_AlarmTitle))
                mBuilder.setMessage(getString(R.string.factoryResetCheckMessage))
                mBuilder.setPositiveButton(getString(R.string.oK_Button)) { dialog, which ->
                    var dialog = ProgressDialog.show(
                        this@SettingActivity, getString(R.string.progressing)
                        , getString(R.string.progressDialogMessage), false
                    )
                    Thread {
                        Initialization(this@SettingActivity, MyStatus.deviceType).startINI()
                        SystemClock.sleep(2000)
                        var array = Tools.sendData("Get", 100, this@SettingActivity, 0)
                        Log.d("Initialization", "GET=:$array ");
                        while (array.size == 0) {
                            array = Tools.sendData("Get", 100, this@SettingActivity, 0)
                            SystemClock.sleep(500)
                        }
                        for (i in 0 until array.size) {
                            if (getModifyIndex(array[i]) != -1) {
                                settingList[getModifyIndex(array[i])]
                                    .setValue(
                                        Tools.returnValue(
                                            array[i]!!.substring(4, 6).toInt(),
                                            Tools.hextoDecShort(array[i]!!.substring(6, 10))
                                        )
                                    )
                                runOnUiThread {
                                    mAdapter?.notifyDataSetChanged()
                                    dialog.dismiss()
                                }
                            }
                        }
                    }.start()
                }
                mBuilder.setNegativeButton(getString(R.string.cancelButton), null)
                mBuilder.show()
            }

            /**設置裝置名稱(綁定感測器的名字)*/
            private fun setDeviceName(mBuilder: AlertDialog.Builder, title: String) {
                var deviceName = Tools.sendData(
                    "Name", 100
                    , this@SettingActivity, 1
                )
                if (deviceName.contains("Error1") || deviceName.isEmpty()) {
                    Toast.makeText(this@SettingActivity, "錯誤，請嘗試重新接線或通知原廠", Toast.LENGTH_LONG)
                        .show()
                    finish()
                    return
                }
                var view = layoutInflater.inflate(R.layout.setting_dialog, null)
                val originName = deviceName[deviceName.size - 1].substring(4)

                mBuilder.setView(view)
                var btOK = view.findViewById<Button>(R.id.button_SettingDialogOK)
                var btCancel = view.findViewById<Button>(R.id.button_SettingDialogCancel)
                var edINput = view.findViewById<EditText>(R.id.editText_SettingDialogInput)
                var tvTitle = view.findViewById<TextView>(R.id.textView_SettingDialogTitle)
                tvTitle.text = title
                val dialog = mBuilder.create()
                dialog.show()
                val dm = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(dm)
                dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.window!!.setLayout(dm.widthPixels - 180, ViewGroup.LayoutParams.WRAP_CONTENT)
                edINput.hint = originName
                edINput.inputType = InputType.TYPE_CLASS_TEXT
                edINput.filters = arrayOf(MyInputFilter(this@SettingActivity))
                btCancel.setOnClickListener {
                    dialog.dismiss()
                }
                btOK.setOnClickListener {
                    val input = edINput.text.toString()
                    if (input.isEmpty()) {
                        Toast.makeText(
                            this@SettingActivity,
                            getString(R.string.dontBlank),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                    val buffer = StringBuffer()
                    val sendByte = Tools.byteArrayToHexStr(input.toByteArray())
                    buffer.append(Tools.byteArrayToHexStr("Name".toByteArray()))
                    buffer.append(sendByte)
                    for (i in 0 until (40 - buffer.length)) {
                        buffer.append("0")
                    }
                    if (buffer.length > 40) {
                        Toast.makeText(
                            this@SettingActivity
                            , getString(R.string.tooLongInput), Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                    Tools.fromHexString(buffer.toString())?.let { it1 ->
                        Tools.sendData(
                            it1, 100
                            , this@SettingActivity, 0
                        )
                    }
                    dialog.dismiss()
                }
            }
        })

        mNormalAdapter?.setOnChange(object :NormalAdapter.OnChangeClickListener{
            override fun onChangeClick(checkBox: CheckBox, position: Int, string: String,status: Boolean) {
                val mBuilder = AlertDialog.Builder(this@SettingActivity)
                if (!checkBox.isPressed) return//解決設定為true時直接被觸發
                when(string){
                    getString(R.string.lockTester)->{
                        var view = layoutInflater.inflate(R.layout.setting_dialog, null)
                        mBuilder.setView(view)
                        var btOK = view.findViewById<Button>(R.id.button_SettingDialogOK)
                        var btCancel = view.findViewById<Button>(R.id.button_SettingDialogCancel)
                        var edINput = view.findViewById<EditText>(R.id.editText_SettingDialogInput)
                        var tvTitle = view.findViewById<TextView>(R.id.textView_SettingDialogTitle)
                        val dialog = mBuilder.create()
                        dialog.show()
                        val dm = DisplayMetrics()
                        windowManager.defaultDisplay.getMetrics(dm)
                        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        dialog.window!!.setLayout(dm.widthPixels - 180, ViewGroup.LayoutParams.WRAP_CONTENT)
                        edINput.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        tvTitle.text = string
                        edINput.hint = getString(R.string.pleaseInput)
                        btCancel.setOnClickListener {
                            checkBox.isChecked = !status
                            dialog.dismiss() }
                        btOK.setOnClickListener {
                            val input = edINput.text.toString()
                            if (input.isEmpty()) {
                                checkBox.isChecked = !status
                                Toast.makeText(this@SettingActivity,getString(R.string.dontBlank),Toast.LENGTH_SHORT).show()
                                return@setOnClickListener}
                            if (input != MyStatus.password){
                                checkBox.isChecked = !status
                                Toast.makeText(this@SettingActivity,
                                        getString(R.string.pswIsWrong),Toast.LENGTH_SHORT).show()
                                return@setOnClickListener
                            }
                            MyStatus.lock = status
                            dialog.dismiss()
                        }
                    }
                }
            }

        })
    }

    /**設置設定sensor的RecyclerView部分*/
    private fun setSensorSetting() {
        var analysisValueInfo = AnalysisValueInfo()
        settingList = analysisValueInfo
            .transSetting(this@SettingActivity, Tools.sendData("Get", 100, this@SettingActivity, 0))
        //        Log.d(TAG, "${Tools.sendData("Get", 100, this, 0)} ");
        val layoutManager = LinearLayoutManager(this@SettingActivity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        val dataList = findViewById<RecyclerView>(R.id.recyclerView_Setting)
        dataList.layoutManager = layoutManager
        mAdapter = MyAdapter(settingList)
        dataList.adapter = mAdapter
        mAdapter?.setOnItemClickListener(object : MyAdapter.OnItemClickListener {
            override fun onMyItemClick(view: View, position: Int) {
                if (Tools.hex2Dec(settingList[position].getType()).toInt() != 4) {
                    normalSettingModify(position)
                }
            }
        })//mAdapterChick
        mAdapter?.setOnSwitchClickLinster(object : MyAdapter.OnSwitchChangedListener {
            override fun onSwitchItemClick(view: View, position: Int, boolean: Boolean) {
                switchC2F(boolean, position)
            }
        })
    }

    /**處理C轉F的部分*/
    private fun switchC2F(boolean: Boolean, position: Int) {
        var value = 9
        if (boolean) {
            value = 9
        } else value = 10
        var originValue = settingList[position].getOriginValue()
        var byte = Tools.fromHexString(
            String.format("%02x", originValue.substring(0, 2).toInt(16))//排數
                    + String.format("%02x", originValue.substring(2, 4).toInt(16))//種類
                    + String.format("%02x", originValue.substring(4, 6).toInt(16))//小數點
                    + String.format("%04x", value)//值
                    + String.format("%02x", originValue.substring(10, 12).toInt(16))//空白
                    + String.format("%02x", value)//單位
        )
        byte?.let { it1 -> Tools.sendData(it1, 100, this@SettingActivity, 0) }?.get(0)//這是送出，故不取回傳

        Thread {
            var array = Tools.sendData("Get", 100, this@SettingActivity, 0)
            for (i in 0 until array.size) {
                if (getModifyIndex(array[i]) != -1) {
                    settingList[getModifyIndex(array[i])]
                        .setValue(
                            Tools.returnValue(
                                array[i]!!.substring(4, 6).toInt(),
                                Tools.hextoDecShort(array[i]!!.substring(6, 10))
                            )
                        )
                    runOnUiThread {
                        mAdapter?.notifyDataSetChanged()
                    }
                }
            }
        }.start()
    }

    /**處理關於一般值得設定部分*/
    private fun normalSettingModify(position: Int) {
        val mBuilder: AlertDialog.Builder = AlertDialog.Builder(this@SettingActivity)
        var view = layoutInflater.inflate(R.layout.setting_dialog, null)
        mBuilder.setView(view)
        var btOK = view.findViewById<Button>(R.id.button_SettingDialogOK)
        var btCancel = view.findViewById<Button>(R.id.button_SettingDialogCancel)
        var edINput = view.findViewById<EditText>(R.id.editText_SettingDialogInput)
        var tvTitle = view.findViewById<TextView>(R.id.textView_SettingDialogTitle)
        val dialog = mBuilder.create()
        dialog.show()
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setLayout(dm.widthPixels - 180, ViewGroup.LayoutParams.WRAP_CONTENT)
        tvTitle.text = settingList[position].getLabel()
        dpFilter = settingList[position].getDP()
        edINput.hint =
            String.format("%." + dpFilter + "f", settingList[position].getValue().toDouble())
        edINput.filters = arrayOf(MyInputFilter(this@SettingActivity, dpFilter))
        edINput.inputType = InputType.TYPE_CLASS_NUMBER
        edINput.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
        edINput.keyListener = DigitsKeyListener.getInstance("0123456789-.")

        btCancel.setOnClickListener { dialog.dismiss() }
        btOK.setOnClickListener {
            if (edINput.text.toString().isNotEmpty() && Numornot(edINput.text.toString())) {
                var originValue = settingList[position].getOriginValue()
                val value =
                    Tools.toHex(
                        Tools.sendValueMultiplyDP(
                            edINput.text.toString().toDouble()
                            , settingList[position].getDP()
                        )
                    )
//            Log.d(TAG, ":${value} ");
                var byte = Tools.fromHexString(
                    String.format("%02x", originValue.substring(0, 2).toInt(16))//排數
                            + String.format("%02x", originValue.substring(2, 4).toInt(16))//種類
                            + String.format("%02x", originValue.substring(4, 6).toInt(16))//小數點
                            + String.format("%04x", value!!.toLong(16))//值
                            + String.format("%02x", originValue.substring(10, 12).toInt(16))//空白
                            + String.format("%02x", originValue.substring(12, 14).toInt(16))//單位
                )
                Thread {
                    var s =
                        byte?.let { it1 -> Tools.sendData(it1, 100, this@SettingActivity, 0) }
                            ?.get(0)
                    if (getModifyIndex(s) != -1) {
                        settingList[getModifyIndex(s)]
                            .setValue(
                                Tools.returnValue(
                                    s!!.substring(4, 6).toInt(),
                                    Tools.hextoDecShort(s!!.substring(6, 10))
                                )
                            )
                        runOnUiThread {
                            mAdapter!!.notifyDataSetChanged()
                        }
                    }
                    dialog.dismiss()
                }.start()

            }//if
            else {
                dialog.dismiss()
            }
        }
    }

    fun Numornot(msg: String): Boolean {
        return try {
            msg.toDouble()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**以回傳的值找出特定index的方法*/
    private fun getModifyIndex(s: String?): Int {
        var comp = s!!.substring(0, 4)
        for (i in 0 until settingList.size) {
            if (comp.contains(settingList[i].getOriginValue().substring(0, 4))) {
                return i
            }
        }
        return -1
    }

    /**設置標題列*/
    private fun setMenu() {
        val tvToolBarTitle = findViewById<TextView>(R.id.textView_toolBarTitleSetting)
        tvToolBarTitle.typeface = Typeface
            .createFromAsset(this.assets, "segoe_print.ttf")
        var mToolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolBarSetting)
        mToolbar.title = ""
        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /**ToolBar的內容物點擊*/
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**設定感測器的RecyclerView的Adapter內容*/
    class MyAdapter : RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private var mSetting: MutableList<DeviceSetting>
        private lateinit var onItemClickListener: OnItemClickListener
        private lateinit var onSwitehClickListener: OnSwitchChangedListener
        val TAG = "SettingActivityMy"

        fun setOnItemClickListener(listener: OnItemClickListener) {
            this.onItemClickListener = listener

        }

        fun setOnSwitchClickLinster(check: OnSwitchChangedListener) {
            this.onSwitehClickListener = check
        }


        constructor(mSetting: MutableList<DeviceSetting>) : super() {
            this.mSetting = mSetting
        }


        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvLabel: TextView = v.findViewById(R.id.textView_SettingLabel)
            val tvValue: TextView = v.findViewById(R.id.textView_SettingValue)
            val swC2F = v.findViewById<Switch>(R.id.switch_C2F)
            val parent = v
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            var v = LayoutInflater.from(parent.context)
                .inflate(R.layout.settinglist_item, parent, false)
            return ViewHolder(v)
        }

        override fun getItemCount(): Int {
            return mSetting.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (mSetting[position].getLabel().contains(
                    Tools.id2String(
                        holder.parent.context,
                        R.string.C2F
                    )
                )
            ) {
                holder.swC2F.visibility = View.VISIBLE
                holder.tvValue.visibility = View.GONE
                holder.tvLabel.text = mSetting[position].getLabel()
                when (mSetting[position].getValue()) {
                    "0.9" -> {
                        holder.swC2F.isChecked = true
                    }
                    "1.0" -> {
                        holder.swC2F.isChecked = false
                    }
                    else -> {
                        holder.swC2F.visibility = View.GONE
                        holder.tvValue.visibility = View.VISIBLE
                        holder.tvValue.text = mSetting[position].getValue()
                    }
                }

            } else {
                holder.swC2F.visibility = View.GONE
                holder.tvValue.visibility = View.VISIBLE
                holder.tvLabel.text = mSetting[position].getLabel()
                holder.tvValue.text = Tools.getDecDisplay(mSetting[position].getDP())
                    .format(mSetting[position].getValue().toDouble())
            }

            holder.parent.setOnClickListener {
                onItemClickListener?.onMyItemClick(holder.parent, position)
            }

            holder.swC2F.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
                if (!compoundButton.isPressed) return@setOnCheckedChangeListener//解決設定為true時直接被觸發
                onSwitehClickListener?.onSwitchItemClick(holder.swC2F, position, b)
                return@setOnCheckedChangeListener
            }

        }

        interface OnItemClickListener {
            fun onMyItemClick(view: View, position: Int)

        }

        interface OnSwitchChangedListener {
            fun onSwitchItemClick(view: View, position: Int, boolean: Boolean)
        }

    }//MyAdapter

    /**設定APP設定的RecyclerView的Adapter內容*/
    class NormalAdapter : RecyclerView.Adapter<NormalAdapter.ViewHolder> {
        private var norSetting: ArrayList<String>
        private lateinit var onItemClick: OnItemClickListener
        private lateinit var onCheckBoxClick: OnChangeClickListener

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvTitle = v.findViewById<TextView>(R.id.textView_norSettingTitle)!!
            val checkBox = v.findViewById<CheckBox>(R.id.checkBox_norSetting)!!
            val parent = v
        }

        fun setOnClick(listener: OnItemClickListener) {
            this.onItemClick = listener
        }
        fun setOnChange(listener: OnChangeClickListener){
            this.onCheckBoxClick = listener
        }

        constructor(arrayList: ArrayList<String>) : super() {
            this.norSetting = arrayList
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            var view = LayoutInflater.from(parent.context)
                .inflate(R.layout.activity_norsetting_item, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return norSetting.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.tvTitle.text = norSetting[position]
            var title = holder.tvTitle.text.toString()
            if (title.contains(holder.parent.context.getString(R.string.nightMode))) {
                holder.checkBox.visibility = View.VISIBLE
            } else if (title.contains(holder.parent.context.getString(R.string.lockTester))) {
                holder.checkBox.visibility = View.VISIBLE
                holder.checkBox.isChecked = MyStatus.lock
            } else holder.checkBox.visibility = View.GONE


            holder.parent.setOnClickListener {
                onItemClick.onItemClick(holder.parent, position, holder.tvTitle.text.toString())
            }
            holder.checkBox.setOnCheckedChangeListener { compoundButton, b ->
                onCheckBoxClick.onChangeClick(holder.checkBox,position,holder.tvTitle.text.toString(),holder.checkBox.isChecked)
            }
        }

        interface OnItemClickListener {
            fun onItemClick(view: View, position: Int, string: String)

        }
        interface OnChangeClickListener {
            fun onChangeClick(view: CheckBox, position: Int, string: String,status:Boolean)
        }

    }


}

