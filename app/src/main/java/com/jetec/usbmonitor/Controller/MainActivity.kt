package com.jetec.usbmonitor.Controller

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jetec.usbmonitor.Model.AnalysisValueInfo
import com.jetec.usbmonitor.Model.DeviceValue
import com.jetec.usbmonitor.Model.Tools.Tools
import com.jetec.usbmonitor.R
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
     val TAG: String = MainActivity::class.java.simpleName+"My"
    private lateinit var mAdapter:MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initSetValue()//初始設置
        setMenu()

        button_Measure.setOnClickListener {
            val sdf= SimpleDateFormat("HH:mm:ss")
            var current = Date()
            textView_timeInfo.text = getString(R.string.timeMeasrue)+"\n"+sdf.format(current)

            val analysisValueInfo = AnalysisValueInfo(
                Tools.sendData("Request",200,this,0))
            Log.d(TAG, ":${Tools.sendData("Request",200,this,0)}");
            val layoutManager = LinearLayoutManager(this)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            val dataList = findViewById<RecyclerView>(R.id.recyclerView_MainValueDisplay)
            dataList.layoutManager = layoutManager
            mAdapter = MyAdapter(analysisValueInfo.requestValue(this))
            dataList.adapter = mAdapter

        }
    }

    private fun initSetValue() {

        var intent = intent
        var infoArrayList = intent.getStringArrayListExtra("DeviceInfo")
        var valueArrayList = intent.getStringArrayListExtra("Value")
//        Log.d(TAG, "$infoArrayList ")
        Log.d(TAG, "$valueArrayList ")
        val sdf= SimpleDateFormat("HH:mm:ss")
        var current = Date()
        textView_timeInfo.text = getString(R.string.timeMeasrue)+"\n"+sdf.format(current)

        val analysisValueInfo = AnalysisValueInfo(valueArrayList)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        val dataList = findViewById<RecyclerView>(R.id.recyclerView_MainValueDisplay)
        dataList.layoutManager = layoutManager
        mAdapter = MyAdapter(analysisValueInfo.requestValue(this))
        dataList.adapter = mAdapter
    }

    private fun setMenu(){
        val toolBar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolBar)
        toolBar.inflateMenu(R.menu.menu_layout)
        textView_toolBarTitle.typeface = Typeface.createFromAsset(this.assets, "segoe_print.ttf")
        toolBar.setOnMenuItemClickListener {
            var intent:Intent?
            when(it.itemId){
                R.id.action_recordHistory ->{
                    intent = Intent(this,RecordHistoryActivity::class.java)
                    startActivity(intent)
                }
                R.id.action_Setting->{
                    intent = Intent(this,SettingActivity::class.java)
                    startActivity(intent)
                }

            }
           false

        }

    }
    private class MyAdapter (val mData:MutableList<DeviceValue> ): RecyclerView.Adapter<MyAdapter.ViewHolder>(){
        val TAG = MainActivity::class.java.simpleName+"My"
        class ViewHolder(v:View):RecyclerView.ViewHolder(v){
            val igBell:ImageView = v.findViewById(R.id.imageView_AlarmImage)
            val igSensor:ImageView = v.findViewById(R.id.imageView_SensorTypeImage)
            val tvValue:TextView = v.findViewById(R.id.textView_ValueDisplay)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.activity_value_display_card, parent, false)
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
            holder.tvValue.text = mData[position].getmValue()+mData[position].getUnit()
            holder.igSensor.setColorFilter(mData[position].getColor())
            holder.igSensor.setImageResource(mData[position].getIcon())
        }

    }

}

