package com.jetec.usbmonitor.Controller

import android.app.Activity
import android.app.AlertDialog
import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Vibrator
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jetec.usbmonitor.Model.Utils.Tools
import com.jetec.usbmonitor.R


class EngineerMode {
    val TAG = EngineerMode::class.java.simpleName

    companion object {
        var engArrayList = ArrayList<String>()
        var engAdapter = EngAdapter(engArrayList)
        lateinit var activity: Activity
        lateinit var recycler: RecyclerView
    }

    constructor(activity: Activity) {
        EngineerMode.activity = activity
    }

    fun engineer() {
        val view = activity.findViewById<View>(R.id.engineerView)
        val btDeviceInfo = view.findViewById<Button>(R.id.button_EngGetDeviceInfo)
        val switch = view.findViewById<Switch>(R.id.switch_EngChangeMode)
        val edInput = view.findViewById<EditText>(R.id.editTextEngInput)
        val btSend = view.findViewById<Button>(R.id.button_EngSend)
        val btClear = view.findViewById<Button>(R.id.button_EngClear)
        recycler = view.findViewById<RecyclerView>(R.id.recyclerView_EngDisplay)

        val layoutManager = LinearLayoutManager(view.context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recycler.layoutManager = layoutManager
//        recycler.addItemDecoration(DividerItemDecoration(view.context,DividerItemDecoration.VERTICAL))

        engAdapter = EngAdapter(engArrayList)
        recycler.adapter = engAdapter
        recycler.scrollToPosition(engArrayList.size - 1)
        val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE)as ClipboardManager
        engAdapter.setCopy(object :EngAdapter.OnLongClick{
            override fun LongClick(getText: String) {
                val v = activity.application.getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
                v.vibrate(100)

                val clipData = ClipData.newPlainText(null,getText)
                clipboard.setPrimaryClip(clipData)
            }
        })

        if (switch.isChecked) edInput.hint = "輸入byteArray..."
        else edInput.hint = "輸入String..."

        switch.setOnCheckedChangeListener { compoundButton, status ->
            if (!compoundButton.isPressed) return@setOnCheckedChangeListener
            if (status) edInput.hint = "輸入byteArray..."
            else edInput.hint = "輸入String..."
        }

        btClear.setOnClickListener {
            engArrayList.clear()
            engAdapter.upDataData(engArrayList)
        }

        btSend.setOnClickListener {
            val inputToSend = edInput.text.toString()
            if (inputToSend.isEmpty()) return@setOnClickListener
            try {
                if (switch.isChecked){
                    Tools.fromHexString(inputToSend)?.let { it1 -> Tools.sendData(it1,100, activity,1) }
                }else{
                    Tools.sendData(inputToSend,100, activity,0)
                }
                edInput.setText("")
            }catch (e:Exception){
                if (switch.isChecked){
                    Tools.sendData(inputToSend,100, activity,0)
                }else{
                    Tools.fromHexString(inputToSend)?.let { it1 -> Tools.sendData(it1,100, activity,1) }
                }
                edInput.setText("")
            }

        }

        btDeviceInfo.setOnClickListener {
            val manager: UsbManager = activity.getSystemService(Context.USB_SERVICE)as UsbManager
            var deviceList:HashMap<String, UsbDevice> = manager.deviceList
            val deviceIterator:Iterator<UsbDevice> = deviceList.values.iterator()
            var info:String = ""
            while (deviceIterator.hasNext()){
                val device = deviceIterator.next()
                info = "Device Name:${device.deviceName}\n" +
                        "Device Class:${device.deviceClass}\n"+
                        "Device DeviceID:${device.deviceId}\n"+
                        "ConfigurationCount:${device.configurationCount}\n"+
                        "DeviceProtocol:${device.deviceProtocol}\n"+
                        "DeviceSubclass:${device.deviceSubclass}\n"+
                        "InterfaceCount:${device.interfaceCount}\n"+
                        "ManufacturerName:${device.manufacturerName}\n"+
                        "ProductId:${device.productId}\n"+
                        "ProductName:${device.productName}\n"+
                        "SerialNumber:${device.serialNumber}\n"+
                        "VendorId:${device.vendorId}\n"+
                        "Version:${device.version}\n"

            }
            val mBuilder: AlertDialog.Builder = AlertDialog.Builder(activity)
            mBuilder.setTitle("裝置資訊")
            mBuilder.setMessage(info)
            mBuilder.show()
        }

        edInput.setOnLongClickListener {
            try {
                val v = activity.application.getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
                v.vibrate(100)
                val word = clipboard.primaryClip
                val item = word?.getItemAt(0)
                val text = item?.text.toString()
                edInput.setText(text)

            }catch (e:Exception){

            }
            true
        }

    }

    class EngAdapter : RecyclerView.Adapter<EngAdapter.ViewHolder> {
        val TAG = EngineerMode::class.java.simpleName
        private var arrayList: ArrayList<String>
        private lateinit var onLongClick:OnLongClick

        constructor(arrayList: ArrayList<String>) {
            this.arrayList = arrayList
        }
        fun setCopy(check:OnLongClick){
            this.onLongClick = check
        }

        fun upDataData(arrayList: ArrayList<String>) {
            Thread {
                this.arrayList = arrayList
                try {
                    activity.runOnUiThread {
                        notifyDataSetChanged()
                        if (arrayList.size != 0) {
                            recycler.scrollToPosition(arrayList.size - 1)
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG, ":$e ");
                }
            }.start()
        }

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tv = v.findViewById<TextView>(R.id.text1)
            val parent = v
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.engineer_info_display_item, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return arrayList.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val mark = arrayList[position].get(index = 0)
            var string = arrayList[position]
            if (mark == '#'){
                holder.tv.gravity = Gravity.RIGHT
                string = string.replace("#","")
            }else{
                holder.tv.gravity = Gravity.LEFT
            }
            holder.tv.text = string
            holder.parent.setOnLongClickListener {
                var copyString = arrayList[position]
                if (mark == '#'){
                    copyString = copyString.replace("#","")
                    copyString = copyString.substring(0,copyString.indexOf("<"))
                }else{
                    copyString = copyString.substring(copyString.indexOf(">")+1)
                }
                onLongClick.LongClick(copyString)
                true
            }
        }
        interface OnLongClick{
            fun LongClick(getText:String)
        }
    }
}