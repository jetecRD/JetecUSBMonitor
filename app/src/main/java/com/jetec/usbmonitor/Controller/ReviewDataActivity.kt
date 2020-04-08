package com.jetec.usbmonitor.Controller

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.*
import android.os.StrictMode.VmPolicy
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.itextpdf.text.*
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.text.pdf.draw.LineSeparator
import com.jetec.usbmonitor.Model.MakeSinglePDFReport
import com.jetec.usbmonitor.Model.RoomDBHelper.Data
import com.jetec.usbmonitor.Model.RoomDBHelper.DataBase
import com.jetec.usbmonitor.Model.Tools.Tools
import com.jetec.usbmonitor.R
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import kotlin.math.log


class ReviewDataActivity : AppCompatActivity() {
    val TAG:String = ReviewDataActivity::class.java.simpleName+"My"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_review_data)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1000);
        }
        setMenu()
        setValue()

    }
    /**設置標題列*/
    private fun setMenu() {
        val tvToolBarTitle = findViewById<TextView>(R.id.textView_ReviewToolBarTitle)
        tvToolBarTitle.typeface = Typeface
            .createFromAsset(this.assets, "segoe_print.ttf")
        var mToolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolBar_ReviewActivity)
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
    private fun setValue() {
        val id = intent.getIntExtra("searchID", 0)
        Thread {

            val saved: List<Data> = DataBase.getInstance(this).dataUao.searchById(id)
            if (saved.isEmpty()) {
                finish()
                Looper.prepare()
                Toast.makeText(this, "資料錯誤", Toast.LENGTH_LONG).show()
                Looper.prepare()
            } else {
                val tester = saved[0].name
                val uuid = saved[0].deviceUUID
                val deviceName = saved[0].deviceName
                val date = saved[0].date
                val time = saved[0].time
                val note = saved[0].note
                val jsonS = JSONArray(saved[0].json_MySave)

                val tvTester: TextView = findViewById(R.id.textView_RWD_Tester)
                val tvUUID: TextView = findViewById(R.id.textView_RWD_UUID)
                val tvDeviceName: TextView = findViewById(R.id.textView_RWD_DeviceName)
                val tvTime: TextView = findViewById(R.id.textView_RWD_Time)
                val edNote: EditText = findViewById(R.id.editText_RWD_Note)
                val imageScreenShot: ImageView = findViewById(R.id.imageView_RH_ScreenShort)
                val imagePicture: ImageView = findViewById(R.id.imageView_RH_Picture)
                val recycler:RecyclerView = findViewById(R.id.recyclerView_RWD_Info)
                val layoutManager =
                    StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.HORIZONTAL);
                layoutManager.orientation = LinearLayoutManager.VERTICAL
                recycler.layoutManager = layoutManager

                var arrayList = ArrayList<HashMap<String,String>>()
                for (i in 0 until jsonS.length()){
                    val hashMap = HashMap<String, String>()
                    val jsonObject = jsonS.getJSONObject(i)
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
                val mAdapter = MySetRecycler(arrayList)
                runOnUiThread {
                    var btExportPDF = findViewById<ImageButton>(R.id.button_ReviewDataExport)
                    btExportPDF.setOnClickListener {
//                        makePDF(saved, arrayList);
                        val makePDF = MakeSinglePDFReport()
                        makePDF.makeSinglePDF(this,saved, arrayList)
                        output(makePDF.fileName)


                    }
                    tvTester.text = tester
                    tvUUID.text = uuid
                    tvDeviceName.text = deviceName
                    tvTime.text = date+" "+time
                    edNote.setText(note)
                    Glide.with(this).load(saved[0].screenShot).fitCenter()
                        .placeholder(R.drawable.create_image).into(imageScreenShot)
                    Glide.with(this).load(saved[0].takeImage).fitCenter()
                        .placeholder(R.drawable.create_image).into(imagePicture)
                    recycler.adapter = mAdapter
                }
            }

        }.start()
    }
    /**輸出檔案*/
    private fun output(fileName: String) {
        //->遇上exposed beyond app through ClipData.Item.getUri() 錯誤時在onCreate加上這行
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        builder.detectFileUriExposure()
        //->遇上exposed beyond app through ClipData.Item.getUri() 錯誤時在onCreate加上這行
        val filelocation = File(Environment.getExternalStorageDirectory(), fileName)
        val path: Uri = Uri.fromFile(filelocation)
        val fileIntent = Intent(Intent.ACTION_SEND)
        fileIntent.type = "text/plain"
        fileIntent.putExtra(Intent.EXTRA_SUBJECT, "我的資料")
        fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        fileIntent.putExtra(Intent.EXTRA_STREAM, path)
        startActivity(Intent.createChooser(fileIntent, "Send Mail"))
    }

    /**設置參數顯示的RecyclerView*/
    private class MySetRecycler : RecyclerView.Adapter<MySetRecycler.ViewHolder> {
        private var arrayList:ArrayList<HashMap<String,String>>

        class ViewHolder(v:View): RecyclerView.ViewHolder(v) {
            val tvTitle = v.findViewById<TextView>(R.id.textView_item_RecordTitle)
            val tvPV = v.findViewById<TextView>(R.id.textView_item_RecordPV)
            val tvEH = v.findViewById<TextView>(R.id.textView_item_RecordEH)
            val tvEL = v.findViewById<TextView>(R.id.textView_item_RecordEL)
            val parent = v
        }

        constructor(arrayList:ArrayList<HashMap<String,String>>){
            this.arrayList = arrayList
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.activity_save_record_item, parent, false)
            return ViewHolder(v)
        }

        override fun getItemCount(): Int {
            return arrayList.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            var unit = Tools.setUnit(
                Tools.hex2Dec(arrayList[position]["Origin"]!!.substring(12, 14)).toInt()
            )
            holder.tvTitle.text =
                "${arrayList[position]["Title"]}:\n${arrayList[position]["value"] + unit}"
            holder.tvPV.text =
                holder.parent.context.getString(R.string.PV) + ": " + arrayList[position]["PV"]
            holder.tvEH.text =
                holder.parent.context.getString(R.string.EH) + ": " + arrayList[position]["EH"]
            holder.tvEL.text =
                holder.parent.context.getString(R.string.EL) + ": " + arrayList[position]["EL"]
        }
    }
}