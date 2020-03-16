package com.jetec.usbmonitor.Model

import android.app.Activity
import android.util.Log
import com.jetec.usbmonitor.Model.Tools.MyStatus
import com.jetec.usbmonitor.Model.Tools.Tools
import java.text.DecimalFormat

class AnalysisValueInfo() {
    val TAG: String = AnalysisValueInfo::class.java.simpleName
    var arrayList = ArrayList<String>()

    constructor(activity: Activity) : this() {

    }

    constructor(arrayList: ArrayList<String>) : this() {
        this.arrayList = arrayList
    }


    /**取得回傳的數值(指令:Request)*/
    fun requestValue(activity: Activity, arrayList: ArrayList<String>): MutableList<DeviceValue> {
        try {
            var mData: MutableList<DeviceValue> = ArrayList()
            for (i in 0 until arrayList.size) {
                var row = arrayList[i].substring(0, 2)
                var type = arrayList[i].substring(2, 4)
                var dp = arrayList[i].substring(4, 6)
                var value = arrayList[i].substring(6, 10)
                var empty = arrayList[i].substring(10, 12)
                var unit = arrayList[i].substring(12, 14)
                mData.add(
                    DeviceValue(
                        row.toInt()
                        , returnType(Tools.hex2Dec(type).toInt())
                        , dp.toInt()
                        , Tools.returnValue(dp.toInt(), Tools.hextoDecShort(value))
                        , empty
                        , Tools.setUnit(Tools.hex2Dec(unit).toInt())
                        , Tools.setLabel(Tools.hex2Dec(unit).toInt(), activity)
                        , arrayList[i]
                        , Tools.setColor(Tools.hex2Dec(unit).toInt())
                        , Tools.setIcon(Tools.hex2Dec(unit).toInt())
                    )
                )
            }
            return mData

        } catch (e: Exception) {
            Log.e(TAG, ": ${e.message}");
            var mData: MutableList<DeviceValue> = ArrayList()

            return mData
        }

    }//取得值

    /**取得回傳的數值(指令:GET),此處是用來搭配給值用來判斷上下限等數據用的，故回傳的陣列會與排數相同*/
    fun requestSetting(
        activity: Activity,
        arrayList: ArrayList<String>
    ): MutableList<DeviceSetting> {
        try {
            var mData: MutableList<DeviceSetting> = ArrayList()
            val mArrayList: ArrayList<ArrayList<String>> = ArrayList()
            for (i in 0 until MyStatus.deviceRow) {
                val liArrayList: ArrayList<String> = ArrayList()
                for (x in 0 until arrayList.size) {
                    if (arrayList[x].substring(0, 2).toInt() - 1 == i) {
                        liArrayList.add(arrayList[x])
                    }
                }
                mArrayList.add(liArrayList)

            }
            Log.d(TAG, "$mArrayList ");

            for (i in 0 until mArrayList.size) {
                for (x in 0 until mArrayList[i].size) {
                    var row = mArrayList[i][x].substring(0, 4)
                    var type = mArrayList[i][x].substring(2, 4)
                    var dp = mArrayList[i][x].substring(4, 6)
                    var value = mArrayList[i][x].substring(6, 10)
                    var empty = mArrayList[i][x].substring(10, 12)
                    var unit = mArrayList[i][x].substring(12, 14)
                    mData.add(DeviceSetting())
                    when (returnType(type.toInt())) {
                        "PV" -> {
                            mData[i].setPV("PV")
                            mData[i].setPVValue(Tools.returnValue(dp.toInt(), Tools.hextoDecShort(value)))
                            mData[i].setPVOrigin(mArrayList[i][x])
                        }
                        "EH" -> {
                            mData[i].setEH("EH")
                            mData[i].setEHValue(Tools.returnValue(dp.toInt(), Tools.hextoDecShort(value)))
                            mData[i].setEHOrigin(mArrayList[i][x])
                        }
                        "EL" -> {
                            mData[i].setEL("EL")
                            mData[i].setELValue(Tools.returnValue(dp.toInt(), Tools.hextoDecShort(value)))
                            mData[i].setELOrigin(mArrayList[i][x])
                        }
                        "trans℃" -> {
                            mData[i].setTR("TR")
                            mData[i].setTRValue(Tools.returnValue(dp.toInt(), Tools.hextoDecShort(value)))
                            mData[i].setTROrigin(mArrayList[i][x])
                        }
                    }


                }
            }

            return mData

        } catch (e: Exception) {
            Log.e(TAG, ": ${e.message}");
            var mData: MutableList<DeviceSetting> = ArrayList()

            return mData
        }

    }



    /**在設定處GET後所回傳的數值*/
    fun transSetting(
        activity: Activity,
        mSetting: ArrayList<String>
    ): MutableList<DeviceSetting> {
        var mData: MutableList<DeviceSetting> = ArrayList()
        try {
//            Log.d(TAG, "$mSetting ");
            for (i in 0 until mSetting.size) {
                var row = mSetting[i].substring(0, 2)
                var type = mSetting[i].substring(2, 4)
                var dp = mSetting[i].substring(4, 6)
                var value = mSetting[i].substring(6, 10)
                var empty = mSetting[i].substring(10, 12)
                var unit = mSetting[i].substring(12, 14)
                mData.add(DeviceSetting())
                mData[i].setLabel(
                    Tools.setLabel(Tools.hex2Dec(unit).toInt(), activity)
                            + "\n" + Tools.setSettingLabel(type.toInt(), activity)
                )
                mData[i].setType(type)
                mData[i].setValue(Tools.returnValue(dp.toInt(), Tools.hextoDecShort(value)))
                mData[i].setDP(dp.toInt())
                mData[i].setOriginValue(mSetting[i])

            }

            return mData
        } catch (e: Exception) {
            Log.e(TAG, "${e.message} ");
            return mData
        }


    }


    /**
     * 將通訊中第二個byte(決定種類:PV/EH/EF...)
     * @param input 輸入種類*/
    private fun returnType(input: Int): String {

        when (input) {
            1 -> {
                return "PV"
            }
            2 -> {
                return "EH"
            }
            3 -> {
                return "EL"
            }
            4 -> {
                return "trans℃"
            }
            else -> {
                return "empty"
            }
        }
    }


}
