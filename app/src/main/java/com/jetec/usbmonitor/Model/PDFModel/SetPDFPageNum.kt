package com.jetec.usbmonitor.Model.PDFModel

import com.itextpdf.text.*
import com.itextpdf.text.pdf.*

class SetPDFPageNum: PdfPageEventHelper {

    var presentFontSize = 13f
    var pageSize = PageSize.A4

    /**組合頁碼的部分*/
    var total: PdfTemplate? = null

    var bf: BaseFont? = null

    var fontDetail: Font? = null

    constructor(baseFont: BaseFont,presentFontSize:Int,pageSize:Rectangle){
        this.bf = baseFont
        this.presentFontSize = presentFontSize.toFloat()
        this.pageSize = pageSize
    }


    override fun onOpenDocument(writer: PdfWriter?, document: Document?) {
        super.onOpenDocument(writer, document)
        total = writer!!.directContent.createTemplate(50f, 50f)

    }

    override fun onEndPage(writer: PdfWriter?, document: Document?) {
        super.onEndPage(writer, document)
        try {
            if (fontDetail == null) {
                fontDetail = Font(bf, presentFontSize, Font.NORMAL)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val pageS = writer!!.pageNumber
        val foot1 = "$pageS  /"
        val footer = Phrase(foot1, fontDetail)
        val len = bf!!.getWidthPoint(foot1, presentFontSize)

        val cb = writer!!.directContent

        ColumnText.showTextAligned(cb,Element.ALIGN_CENTER, footer,
            (document!!.rightMargin() + document!!.right() + document!!.leftMargin() - document!!.left() - len) / 2.0f + 20f,
            document!!.bottom() - 20,
            0f)//設置頁碼

        cb.addTemplate(total,
            (document!!.rightMargin()
                    + document!!.right()
                    + document!!.leftMargin() - document!!.left()) / 2.0f + 20f,
            document!!.bottom() - 20)//調整頁碼位置
    }

    override fun onCloseDocument(writer: PdfWriter?, document: Document?) {
        super.onCloseDocument(writer, document)
        total!!.beginText()
        total!!.setFontAndSize(bf, presentFontSize)
        val foot2 = " ${(writer!!.pageNumber - 1)}"//設置頁碼的"總頁碼"的部分
        total!!.showText(foot2)
        total!!.endText()
        total!!.closePath()
    }
}