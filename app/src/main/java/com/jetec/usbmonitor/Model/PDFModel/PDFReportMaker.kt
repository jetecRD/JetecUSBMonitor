package com.jetec.usbmonitor.Model.PDFModel

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.os.StrictMode
import android.util.Log
import android.widget.Toast
import com.itextpdf.text.*
import com.itextpdf.text.pdf.*
import com.itextpdf.text.pdf.draw.LineSeparator
import com.jetec.usbmonitor.Model.RoomDBHelper.Data
import com.jetec.usbmonitor.Model.Utils.Tools.Companion.hex2Dec
import com.jetec.usbmonitor.Model.Utils.Tools.Companion.setUnit
import com.jetec.usbmonitor.R
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class PDFReportMaker {
    val TAG = PDFReportMaker::class.java.simpleName + "My"
    private var fileName: String
    private var deviceUUID: String? = null
    private var deviceName: String? = null
    private var deviceType: String? = null
    private var tester: String? = null
    private var date: String? = null
    private var time: String? = null
    private var note: String? = null
    private var screenShot: ByteArray? = null
    private var takeImage: String? = null

    constructor() {
        fileName = "/USB_Monitor.pdf"
    }

    class Header : PdfPageEventHelper() {
        private var header: Phrase? = null
        private var fileName: String? = null


        fun setHeader(header: Phrase, fileName: String) {
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
            ColumnText.showTextAligned(
                canvas,
                Element.ALIGN_RIGHT,
                Phrase("FileName: " + fileName?.replace("/", "")),
                559f,
                60f,
                0f
            )
        }

    }

    /**????????????PDF??????*/
    fun makeSinglePDF(
        activity: Activity,
        saved: List<Data>,
        arrayList: ArrayList<HashMap<String, String>>
    ) {
        val dialog =
            ProgressDialog.show(activity, "", activity.getString(R.string.pleaseWait), true)
        val sdf = SimpleDateFormat("yyyyMMddHHmm")
        val now = Date()
        fileName = "/USB_${sdf.format(now)}.pdf"
        Thread {
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
                val mFilePath =
                    Environment.getExternalStorageDirectory().toString() + fileName //????????????
                val document = Document(PageSize.A4) //??????????????????
                val writer = PdfWriter.getInstance(document, FileOutputStream(mFilePath))
                val event =
                    Header()
                writer.pageEvent = event
                document.open()
                /**===============================================================================*/
                setTitle(document) //???????????????????????????(???????????????????????????)
                setSubtitle(document, "Device Information") //???????????????????????????
                setDeviceInformation(document) //??????????????????????????????
                setSubtitle(document, "Device Setting & Measured Data") //???????????????????????????(?????????)
                setSettingParameterInfo(activity, arrayList, document) //??????????????????(?????????)
                setSubtitle(document, "Measured Image") //???????????????????????????(?????????)
                setImage(activity, document) //????????????
                val segoe = BaseFont.createFont(
                    "assets/segoe_print.ttf",
                    BaseFont.IDENTITY_H,
                    BaseFont.NOT_EMBEDDED
                )
                val footerSegoe =
                    Font(segoe, 14f, Font.NORMAL)
                event.setHeader(Phrase("Jetec Electronics Co., Ltd.", footerSegoe), fileName)
                document.newPage()
                /**===============================================================================*/
                document.close()
                dialog.dismiss()
                activity.runOnUiThread {
                    Toast.makeText(activity, "Success!", Toast.LENGTH_SHORT).show()
                    output(fileName, activity)
                }
            } catch (e: Exception) {
                dialog.dismiss()
                Log.w(TAG, "makeSinglePDF: $e")
                Toast.makeText(activity, "????????????????????????????????????", Toast.LENGTH_SHORT).show()
            }

        }.start()
    }

    /**??????????????????PDF??????*/
    fun makeMultiplePDF(activity: Activity, saved: List<Data>) {
        if (saved.isEmpty()) return
        val dialog =
            ProgressDialog.show(activity, "", activity.getString(R.string.pleaseWait), true)
        val sdf = SimpleDateFormat("yyyyMMddHHmm")
        val now = Date()
        fileName = "/All_${sdf.format(now)}.pdf"
        Thread {
            try {
                val mFilePath =
                    Environment.getExternalStorageDirectory().toString() + fileName //????????????
                val document = Document(PageSize.A4) //??????????????????
                val writer = PdfWriter.getInstance(document, FileOutputStream(mFilePath))
                val word = BaseFont.createFont(
                    "assets/helvetica.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED
                ) //????????????
                val event = SetPDFPageNum(word, 13, PageSize.A4)
                var eventLogo = Header()
                writer.pageEvent = eventLogo
                writer.pageEvent = event
                document.open()

                /**===============================================================================*/
                for (x in saved.indices) {//???????????????????????????
                    deviceUUID = saved[x].deviceUUID
                    deviceName = saved[x].deviceName
                    deviceType = saved[x].deviceType
                    tester = saved[x].name
                    date = saved[x].date
                    time = saved[x].time
                    note = saved[x].note
                    screenShot = saved[x].screenShot
                    takeImage = saved[x].takeImage
                    var jsonArray = JSONArray(saved[x].json_MySave)
                    var arrayList = ArrayList<HashMap<String, String>>()
                    for (i in 0 until jsonArray.length()) {//??????json?????????
                        val jsonObject = jsonArray.getJSONObject(i)
                        val hashMap = HashMap<String, String>()
                        val strValue = jsonObject.getString("value")
                        val strPV = jsonObject.getString("PV")
                        val strEH = jsonObject.getString("EH")
                        val strEL = jsonObject.getString("EL")
                        val strTitle = jsonObject.getString("Title")
                        val strOrigin = jsonObject.getString("Origin")
                        hashMap["value"] = strValue
                        hashMap["PV"] = strPV
                        hashMap["EH"] = strEH
                        hashMap["EL"] = strEL
                        hashMap["Title"] = strTitle
                        hashMap["Origin"] = strOrigin
                        arrayList.add(hashMap)
                    }

                    setTitle(document)
                    setSubtitle(document, "Device Information") //???????????????????????????
                    setDeviceInformation(document) //??????????????????????????????
                    setSubtitle(document, "Device Setting & Measured Data") //???????????????????????????(?????????)
                    setSettingParameterInfo(activity, arrayList, document) //??????????????????(?????????)
                    setSubtitle(document, "Measured Image") //???????????????????????????(?????????)
                    setImage(activity, document) //????????????
                    val segoe = BaseFont.createFont(
                        "assets/segoe_print.ttf",
                        BaseFont.IDENTITY_H,
                        BaseFont.NOT_EMBEDDED
                    )
                    val footerSegoe =
                        Font(segoe, 14f, Font.NORMAL)
                    eventLogo.setHeader(
                        Phrase("Jetec Electronics Co., Ltd.", footerSegoe),
                        fileName
                    )
                    document.newPage()
                }


                /**===============================================================================*/
                document.close()
                activity.runOnUiThread {
                    Toast.makeText(activity, "Success!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    output(fileName, activity)
                }
            } catch (e: Exception) {
                Log.w(TAG, "makeSinglePDF: $e")
                activity.runOnUiThread {
                    Toast.makeText(activity, "????????????????????????????????????", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }

        }.start()

    }


    /**???????????????????????????(???????????????????????????)*/
    @Throws(DocumentException::class, IOException::class)
    private fun setTitle(document: Document) {
        val line =
            LineSeparator(
                2f,
                100f,
                BaseColor.BLACK,
                Element.ALIGN_CENTER,
                10f
            ) //????????????????????????
        val chinese = BaseFont.createFont(
            "assets/helvetica.ttf"
            , BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED
        ) //????????????
        val titleFont = Font(chinese, 24f, Font.BOLD, BaseColor(102, 50, 124)) //?????????~?????????
        val inputFont = Font(chinese, 12f, Font.BOLD, BaseColor(102, 50, 124)) //?????????~?????????
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        val now = Date()
        val createTime = "Create time: " + sdf.format(now)
        val titleTable = PdfPTable(2)
        titleTable.widthPercentage = 100f
        val titleCell = PdfPCell(Paragraph(Phrase(0f, "USB Monitor", titleFont))) //?????????
        titleCell.verticalAlignment = Element.ALIGN_CENTER
        titleCell.border = Rectangle.NO_BORDER //??????????????????
        titleTable.addCell(titleCell)
        val titleTimeCell =
            PdfPCell(Paragraph(Phrase(0f, createTime, inputFont))) //???????????????????????????
        titleTimeCell.verticalAlignment = Element.ALIGN_BOTTOM
        titleTimeCell.horizontalAlignment = Element.ALIGN_RIGHT
        titleTimeCell.border = Rectangle.NO_BORDER
        titleTable.addCell(titleTimeCell)
        document.add(titleTable)
        document.add(Paragraph(" ")) //?????????
        document.add(line) //????????????
    }

    /**??????????????????????????? */
    @Throws(DocumentException::class)
    private fun setSubtitle(document: Document, title: String) {
        val table = PdfPTable(1)
        table.widthPercentage = 100f
        val titleFont = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD)
        val titleCell = PdfPCell(Paragraph(Phrase(0f, title, titleFont))) //?????????????????????
        titleCell.paddingBottom = 7f
        titleCell.border = Rectangle.NO_BORDER //??????????????????
        titleCell.backgroundColor = BaseColor(189, 192, 186)
        table.addCell(titleCell)
        document.add(table)
    }

    /**????????????*/
    @Throws(IOException::class, DocumentException::class)
    private fun setImage(activity: Activity, document: Document) {
        val table = PdfPTable(2)
        table.horizontalAlignment = 100
        val bos = ByteArrayOutputStream()
        val bitmap: Bitmap = if (screenShot!!.isNotEmpty()) {
            BitmapFactory.decodeByteArray(screenShot, 0, screenShot!!.size)
        } else BitmapFactory.decodeResource(activity.resources, R.drawable.no_image)

        bitmap.compress(Bitmap.CompressFormat.PNG, 30, bos)
        val bytes = bos.toByteArray()
        val imageL =
            Image.getInstance(bytes)
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
            ) //????????????????????????
        document.add(line)
    }


    /**??????????????????(?????????) */
    @Throws(DocumentException::class, IOException::class)
    private fun setSettingParameterInfo(
        activity: Activity,
        arrayList: ArrayList<HashMap<String, String>>,
        document: Document
    ) {
        val chinese = BaseFont.createFont(
            "assets/taipei.ttf"
            , BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED
        ) //????????????
        val titleChinese =
            Font(chinese, 14f, Font.NORMAL) //?????????~?????????
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
                if (setSettingTitle[x].isEmpty()) { //???????????????????????????
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
        document.add(Paragraph(" ")) //?????????
    }

    /**?????????????????????????????? */
    @Throws(DocumentException::class, IOException::class)
    private fun setDeviceInformation(document: Document) {
        val chinese = BaseFont.createFont(
            "assets/taipei.ttf"
            , BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED
        ) //????????????
        val table = PdfPTable(2)
        table.widthPercentage = 100f
        val cellContent = arrayOf(
            "Device ID: $deviceUUID", "Device Name: $deviceName"
            , "Device's Sensor Type: $deviceType", "Tester: $tester"
            , "Measure Time: $date $time", "Note: $note"
        )
        for (i in cellContent.indices) {
            val titleChinese =
                Font(chinese, 14f, Font.NORMAL) //?????????~?????????
            val cell = PdfPCell(Paragraph(Phrase(0f, cellContent[i], titleChinese)))
            cell.border = Rectangle.NO_BORDER
            cell.paddingTop = 5f
            cell.paddingBottom = 5f
            table.addCell(cell)
        }
        document.add(table)
        document.add(Paragraph(" ")) //?????????
    }

    /**????????????*/
    private fun output(fileName: String, activity: Activity) {
        //->??????exposed beyond app through ClipData.Item.getUri() ????????????onCreate????????????
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        builder.detectFileUriExposure()
        //->??????exposed beyond app through ClipData.Item.getUri() ????????????onCreate????????????
        val filelocation = File(Environment.getExternalStorageDirectory(), fileName)
        val path: Uri = Uri.fromFile(filelocation)
        val fileIntent = Intent(Intent.ACTION_SEND)
        fileIntent.type = "text/plain"
        fileIntent.putExtra(Intent.EXTRA_SUBJECT, "????????????")
        fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        fileIntent.putExtra(Intent.EXTRA_STREAM, path)
        activity.startActivity(Intent.createChooser(fileIntent, "Send Mail"))
    }


}