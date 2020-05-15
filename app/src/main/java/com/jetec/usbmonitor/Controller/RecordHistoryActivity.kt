package com.jetec.usbmonitor.Controller

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.os.SystemClock
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.EventDay
import com.bumptech.glide.Glide
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import com.jetec.usbmonitor.Model.CrashHandler
import com.jetec.usbmonitor.Model.GetSavedHashArray
import com.jetec.usbmonitor.Model.RoomDBHelper.Data
import com.jetec.usbmonitor.Model.RoomDBHelper.DataBase
import com.jetec.usbmonitor.Model.PDFModel.PDFReportMaker
import com.jetec.usbmonitor.Model.Tools.Tools
import com.jetec.usbmonitor.R
import org.json.JSONArray
import java.io.File

import java.text.SimpleDateFormat
import java.util.*

import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

//import kotlinx.android.synthetic.main.activity_setting.*

class RecordHistoryActivity : AppCompatActivity() {
    val TAG = RecordHistoryActivity::class.java.simpleName + "My"
    var filtered: Boolean = false
    lateinit var btFilterReturn: ImageButton

    companion object {
        val RESULT_CODE = 1
        const val IMAGE_REQUEST = 100
        const val REQUEST_FINE_LOCATION_PERMISSION = 101;

    }


    private lateinit var mAdapter: MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)//保持螢幕直向
        setContentView(R.layout.activity_record_history)
        CrashHandler.getInstance().init(this)
        returnSearchButton()
        setMenu()
        initReadData()
        setClick()
    }

    /**設置回復搜尋的按鈕*/
    fun returnSearchButton() {
        btFilterReturn = findViewById(R.id.button_filterReturn)
        if (!filtered) {
            btFilterReturn.visibility = View.GONE
        } else btFilterReturn.visibility = View.VISIBLE

        btFilterReturn.setOnClickListener {
            filtered = false
            returnSearchButton()
            Toast.makeText(this, getString(R.string.resultFilter), Toast.LENGTH_LONG).show()
            mAdapter.updateList()


        }
    }

    /**設置點擊事件*/
    private fun setClick() {
        val btExportPDF: ImageButton =
            findViewById(R.id.button_HistoryDataExport)
        val floatingActionMenu =
            findViewById<FloatingActionMenu>(R.id.floatingActionMenuButton_Filter)
        val floatUUID = findViewById<FloatingActionButton>(R.id.floatingActionButton_FilterByUUID)
        val floatDeviceName =
            findViewById<FloatingActionButton>(R.id.floatingActionButton_FilterByDeviceName)
        val floatTester =
            findViewById<FloatingActionButton>(R.id.floatingActionButton_FilterByTester)
        val floatDate = findViewById<FloatingActionButton>(R.id.floatingActionButton_FilterByDate)
        btExportPDF.setOnClickListener {
            val maker = PDFReportMaker()
            val mSaved = mAdapter.getNowDisplayData()
            maker.makeMultiplePDF(this,mSaved)
        }
        floatUUID.setOnClickListener {
            filterEvent(getString(R.string.searchBytUUIDLabel))
            floatingActionMenu.close(true)
        }
        floatDeviceName.setOnClickListener {
            filterEvent(getString(R.string.searchByDaviceNameLabel))
            floatingActionMenu.close(true)
        }
        floatTester.setOnClickListener {
            filterEvent(getString(R.string.searchByTester))
            floatingActionMenu.close(true)
        }
        floatDate.setOnClickListener {
            filterEvent(getString(R.string.searchByDate))
            floatingActionMenu.close(true)
        }

    }



    /**設置篩選功能*/
    private fun filterEvent(title: String) {
        val mBuilder = AlertDialog.Builder(this)
        val view: View = if (title.contains(getString(R.string.searchByDate))) {
            layoutInflater.inflate(R.layout.history_date_filter_dialog, null)
        } else {
            layoutInflater.inflate(R.layout.history_filter_dialog, null)
        }

        mBuilder.setView(view)
        val titleTitle = view.findViewById<TextView>(R.id.textView_FilterDialogTitle)//主標題
        titleTitle.text = title
        val dialog = mBuilder.create()
        val btCancel: Button = view.findViewById(R.id.button_SettingDialogCancel)
        val btOK: Button = view.findViewById(R.id.button_SettingDialogOK)
        btCancel.setOnClickListener { dialog.dismiss() }

        if (!title.contains(getString(R.string.searchByDate))) {
            /**以一般spinner做篩選*/
            val spinner: Spinner = view.findViewById(R.id.spinner_FilterId)
            val headerTitle = view.findViewById<TextView>(R.id.textView_FilterDialogHeader)//副標題
            var headerString = title
            try {
                headerString = title.substring(title.lastIndexOf(" "))
            }catch (e:Exception){

            }


            headerTitle.text = headerString
            GetSavedHashArray(this, object : GetSavedHashArray.AsyncResponse {
                override fun processFinish(hashArray: HashMap<Int, HashSet<String>>) {
                    Log.d(TAG, ": $hashArray");
                    var arrayList = ArrayList<String>()
                    when (title) {
                        getString(R.string.searchBytUUIDLabel) -> {
                            arrayList = toArrayList(hashArray[GetSavedHashArray.DEVICE_UUID])
                        }
                        getString(R.string.searchByDaviceNameLabel) -> {
                            arrayList = toArrayList(hashArray[GetSavedHashArray.DEVICE_NAME])
                        }
                        getString(R.string.searchByTester) -> {
                            arrayList = toArrayList(hashArray[GetSavedHashArray.TESTER])
                        }
                        getString(R.string.searchByDate) -> {
                            arrayList = toArrayList(hashArray[GetSavedHashArray.TIME_DATE])
                        }

                    }
                    val arrayAdapter = ArrayAdapter(
                        this@RecordHistoryActivity
                        , android.R.layout.simple_dropdown_item_1line, arrayList
                    )
                    spinner.adapter = arrayAdapter
                    runOnUiThread {
                        btOK.setOnClickListener {
                            when (title) {
                                getString(R.string.searchBytUUIDLabel) -> {
                                    mAdapter.updateFilterList(
                                        spinner.selectedItem.toString()
                                        , GetSavedHashArray.DEVICE_UUID
                                    )
                                }
                                getString(R.string.searchByDaviceNameLabel) -> {
                                    mAdapter.updateFilterList(
                                        spinner.selectedItem.toString()
                                        , GetSavedHashArray.DEVICE_NAME
                                    )
                                }
                                getString(R.string.searchByTester) -> {
                                    mAdapter.updateFilterList(
                                        spinner.selectedItem.toString()
                                        , GetSavedHashArray.TESTER
                                    )
                                }
                                getString(R.string.searchByDate) -> {

                                }
                            }
                            dialog.dismiss()
                            filtered = true
                            returnSearchButton()

                        }
                    }
                }
            }).execute()
        } else {
            /**以Date日曆做篩選*/
            var calendar: CalendarView = view.findViewById(R.id.calendarView_dataFilter)

            GetSavedHashArray(this, object : GetSavedHashArray.AsyncResponse {
                override fun processFinish(hashArray: HashMap<Int, HashSet<String>>) {
                    var arrayList = ArrayList<String>()
                    when (title) {
                        getString(R.string.searchByDate) -> {
                            arrayList = toArrayList(hashArray[GetSavedHashArray.TIME_DATE])
                        }
                    }
                    var events: MutableList<EventDay> = ArrayList()
                    for (i in 0 until arrayList.size) {
                        val mCalender = Calendar.getInstance()
                        val sdf = SimpleDateFormat("yyyy/MM/dd")
                        var date: Date
                        try {
                            date = sdf.parse(arrayList[i])
                        } catch (e: Exception) {
                            date = sdf.parse("2019/12/01")
                            Log.w(TAG, "錯誤: $e");
                        }
                        mCalender.time = date
                        events.add(EventDay(mCalender, R.drawable.noun_save_calenders))
                        runOnUiThread {
                            calendar.setEvents(events)
                        }
                    }
                    runOnUiThread {
                        btOK.setOnClickListener {
                            when (title) {
                                getString(R.string.searchByDate) -> {
                                    val sdf = SimpleDateFormat("yyyy/MM/dd")

                                    for (calendar in calendar.selectedDates) {
                                        val getCalender = sdf.format(calendar.time)
                                        if (arrayList.contains(getCalender)) {
                                            mAdapter.updateFilterList(
                                                getCalender
                                                , GetSavedHashArray.TIME_DATE
                                            )
                                            dialog.dismiss()
                                            filtered = true
                                            returnSearchButton()
                                        } else {
                                            Toast.makeText(
                                                this@RecordHistoryActivity
                                                , "The $getCalender is no data", Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }).execute()
        }


        dialog.show()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        dialog.window!!.setLayout(dm.widthPixels - 180, ViewGroup.LayoutParams.WRAP_CONTENT)

    }

    /**將HashSet轉為ArrayList*/
    fun toArrayList(input: HashSet<String>?): ArrayList<String> {
        val hashSet = input?.toArray() as Array<out Any>
        var arrayList = ArrayList<String>()

        for (element in hashSet) arrayList.add(element.toString())
        return arrayList

    }


    /**讀取DB內資料*/
    private fun initReadData() {
        var dialog = ProgressDialog.show(this, "", "讀取中...", true)
        dialog.setCancelable(true)
        Thread {
            val savedData: MutableList<Data> = DataBase.getInstance(this).dataUao.allData.reversed().toMutableList()

            var arrayTotal = ArrayList<ArrayList<HashMap<String, String>>>()
            for (i in 0 until savedData.size) {//表示有幾個儲存
                var arrayList = ArrayList<HashMap<String, String>>()
                var jsonArray = JSONArray(savedData[i].json_MySave)
                for (x in 0 until jsonArray.length()) {//分解json的內容
                    val jsonObject = jsonArray.getJSONObject(x)
                    val hashMap = HashMap<String, String>()
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
                }
                arrayTotal.add(arrayList)
            }

            val layoutManager = LinearLayoutManager(this)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            val recycler = findViewById<RecyclerView>(R.id.recyclerView_RecordHistoryDisplay)
            layoutManager.recycleChildrenOnDetach = true
            recycler.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
            recycler.layoutManager = layoutManager
            recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val floatingActionMenu =
                        findViewById<FloatingActionMenu>(R.id.floatingActionMenuButton_Filter)
                    if (isSlideToBottom(recyclerView)) {
                        floatingActionMenu.hideMenu(true)
                    } else floatingActionMenu.showMenu(true)
                }
            })
            recycler.isNestedScrollingEnabled = false
            mAdapter = MyAdapter(savedData, this, arrayTotal)
            runOnUiThread {
                mAdapter.setHasStableIds(false)
                recycler.adapter = mAdapter
            }
            SystemClock.sleep(1000)
            dialog.dismiss()

        }.start()
    }

    /**判斷RecyclerView是否已到底*/
    fun isSlideToBottom(recyclerView: RecyclerView): Boolean {
        if (recyclerView == null) return false

        if (recyclerView.computeVerticalScrollExtent() +
            recyclerView.computeVerticalScrollOffset() >= recyclerView.computeVerticalScrollRange()
        ) {
            return mAdapter.itemCount > 2

        } else {
            return false
        }

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

    /**設置使用者碰到螢幕後要做的事*/
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        val floatingActionMenu =
            findViewById<FloatingActionMenu>(R.id.floatingActionMenuButton_Filter)
        if (ev?.action == MotionEvent.ACTION_DOWN && floatingActionMenu.isOpened) {
            floatingActionMenu.close(true)
        }
        return super.dispatchTouchEvent(ev)
    }

    /**ToolBar的內容物點擊*/
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**回傳修改後的值*/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        Log.d(TAG, ":$requestCode $resultCode ${data?.getStringExtra("RESULT")} ");
        if (requestCode == RESULT_CODE && resultCode == 1) {
            val id = data?.getIntExtra("modifiedIndex", 0)
            mAdapter.updateList()
        }
    }

    /**第一個RecyclerView*/
    class MyAdapter : RecyclerView.Adapter<MyAdapter.ViewHolder> {

        val TAG = RecordHistoryActivity::class.java.simpleName + "My"
        private var data: MutableList<Data>
        private val binderHelper = ViewBinderHelper()
        private val activity: Activity
        private var arrayList: ArrayList<ArrayList<HashMap<String, String>>>
        private lateinit var cAdapter: ChildRecyclerView

        constructor(
            data: MutableList<Data>,
            activity: Activity,
            arrayList: ArrayList<ArrayList<HashMap<String, String>>>
        ) : super() {
            this.data = data
            this.activity = activity
            this.arrayList = arrayList
        }
        fun getNowDisplayData():List<Data>{
            return data
        }

        fun updateList() {
            Thread {
                data.clear()
                data = DataBase.getInstance(activity).dataUao.allData.reversed().toMutableList()
                var arrayTotal = setChildValues()
                arrayList = arrayTotal
                activity.runOnUiThread {
                    notifyDataSetChanged()
                    cAdapter.notifyDataSetChanged()
                }
            }.start()
        }

        private fun setChildValues(): ArrayList<ArrayList<HashMap<String, String>>> {
            var arrayTotal = ArrayList<ArrayList<HashMap<String, String>>>()
            for (i in 0 until data.size) {//表示有幾個儲存
                var arrayList = ArrayList<HashMap<String, String>>()
                var jsonArray = JSONArray(data[i].json_MySave)
                for (x in 0 until jsonArray.length()) {//分解json的內容
                    val jsonObject = jsonArray.getJSONObject(x)
                    val hashMap = HashMap<String, String>()
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
                }
                arrayTotal.add(arrayList)
            }
            return arrayTotal
        }

        fun updateFilterList(condition: String, search: Int) {
            Thread {
                data.clear()
                var mSaved: MutableList<Data>
                when (search) {
                    GetSavedHashArray.DEVICE_UUID -> {
                        mSaved = DataBase.getInstance(activity).dataUao.searchByUUID(condition)
                    }
                    GetSavedHashArray.DEVICE_NAME -> {
                        mSaved =
                            DataBase.getInstance(activity).dataUao.searchByDeviceName(condition)
                    }
                    GetSavedHashArray.TESTER -> {
                        mSaved = DataBase.getInstance(activity).dataUao.searchByTester(condition)
                    }
                    GetSavedHashArray.TIME_DATE -> {
                        mSaved = DataBase.getInstance(activity).dataUao.searchByTimeDate(condition)
                    }
                    else -> {
                        mSaved = DataBase.getInstance(activity).dataUao.allData
                    }
                }
                data = mSaved
                var arrayTotal = setChildValues()
                arrayList = arrayTotal
                activity.runOnUiThread {
                    notifyDataSetChanged()
                    cAdapter.notifyDataSetChanged()

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

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun getItemViewType(position: Int): Int {
            return position
        }

        override fun onViewDetachedFromWindow(holder: ViewHolder) {
            super.onViewDetachedFromWindow(holder)
            holder.setIsRecyclable(false)
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
                val intent = Intent(activity, ModifyHistoryDataActivity::class.java)
                intent.putExtra("position", id)
                activity.startActivityForResult(intent, RESULT_CODE)
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
            val title =
                holder.parent.context.getString(R.string.tester) + data[position].name
            val context = holder.parent.context
            val deviceID = context.getString(R.string.deviceId) + data[position].deviceUUID
            val deviceName = context.getString(R.string.deviceName) + data[position].deviceName
            val time =
                context.getString(R.string.measureTime) + data[position].date + " " + data[position].time

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
            Thread {
                val layoutManager =
                    StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.HORIZONTAL);
                layoutManager.orientation = LinearLayoutManager.VERTICAL
                cAdapter = ChildRecyclerView(arrayList[position])
                holder.recyclerChild.layoutManager = layoutManager
                holder.recyclerChild.adapter = cAdapter


            }.start()


        }
    }

    /**包在第一個RecyclerView內的那個Recycler*/
    private class ChildRecyclerView(var recordInfo: ArrayList<HashMap<String, String>>) :
        RecyclerView.Adapter<ChildRecyclerView.ViewHolder>() {

        val TAG = RecordHistoryActivity::class.java.simpleName + "My"


        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvTitle = v.findViewById<TextView>(R.id.textView_item_RecordTitle)
            val tvPV = v.findViewById<TextView>(R.id.textView_item_RecordPV)
            val tvEH = v.findViewById<TextView>(R.id.textView_item_RecordEH)
            val tvEL = v.findViewById<TextView>(R.id.textView_item_RecordEL)
            val parent = v

        }

        override fun getItemViewType(position: Int): Int {
            return position
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.activity_save_record_item, parent, false)
            return ViewHolder(v)
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
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
