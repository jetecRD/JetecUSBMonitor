package com.jetec.usbmonitor.Controller

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.jetec.usbmonitor.R

class ModifyHistoryDataActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_history_data)
        val id = intent.getIntExtra("position",0)







/**要確認有修改數值，回傳要用這個，然後要帶值*/
//            intent.putExtra("RESULT","OK")
//            setResult(1,intent)
//            finish()
    }

}
