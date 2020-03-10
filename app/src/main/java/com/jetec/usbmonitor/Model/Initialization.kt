package com.jetec.usbmonitor.Model

import android.app.Activity
import android.util.Log
import com.jetec.usbmonitor.Model.Tools.Tools

class Initialization {
    private val TAG = Initialization::class.java.simpleName
    private var activity: Activity
    private var type:String


    constructor(activity: Activity,type:String) {
        this.activity = activity
        this.type = type

    }

    fun startINI() {
       for (i in type.indices){
           var row = i+1
           when(type[i]){
               'T'->{
                   mSend(row,4,0,0,0,0)//注意:度C的指令要要先丟
                   mSend(row,1,1,0,0,0)
                   mSend(row,2,1,65,0,0)
                   mSend(row,3,1,-10,0,0)
               }
               'H'->{
                   mSend(row,1,0,0,0,0)
                   mSend(row,2,0,100,0,0)
                   mSend(row,3,0,0,0,0)
               }
               'C'->{
                   mSend(row,1,0,0,0,0)
                   mSend(row,2,0,2000,0,0)
                   mSend(row,3,0,0,0,0)
               }
               'D'->{
                   mSend(row,1,0,0,0,0)
                   mSend(row,2,0,3000,0,0)
                   mSend(row,3,0,0,0,0)
               }
               'E'->{
                   mSend(row,1,0,0,0,0)
                   mSend(row,2,0,5000,0,0)
                   mSend(row,3,0,0,0,0)
               }
               'P'->{
                   mSend(row,1,0,0,0,0)
                   mSend(row,2,0,1000,0,0)
                   mSend(row,3,0,0,0,0)
               }
               'M'->{
                   mSend(row,1,0,0,0,0)
                   mSend(row,2,0,1000,0,0)
                   mSend(row,3,0,0,0,0)
               }
               'Q'->{
                   mSend(row,1,0,0,0,0)
                   mSend(row,2,0,1000,0,0)
                   mSend(row,3,0,0,0,0)
               }
               'O'->{
                   mSend(row,1,0,0,0,0)
                   mSend(row,2,0,130,0,0)
                   mSend(row,3,0,30,0,0)
               }
               'G'->{
                   mSend(row,1,0,0,0,0)
                   mSend(row,2,0,300,0,0)
                   mSend(row,3,0,0,0,0)
               }
               else->{
                   mSend(row,1,0,0,0,0)
                   mSend(row,2,0,9999,0,0)
                   mSend(row,3,0,-9999,0,0)
               }
           }
       }
    }

    private fun mSend(row: Int, type: Int, dp: Int, input: Int, empty: Int, unit: Int) {
        val value = Tools.toHex(
            Tools.sendValueMultiplyDP(
                input.toDouble(), dp
            )
        )
        var byte = Tools.fromHexString(
            String.format("%02x", row)//排數
                    + String.format("%02x", type)//種類
                    + String.format("%02x", dp)//小數點
                    + String.format("%04x", value?.toLong(16))//值
                    + String.format("%02x", empty)//空白
                    + String.format("%02x", unit)//單位
        )
        Log.d(TAG, ":${Tools.byteArrayToHexStr(byte)} ");
         byte?.let { it1 -> Tools.sendData(it1, 300, activity, 1) }
    }


}