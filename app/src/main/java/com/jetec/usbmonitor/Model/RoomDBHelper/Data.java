package com.jetec.usbmonitor.Model.RoomDBHelper;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "myRecordData")
public class Data {

    @PrimaryKey(autoGenerate = true)
    private int id;//id
    private String deviceUUID;//裝置ID
    private String deviceType;//裝置型號
    private String name;//記錄人名字
    private String json_MySave;//當下的儲存值

    private byte[] screenShot;//螢幕截圖
    private byte[] takeImage;//自己拍的
    private String note;//備註欄

    private String dateTime;//紀錄時間

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJson_MySave() {
        return json_MySave;
    }

    public void setJson_MySave(String json_MySave) {
        this.json_MySave = json_MySave;
    }

    public byte[] getScreenShot() {
        return screenShot;
    }

    public void setScreenShot(byte[] screenShot) {
        this.screenShot = screenShot;
    }

    public byte[] getTakeImage() {
        return takeImage;
    }

    public void setTakeImage(byte[] takeImage) {
        this.takeImage = takeImage;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getDeviceUUID() {
        return deviceUUID;
    }

    public void setDeviceUUID(String deviceUUID) {
        this.deviceUUID = deviceUUID;
    }
}
