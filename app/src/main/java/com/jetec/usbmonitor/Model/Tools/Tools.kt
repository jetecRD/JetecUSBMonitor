package com.jetec.usbmonitor.Model.Tools

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.SystemClock
import com.hoho.android.usbserial.driver.*
import com.hoho.android.usbserial.util.SerialInputOutputManager
import com.jetec.usbmonitor.R
import java.math.BigInteger
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class Tools() {

    /**我將所有方法寫在這邊了
     * 以後有需要改甚麼就統一這邊改
     * 目錄:
     *   @see setUnit           判斷顯示單位
     *   @see setLabel          設置名稱標籤
     *   @see cleanStatic       清除所有靜態值
     *   @see id2String         將StringID 轉為字串
     *   @see byteArrayToHexStr 將傳過來的byteArray轉為16進字串
     *   @see ascii2String      將傳過來的byteArray轉為一般字串(ASCII)
     *   @see sendData          發送資料用的模組
     *   @see setColor          設置icon的顏色
     *   @see setSettingLabel   設置設定選項的標籤(EX:PV->補正)
     *   @see returnValue       將數值乘上小數點
     *   @see hex2Dec           將16進位轉為10進位
     *   @see toHex             將10進位轉為短整數16進位
     *   */
    companion object {

        fun fromHexString(src: String): ByteArray? {
            val biBytes =
                BigInteger("10" + src.replace("\\s".toRegex(), ""), 16).toByteArray()
            return Arrays.copyOfRange(biBytes, 1, biBytes.size)
        }

        fun toHex(t: Int): String? {
            val it = Math.round(t.toDouble() * 1).toShort()
            return String.format("%04x", it)
        }

        fun hextoDecShort(input: String): String {
            return input.toLong(16).toShort().toString()
        }

        fun hex2Dec(input: String): String {
            return input.toLong(16).toInt().toString()
        }

        fun getDecDisplay(dp: Int): DecimalFormat {
            when (dp) {
                1 -> {
                    return DecimalFormat("###0.0")
                }
                2 -> {
                    return DecimalFormat("###0.00")
                }
                3 -> {
                    return DecimalFormat("###0.000")
                }
                else->{
                    return DecimalFormat("###0")
                }

            }

        }
        fun sendValueMultiplyDP(input:Double,dp: Int):Int{
            return when (dp) {
                1 -> {
                    (input*10).toInt()
                }
                2 -> {
                    (input*100).toInt()
                }
                3 -> {
                    (input*1000).toInt()
                }
                else->{
                    (input*1).toInt()
                }

            }
        }

        /**
         * 將數值乘以小數點
         * @param dp 小數點
         * @param input 輸入字串*/
        fun returnValue(dp: Int, input: String): String {
            var d: Double = input.toDouble()
            val decimalFormat = DecimalFormat("###0.0")
            return when (dp) {
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

        fun setSettingLabel(intTag: Int, activity: Activity): String {

            when (intTag) {
                1 -> {
                    return id2String(activity, R.string.PV)
                }
                2 -> {
                    return id2String(activity, R.string.EH)
                }
                3 -> {
                    return id2String(activity, R.string.EL)
                }
                4 -> {
                    return id2String(activity, R.string.C2F)
                }
                else -> {
                    return ""
                }
            }


        }

        fun setColor(intTag: Int): Int {
            when (intTag) {
                0 -> {
                    return Color.parseColor("#24936E")
                }//
                1 -> {
                    return Color.parseColor("#24936E")
                }//
                2 -> {
                    return Color.parseColor("#24936E")
                }//
                3 -> {
                    return Color.parseColor("#24936E")
                }//
                4 -> {
                    return Color.parseColor("#24936E")
                }//
                5 -> {
                    return Color.parseColor("#24936E")
                }//
                6 -> {
                    return Color.parseColor("#24936E")
                }//
                7 -> {
                    return Color.parseColor("#24936E")
                }//
                8 -> {
                    return Color.parseColor("#24936E")
                }//
                9 -> {
                    return Color.parseColor("#CB1B45")
                }//
                10 -> {
                    return Color.parseColor("#CB1B45")
                }//
                11 -> {
                    return Color.parseColor("#0089A7")
                }//
                12 -> {
                    return Color.parseColor("#66327C")
                }//
                13 -> {
                    return Color.parseColor("#66327C")
                }//
                14 -> {
                    return Color.parseColor("#66327C")
                }//
                15 -> {
                    return Color.parseColor("#0C0C0C")
                }//
                16 -> {
                    return Color.parseColor("#0C0C0C")
                }//
                18 -> {
                    return Color.parseColor("#0089A7")
                }//
                else -> {
                    return Color.parseColor("#0C0C0C")
                }
            }

        }

        fun setIcon(intTag: Int): Int {
            when (intTag) {
                0 -> {
                    return R.drawable.noun_pressure
                }//
                1 -> {
                    return R.drawable.noun_pressure
                }//
                2 -> {
                    return R.drawable.noun_pressure
                }//
                3 -> {
                    return R.drawable.noun_pressure
                }//
                4 -> {
                    return R.drawable.noun_pressure
                }//
                5 -> {
                    return R.drawable.noun_pressure
                }//
                6 -> {
                    return R.drawable.noun_pressure
                }//
                7 -> {
                    return R.drawable.noun_pressure
                }//
                8 -> {
                    return R.drawable.noun_pressure
                }//
                9 -> {
                    return R.drawable.noun_thermometer
                }//
                10 -> {
                    return R.drawable.noun_thermometer
                }//
                11 -> {
                    return R.drawable.noun_humidity
                }//
                12 -> {
                    return R.drawable.noun_co2
                }//
                13 -> {
                    return R.drawable.noun_corsican
                }//
                14 -> {
                    return R.drawable.noun_air_pollution
                }//
                15 -> {
                    return R.drawable.noun_pressure
                }//
                16 -> {
                    return R.drawable.noun_pressure
                }//
                18 -> {
                    return R.drawable.noun_water
                }//
                else -> {
                    return R.drawable.noun_pressure
                }
            }
        }

        /**@param intTag 從byteArray解出的數字來此判斷單位*/
        fun setUnit(intTag: Int): String {

            when (intTag) {
                0 -> {
                    return "Mpa"
                }//
                1 -> {
                    return "Kpa"
                }//
                2 -> {
                    return "pa"
                }//
                3 -> {
                    return "Bar"
                }//
                4 -> {
                    return "MBar"
                }//
                5 -> {
                    return "kg/cm²"
                }//
                6 -> {
                    return "psi"
                }//
                7 -> {
                    return "mh2O"
                }//
                8 -> {
                    return "mmh2O"
                }//
                9 -> {
                    return "°C"
                }//
                10 -> {
                    return "°F"
                }//
                11 -> {
                    return "%"
                }//
                12 -> {
                    return "ppm"
                }//
                13 -> {
                    return "ppm"
                }//
                14 -> {
                    return "µg/m³"
                }//
                15 -> {
                    return "%"
                }//
                16 -> {
                    return "inch"
                }//
                18 -> {
                    return "L"
                }//
                else -> {
                    return ""
                }
            }

        }//setUnit

        /**@param intTag 從byteArray解出的數字來此判斷顯示標籤*/
        fun setLabel(intTag: Int, activity: Activity): String {

            when (intTag) {
                0 -> {
                    return id2String(activity, R.string.Pressure)
                }//
                1 -> {
                    return id2String(activity, R.string.Pressure)
                }//
                2 -> {
                    return id2String(activity, R.string.Pressure)
                }//
                3 -> {
                    return id2String(activity, R.string.Pressure)
                }//
                4 -> {
                    return id2String(activity, R.string.Pressure)
                }//
                5 -> {
                    return id2String(activity, R.string.Pressure)
                }//
                6 -> {
                    return id2String(activity, R.string.Pressure)
                }//
                7 -> {
                    return id2String(activity, R.string.Pressure)
                }//
                8 -> {
                    return id2String(activity, R.string.Pressure)
                }//
                9 -> {
                    return id2String(activity, R.string.Temp)
                }//
                10 -> {
                    return id2String(activity, R.string.Temp)
                }//
                11 -> {
                    return id2String(activity, R.string.Humidity)
                }//
                12 -> {
                    return id2String(activity, R.string.CO2)
                }//
                13 -> {
                    return id2String(activity, R.string.CO)
                }//
                14 -> {
                    return id2String(activity, R.string.PM2_5)
                }//
                15 -> {
                    return id2String(activity, R.string.Proportional)
                }//
                16 -> {
                    return id2String(activity, R.string.Level)
                }//
                18 -> {
                    return id2String(activity, R.string.Flow)
                }//
                else -> {
                    return ""
                }
            }

        }//setLabel

        fun cleanStatic() {
            MyStatus.isOurDevice = ""
            MyStatus.deviceType = ""
            MyStatus.deviceRow = 0
            MyStatus.usbType = ""
            MyStatus.engineerModel = false

        }

        /**@param activity 取得該Activity
         * @param id 字串ID*/
        fun id2String(activity: Activity, id: Int): String {
            return activity.resources.getString(id)
        }

        /**@param context 取得該Context
         * @param id 字串ID*/
        fun id2String(context: Context, id: Int): String {
            return context.resources.getString(id)
        }

        fun byteArrayToHexStr(byteArray: ByteArray?): String? {
            if (byteArray == null) {
                return null
            }
            val hex = StringBuilder(byteArray.size * 2)
            for (aData in byteArray) {
                hex.append(String.format("%02X", aData))
            }
            return hex.toString()
        }

        fun ascii2String(`in`: ByteArray): String? {
            val stringBuilder = StringBuilder(`in`.size)
            for (byteChar in `in`) stringBuilder.append(
                String.format(
                    "%02X ",
                    byteChar
                )
            )
            return String(`in`)
        }

        /**
         * 傳送String模組
         * @param sendWord 欲送出之指令
         * @param delay 延遲時間，太短會收不到值
         * @param activity 取得該Controller畫面
         * @param returnWho 決定回傳誰；0->byteArray,1->StringArray*/
        fun sendData(
            sendWord: String,
            delay: Long,
            activity: Activity,
            returnWho: Int
        ): ArrayList<String> {
            try {
                val manager: UsbManager =
                    activity.getSystemService(Context.USB_SERVICE) as UsbManager
                var deviceList: HashMap<String, UsbDevice> = manager.deviceList
                val deviceIterator: Iterator<UsbDevice> = deviceList.values.iterator()
                var customTable: ProbeTable = ProbeTable()
                var drivers: List<UsbSerialDriver>? = null
                while (deviceIterator.hasNext()) {
                    val device = deviceIterator.next()
                    when (MyStatus.usbType) {
                        "CDC" -> {
                            customTable.addProduct(
                                device.vendorId,
                                device.productId,
                                CdcAcmSerialDriver::class.java
                            )
                        }
                        "CP21" -> {
                            customTable.addProduct(
                                device.vendorId,
                                device.productId,
                                Cp21xxSerialDriver::class.java
                            )
                        }
                        "CH34" -> {
                            customTable.addProduct(
                                device.vendorId,
                                device.productId,
                                Ch34xSerialDriver::class.java
                            )
                        }
                        "FTD" -> {
                            customTable.addProduct(
                                device.vendorId,
                                device.productId,
                                FtdiSerialDriver::class.java
                            )
                        }
                        "PRO" -> {
                            customTable.addProduct(
                                device.vendorId,
                                device.productId,
                                ProlificSerialDriver::class.java
                            )
                        }
                    }
                    var prober: UsbSerialProber = UsbSerialProber(customTable)
                    drivers = prober.findAllDrivers(manager)
                }

                val connection: UsbDeviceConnection = manager.openDevice(drivers!![0].device)
                val port: UsbSerialPort = drivers[0].ports[0]

                var arrayList: ArrayList<String> = ArrayList()
                try {
                    port.open(connection)
                    port.setParameters(
                        9600,
                        8,
                        UsbSerialPort.STOPBITS_1,
                        UsbSerialPort.PARITY_NONE
                    )
                    port.write(sendWord.toByteArray(), 200)
                } catch (e: Exception) {

                }
                val mL: SerialInputOutputManager.Listener =
                    object : SerialInputOutputManager.Listener {
                        override fun onNewData(data: ByteArray) {
                            when (returnWho) {
                                0 -> {
                                    byteArrayToHexStr(data)?.let { arrayList.add(it) }
                                }
                                else -> {
                                    ascii2String(data)?.let { arrayList.add(it) }
                                }
                            }


                        }

                        override fun onRunError(e: Exception) {}
                    }
                val sL = SerialInputOutputManager(port, mL)
                val mExecutor = Executors.newSingleThreadExecutor()
                mExecutor.submit(sL)
                SystemClock.sleep(delay)

                return arrayList
            } catch (e: KotlinNullPointerException) {
                return ArrayList()
            } catch (e: Exception) {
                return ArrayList()
            }

        }//sendString

        /**
         * 傳送ByteArray模組
         * @param sendWord 欲送出之指令
         * @param delay 延遲時間，太短會收不到值
         * @param activity 取得該Controller畫面
         * @param returnWho 決定回傳誰；0->byteArray,1->StringArray*/
        fun sendData(
            sendWord: ByteArray,
            delay: Long,
            activity: Activity,
            returnWho: Int
        ): ArrayList<String> {
            try {
                val manager: UsbManager =
                    activity.getSystemService(Context.USB_SERVICE) as UsbManager
                var deviceList: HashMap<String, UsbDevice> = manager.deviceList
                val deviceIterator: Iterator<UsbDevice> = deviceList.values.iterator()
                var customTable: ProbeTable = ProbeTable()
                var drivers: List<UsbSerialDriver>? = null
                while (deviceIterator.hasNext()) {
                    val device = deviceIterator.next()
                    when (MyStatus.usbType) {
                        "CDC" -> {
                            customTable.addProduct(
                                device.vendorId,
                                device.productId,
                                CdcAcmSerialDriver::class.java
                            )
                        }
                        "CP21" -> {
                            customTable.addProduct(
                                device.vendorId,
                                device.productId,
                                Cp21xxSerialDriver::class.java
                            )
                        }
                        "CH34" -> {
                            customTable.addProduct(
                                device.vendorId,
                                device.productId,
                                Ch34xSerialDriver::class.java
                            )
                        }
                        "FTD" -> {
                            customTable.addProduct(
                                device.vendorId,
                                device.productId,
                                FtdiSerialDriver::class.java
                            )
                        }
                        "PRO" -> {
                            customTable.addProduct(
                                device.vendorId,
                                device.productId,
                                ProlificSerialDriver::class.java
                            )
                        }
                    }
                    customTable.addProduct(
                        device.vendorId,
                        device.productId,
                        CdcAcmSerialDriver::class.java
                    )
                    var prober: UsbSerialProber = UsbSerialProber(customTable)
                    drivers = prober.findAllDrivers(manager)
                }

                val connection: UsbDeviceConnection = manager.openDevice(drivers!![0].device)
                val port: UsbSerialPort = drivers[0].ports[0]

                var arrayList: ArrayList<String> = ArrayList()
                try {
                    port.open(connection)
                    port.setParameters(
                        9600,
                        8,
                        UsbSerialPort.STOPBITS_1,
                        UsbSerialPort.PARITY_NONE
                    )
                    port.write(sendWord, 200)
                } catch (e: Exception) {
                }


                val mL: SerialInputOutputManager.Listener =
                    object : SerialInputOutputManager.Listener {
                        override fun onNewData(data: ByteArray) {
                            when (returnWho) {
                                0 -> {
                                    byteArrayToHexStr(data)?.let { arrayList.add(it) }
                                }
                                else -> {
                                    ascii2String(data)?.let { arrayList.add(it) }
                                }
                            }

                        }

                        override fun onRunError(e: Exception) {}
                    }
                val sL = SerialInputOutputManager(port, mL)
                val mExecutor = Executors.newSingleThreadExecutor()
                mExecutor.submit(sL)
                SystemClock.sleep(delay)
                return arrayList
            } catch (e: KotlinNullPointerException) {
                return ArrayList()
            }

        }//sendByteArray


    }
}