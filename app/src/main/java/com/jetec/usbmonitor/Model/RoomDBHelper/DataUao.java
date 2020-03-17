package com.jetec.usbmonitor.Model.RoomDBHelper;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DataUao {

    String tableName = "myRecordData";

    @Query("SELECT * FROM "+tableName)
    List<Data> getAllData();

    @Query("INSERT INTO "+tableName+"(deviceUUID,deviceType,name,json_MySave,screenShot,takeImage,note,dateTime) " +
            "VALUES (:deviceUUID,:deviceType,:name,:json_MySave,:screenShot,:takeImage,:note,:dateTime)")
    void insertData(
            String deviceUUID
            ,String deviceType
            ,String name
            ,String json_MySave
            ,byte[] screenShot
            ,byte[] takeImage
            ,String note
            ,String dateTime);


}
