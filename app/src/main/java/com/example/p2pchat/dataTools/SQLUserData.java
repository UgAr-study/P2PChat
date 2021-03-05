package com.example.p2pchat.dataTools;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class SQLUserData {
    SQLUserDataHelper helper;
    SQLiteDatabase db;
    Cursor cursor;

    public SQLUserData(Context context, String id) {
        helper = new SQLUserDataHelper(context, id);
        db = helper.getReadableDatabase();
        cursor = db.query(SQLUserInfoHelper.TABLE_NAME, null,
                null, null, null, null, null);
        cursor.moveToLast();
    }

    public void insert(int timeStamp, String msg) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(SQLUserDataHelper.KEY_TIME, timeStamp);
        contentValues.put(SQLUserDataHelper.KEY_MSG, msg);

        SQLiteDatabase dataBase = helper.getWritableDatabase();
        dataBase.insert(SQLUserInfoHelper.TABLE_NAME, null, contentValues);
    }

    public ArrayList<Pair<Integer, String>> loadLastMsg(int numRows) {
        ArrayList<Pair<Integer, String>> res = new ArrayList<>();

        int i = 0;
        while (!cursor.isFirst()) {
            if (i == numRows) {
                break;
            }
            cursor.moveToPrevious();
            res.add(new Pair<>(cursor.getInt(cursor.getColumnIndex(SQLUserDataHelper.KEY_TIME)),
                    cursor.getString(cursor.getColumnIndex(SQLUserDataHelper.KEY_MSG))));
            i++;
        }
        return res;
    }

    public void close() {
        cursor.close();
        db.close();
        helper.close();
    }
}

class SQLUserDataHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "USERS_MSGS";
    public static String TABLE_NAME;
    public static final String KEY_ID = "_id";
    public static final String KEY_TIME = "time";
    public static final String KEY_MSG = "msg";


    public SQLUserDataHelper(@Nullable Context context, String id) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        TABLE_NAME = id + "_HISTORY";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + "(" + KEY_ID + " integer primary key," + KEY_TIME + " integer,"
                + KEY_MSG + " text," + " );");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //do nothing
    }
}
