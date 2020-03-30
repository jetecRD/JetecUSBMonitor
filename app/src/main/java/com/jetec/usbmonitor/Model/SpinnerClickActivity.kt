package com.jetec.usbmonitor.Model

import android.R
import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import com.jetec.usbmonitor.Controller.RecordHistoryActivity
import com.jetec.usbmonitor.Controller.SettingActivity
import java.lang.Exception

class SpinnerClickActivity: AdapterView.OnItemSelectedListener{
    private var search = 100
    val TAG = SpinnerClickActivity::class.java.simpleName
    private val activity:Activity
    private var arrayUUID:ArrayList<String>
    private var arrayDeviceName:ArrayList<String>
    private var arrayTester:ArrayList<String>
    private var arrayDateTime:ArrayList<String>



    constructor(search:Int,activity: Activity
                ,arrayUUID:ArrayList<String>,
                arrayDeviceName:ArrayList<String>,
                arrayTester:ArrayList<String>,
                arrayDateTime:ArrayList<String>){
        this.search = search
        this.activity = activity
        this.arrayUUID = arrayUUID
        this.arrayDeviceName = arrayDeviceName
        this.arrayTester = arrayTester
        this.arrayDateTime = arrayDateTime


    }
    override fun onNothingSelected(p0: AdapterView<*>?) {
        Log.d(TAG, "onNothingSelected")
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

        if (p2 !=0){
            val getString = p0?.getItemAtPosition(p2) as String
            GetSavedHashArray(activity,getString,object :GetSavedHashArray.AsyncResponse{
                override fun processFinish(hashArray: HashMap<Int, HashSet<String>>) {
                    Log.d(TAG, "得到的回傳2 $hashArray");
                    try {
                        arrayUUID.clear()
                        arrayDeviceName.clear()
                        arrayTester.clear()
                        arrayDateTime.clear()
                        for (i in 0 until toArrayList(hashArray[GetSavedHashArray.DEVICE_UUID]).size){
                            arrayUUID.add(toArrayList(hashArray[GetSavedHashArray.DEVICE_UUID])[i])
                        }
                        val deviceNameArray = toArrayList(hashArray[GetSavedHashArray.DEVICE_NAME])
                        val testerArray = toArrayList(hashArray[GetSavedHashArray.TESTER])
                        val dateTimeArray = toArrayList(hashArray[GetSavedHashArray.TIME_DATE])
                        for (i in 0 until deviceNameArray.size){
                            arrayDeviceName.add(deviceNameArray[i])
                        }
                        for (i in 0 until testerArray.size){
                            arrayTester.add(testerArray[i])
                        }
                        for (i in 0 until dateTimeArray.size){
                            arrayDateTime.add(dateTimeArray[i])
                        }
                        arrayUUID.add(0,"---Please select---")
                        arrayDeviceName.add(0,"---Please select---")
                        arrayTester.add(0,"---Please select---")
                        arrayDateTime.add(0,"---Please select---")

                        activity.runOnUiThread{
                            val view = activity.layoutInflater.inflate(com.jetec.usbmonitor.R.layout.history_filter_dialog, null)
                            val btOK: Button = view.findViewById(com.jetec.usbmonitor.R.id.button_SettingDialogOK)
                            btOK.setOnClickListener {

                            }
                        }


                    }catch (e: Exception){
                        Toast.makeText(activity,"ERROR?", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, e.message);
                    }
                }
            },search).execute()
        }

    }
    /**將HashSet轉為ArrayList*/
    fun toArrayList(input: HashSet<String>?):ArrayList<String>{
        val hashSet = input?.toArray()as Array<out Any>
        var arrayList = ArrayList<String>()

        for (element in hashSet) arrayList.add(element.toString())
        return arrayList

    }

}