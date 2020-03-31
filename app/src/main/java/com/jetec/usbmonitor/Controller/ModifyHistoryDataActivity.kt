package com.jetec.usbmonitor.Controller

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.FileProvider
import androidx.core.widget.ContentLoadingProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.google.gson.JsonArray
import com.jetec.usbmonitor.Model.RoomDBHelper.Data
import com.jetec.usbmonitor.Model.RoomDBHelper.DataBase
import com.jetec.usbmonitor.Model.Tools.Tools
import com.jetec.usbmonitor.R
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ModifyHistoryDataActivity : AppCompatActivity() {
    val TAG = ModifyHistoryDataActivity::class.java.simpleName + "My"
    private var currentImagePath = ""

    companion object {
        const val IMAGE_REQUEST = 100
        const val REQUEST_FINE_LOCATION_PERMISSION = 101
    }

    lateinit var imageView: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)
        setMenu()
        var hasGone = checkSelfPermission(Manifest.permission.CAMERA)
        if (hasGone != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                RecordActivity.REQUEST_FINE_LOCATION_PERMISSION
            )
        }//get Permission
        val id = intent.getIntExtra("position", 0)
        Thread {
            val savedData: List<Data> = DataBase.getInstance(this).dataUao.searchById(id)
            if (savedData.isEmpty()) {
                finish()
                Toast.makeText(this, "錯誤，請重試或聯絡開發者人員", Toast.LENGTH_SHORT).show()
            } else {
                var tester = savedData[0].name
                var date = savedData[0].date
                var time = savedData[0].time
                var note = savedData[0].note
                val jsonArray = JSONArray(savedData[0].json_MySave)
                currentImagePath = savedData[0].takeImage

                var arrayList = ArrayList<HashMap<String, String>>()
                Log.d(TAG, ": $jsonArray");
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
                }

                val recyclerView = findViewById<RecyclerView>(R.id.recyclerView_RecordData)
                val layoutManager =
                    StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
                recyclerView.layoutManager = layoutManager
                var mAdapter = DataAdapter(arrayList)
                recyclerView.adapter = mAdapter
                val edTester = findViewById<EditText>(R.id.editText_RecordName)
                val edDateTime = findViewById<EditText>(R.id.editText_RecordTime)
                val edNote = findViewById<EditText>(R.id.editText_RecordNotes)
                val btCancel = findViewById<Button>(R.id.button_RecordCancel)
                val btSave = findViewById<Button>(R.id.button_RecordSave)
                imageView = findViewById(R.id.imageView_RecordTakePhoto)
                runOnUiThread {
                    edTester.setText(tester)
                    edDateTime.setText("$date\n$time")
                    edNote.setText(note)
                    Glide.with(this).load(currentImagePath).fitCenter()
                        .placeholder(R.drawable.create_image)
                        .into(imageView)

                    /**拍照*/
                    imageView.setOnClickListener {
                        takePicture()
                    }
                    /**取消*/
                    btCancel.setOnClickListener {
                        finish()
                    }
                    /**儲存*/
                    btSave.setOnClickListener {
                        var dialog = ProgressDialog.show(this,getString(R.string.saving),getString(R.string.pleaseWait),true)
                        dialog.setCancelable(true)
                        Thread{
                            val mdTester = edTester.text.toString()
                            val mdNote =edNote.text.toString()
                            val mdPicture = currentImagePath

                            DataBase.getInstance(this).dataUao.updateData(
                                id,
                                mdTester,
                                mdPicture,
                                mdNote
                            )
                            intent.putExtra("RESULT", "OK")
                            intent.putExtra("modifiedIndex",id)
                            setResult(1, intent)

                            runOnUiThread {
                                dialog.dismiss()
                                finish()


                            }
                            Looper.prepare()
                            Toast.makeText(this,getString(R.string.successModify),Toast.LENGTH_LONG).show()
                            Looper.loop()
                        }.start()




                    }
                }
            }
        }.start()

    }

    /**拍攝照片*/
    private fun takePicture() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (cameraIntent.resolveActivity(packageManager) != null) {
            var imageFile: File? = null
            try {
                imageFile = getImageFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (imageFile != null) {
                val imageUri = FileProvider.getUriForFile(
                    this,
                    "com.example.android.fileprovider",
                    imageFile
                )
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                startActivityForResult(cameraIntent, RecordActivity.IMAGE_REQUEST)
            }
        }
    }

    /**撰寫照片標題名稱*/
    @Throws(IOException::class)
    private fun getImageFile(): File? {
        val timeStamp =
            SimpleDateFormat("yyyyMMdd").format(Date())
        val imageName = "jpg_" + timeStamp + "_"
        val storageDir =
            getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile(imageName, ".jpg", storageDir)
        currentImagePath = imageFile.absolutePath
        return imageFile
    }

    /**設置Menu*/
    private fun setMenu() {
        val tvToolBarTitle = findViewById<TextView>(R.id.textView_RecordToolBarTitle)
        tvToolBarTitle.typeface = Typeface
            .createFromAsset(this.assets, "segoe_print.ttf")
        tvToolBarTitle.text = "Edit"
        var mToolbar = findViewById<androidx.appcompat
        .widget.Toolbar>(R.id.toolBar_RecordActivity)
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

    /**設置值的RecyclerView*/
    private class DataAdapter : RecyclerView.Adapter<DataAdapter.ViewHolder> {
        private val recordInfo: ArrayList<HashMap<String, String>>

        constructor(mData: ArrayList<HashMap<String, String>>) {
            this.recordInfo = mData
        }

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

    /**回傳圖像*/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        Log.d(TAG, ":$requestCode , $resultCode ");
        if (requestCode == IMAGE_REQUEST && resultCode == -1) {
            if (currentImagePath.isNotEmpty()) {
                val progress = findViewById<ContentLoadingProgressBar>(R.id.progress_horizontal)
                progress.visibility = View.VISIBLE
                progress.show()
                Thread {
                    progress.progress = 0
                    val imageView = findViewById<ImageView>(R.id.imageView_RecordTakePhoto)
                    progress.progress = 10
                    var bitmap: AtomicReference<Bitmap> =
                        AtomicReference(BitmapFactory.decodeFile(currentImagePath))
                    progress.progress = 40
                    var matrix = Matrix()
                    progress.progress = 60
                    matrix.setRotate(90f)
                    progress.progress = 80
                    SystemClock.sleep(200)
                    bitmap.set(
                        Bitmap.createBitmap(
                            bitmap.get(), 0, 0, bitmap.get().width
                            , bitmap.get().height, matrix,
                            true
                        )
                    )
                    progress.progress = 100
                    runOnUiThread {
                        Glide.with(this).load(bitmap.get()).fitCenter()
                            .into(imageView)
                        progress.hide()
                    }
                }.start()
            } else {
                Toast.makeText(this, getString(R.string.noImageWasTaken), Toast.LENGTH_SHORT).show()
            }
        }else {
            Toast.makeText(this, getString(R.string.noImageWasTaken), Toast.LENGTH_SHORT).show()
        }
    }
}
