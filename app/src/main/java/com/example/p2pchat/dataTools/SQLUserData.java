package com.example.p2pchat.dataTools;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;

import com.example.p2pchat.ui.chat.MessageItem;
import com.example.p2pchat.ui.chat.RecyclerViewAdapter;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class SQLUserData {
    SQLUserDataHelper helper;
    Cursor cursor;
    String userId;

    public SQLUserData(Context context, String identifier) {
        helper = new SQLUserDataHelper(context, identifier);
        userId = identifier;
        cursor = null;
    }

    public void insert(Calendar timeStamp, String msg) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(SQLUserDataHelper.KEY_TIME, timeStamp.getTime().getTime());
        contentValues.put(SQLUserDataHelper.KEY_MSG, msg);

        SQLiteDatabase dataBase = helper.getWritableDatabase();
        dataBase.insert(SQLUserInfoHelper.TABLE_NAME, null, contentValues);
        dataBase.close();
    }

    public ArrayList<MessageItem> loadLastMsg(int numRows) {
        ArrayList<MessageItem> res = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        if (cursor == null) {
            cursor = db.query(helper.TABLE_NAME, null,
                    null, null, null, null, null);
            cursor.moveToLast();
        }
        int i = 0;
        while (!cursor.isFirst() && cursor.getPosition() == 0) {
            if (i == numRows) {
                break;
            }
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(new Date(cursor.getInt(cursor.getColumnIndex(SQLUserDataHelper.KEY_TIME))));
            res.add(new MessageItem(userId, cursor.getString(cursor.getColumnIndex(SQLUserDataHelper.KEY_MSG)),
                    calendar));
            i++;
            cursor.moveToPrevious();
        }
        db.close();
        return res;
    }

    public void close() {
        cursor.close();
        helper.close();
    }
}

class SQLUserDataHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "USERS_MSGS";
    public String TABLE_NAME;
    public static final String KEY_ID = "_id";
    public static final String KEY_TIME = "time";
    public static final String KEY_MSG = "msg";


    public SQLUserDataHelper(@Nullable Context context, String id) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        TABLE_NAME = "PK" + id + "_HISTORY";
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TIME + " INTEGER,"
                + KEY_MSG + " TEXT);");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE" + TABLE_NAME + " (" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TIME + " INTEGER,"
                + KEY_MSG + " TEXT);");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //do nothing
    }
}
