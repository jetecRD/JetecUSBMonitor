package com.jetec.usbmonitor.Controller

import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.jetec.usbmonitor.Model.DeviceSetting
import com.jetec.usbmonitor.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        setMenu()
    }

    private fun setMenu() {
        textView_toolBarTitleSetting.typeface = Typeface
            .createFromAsset(this.assets, "segoe_print.ttf")
        var mToolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolBarSetting)
        mToolbar.title = ""
        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home){
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    class MyAdapter(val mSetting:MutableList<DeviceSetting>):
        RecyclerView.Adapter<MyAdapter.ViewHolder>() {
        class ViewHolder(v:View) :RecyclerView.ViewHolder(v) {
            val tvLabel:TextView = v.findViewById(R.id.textView_SettingLabel)
            val tvValue:TextView = v.findViewById(R.id.textView_SettingValue)
            val parent = v

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            var v = LayoutInflater.from(parent.context).inflate(R.layout.settinglist_item,parent,false)
            return ViewHolder(v)
        }

        override fun getItemCount(): Int {
            return mSetting.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            TODO("Not yet implemented")
        }
    }
}
