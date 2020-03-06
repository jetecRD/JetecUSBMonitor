package com.jetec.usbmonitor.Model

import kotlin.math.E

class DeviceSetting {
    private var row: Int = 0
    private var EH: String = ""
    private var EL: String = ""
    private var PV: String = ""
    private var TR:String = ""
    private var EHValue: String = ""
    private var ELValue: String = ""
    private var PVValue: String = ""
    private var TRValue:String = ""
    private var dp: Int = 0

    private var empty: String = ""
    private var unit: String = ""
    private var originValue: String = ""
    private var label: String = ""
    private var value:String = ""
    private var type:String = ""


    constructor()


    public fun getRow(): Int {
        return row
    }

    public fun getEH(): String {
        return EH
    }

    public fun getEL(): String {
        return EL
    }

    public fun getPV(): String {
        return PV
    }
    public fun getTR(): String {
        return TR
    }
    public fun getTRValue(): String {
        return TRValue
    }

    public fun getEHValue(): String {
        return EHValue
    }

    public fun getELValue(): String {
        return ELValue
    }

    public fun getPVValue(): String {
        return PVValue
    }

    public fun getDP(): Int {
        return dp
    }

    public fun getEmpty(): String {
        return empty
    }

    public fun getUnit(): String {
        return unit
    }

    public fun getOriginValue(): String {
        return originValue
    }

    public fun getLabel(): String {
        return label
    }
    public fun getValue():String{
        return value
    }
    public fun getType():String {
        return type
    }
/**================================================================================================*/
    fun setType(type:String){
        this.type = type
    }

    public fun setLabel(label: String) {
        this.label = label
    }

    public fun setRow(row: Int) {
        this.row = row
    }
    public fun setTR(TR:String){
        this.TR = TR
    }
    public fun setTRValue(TRValue:String){
        this.TRValue = TRValue
    }

    public fun setEH(EH: String) {
        this.EH = EH
    }

    public fun setEL(EL: String) {
        this.EL = EL
    }

    public fun setPV(PV: String) {
        this.PV = PV
    }

    public fun setEHValue(EHValue: String) {
        this.EHValue = EHValue
    }

    public fun setELValue(ELValue: String) {
        this.ELValue = ELValue
    }

    public fun setPVValue(PVValue: String) {
        this.PVValue = PVValue
    }


    public fun setDP(dp: Int) {
        this.dp = dp
    }

    public fun setEmpty(empty: String) {
        this.empty = empty
    }

    public fun setUnit(unit: String) {
        this.unit = unit
    }

    public fun setOriginValue(originValue: String) {
        this.originValue = originValue
    }
    public fun setValue(value:String){
        this.value = value
    }


}