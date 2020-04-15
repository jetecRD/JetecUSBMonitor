package com.jetec.usbmonitor.Controller

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import androidx.recyclerview.widget.RecyclerView
import com.jetec.usbmonitor.R

class EngineerMode {
    val  activity:Activity

    constructor(activity: Activity){
        this.activity = activity
    }

    companion object{
        var engArrayList = ArrayList<String>()
        var engAdapter = EngAdapter(engArrayList)

    }
    fun engineer(){
        val view = activity.findViewById<View>(R.id.engineerView)
        val btDeviceInfo = view.findViewById<Button>(R.id.button_EngGetDeviceInfo)
        val switch = view.findViewById<Switch>(R.id.switch_EngChangeMode)
        val edInput = view.findViewById<EditText>(R.id.editTextEngInput)
        val btSend = view.findViewById<Button>(R.id.button_EngSend)
        val btClear = view.findViewById<Button>(R.id.button_EngClear)






    }

    class EngAdapter : RecyclerView.Adapter<EngAdapter.ViewHolder> {
        private var arrayList:ArrayList<String>

        constructor(arrayList: ArrayList<String>){
            this.arrayList = arrayList
        }

        public fun upDataData(arrayList: ArrayList<String>){
            Thread{


            }.start()
        }

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            TODO("Not yet implemented")
        }

        override fun getItemCount(): Int {
            TODO("Not yet implemented")
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            TODO("Not yet implemented")
        }
    }
}