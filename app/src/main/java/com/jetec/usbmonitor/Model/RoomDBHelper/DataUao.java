package com.jetec.usbmonitor.Model.RoomDBHelper;

import androidx.room.Dao;
import androidx.room.Query;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.annotation.Nullable;

@Dao
public interface DataUao {

    String tableName = "myRecordData";

    @Query("SELECT * FROM "+tableName)
    List<Data> getAllData();


    @Query("INSERT INTO "+tableName+"(deviceUUID,deviceName,deviceType,name,json_MySave,screenShot,takeImage,note,dateTime) " +
            "VALUES (:deviceUUID,:deviceName,:deviceType,:name,:json_MySave,:screenShot,:takeImage,:note,:dateTime)")
    void insertData(
            @NotNull String deviceUUID
            ,@NotNull String deviceName
            ,@NotNull String deviceType
            ,@Nullable String name
            ,@NotNull String json_MySave
            ,@NotNull byte[] screenShot
            ,@Nullable String takeImage
            ,@Nullable String note
            ,@NotNull String dateTime);

    @Query("SELECT * FROM "+tableName+" WHERE deviceUUID = :deviceUUID")
        List<Data> searchByUUID(String deviceUUID);


}