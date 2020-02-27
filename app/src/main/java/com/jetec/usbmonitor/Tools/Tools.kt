package com.jetec.usbmonitor.Tools

import android.app.Activity
import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.SystemClock
import com.hoho.android.usbserial.driver.*
import com.hoho.android.usbserial.util.SerialInputOutputManager
import com.jetec.usbmonitor.R
import java.lang.Exception
import java.util.concurrent.Executors

class Tools() {

    companion object {

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
                    return id2String(activity, R.string.CO)
                }//
                13 -> {
                    return id2String(activity, R.string.CO2)
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
         * @param activity 取得該Controller畫面*/
        fun sendData(sendWord: String, delay: Long, activity: Activity): ArrayList<ByteArray> {
            try {
                val manager: UsbManager = activity.getSystemService(Context.USB_SERVICE) as UsbManager
                var deviceList: HashMap<String, UsbDevice> = manager.deviceList
                val deviceIterator: Iterator<UsbDevice> = deviceList.values.iterator()
                var customTable: ProbeTable = ProbeTable()
                var drivers: List<UsbSerialDriver>? = null
                while (deviceIterator.hasNext()) {
                    val device = deviceIterator.next()
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

                var arrayList: ArrayList<ByteArray> = ArrayList()
                try {
                    port.open(connection)
                } catch (e: Exception) {
                }
                port.setParameters(
                    9600,
                    8,
                    UsbSerialPort.STOPBITS_1,
                    UsbSerialPort.PARITY_NONE
                )
                port.write(sendWord.toByteArray(), 200)

                val mL: SerialInputOutputManager.Listener =
                    object : SerialInputOutputManager.Listener {
                        override fun onNewData(data: ByteArray) {
                            arrayList.add(data)

                        }

                        override fun onRunError(e: Exception) {}
                    }
                val sL = SerialInputOutputManager(port, mL)
                val mExecutor = Executors.newSingleThreadExecutor()
                mExecutor.submit(sL)
                SystemClock.sleep(delay)
                return arrayList
            }catch (e:KotlinNullPointerException){
                return ArrayList()
            }

        }//sendString
        /**
         * 傳送ByteArray模組
         * @param sendWord 欲送出之指令
         * @param delay 延遲時間，太短會收不到值
         * @param activity 取得該Controller畫面*/
        fun sendData(sendWord: ByteArray, delay: Long, activity: Activity): ArrayList<ByteArray> {
            try {
                val manager: UsbManager = activity.getSystemService(Context.USB_SERVICE) as UsbManager
                var deviceList: HashMap<String, UsbDevice> = manager.deviceList
                val deviceIterator: Iterator<UsbDevice> = deviceList.values.iterator()
                var customTable: ProbeTable = ProbeTable()
                var drivers: List<UsbSerialDriver>? = null
                while (deviceIterator.hasNext()) {
                    val device = deviceIterator.next()
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

                var arrayList: ArrayList<ByteArray> = ArrayList()
                try {
                    port.open(connection)
                } catch (e: Exception) {
                }
                port.setParameters(
                    9600,
                    8,
                    UsbSerialPort.STOPBITS_1,
                    UsbSerialPort.PARITY_NONE
                )
                port.write(sendWord, 200)

                val mL: SerialInputOutputManager.Listener =
                    object : SerialInputOutputManager.Listener {
                        override fun onNewData(data: ByteArray) {
                            arrayList.add(data)

                        }

                        override fun onRunError(e: Exception) {}
                    }
                val sL = SerialInputOutputManager(port, mL)
                val mExecutor = Executors.newSingleThreadExecutor()
                mExecutor.submit(sL)
                SystemClock.sleep(delay)
                return arrayList
            }catch (e:KotlinNullPointerException){
                return ArrayList()
            }

        }//sendByteArray

    }
}