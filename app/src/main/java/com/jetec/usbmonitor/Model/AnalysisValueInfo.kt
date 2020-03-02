package com.jetec.usbmonitor.Model

import android.app.Activity
import android.util.Log
import com.jetec.usbmonitor.Controller.MainActivity
import com.jetec.usbmonitor.Model.Tools.Tools
import java.text.DecimalFormat

class AnalysisValueInfo(){
    val TAG:String = AnalysisValueInfo::class.java.simpleName
    var arrayList = ArrayList<String>()

    constructor(arrayList: ArrayList<String>):this(){
        this.arrayList = arrayList
    }
    fun requestValue(activity: Activity):MutableList<DeviceValue>{


        try{
            var mData:MutableList<DeviceValue> =ArrayList()
            for (i in 0 until arrayList.size){
                var row = arrayList[i].substring(0,2)
                var type = arrayList[i].substring(2,4)
                var dp = arrayList[i].substring(4,6)
                var value = arrayList[i].substring(6,10)
                var empty = arrayList[i].substring(10,12)
                var unit = arrayList[i].substring(12,14)
                mData.add(DeviceValue(
                    row.toInt()
                    ,returnType(Tools.hex2Dec(type).toInt())
                    ,dp.toInt()
                    ,returnValue(dp.toInt(),Tools.hex2Dec(value))
                    ,empty
                    ,Tools.setUnit(Tools.hex2Dec(unit).toInt())
                    ,Tools.setLabel(Tools.hex2Dec(unit).toInt(),activity)
                    ,arrayList[i]
                    ,Tools.setColor(Tools.hex2Dec(unit).toInt())
                    ,Tools.setIcon(Tools.hex2Dec(unit).toInt())
                ))
            }
        return mData

        }catch (e:Exception){
            Log.e(TAG, ": ${e.message}");
            var mData:MutableList<DeviceValue> =ArrayList()

            return mData
        }

    }//取得值

    /**
     * 將通訊中第二個byte(決定種類:PV/EH/EF...)
     * @param input 輸入種類*/
    private fun returnType(input:Int):String{

        when(input){
            1->{
                return "PV"
            }
            2->{
                return "EH"
            }
            3->{
                return "EL"
            }
            4->{
                return "trans℃"
            }
            else->{
               return "empty"
            }
        }
    }
    /**
     * 將數值乘以小數點
     * @param dp 小數點
     * @param input 輸入字串*/
    private fun returnValue(dp:Int,input: String):String{
        var d:Double = input.toDouble()
        val decimalFormat = DecimalFormat("###0.0")
        return when(dp) {
            1 -> {
                decimalFormat.format(d / 10)
            }
            2 -> {
                decimalFormat.format(d / 100)
            }
            3 -> {
                decimalFormat.format(d / 1000)
            }
            else -> {
                decimalFormat.format(d)
            }

        }

    }




}