package com.jetec.usbmonitor.Controller

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.github.clans.fab.FloatingActionMenu
import com.jetec.usbmonitor.Model.CrashHandler
import com.jetec.usbmonitor.Model.RoomDBHelper.Data
import com.jetec.usbmonitor.Model.RoomDBHelper.DataBase
import com.jetec.usbmonitor.Model.Tools.Tools
import com.jetec.usbmonitor.R
import org.json.JSONArray

//import kotlinx.android.synthetic.main.activity_setting.*

class RecordHistoryActivity : AppCompatActivity() {
    val TAG = RecordHistoryActivity::class.java.simpleName + "My"

    companion object{
        val RESULT_CODE = 1
        const val IMAGE_REQUEST = 100
        const val REQUEST_FINE_LOCATION_PERMISSION = 101;

    }
    lateinit var imageView: ImageView

    private lateinit var mAdapter: MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_history)
        CrashHandler.getInstance().init(this)
        setMenu()
        /**讀取DB內資料*/
        var dialog = ProgressDialog.show(this, "", "讀取中...", true)
        Thread {
            val savedData: MutableList<Data> = DataBase.getInstance(this).dataUao.allData
            val layoutManager = LinearLayoutManager(this)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            val recycler = findViewById<RecyclerView>(R.id.recyclerView_RecordHistoryDisplay)
            layoutManager.recycleChildrenOnDetach = true
            recycler.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
            recycler.layoutManager = layoutManager
            dialog.setCancelable(true)
            mAdapter = MyAdapter(savedData, this)
            runOnUiThread {

                recycler.adapter = mAdapter
            }
            SystemClock.sleep(1000)
            dialog.dismiss()

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
    /**設置使用者碰到螢幕後要做的事*/
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val floatMenu = findViewById<FloatingActionMenu>(R.id.floatingActionMenuButton_Filter)
        if (event!!.action >=0 && floatMenu.isOpened){
            floatMenu.close(true)
        }
        return super.onTouchEvent(event)
    }

    /**第一個RecyclerView*/
    private class MyAdapter : RecyclerView.Adapter<MyAdapter.ViewHolder> {

        val TAG = RecordHistoryActivity::class.java.simpleName + "My"
        private var data: MutableList<Data>
        private val binderHelper = ViewBinderHelper()
        private val activity: Activity

        constructor(data: MutableList<Data>, activity: Activity) : super() {
            this.data = data
            this.activity = activity
        }
        public fun updateList(){
            Thread{
                data = DataBase.getInstance(activity).dataUao.allData
                activity.runOnUiThread{
                    notifyDataSetChanged()
                }
            }.start()


        }

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvTitle = v.findViewById<TextView>(R.id.textView_RH_Title)
            val recyclerChild = v.findViewById<RecyclerView>(R.id.recyclerView_RH_child)
            val tvID = v.findViewById<TextView>(R.id.textView_RH_ID)
            val tvDeviceName = v.findViewById<TextView>(R.id.textView_RH_DeviceName)
            val tvTime = v.findViewById<TextView>(R.id.textView_RH_Time)
            val btDelete = v.findViewById<Button>(R.id.button_RH_Delete)
            val btReview = v.findViewById<Button>(R.id.button_RH_Review)
            val btModify = v.findViewById<Button>(R.id.button_RH_Modify)
            val swipeLayout = v.findViewById<SwipeRevealLayout>(R.id.swipe_layout)
            val igPicture = v.findViewById<ImageView>(R.id.imageView_RH_Image)
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
            setValue(holder, position)//打入需顯示的值
            setSecondRecyclerView(position, holder)//打入巢狀RecyclerView的值
            setClick(holder, position, holder.parent.context)//設置點擊事件(們)

        }
        /**設置RecyclerView按鈕內的點擊事件*/
        private fun setClick(
            holder: ViewHolder,
            position: Int,
            context: Context
        ) {
            /**刪除*/
            holder.btDelete.setOnClickListener {
                holder.swipeLayout.close(true)
                val builder = AlertDialog.Builder(context)
                builder.setMessage("Sure to delete this data?")
                    .setPositiveButton(context.getString(R.string.oK_Button)) { dialog, i ->
                        val id = data[position].id
                        Thread {
                            DataBase.getInstance(context).dataUao.deleteByID(id)
                            data.removeAt(position)
                            activity.runOnUiThread {
                                notifyItemRemoved(position)
                            }
                        }.start()
                    }
                    .setNegativeButton(context.getString(R.string.cancelButton), null)
                    .show()
            }//onC1
            /**檢視*/
            holder.btReview.setOnClickListener {
                val intent = Intent(activity, ReviewDataActivity::class.java)
                intent.putExtra("searchID", data[position].id)
                activity.startActivity(intent)
                holder.swipeLayout.close(true)

            }//onC2
            /**修改*/
            holder.btModify.setOnClickListener {
                val id = data[position].id
                val intent = Intent(activity,ModifyHistoryDataActivity::class.java)
                intent.putExtra("position",id)
                activity.startActivityForResult(intent,RESULT_CODE)
                holder.swipeLayout.close(true)

            }//onC3
        }
        /**填入數值*/
        private fun setValue(
            holder: ViewHolder,
            position: Int
        ) {
            binderHelper.bind(holder.swipeLayout, position.toString())
            binderHelper.setOpenOnlyOne(true)
            var title = ""
            val context = holder.parent.context
            if (data[position].name.isEmpty()) {

                title =
                    context.getString(R.string.tester) + ": " + holder.parent.context.getString(R.string.unfilled)
            } else {
                title = context.getString(R.string.tester) + ": " + data[position].name
            }
            val deviceID = context.getString(R.string.deviceId) + data[position].deviceUUID
            val deviceName = context.getString(R.string.deviceName) + data[position].deviceName
            val time =
                context.getString(R.string.measureTime) + data[position].dateTime.replace("#", " ")

            holder.tvTitle.text = title
            holder.tvID.text = deviceID
            holder.tvDeviceName.text = deviceName
            holder.tvTime.text = time

            Glide.with(holder.parent).load(data[position].takeImage)
                .centerCrop()
                .placeholder(R.drawable.create_image)
                .into(holder.igPicture)

        }
        /**設置第二個RecyclerView*/
        private fun setSecondRecyclerView(
            position: Int,
            holder: ViewHolder
        ) {

            val jsonArray = JSONArray(data[position].json_MySave)
            val arrayList = ArrayList<HashMap<String, String>>()
            for (i in 0 until jsonArray.length()) {
                val hashMap = HashMap<String, String>()
                val jsonObject = jsonArray.getJSONObject(i)
                val strValue = jsonObject.getString("value")
                val strPV = jsonObject.getString("PV")
                val strEH = jsonObject.getString("EH")
                val strEL = jsonObject.getString("EL")
                val strTitle = jsonObject.getString("Title")
                val strOrigin = jsonObject.getString("Origin")
                hashMap["value"] = strValue
                hashMap["PV"] = strPV
                hashMap["EH"] = strEH
                hashMap["EL"] = strEL
                hashMap["Title"] = strTitle
                hashMap["Origin"] = strOrigin
                arrayList.add(hashMap)


                val layoutManager =
                    StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.HORIZONTAL);
                layoutManager.orientation = LinearLayoutManager.VERTICAL
                val cAdapter = ChildRecyclerView(arrayList)
                holder.recyclerChild.layoutManager = layoutManager

                holder.recyclerChild.adapter = cAdapter
            }
        }
    }
    /**回傳修改後的值*/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        Log.d(TAG, ":$requestCode $resultCode ${data?.getStringExtra("RESULT")} ");
        if (requestCode == RESULT_CODE&&resultCode == 1){
            val id = data?.getIntExtra("modifiedIndex",0)
            mAdapter.updateList()
        }
    }

    /**包在第一個RecyclerView內的那個Recycler*/
    private class ChildRecyclerView(val recordInfo: ArrayList<HashMap<String, String>>) :
        RecyclerView.Adapter<ChildRecyclerView.ViewHolder>() {

        val TAG = RecordHistoryActivity::class.java.simpleName + "My"

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvTitle = v.findViewById<TextView>(R.id.textView_item_RecordTitle)
            val tvPV = v.findViewById<TextView>(R.id.textView_item_RecordPV)
            val tvEH = v.findViewById<TextView>(R.id.textView_item_RecordEH)
            val tvEL = v.findViewById<TextView>(R.id.textView_item_RecordEL)
            val parent = v

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.activity_save_record_item, parent, false)
            return ViewHolder(v)
        }

        override fun getItemCount(): Int {
            return recordInfo.size
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            var unit = Tools.setUnit(
                Tools.hex2Dec(recordInfo[position]["Origin"]!!.substring(12, 14)).toInt()
            )
            holder.tvTitle.text =
                "${recordInfo[position]["Title"]}:\n${recordInfo[position]["value"] + unit}"
            holder.tvPV.text =
                holder.parent.context.getString(R.string.PV) + ": " + recordInfo[position]["PV"]
            holder.tvEH.text =
                holder.parent.context.getString(R.string.EH) + ": " + recordInfo[position]["EH"]
            holder.tvEL.text =
                holder.parent.context.getString(R.string.EL) + ": " + recordInfo[position]["EL"]


        }
    }


}
