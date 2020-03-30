package com.jetec.usbmonitor.Model.RoomDBHelper;

import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Query;

import com.google.gson.internal.$Gson$Preconditions;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

@Dao
public interface DataUao {

    String tableName = "myRecordData";

    /**
     * 顯示全部資料
     */
    @Query("SELECT * FROM " + tableName)
    List<Data> getAllData();

    /**
     * 加入資料
     */
    @Query("INSERT INTO " + tableName + "(deviceUUID,deviceName,deviceType,name,json_MySave,screenShot,takeImage,note,date,time) " +
            "VALUES (:deviceUUID,:deviceName,:deviceType,:name,:json_MySave,:screenShot,:takeImage,:note,:date,:time)")
    void insertData(
            @NotNull String deviceUUID
            , @NotNull String deviceName
            , @NotNull String deviceType
            , @Nullable String name
            , @NotNull String json_MySave
            , @NotNull byte[] screenShot
            , @Nullable String takeImage
            , @Nullable String note
            , @NotNull String date
            , @NotNull String time);

    /**
     * 以UUID搜尋資料
     */
    @Query("SELECT * FROM " + tableName + " WHERE deviceUUID = :deviceUUID")
    List<Data> searchByUUID(String deviceUUID);

    /**
     * 以裝置名稱搜尋資料
     */
    @Query("SELECT * FROM " + tableName + " WHERE deviceName = :deviceName")
    List<Data> searchByDeviceName(String deviceName);

    /**以測試者(Tester)搜尋資料*/
    @Query("SELECT * FROM " + tableName + " WHERE name = :tester" )
    List<Data> searchByTester (String tester);

    /**以日期(Date)搜尋資料*/
    @Query("SELECT * FROM "+tableName+" WHERE date = :date ")
    List<Data> searchByTimeDate(String date);

    /**
     * 以ID刪除
     */
    @Query("DELETE  FROM " + tableName + " WHERE id = :id")
    void deleteByID(int id);

    /**
     * 以ID搜尋
     */
    @Query("SELECT * FROM " + tableName + " WHERE id = :id")
    List<Data> searchById(int id);



    /**
     * 寫入更新
     */
    @Query("UPDATE " + tableName + " SET name=:name ,takeImage=:takeImage ,note = :note WHERE id = :id")
    void updateData(
            @NotNull int id,
            @Nullable String name,
            @Nullable String takeImage,
            @Nullable String note);

}
