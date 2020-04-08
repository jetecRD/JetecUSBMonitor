package com.jetec.usbmonitor.Model;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.facebook.stetho.inspector.database.ContentProviderSchema;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.itextpdf.text.pdf.draw.VerticalPositionMark;
import com.jetec.usbmonitor.Model.RoomDBHelper.Data;
import com.jetec.usbmonitor.Model.Tools.Tools;
import com.jetec.usbmonitor.R;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MakeSinglePDFReport {
    public static final String TAG = MakeSinglePDFReport.class.getSimpleName()+"My";

    private String fileName = "/USBMonitor.pdf";
    private String deviceUUID;
    private String deviceName;
    private String deviceType;
    private String tester;
    private String date;
    private String time;
    private String note;

    public MakeSinglePDFReport() {}

    /**撰寫單一頁面PDF內容*/
    public void makeSinglePDF(Activity activity, List<Data> saved, ArrayList<HashMap<String, String>> arrayList) {
        ProgressDialog dialog = ProgressDialog.show(activity,"", activity.getString(R.string.pleaseWait),true);
        dialog.setCancelable(true);
        new Thread(()->{
            try{
                deviceUUID = saved.get(0).getDeviceUUID();
                deviceName = saved.get(0).getDeviceName();
                deviceType = saved.get(0).getDeviceType();
                tester = saved.get(0).getName();
                date = saved.get(0).getDate();
                time = saved.get(0).getTime();
                note = saved.get(0).getNote();
                String mFilePath = Environment.getExternalStorageDirectory() + fileName;//決定路徑
                Document document = new Document(PageSize.A4);//設置紙張大小
                PdfWriter.getInstance(document, new FileOutputStream(mFilePath));
                //這裏開始寫內容
                document.open();
                /**===============================================================================*/
                setTitle(document);//設置標題欄位的部分(包含黑線及黑線上面)
                setSubtitle(document,"Device Information");//設置灰色底分項標題
                setDeviceInformation(document);//設置第一層的裝置資訊
                setSubtitle(document,"Device Setting & Measured Data");//設置灰色底分項標題(第二層)
                setSettingParameterInfo(activity, arrayList, document);//設置參數顯示(第二層)
                setSubtitle(document,"Measured Image");//設置灰色底分項標題(第三層)
                setImage(activity, saved, document);

                /**===============================================================================*/
                document.close();
            }catch (Exception e){
                dialog.dismiss();
                Log.w(TAG, "makeSinglePDF: "+e.toString());
                Toast.makeText(activity,"輸出失敗，請聯繫開發人員",Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        }).start();
    }

    private void setImage(Activity activity, List<Data> saved, Document document) throws IOException, DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setHorizontalAlignment(100);
        Image imageL = Image.getInstance(saved.get(0).getScreenShot());
        PdfPCell cellL = new PdfPCell(imageL,true);
        cellL.setBorder(Rectangle.NO_BORDER);
        cellL.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellL.setVerticalAlignment(Element.ALIGN_CENTER);
        cellL.setPaddingTop(10f);
        cellL.setPaddingRight(10f);
        table.addCell(cellL);

        Image imageR;
        if (saved.get(0).getTakeImage().length() != 0){
            imageR = Image.getInstance(saved.get(0).getTakeImage());
            imageR.setRotationDegrees(-90f);
        }else{
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(),R.drawable.create_image);
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,bos);
            byte[] bytes = bos.toByteArray();
            imageR = Image.getInstance(bytes);

        }
        PdfPCell cellR = new PdfPCell(imageR,true);
        cellR.setBorder(Rectangle.NO_BORDER);
        cellR.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellR.setVerticalAlignment(Element.ALIGN_CENTER);
        cellR.setPaddingTop(10f);
        cellR.setPaddingLeft(10f);
        table.addCell(cellR);

        table.setHorizontalAlignment(Element.ALIGN_CENTER);


        document.add(table);
        LineSeparator line = new LineSeparator(2f, 100, BaseColor.BLACK, Element.ALIGN_CENTER, -20f);//設定一條黑粗橫線
        document.add(line);
    }


    /**設置所量測到的數值*/
    private void setValue(ArrayList<HashMap<String, String>> arrayList, Document document) throws DocumentException, IOException {
        BaseFont chinese = BaseFont.createFont("assets/taipei.ttf"
                , BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
        Font titleChinese = new Font(chinese, 16,Font.NORMAL);
        PdfPTable table = new PdfPTable(arrayList.size());
        table.setWidthPercentage(100f);
        for (int i =0;i<arrayList.size();i++){
            String unit = Tools.Companion.setUnit(Integer.parseInt(Tools.Companion
                    .hex2Dec(arrayList.get(i).get("Origin").substring(12,14))));

            String setValue = arrayList.get(i).get("Title")+"\n"+arrayList.get(i).get("value")+unit;
            PdfPCell cell = new PdfPCell(new Paragraph(new Phrase(0f,setValue,titleChinese)));
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setPaddingBottom(5f);
            cell.setPaddingTop(5f);
            table.addCell(cell);
        }
        document.add(table);

        LineSeparator line = new LineSeparator(2f, 100, BaseColor.BLACK, Element.ALIGN_CENTER, 0f);//設定一條黑粗橫線
        document.add(line);
    }

    /**設置參數顯示(第二層)*/
    private void setSettingParameterInfo(Activity activity
            , ArrayList<HashMap<String, String>> arrayList, Document document) throws DocumentException
            , IOException {
        BaseFont chinese = BaseFont.createFont("assets/taipei.ttf"
                , BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);//設置字體
        Font titleChinese = new Font(chinese, 14,Font.NORMAL);//這是大~標題的
//                Log.d(TAG, "makeSinglePDF: "+arrayList);
        String[] setSettingTitle = {"",activity.getString(R.string.PV)+": "
                ,activity.getString(R.string.EH)+": ",activity.getString(R.string.EL)+": "};
        String[] hashIndex = {"Title","PV","EH","EL"};
        PdfPTable table = new PdfPTable(arrayList.size());
        table.setWidthPercentage(100);
        for (int i=0;i<arrayList.size();i++){
            PdfPTable table1 = new PdfPTable(1);
            for (int x=0;x<hashIndex.length;x++){
                String showContent = setSettingTitle[x]+arrayList.get(i).get(hashIndex[x]);
                if (setSettingTitle[x].length() == 0){//如果是顯示值的欄位
                    String unit = Tools.Companion.setUnit(Integer.parseInt(Tools.Companion
                            .hex2Dec(arrayList.get(i).get("Origin").substring(12,14))));
                    showContent = showContent+": "+arrayList.get(i).get("value")+unit;
                }
                PdfPCell cell = new PdfPCell(new Paragraph(new Phrase(0f,showContent,titleChinese)));
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setPaddingTop(5f);
                cell.setPaddingBottom(5f);
                table1.addCell(cell);
            }
            PdfPCell bigCell = new PdfPCell(table1);
            bigCell.setBorder(Rectangle.NO_BORDER);
            table.addCell(bigCell);
        }
        document.add(table);
        document.add(new Paragraph(" "));//空白行

    }

    /**設置第一層的裝置資訊*/
    private void setDeviceInformation(Document document) throws DocumentException, IOException {
        BaseFont chinese = BaseFont.createFont("assets/taipei.ttf"
                , BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);//設置字體
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        String[] cellContent = {"Device ID: "+deviceUUID,"Device Name: "+deviceName
                                ,"Device's Sensor Type: "+deviceType,"Tester: "+tester
                                ,"Measure Time: "+date+time,"Note: "+note};
        for (int i= 0 ;i<cellContent.length;i++){
            Font titleChinese = new Font(chinese, 14,Font.NORMAL);//這是大~標題的
            PdfPCell cell = new PdfPCell(new Paragraph(new Phrase(0f,cellContent[i],titleChinese)));
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setPaddingTop(5f);
            cell.setPaddingBottom(5f);
            table.addCell(cell);
        }
        document.add(table);
        document.add(new Paragraph(" "));//空白行
    }

    /**設置灰色底分項標題*/
    private void setSubtitle(Document document,String title) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        Font titleFont = new Font(Font.FontFamily.HELVETICA,16,Font.BOLD);
        PdfPCell titleCell =  new PdfPCell(new Paragraph(new Phrase(0f,title,titleFont)));//灰色小標題背景
        titleCell.setPaddingBottom(7f);
        titleCell.setBorder(Rectangle.NO_BORDER);//令他沒有外框
        titleCell.setBackgroundColor(new BaseColor(189,192,186));
        table.addCell(titleCell);
        document.add(table);
    }

    /**設置標題欄位的部分(包含黑線及黑線上面)*/
    private void setTitle(Document document) throws DocumentException, IOException {
        LineSeparator line = new LineSeparator(2f, 100, BaseColor.BLACK, Element.ALIGN_CENTER, 10f);//設定一條黑粗橫線
        BaseFont chinese = BaseFont.createFont("assets/helvetica.ttf"
                , BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);//設置字體
        Font titleFont = new Font(chinese, 24,Font.BOLD,new BaseColor(102,50,124));//這是大~標題的
        Font inputFont = new Font(chinese, 12,Font.BOLD,new BaseColor(102,50,124));//這是小~內容的
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date now = new Date();
        String createTime = "Create time: "+sdf.format(now);
        PdfPTable titleTable = new PdfPTable(2);
        titleTable.setWidthPercentage(100);
        PdfPCell titleCell = new PdfPCell(new Paragraph(new Phrase(0f,"USB Monitor",titleFont)));//大標題
        titleCell.setVerticalAlignment(Element.ALIGN_CENTER);
        titleCell.setBorder(Rectangle.NO_BORDER);//令他沒有外框
        titleTable.addCell(titleCell);

        PdfPCell titleTimeCell = new PdfPCell(new Paragraph(new Phrase(0f,createTime,inputFont)));//右邊建立檔案的時間
        titleTimeCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        titleTimeCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        titleTimeCell.setBorder(Rectangle.NO_BORDER);
        titleTable.addCell(titleTimeCell);
        document.add(titleTable);
        document.add(new Paragraph(" "));//空白行
        document.add(line);//畫一條線
    }

    /**設置接口令PDF的檔案名稱可輸出至外部*/
    public String getFileName() {
        return fileName;
    }

}
