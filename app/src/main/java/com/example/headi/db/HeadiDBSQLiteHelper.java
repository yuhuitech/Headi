package com.example.headi.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HeadiDBSQLiteHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "headi_database";

    public HeadiDBSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(HeadiDBContract.Pains.CREATE_TABLE);
        sqLiteDatabase.execSQL(HeadiDBContract.Diary.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + HeadiDBContract.Pains.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + HeadiDBContract.Diary.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public PainsCourserAdapter readPainsFromDB(Context context){
        SQLiteDatabase database = new HeadiDBSQLiteHelper(context).getReadableDatabase();

        String[] projection = {
                HeadiDBContract.Pains._ID,
                HeadiDBContract.Pains.COLUMN_PAIN,
        };

        Cursor cursor = database.query(
                HeadiDBContract.Pains.TABLE_NAME,         // The table to query
                projection,                               // The columns to return
                null,                            // The columns for the WHERE clause
                null,                         // The values for the WHERE clause
                null,                             // don't group the rows
                null,                              // don't filter by row groups
                null                              // don't sort
        );

        // Setup cursor adapter using cursor from last step
        return new PainsCourserAdapter(context, cursor, 0);
    }

    public DiaryCourserAdapter readDiaryFromDB(Context context){
        SQLiteDatabase database = new HeadiDBSQLiteHelper(context).getReadableDatabase();

        String[] projection = {
                HeadiDBContract.Diary._ID,
                HeadiDBContract.Diary.COLUMN_DESCRIPTION,
                HeadiDBContract.Diary.COLUMN_DURATION,
                HeadiDBContract.Diary.COLUMN_END_DATE,
                HeadiDBContract.Diary.COLUMN_PAIN,
                HeadiDBContract.Diary.COLUMN_REGION,
                HeadiDBContract.Diary.COLUMN_START_DATE,
        };

        String orderBy = HeadiDBContract.Diary.COLUMN_START_DATE + " DESC";

        Cursor cursor = database.query(
                HeadiDBContract.Diary.TABLE_NAME,         // The table to query
                projection,                               // The columns to return
                null,                            // The columns for the WHERE clause
                null,                         // The values for the WHERE clause
                null,                             // don't group the rows
                null,                              // don't filter by row groups
                orderBy                                   // sort
        );

        // Setup cursor adapter using cursor from last step
        return new DiaryCourserAdapter(context, cursor, 0);
    }

}
