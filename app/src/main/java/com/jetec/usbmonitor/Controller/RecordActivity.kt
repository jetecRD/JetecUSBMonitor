package com.jetec.usbmonitor.Controller

//import kotlinx.android.synthetic.main.activity_record.*
import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.widget.ContentLoadingProgressBar
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.facebook.stetho.Stetho
import com.google.gson.Gson
import com.jetec.usbmonitor.Model.CrashHandler
import com.jetec.usbmonitor.Model.RoomDBHelper.DataBase
import com.jetec.usbmonitor.Model.Tools.MyStatus
import com.jetec.usbmonitor.Model.Tools.Tools
import com.jetec.usbmonitor.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicReference

class RecordActivity : AppCompatActivity() {
    val TAG = RecordActivity::class.java.simpleName + "My"
    private var currentImagePath = ""

    companion object {
        const val IMAGE_REQUEST = 100
        const val REQUEST_FINE_LOCATION_PERMISSION = 101;
        const val INTENTNOW = "NowData"
        const val IntentGetTitle = "Title"
        const val IntentGetValue = "value"
        const val IntentGetOriginValue = "Origin"
        const val IntentMyNowYMd = "GetYMd"
        const val IntentMyNowHms = "GetHms"
        const val IntentPVValue = "PV"
        const val IntentEHValue = "EH"
        const val IntentELValue = "EL"
        const val GetPVOrigin = "GetPVOrigin"
        const val GetEHOrigin = "GetEHOrigin"
        const val GetELOrigin = "GetELOrigin"
        const val GetScreenShot = "ScreenShot"
    }

    lateinit var imageView: ImageView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)
        CrashHandler.getInstance().init(this)
        Stetho.initializeWithDefaults(this)
        getIntentValue()
        takePicture()
        setMenu()

    }
    private fun setMenu() {
        val tvToolBarTitle = findViewById<TextView>(R.id.textView_RecordToolBarTitle)
        tvToolBarTitle.typeface = Typeface
            .createFromAsset(this.assets, "segoe_print.ttf")
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
    @SuppressLint("SetTextI18n")
    private fun getIntentValue() {
        val intent = intent
        val infoArrayList:ArrayList<HashMap<String,String>>
                = intent.getStringArrayListExtra(INTENTNOW)as ArrayList<HashMap<String,String>>
        val date = intent.getStringExtra(IntentMyNowYMd)
        val time = intent.getStringExtra(IntentMyNowHms)
        val b = intent.getByteArrayExtra(GetScreenShot)
//        val bs:ByteArrayOutputStream = ByteArrayOutputStream()
//                getScreenShot()?.compress(Bitmap.CompressFormat.JPEG,100,bs)
//                intent.putExtra(RecordActivity.GetScreenShot,bs.toByteArray())
        var screenShot = BitmapFactory.decodeByteArray(b, 0, b.size);
        //        imageView_RecordTakePhoto.setImageBitmap(SSB)
        var edTime = findViewById<EditText>(R.id.editText_RecordTime)
            edTime.setText("$date $time")
        val layoutManager = StaggeredGridLayoutManager(3,StaggeredGridLayoutManager.VERTICAL);
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView_RecordData)
        recyclerView.layoutManager = layoutManager
        val mAdapter = MyRecyclerView(infoArrayList)
        recyclerView.adapter = mAdapter

        val btRecordSave = findViewById<Button>(R.id.button_RecordSave)
        val btRecordCancel = findViewById<Button>(R.id.button_RecordCancel)
        btRecordSave.setOnClickListener {
            saveData2DataBase(inforArray = infoArrayList,screenShot = b,dateTime = "$date#$time")
        }
        btRecordCancel.setOnClickListener {
            finish()
        }
    }

    private fun saveData2DataBase(inforArray:ArrayList<HashMap<String,String>>
                                  ,screenShot:ByteArray,dateTime:String){
        val deivceUUID = Tools.sendData("Name",200,this,0)//獲取裝置唯一碼
        val deivceName = Tools.sendData("Name",200,this,1)
//        Log.d(TAG, ":$deivceUUID ");
//        Log.d(TAG, ":$deivceName ");
        val edName = findViewById<EditText>(R.id.editText_RecordName)
        val edNote = findViewById<EditText>(R.id.editText_RecordNotes)
        val imageView = findViewById<ImageView>(R.id.imageView_RecordTakePhoto)
//        val cameraBitmap = (imageView.drawable as BitmapDrawable).bitmap//拍照的
//        val cameraBs: ByteArrayOutputStream = ByteArrayOutputStream()//拍照的
//        cameraBitmap.compress(Bitmap.CompressFormat.JPEG,100,cameraBs)//拍照的
//        var camera = cameraBs.toByteArray()//拍照的
        val json = Gson().toJson(inforArray)
        var nameString = edName.text.toString()
        var noteString = edNote.text.toString()

        var dialog = ProgressDialog.show(this,getString(R.string.saving),getString(R.string.pleaseWait),true)
        dialog.setCancelable(true)
        Thread{
            DataBase.getInstance(this).dataUao.insertData(
                deivceUUID[0]
                ,deivceName[1].substring(4)
                ,MyStatus.deviceType
                ,nameString
                ,json
                ,screenShot
                ,currentImagePath
                ,noteString
                ,dateTime)
            runOnUiThread{
                dialog.dismiss()
                finish()
            }
            Looper.prepare()
            Toast.makeText(this,getString(R.string.succesSaved),Toast.LENGTH_LONG).show()
            Looper.loop()

        }.start()

    }
    private fun takePicture() {
        var hasGone = checkSelfPermission(Manifest.permission.CAMERA)
        if (hasGone != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_FINE_LOCATION_PERMISSION
            )
        }//get Permission
        imageView = findViewById(R.id.imageView_RecordTakePhoto)
        imageView.setOnClickListener {
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
    }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        Log.d(TAG, ":$resultCode ");
        if (requestCode == IMAGE_REQUEST && resultCode == -1){
            if (currentImagePath.isNotEmpty()){
                val progress = findViewById<ContentLoadingProgressBar>(R.id.progress_horizontal)
                progress.visibility = View.VISIBLE
                progress.show()
                Thread{
                    progress.progress = 0
                    val imageView = findViewById<ImageView>(R.id.imageView_RecordTakePhoto)
                    progress.progress = 10
                    var bitmap:AtomicReference<Bitmap> = AtomicReference(BitmapFactory.decodeFile(currentImagePath))
                    progress.progress = 40
                    var matrix = Matrix()
                    progress.progress = 60
                    matrix.setRotate(90f)
                    progress.progress = 80
                    SystemClock.sleep(200)
                    bitmap.set(Bitmap.createBitmap(bitmap.get(), 0, 0, bitmap.get().width
                        , bitmap.get().height, matrix,
                        true))
                    progress.progress = 100
                    runOnUiThread {
                        imageView.setImageBitmap(bitmap.get())
                        progress.hide()
                    }
                }.start()
            }
        }else{
            Toast.makeText(this,getString(R.string.noImageWasTaken),Toast.LENGTH_SHORT).show()
        }
    }

    private class MyRecyclerView(val mData:ArrayList<HashMap<String,String>>): RecyclerView.Adapter<MyRecyclerView.ViewHolder>() {
        val TAG = RecordActivity::class.java.simpleName + "My"
        class ViewHolder (v:View): RecyclerView.ViewHolder(v) {
            val tvTitle = v.findViewById<TextView>(R.id.textView_item_RecordTitle)
            val tvPV = v.findViewById<TextView>(R.id.textView_item_RecordPV)
            val tvEH = v.findViewById<TextView>(R.id.textView_item_RecordEH)
            val tvEL = v.findViewById<TextView>(R.id.textView_item_RecordEL)
            val parent = v

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.activity_save_record_item,parent,false)
            return ViewHolder(v)
        }

        override fun getItemCount(): Int {
            return mData.size
        }


        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            var unit = Tools.setUnit(Tools.hex2Dec(mData[position][IntentGetOriginValue]!!.substring(12,14)).toInt())
            holder.tvTitle.text ="${mData[position][IntentGetTitle]}:\n${mData[position][IntentGetValue]+unit}"
            val context=  holder.parent.context
            holder.tvPV.text =context.getString(R.string.PV)+": "+ mData[position][IntentPVValue]
            holder.tvEH.text = context.getString(R.string.EH)+": " + mData[position][IntentEHValue]
            holder.tvEL.text = context.getString(R.string.EL)+": "+ mData[position][IntentELValue]
        }
    }

}

