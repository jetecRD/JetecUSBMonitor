package com.jetec.usbmonitor.Model.PDFModel

import android.app.Activity
import android.app.ProgressDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.itextpdf.text.*
import com.itextpdf.text.pdf.*
import com.itextpdf.text.pdf.draw.LineSeparator
import com.jetec.usbmonitor.Model.RoomDBHelper.Data
import com.jetec.usbmonitor.Model.Tools.Tools.Companion.hex2Dec
import com.jetec.usbmonitor.Model.Tools.Tools.Companion.setUnit
import com.jetec.usbmonitor.R
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class PDFReportMaker {
    val TAG = PDFReportMaker::class.java.simpleName + "My"
    private var fileName:String
    private var deviceUUID: String? = null
    private var deviceName: String? = null
    private var deviceType: String? = null
    private var tester: String? = null
    private var date: String? = null
    private var time: String? = null
    private var note: String? = null
    private var screenShot:ByteArray? = null
    private var takeImage:String? = null

    constructor(){
        val sdf = SimpleDateFormat("yyyyMMddHHmm")
        val now = Date()
//        fileName = "/USB_" + sdf.format(now) + ".pdf"
        fileName = "/USB_Monitor.pdf"
    }

    class Header : PdfPageEventHelper() {
        private lateinit var header:Phrase
        private lateinit var fileName:String


        fun setHeader(header:Phrase,fileName:String){
            this.header = header
            this.fileName = fileName
        }

        override fun onEndPage(
            writer: PdfWriter,
            document: Document
        ) {
            val canvas = writer.directContentUnder
            ColumnText.showTextAligned(
                canvas,
                Element.ALIGN_RIGHT,
                header,
                559f,
                80f,
                0f
            )
            ColumnText.showTextAligned(canvas, Element.ALIGN_RIGHT, Phrase("FileName: " + fileName.replace("/", "")), 559f, 60f, 0f)
        }

    }
    /**設置單份PDF輸出*/
    fun  makeSinglePDF(activity:Activity,saved:List<Data>,arrayList: ArrayList<HashMap<String,String>>){
        val dialog = ProgressDialog.show(activity, "", activity.getString(R.string.pleaseWait), true)
        dialog.setCancelable(true)
        Thread{
            try {
                deviceUUID = saved[0].deviceUUID
                deviceName = saved[0].deviceName
                deviceType = saved[0].deviceType
                tester = saved[0].name
                date = saved[0].date
                time = saved[0].time
                note = saved[0].note
                screenShot = saved[0].screenShot
                takeImage = saved[0].takeImage
                val mFilePath = Environment.getExternalStorageDirectory().toString() + fileName //決定路徑
                val document = Document(PageSize.A4) //設置紙張大小
                val writer = PdfWriter.getInstance(document, FileOutputStream(mFilePath))
                val event =
                    Header()
                writer.pageEvent = event
                document.open()
                /**===============================================================================*/
                setTitle(document) //設置標題欄位的部分(包含黑線及黑線上面)
                setSubtitle(document, "Device Information") //設置灰色底分項標題
                setDeviceInformation(document) //設置第一層的裝置資訊
                setSubtitle(document, "Device Setting & Measured Data") //設置灰色底分項標題(第二層)
                setSettingParameterInfo(activity, arrayList, document) //設置參數顯示(第二層)
                setSubtitle(document, "Measured Image") //設置灰色底分項標題(第三層)
                setImage(activity, document) //放入圖片
                val segoe = BaseFont.createFont("assets/segoe_print.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED)
                val footerSegoe =
                    Font(segoe, 14f, Font.NORMAL)
                event.setHeader(Phrase("Jetec Electronics Co., Ltd.", footerSegoe),fileName)
                document.newPage()
                /**===============================================================================*/
                document.close()
                dialog.dismiss()
            }catch (e:Exception){
                dialog.dismiss()
                Log.w(TAG, "makeSinglePDF: $e")
                Toast.makeText(activity, "輸出失敗，請聯繫開發人員", Toast.LENGTH_SHORT).show()
            }

        }.start()
    }
    /**設置一次多筆PDF輸出*/
    fun makeMultiplePDF(activity:Activity,saved:List<Data>){
        if (saved.isEmpty()) return
        val dialog = ProgressDialog.show(activity, "", activity.getString(R.string.pleaseWait), true)
        dialog.setCancelable(true)
        Thread{
            try {
                val mFilePath = Environment.getExternalStorageDirectory().toString() + fileName //決定路徑
                val document = Document(PageSize.A4) //設置紙張大小
                val writer = PdfWriter.getInstance(document, FileOutputStream(mFilePath))

                val word = BaseFont.createFont(
                    "assets/helvetica.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED) //設置字體
                val event = SetPDFPageNum(word, 13, PageSize.A4)
                var eventLogo = Header()
                writer.pageEvent = eventLogo
                writer.pageEvent = event
                document.open()

                /**===============================================================================*/
                for (x in saved.indices){//這裡會決定生出幾張
                    deviceUUID = saved[x].deviceUUID
                    deviceName = saved[x].deviceName
                    deviceType = saved[x].deviceType
                    tester = saved[x].name
                    date = saved[x].date
                    time = saved[x].time
                    note = saved[x].note
                    screenShot = saved[x].screenShot
                    takeImage = saved[x].takeImage
                    setTitle(document)
                    setSubtitle(document, "Device Information") //設置灰色底分項標題
                    setDeviceInformation(document) //設置第一層的裝置資訊
                    setSubtitle(document, "Device Setting & Measured Data") //設置灰色底分項標題(第二層)
//                    setSettingParameterInfo(activity, arrayList, document) //設置參數顯示(第二層)
                    setSubtitle(document, "Measured Image") //設置灰色底分項標題(第三層)
//                    setImage(activity, document) //放入圖片
                    val segoe = BaseFont.createFont("assets/segoe_print.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED)
                    val footerSegoe =
                        Font(segoe, 14f, Font.NORMAL)
                    eventLogo.setHeader(Phrase("Jetec Electronics Co., Ltd.", footerSegoe),fileName)



                    document.newPage()
                }


                /**===============================================================================*/
                document.close()
                dialog.dismiss()
            }catch (e:Exception){
                Log.w(TAG, "makeSinglePDF: $e")
                activity.runOnUiThread{
                    Toast.makeText(activity, "輸出失敗，請聯繫開發人員", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }

        }.start()

    }


    /**設置標題欄位的部分(包含黑線及黑線上面)*/
    @Throws(DocumentException::class, IOException::class)
    private fun setTitle(document: Document) {
        val line =
            LineSeparator(
                2f,
                100f,
                BaseColor.BLACK,
                Element.ALIGN_CENTER,
                10f
            ) //設定一條黑粗橫線
        val chinese = BaseFont.createFont(
            "assets/helvetica.ttf"
            , BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED
        ) //設置字體
        val titleFont = Font(chinese, 24f, Font.BOLD, BaseColor(102, 50, 124)) //這是大~標題的
        val inputFont = Font(chinese, 12f, Font.BOLD, BaseColor(102, 50, 124)) //這是小~內容的
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        val now = Date()
        val createTime = "Create time: " + sdf.format(now)
        val titleTable = PdfPTable(2)
        titleTable.widthPercentage = 100f
        val titleCell = PdfPCell(Paragraph(Phrase(0f, "USB Monitor", titleFont))) //大標題
        titleCell.verticalAlignment = Element.ALIGN_CENTER
        titleCell.border = Rectangle.NO_BORDER //令他沒有外框
        titleTable.addCell(titleCell)
        val titleTimeCell =
            PdfPCell(Paragraph(Phrase(0f, createTime, inputFont))) //右邊建立檔案的時間
        titleTimeCell.verticalAlignment = Element.ALIGN_BOTTOM
        titleTimeCell.horizontalAlignment = Element.ALIGN_RIGHT
        titleTimeCell.border = Rectangle.NO_BORDER
        titleTable.addCell(titleTimeCell)
        document.add(titleTable)
        document.add(Paragraph(" ")) //空白行
        document.add(line) //畫一條線
    }

    /**設置灰色底分項標題 */
    @Throws(DocumentException::class)
    private fun setSubtitle(document: Document, title: String) {
        val table = PdfPTable(1)
        table.widthPercentage = 100f
        val titleFont = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD)
        val titleCell = PdfPCell(Paragraph(Phrase(0f, title, titleFont))) //灰色小標題背景
        titleCell.paddingBottom = 7f
        titleCell.border = Rectangle.NO_BORDER //令他沒有外框
        titleCell.backgroundColor = BaseColor(189, 192, 186)
        table.addCell(titleCell)
        document.add(table)
    }

    /**設置圖片*/
    @Throws(IOException::class, DocumentException::class)
    private fun setImage(activity: Activity, document: Document) {
        val table = PdfPTable(2)
        table.horizontalAlignment = 100
        val imageL =
            Image.getInstance(screenShot)
        val cellL = PdfPCell(imageL, true)
        cellL.border = Rectangle.NO_BORDER
        cellL.horizontalAlignment = Element.ALIGN_CENTER
        cellL.verticalAlignment = Element.ALIGN_CENTER
        cellL.paddingTop = 10f
        cellL.paddingRight = 10f
        table.addCell(cellL)
        val imageR: Image
        if (takeImage?.isNotEmpty()!!) {
            imageR = Image.getInstance(takeImage)
            imageR.setRotationDegrees(-90f)
        } else {
            val bos = ByteArrayOutputStream()
            val bitmap =
                BitmapFactory.decodeResource(activity.resources, R.drawable.no_image)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
            val bytes = bos.toByteArray()
            imageR = Image.getInstance(bytes)
        }
        val cellR = PdfPCell(imageR, true)
        cellR.border = Rectangle.NO_BORDER
        cellR.horizontalAlignment = Element.ALIGN_CENTER
        cellR.verticalAlignment = Element.ALIGN_CENTER
        cellR.paddingTop = 10f
        cellR.paddingLeft = 10f
        table.addCell(cellR)
        table.horizontalAlignment = Element.ALIGN_CENTER
        document.add(table)
        val line =
            LineSeparator(
                2f,
                100f,
                BaseColor.BLACK,
                Element.ALIGN_CENTER,
                -20f
            ) //設定一條黑粗橫線
        document.add(line)
    }


    /**設置所量測到的數值 */
    @Throws(DocumentException::class, IOException::class)
    private fun setValue(arrayList: ArrayList<HashMap<String, String>>, document: Document) {
        val chinese = BaseFont.createFont(
            "assets/taipei.ttf"
            , BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED
        )
        val titleChinese =
            Font(chinese, 16f, Font.NORMAL)
        val table = PdfPTable(arrayList.size)
        table.widthPercentage = 100f
        for (i in arrayList.indices) {
            val unit = setUnit(
                hex2Dec(arrayList[i]["Origin"]!!.substring(12, 14)).toInt()
            )
            val setValue =
                """
                ${arrayList[i]["Title"].toString()}
                ${arrayList[i]["value"]}$unit
                """.trimIndent()
            val cell = PdfPCell(Paragraph(Phrase(0f, setValue, titleChinese)))
            cell.border = Rectangle.NO_BORDER
            cell.paddingBottom = 5f
            cell.paddingTop = 5f
            table.addCell(cell)
        }
        document.add(table)
        val line =
            LineSeparator(
                2f,
                100f,
                BaseColor.BLACK,
                Element.ALIGN_CENTER,
                0f
            ) //設定一條黑粗橫線
        document.add(line)
    }

    /**設置參數顯示(第二層) */
    @Throws(DocumentException::class, IOException::class)
    private fun setSettingParameterInfo(activity: Activity, arrayList: ArrayList<HashMap<String, String>>, document: Document) {
        val chinese = BaseFont.createFont(
            "assets/taipei.ttf"
            , BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED
        ) //設置字體
        val titleChinese =
            Font(chinese, 14f, Font.NORMAL) //這是大~標題的
        //                Log.d(TAG, "makeSinglePDF: "+arrayList);
        val setSettingTitle = arrayOf(
            "",
            activity.getString(R.string.PV) + ": ",
            activity.getString(R.string.EH) + ": ",
            activity.getString(R.string.EL) + ": "
        )
        val hashIndex =
            arrayOf("Title", "PV", "EH", "EL")
        val table = PdfPTable(arrayList.size)
        table.widthPercentage = 100f
        for (i in arrayList.indices) {
            val table1 = PdfPTable(1)
            for (x in hashIndex.indices) {
                var showContent =
                    setSettingTitle[x] + arrayList[i][hashIndex[x]]
                if (setSettingTitle[x].isEmpty()) { //如果是顯示值的欄位
                    val unit = setUnit(
                        hex2Dec(arrayList[i]["Origin"]!!.substring(12, 14)).toInt()
                    )
                    showContent = showContent + ": " + arrayList[i]["value"] + unit
                }
                val cell = PdfPCell(Paragraph(Phrase(0f, showContent, titleChinese)))
                cell.border = Rectangle.NO_BORDER
                cell.paddingTop = 5f
                cell.paddingBottom = 5f
                table1.addCell(cell)
            }
            val bigCell = PdfPCell(table1)
            bigCell.border = Rectangle.NO_BORDER
            table.addCell(bigCell)
        }
        document.add(table)
        document.add(Paragraph(" ")) //空白行
    }

    /**設置第一層的裝置資訊 */
    @Throws(DocumentException::class, IOException::class)
    private fun setDeviceInformation(document: Document) {
        val chinese = BaseFont.createFont(
            "assets/taipei.ttf"
            , BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED
        ) //設置字體
        val table = PdfPTable(2)
        table.widthPercentage = 100f
        val cellContent = arrayOf(
            "Device ID: $deviceUUID", "Device Name: $deviceName"
            , "Device's Sensor Type: $deviceType", "Tester: $tester"
            , "Measure Time: $date$time", "Note: $note"
        )
        for (i in cellContent.indices) {
            val titleChinese =
                Font(chinese, 14f, Font.NORMAL) //這是大~標題的
            val cell = PdfPCell(Paragraph(Phrase(0f, cellContent[i], titleChinese)))
            cell.border = Rectangle.NO_BORDER
            cell.paddingTop = 5f
            cell.paddingBottom = 5f
            table.addCell(cell)
        }
        document.add(table)
        document.add(Paragraph(" ")) //空白行
    }

    /**設置接口令PDF的檔案名稱可輸出至外部 */
    fun getFileName(): String? {
        return fileName
    }


}