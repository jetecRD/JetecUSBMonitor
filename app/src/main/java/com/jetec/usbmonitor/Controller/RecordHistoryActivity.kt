package com.jetec.usbmonitor.Controller

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.jetec.usbmonitor.Model.CrashHandler
import com.jetec.usbmonitor.Model.RoomDBHelper.Data
import com.jetec.usbmonitor.Model.RoomDBHelper.DataBase
import com.jetec.usbmonitor.R

//import kotlinx.android.synthetic.main.activity_setting.*

class RecordHistoryActivity : AppCompatActivity() {
    val TAG = RecordHistoryActivity::class.java.simpleName + "My"

    private lateinit var mAdapter: MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_history)
        CrashHandler.getInstance().init(this)
        setMenu()

        Thread {
            val savedData: List<Data> = DataBase.getInstance(this).dataUao.allData
            runOnUiThread {
                val layoutManager = LinearLayoutManager(this)
                layoutManager.orientation = LinearLayoutManager.VERTICAL
                val recycler = findViewById<RecyclerView>(R.id.recyclerView_RecordHistoryDisplay)
                layoutManager.recycleChildrenOnDetach = true
                mAdapter = MyAdapter(savedData)
                recycler.layoutManager = layoutManager
                recycler.adapter = mAdapter
            }

        }.start()

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

    /**第一個RecyclerView*/
    private class MyAdapter : RecyclerView.Adapter<MyAdapter.ViewHolder> {

        private val viewPool = RecyclerView.RecycledViewPool()
        val TAG = RecordHistoryActivity::class.java.simpleName + "My"
        private var data: List<Data>

        constructor(data: List<Data>) : super() {
            this.data = data
        }

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvTitle = v.findViewById<TextView>(R.id.textView_RH_Title)
            val recyclerChild = v.findViewById<RecyclerView>(R.id.recyclerView_RH_child)
            val parent = v
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            var view = LayoutInflater.from(parent.context)
                .inflate(R.layout.record_histroy_item, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return data.size
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.tvTitle.setText("測試人員:${data[position].name}")

            val layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            val cAdapter = ChildRecyclerView(data)
            holder.recyclerChild.layoutManager = layoutManager
            holder.recyclerChild.adapter = cAdapter

        }


    }
    /**包在第一個RecyclerView內的那個Recycler*/
    private class ChildRecyclerView(val cData: List<Data>) :
        RecyclerView.Adapter<ChildRecyclerView.ViewHolder>() {

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.activity_save_record_item, parent, false)
            return ViewHolder(v)
        }

        override fun getItemCount(): Int {
            return 3
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        }
    }
}
