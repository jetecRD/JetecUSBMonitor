package com.jetec.usbmonitor.Controller

import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jetec.usbmonitor.Model.CrashHandler
import com.jetec.usbmonitor.Model.RoomDBHelper.Data
import com.jetec.usbmonitor.Model.RoomDBHelper.DataBase
import com.jetec.usbmonitor.R

//import kotlinx.android.synthetic.main.activity_setting.*

class RecordHistoryActivity : AppCompatActivity() {
    val TAG = RecordHistoryActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_history)
        CrashHandler.getInstance().init(this)
        setMenu()
        Thread{
            val savedData: List<Data> = DataBase.getInstance(this).dataUao.allData
            Log.d(TAG, ":${savedData[0].deviceUUID} ");
            runOnUiThread{
                var imageView = findViewById<ImageView>(R.id.imageView_tttttttttt)

                var screenShot = BitmapFactory.decodeByteArray(savedData[1].screenShot, 0, savedData[1].screenShot.size);
                imageView.setImageBitmap(screenShot)
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
}
