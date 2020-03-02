package com.jetec.usbmonitor.Model

import android.graphics.Color
import com.jetec.usbmonitor.R

class DeviceValue {


    private var row: Int = 0
    private var type: String = ""
    private var dp: Int = 0
    private var mValue:String = ""
    private var empty:String = ""
    private var unit:String = ""
    private var originValue: String = ""
    private var label:String = ""
    private var color: Int = Color.RED
    private var icon:Int =0

    constructor(row: Int
                ,type:String
                ,dp:Int
                ,mValue:String
                ,empty:String
                ,unit:String
                ,label:String
                ,originValue:String
                ,color: Int
                ,icon:Int){
        this.row = row
        this.type = type
        this.dp = dp
        this.mValue = mValue
        this.empty = empty
        this.unit = unit
        this.originValue = originValue
        this.label = label
        this.color = color
        this.icon = icon
    }
    public fun getRow(): Int {
        return row
    }
    public fun getType():String{
        return type
    }
    public fun getDP():Int{
        return dp
    }
    public fun getmValue():String{
        return mValue
    }
    public fun getEmpty():String{
        return empty
    }
    public fun getUnit():String{
        return unit
    }
    public fun getOriginValue():String{
        return originValue
    }
    public fun getLabel():String{
        return label
    }
    public fun getColor():Int{
        return color
    }
    public fun getIcon():Int{
            return icon
    }


    public fun setIcon(icon: Int){
        this.icon
    }

    public fun setColor(color: Int){
        this.color = color
    }
    public fun setLabel(label:String){
        this.label = label
    }
    public fun setRow(row: Int){
        this.row = row
    }
    public fun setType(type: String){
        this.type = type
    }
    public fun setDP(dp: Int){
        this.dp = dp
    }
    public fun setmValue(mValue: String){
        this.mValue = mValue
    }
    public fun setEmpty(empty: String){
        this.empty = empty
    }
    public fun setUnit(unit: String){
        this.unit = unit
    }
    public fun setOriginValue(originValue: String){
        this.originValue = originValue
    }



}
