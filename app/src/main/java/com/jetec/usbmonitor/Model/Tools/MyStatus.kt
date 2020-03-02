package com.jetec.usbmonitor.Model.Tools

class MyStatus{
    companion object{
        var isOurDevice:String = ""//確認使用者不是接公司以外的產品(必須為UF)
        var deviceType:String =""//範例:THC
        var deviceRow:Int =0//一個Sensor有幾個參數
        var usbType:String = ""//USB模式，例如:CDC
        var engineerModel:Boolean = false//工程師模式
        var nightModeSwitch:Boolean = false//夜間模式


    }
}