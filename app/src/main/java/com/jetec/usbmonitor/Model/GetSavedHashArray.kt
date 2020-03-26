package com.jetec.usbmonitor.Model

import android.app.Activity
import android.os.AsyncTask
import android.util.Log
import com.jetec.usbmonitor.Model.RoomDBHelper.Data
import com.jetec.usbmonitor.Model.RoomDBHelper.DataBase


class GetSavedHashArray : AsyncTask<Int, Int, List<Data>> {
    private val activity: Activity
    private val TAG = GetSavedHashArray::class.java.simpleName + "My"
    private var delegate: AsyncResponse
    private var search:Int = SEARCH_ALL
    private var searchString:String = ""


    companion object {
        val DEVICE_UUID = 0
        val DEVICE_NAME = 1
        val TESTER = 2
        val TIME_DATE = 3
        val SEARCH_ALL = 5

    }

    constructor(activity: Activity, delegate: AsyncResponse?) {
        this.activity = activity
        this.delegate = delegate!!

    }
    constructor(activity: Activity,searchString: String, delegate: AsyncResponse?, search:Int) {
        this.activity = activity
        this.delegate = delegate!!
        this.search = search
        this.searchString = searchString
    }

    override fun doInBackground(vararg p0: Int?): List<Data> {

        when(search){
            DEVICE_UUID->{
                return DataBase.getInstance(activity).dataUao.searchByUUID(searchString)
            }
            DEVICE_NAME->{
                return DataBase.getInstance(activity).dataUao.searchByDeviceName(searchString)
            }
            TESTER->{
                return DataBase.getInstance(activity).dataUao.searchByTester(searchString)
            }
            TIME_DATE->{
                return DataBase.getInstance(activity).dataUao.searchByTimeDate(searchString)
            }
            else->{

                return DataBase.getInstance(activity).dataUao.allData
            }
        }

    }


    override fun onPostExecute(result: List<Data>?) {
        val hsMap = HashMap<Int, HashSet<String>>()
        val indexArray = arrayOf(DEVICE_UUID, DEVICE_NAME, TESTER, TIME_DATE)
        var hashSet0 = HashSet<String>()
        var hashSet1 = HashSet<String>()
        var hashSet2 = HashSet<String>()
        var hashSet3 = HashSet<String>()
        for (index in indexArray) {
            when (index) {
                DEVICE_UUID -> {
                    for (i in result?.indices!!) {
                        hashSet0.add(result[i].deviceUUID)
                    }
                    hsMap[index] = hashSet0
                }//
                DEVICE_NAME -> {
                    for (i in result?.indices!!) {
                        hashSet1.add(result[i].deviceName)
                    }
                    hsMap[index] = hashSet1
                }//
                TESTER -> {
                    for (i in result?.indices!!) {
                        hashSet2.add(result[i].name)
                    }
                    hsMap[index] = hashSet2
                }//
                TIME_DATE -> {
                    for (i in result?.indices!!) {
                        hashSet3.add(result[i].dateTime.substring(0,result[i].dateTime.lastIndexOf("#")))
                    }
                    hsMap[index] = hashSet3
                }
            }

        }
        delegate.processFinish(hashArray = hsMap)

        super.onPostExecute(result)

    }

    interface AsyncResponse {
        fun processFinish(hashArray: HashMap<Int, HashSet<String>>)
    }

}