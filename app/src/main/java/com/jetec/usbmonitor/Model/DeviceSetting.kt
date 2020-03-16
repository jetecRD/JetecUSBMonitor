package com.jetec.usbmonitor.Model

import kotlin.math.E

class DeviceSetting {
    private var row: Int = 0

    private var EH: String = ""
    private var EHValue: String = ""
    private var EHOrigin:String = ""

    private var EL: String = ""
    private var ELValue: String = ""
    private var ELOrigin:String = ""

    private var PV: String = ""
    private var PVValue: String = ""
    private var PVOrigin:String = ""

    private var TR:String = ""
    private var TRValue:String = ""
    private var TROrigin:String = ""

    private var dp: Int = 0
    private var empty: String = ""
    private var unit: String = ""
    private var originValue: String = ""
    private var label: String = ""
    private var value:String = ""
    private var type:String = ""


    constructor()


    fun getRow(): Int {
        return row
    }

    fun getEH(): String {
        return EH
    }

    fun getEL(): String {
        return EL
    }

    fun getPV(): String {
        return PV
    }
    fun getTR(): String {
        return TR
    }
    fun getTRValue(): String {
        return TRValue
    }

    fun getPVOrigin():String{
        return PVOrigin
    }
    fun getTROrigin():String{
        return TROrigin
    }
    fun getEHValue(): String {
        return EHValue
    }
    fun getEHOrigin():String{
        return EHOrigin
    }
    fun getELValue(): String {
        return ELValue
    }
    fun getELOrigin():String{
        return ELOrigin
    }
    fun getPVValue(): String {
        return PVValue
    }

    fun getDP(): Int {
        return dp
    }

    fun getEmpty(): String {
        return empty
    }

    fun getUnit(): String {
        return unit
    }

    fun getOriginValue(): String {
        return originValue
    }

    fun getLabel(): String {
        return label
    }
    fun getValue():String{
        return value
    }
    fun getType():String {
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
    fun setTROrigin(TROrigin:String){
        this.TROrigin = TROrigin
    }

    public fun setEH(EH: String) {
        this.EH = EH
    }
    fun setEHOrigin(EHOrigin:String){
        this.EHOrigin = EHOrigin
    }

    public fun setEL(EL: String) {
        this.EL = EL
    }
    fun setELOrigin(ELOrigin:String){
        this.ELOrigin = ELOrigin
    }
    public fun setPV(PV: String) {
        this.PV = PV
    }
    fun setPVOrigin(PVOrigin:String){
        this.PVOrigin = PVOrigin
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