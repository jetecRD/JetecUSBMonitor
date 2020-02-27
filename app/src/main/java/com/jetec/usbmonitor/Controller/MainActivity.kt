package com.jetec.usbmonitor.Controller

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import com.jetec.usbmonitor.R
import kotlinx.android.synthetic.main.activity_connect_status.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    val TAG: String = MainActivity::class.java.simpleName+"My"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var intent = intent
        var infoArrayList = intent.getStringArrayListExtra("deviceInfo")
        Log.d(TAG, "$infoArrayList ")
        setMenu()

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


}
