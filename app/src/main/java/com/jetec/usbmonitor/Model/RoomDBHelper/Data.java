package com.jetec.usbmonitor.Model.RoomDBHelper;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "myRecordData")
public class Data {

    @PrimaryKey(autoGenerate = true)
    private int id;//id
    private String deviceUUID;//裝置ID(跟裝置跑的)
    private String deviceName;//裝置名字(跟裝置跑的)
    private String deviceType;//裝置型號(跟裝置跑的)
    private String name;//記錄人名字
    private String json_MySave;//當下的儲存值

    private byte[] screenShot;//螢幕截圖
    private String takeImage;//自己拍的(存路徑)
    private String note;//備註欄

    private String date;//紀錄日期
    private String time;//紀錄時間
    private int lockTester;//鎖定測試員

    public int getLockTester() {
        return lockTester;
    }

    public void setLockTester(int lockTester) {
        this.lockTester = lockTester;
    }



    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

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

    public String getTakeImage() {
        return takeImage;
    }

    public void setTakeImage(String takeImage) {
        this.takeImage = takeImage;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }


    public String getDeviceUUID() {
        return deviceUUID;
    }

    public void setDeviceUUID(String deviceUUID) {
        this.deviceUUID = deviceUUID;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
