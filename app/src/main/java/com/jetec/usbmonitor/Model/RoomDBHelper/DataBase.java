package com.jetec.usbmonitor.Model.RoomDBHelper;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Data.class}, version = 2,exportSchema = true)
public abstract class DataBase extends RoomDatabase {

    public static final String DB_NAME = "RecordData.db";
    private static volatile DataBase instance;

    public static synchronized DataBase getInstance(Context context){
        if(instance == null){
//            instance = create(context);
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    DataBase.class,DB_NAME).addMigrations(MIGRATION_1_2).build();
        }
        return instance;
    }

    private static DataBase create(final Context context){
        return Room.databaseBuilder(context,DataBase.class,DB_NAME).build();
    }
    private static final Migration MIGRATION_1_2 = new Migration(1,2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE myRecordData "
                    + " ADD COLUMN lockTester INTEGER  NOT NULL DEFAULT 0");
        }
    };

    public abstract DataUao getDataUao();
}
