package com.jetec.usbmonitor.Controller

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.widget.ContentLoadingProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jetec.usbmonitor.R
import kotlinx.android.synthetic.main.activity_record.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class RecordActivity : AppCompatActivity() {
    val TAG = RecordActivity::class.java.simpleName + "My"
    private var currentImagePath = ""

    companion object {
        const val IMAGE_REQUEST = 1
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

        getIntentValue()
        takePicture()

        button_RecordSave.setOnClickListener { buttonSOnClick }
        button_RecordCancel.setOnClickListener { buttonSOnClick }
    }

    private fun getIntentValue() {
        var intent = intent
        var infoArrayList:ArrayList<HashMap<String,String>>
                = intent.getStringArrayListExtra(INTENTNOW)as ArrayList<HashMap<String,String>>
        var date = intent.getStringExtra(IntentMyNowYMd)
        var time = intent.getStringExtra(IntentMyNowHms)
        var b = intent.getByteArrayExtra(GetScreenShot)
        var SSB = BitmapFactory.decodeByteArray(b, 0, b.size);
        //        imageView_RecordTakePhoto.setImageBitmap(SSB)
        Log.d(TAG, ": $infoArrayList");
        Log.d(TAG, ": $date");
        Log.d(TAG, ": $time");

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        val data = findViewById<RecyclerView>(R.id.recyclerView_RecordData)
        data.layoutManager = layoutManager
        val mAdapter = MyRecyclerView(infoArrayList)




    }

    private var buttonSOnClick = View.OnClickListener {
        var itemId = it.id

        when(itemId){
            R.id.button_RecordSave->{

            }
            R.id.button_RecordCancel->{
                finish()
            }
        }

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
                runOnUiThread {
                    bitmap.set(Bitmap.createBitmap(bitmap.get(), 0, 0, bitmap.get().width
                        , bitmap.get().height, matrix,
                            true))
                    progress.progress = 100
                    imageView.setImageBitmap(bitmap.get())
                    progress.hide()
                }
            }.start()


        }
    }

    private class MyRecyclerView(val mData:ArrayList<HashMap<String,String>>): RecyclerView.Adapter<MyRecyclerView.ViewHolder>() {

        class ViewHolder (v:View): RecyclerView.ViewHolder(v) {
            val tvTitle = v.findViewById<TextView>(R.id.textView_item_RecordTitle)
            val tvPV = v.findViewById<TextView>(R.id.textView_item_RecordPV)
            val tvEH = v.findViewById<TextView>(R.id.textView_item_RecordEH)
            val tvEL = v.findViewById<TextView>(R.id.textView_item_RecordEL)

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.activity_save_record_item,parent,false)
            return ViewHolder(v)
        }

        override fun getItemCount(): Int {
            return mData.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        }
    }
}

