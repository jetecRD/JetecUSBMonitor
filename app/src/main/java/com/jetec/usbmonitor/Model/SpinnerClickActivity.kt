package com.jetec.usbmonitor.Model

import android.R
import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import java.lang.Exception

class SpinnerClickActivity: AdapterView.OnItemSelectedListener{
    private var search = 100
    val TAG = SpinnerClickActivity::class.java.simpleName
    private val activity:Activity



    constructor(search:Int,activity: Activity){
        this.search = search
        this.activity = activity


    }
    override fun onNothingSelected(p0: AdapterView<*>?) {
        Log.d(TAG, "onNothingSelected")
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        Log.d(TAG, ": $p2");
        if (p2 !=0){
            val getString = p0?.getItemAtPosition(p2) as String
            GetSavedHashArray(activity,getString,object :GetSavedHashArray.AsyncResponse{
                override fun processFinish(hashArray: HashMap<Int, HashSet<String>>) {
                    Log.d(TAG, "得到的回傳2 $hashArray");
                    try {

                        val uuidArray =toArrayList(hashArray[GetSavedHashArray.DEVICE_UUID])
                        val deviceNameArray = toArrayList(hashArray[GetSavedHashArray.DEVICE_NAME])
                        val testerArray = toArrayList(hashArray[GetSavedHashArray.TESTER])
                        val dateTimeArray = toArrayList(hashArray[GetSavedHashArray.TIME_DATE])
                        uuidArray.add(0,"---Please select---")
                        deviceNameArray.add(0,"---Please select---")
                        testerArray.add(0,"---Please select---")
                        dateTimeArray.add(0,"---Please select---")

                        val uuidAdapter = ArrayAdapter(activity
                            ,R.layout.simple_dropdown_item_1line, uuidArray )


                        val deviceAdapter =  ArrayAdapter(activity
                            ,R.layout.simple_dropdown_item_1line, deviceNameArray )

                        val testerAdapter = ArrayAdapter(activity
                            ,R.layout.simple_dropdown_item_1line, testerArray )


                        val dateAdapter = ArrayAdapter(activity
                            ,R.layout.simple_dropdown_item_1line, dateTimeArray )

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