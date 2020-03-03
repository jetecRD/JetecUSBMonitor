package com.jetec.usbmonitor.Model

import android.app.Activity
import android.util.Log
import com.jetec.usbmonitor.Controller.MainActivity
import com.jetec.usbmonitor.Model.Tools.MyStatus
import com.jetec.usbmonitor.Model.Tools.Tools
import java.text.DecimalFormat

class AnalysisValueInfo(){
    val TAG:String = AnalysisValueInfo::class.java.simpleName
    var arrayList = ArrayList<String>()

    constructor(activity: Activity):this(){

    }

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
    fun requestValue(activity: Activity,arrayList: ArrayList<String>):MutableList<DeviceValue>{
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

    fun requestSetting(activity: Activity,arrayList: ArrayList<String>):MutableList<DeviceSetting>{
        try{
            var mData:MutableList<DeviceSetting> =ArrayList()
            val mArrayList:ArrayList<ArrayList<String>> = ArrayList()
            for(i in 0 until MyStatus.deviceRow){
                val liArrayList:ArrayList<String> = ArrayList()
                for (x in 0 until arrayList.size){
                    if (arrayList[x].substring(0,2).toInt()-1 == i){
                        liArrayList.add(arrayList[x])
                    }
                }
                mArrayList.add(liArrayList)

            }
            Log.d(TAG, "$mArrayList ");

            for (i in 0 until mArrayList.size){
                for (x in 0 until mArrayList[i].size){
                    var row = mArrayList[i][x].substring(0,4)
                    var type = mArrayList[i][x].substring(2,4)
                    var dp = mArrayList[i][x].substring(4,6)
                    var value = mArrayList[i][x].substring(6,10)
                    var empty = mArrayList[i][x].substring(10,12)
                    var unit = mArrayList[i][x].substring(12,14)
                    mData.add(DeviceSetting())
                    when(returnType(type.toInt())){
                        "PV"->{
                            mData[i].setPV("PV")
                            mData[i].setPVValue(returnValue(dp.toInt(),Tools.hex2Dec(value)))
                        }
                        "EH"->{
                            mData[i].setEH("EH")
                            mData[i].setEHValue(returnValue(dp.toInt(),Tools.hex2Dec(value)))
                        }
                        "EL"->{
                            mData[i].setEL("EL")
                            mData[i].setELValue(returnValue(dp.toInt(),Tools.hex2Dec(value)))
                        }
                        "trans℃"->{
                            mData[i].setTR("TR")
                            mData[i].setTRValue(returnValue(dp.toInt(),Tools.hex2Dec(value)))
                        }
                    }
                    mData[i].setRow(row.toInt())
                    mData[i].setEmpty(empty)
                    mData[i].setUnit(Tools.setUnit(Tools.hex2Dec(unit).toInt()))
                    mData[i].setOriginValue(mArrayList[i][x])

                }
            }

            return mData

        }catch (e:Exception){
            Log.e(TAG, ": ${e.message}");
            var mData:MutableList<DeviceSetting> =ArrayList()

            return mData
        }

    }

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