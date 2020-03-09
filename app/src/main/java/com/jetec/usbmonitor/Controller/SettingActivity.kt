package com.jetec.usbmonitor.Controller

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
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
import com.jetec.usbmonitor.Model.DeviceSetting
import com.jetec.usbmonitor.Model.Tools.Tools
import com.jetec.usbmonitor.R
import kotlinx.android.synthetic.main.activity_setting.*
import java.math.BigInteger
import java.util.*
import kotlin.collections.ArrayList

class SettingActivity : AppCompatActivity() {
    val TAG = SettingActivity::class.java.simpleName + "My"
    private var mAdapter: MyAdapter? = null
    public lateinit var settingList: MutableList<DeviceSetting>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        setMenu()

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
            }//



        })//mAdapterChick
        mAdapter?.setOnSwitchClickLinster(object :MyAdapter.OnSwitchChangedListener{
            override fun onSwitchItemClick(view: View, position: Int, boolean: Boolean) {
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
                var r = byte?.let { it1 -> Tools.sendData(it1, 100, this@SettingActivity, 0) }
                    ?.get(0)
                Thread{
                    var array = Tools.sendData("Get", 100, this@SettingActivity, 0)
                    for (i in 0 until array.size){
                        if(getModifyIndex(array[i]) != -1){
                            settingList[getModifyIndex(array[i])]
                                .setValue(
                                    Tools.returnValue(
                                        array[i]!!.substring(4, 6).toInt(),
                                        Tools.hextoDecShort(array[i]!!.substring(6, 10))
                                    )
                                )
                            runOnUiThread{
                                mAdapter?.notifyDataSetChanged()
                            }
                        }
                    }
                }.start()
            }//到這

        })

    }

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
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setLayout(900, ViewGroup.LayoutParams.WRAP_CONTENT)
        tvTitle.text = settingList[position].getLabel()
        edINput.hint = settingList[position].getValue()
        btCancel.setOnClickListener { dialog.dismiss() }
        btOK.setOnClickListener {

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


        }
    }

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
        textView_toolBarTitleSetting.typeface = Typeface
            .createFromAsset(this.assets, "segoe_print.ttf")
        var mToolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolBarSetting)
        mToolbar.title = ""
        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    class MyAdapter : RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private var mSetting: MutableList<DeviceSetting>
        private lateinit var onItemClickListener: OnItemClickListener
        private lateinit var onSwitehClickListener: OnSwitchChangedListener
        val TAG = "SettingActivityMy"

        fun setOnItemClickListener(listener: OnItemClickListener) {
            this.onItemClickListener = listener

        }
        fun setOnSwitchClickLinster(check:OnSwitchChangedListener){
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
                if (!compoundButton.isPressed)return@setOnCheckedChangeListener//解決設定為true時直接被觸發
                onSwitehClickListener?.onSwitchItemClick(holder.swC2F, position, b)
                return@setOnCheckedChangeListener
            }

        }

        interface OnItemClickListener {
            fun onMyItemClick(view: View, position: Int)

        }
        interface OnSwitchChangedListener{
            fun onSwitchItemClick(view: View, position: Int, boolean: Boolean)
        }

    }//MyAdapter

}

