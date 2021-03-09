package com.example.p2pchat.dataTools;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.p2pchat.ui.chat.MessageItem;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class SQLUserData {
    SQLUserDataHelper helper;
    Cursor cursor;
    String userId;

    public SQLUserData(Context context, String identifier) {
        helper = new SQLUserDataHelper(context, identifier);
        userId = identifier;
        cursor = null;
    }

    static public void insertByIdentifier(String identifierUser, MessageItem msgItem, Context context) {
        SQLUserData db = new SQLUserData(context, identifierUser);
        db.insert(msgItem);
    }

    public void insert(MessageItem msgItem) {
        insert(msgItem.getName(), msgItem.getTime(), msgItem.getMessage());
    }

    public void insert(String author ,Calendar timeStamp, String msg) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SQLUserDataHelper.KEY_AUTHOR, author);
        contentValues.put(SQLUserDataHelper.KEY_TIME, timeStamp.getTimeInMillis());
        contentValues.put(SQLUserDataHelper.KEY_MSG, msg);

        SQLiteDatabase dataBase = helper.getWritableDatabase();
        dataBase.insert(helper.TABLE_NAME, null, contentValues);
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
        if (cursor.getPosition() == -1) {
            db.close();
            return res;
        }
        if (cursor.isFirst()) {
            Calendar calendar = new GregorianCalendar(TimeZone.getDefault());
            calendar.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(SQLUserDataHelper.KEY_TIME)));
            calendar.setTimeZone(TimeZone.getDefault());
            res.add(new MessageItem(cursor.getString(cursor.getColumnIndex(SQLUserDataHelper.KEY_AUTHOR)),
                    cursor.getString(cursor.getColumnIndex(SQLUserDataHelper.KEY_MSG)),
                    calendar));
            db.close();
            return res;
        }
        if (cursor.getPosition() > numRows) {
            cursor.moveToPosition(cursor.getPosition() - numRows + 1);
        } else {
            cursor.moveToFirst();
        }
        int i = 0;
        while (!cursor.isAfterLast()) {
            if (i == numRows) {
                break;
            }
            Calendar calendar = new GregorianCalendar(TimeZone.getDefault());
            calendar.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(SQLUserDataHelper.KEY_TIME)));
            res.add(new MessageItem(cursor.getString(cursor.getColumnIndex(SQLUserDataHelper.KEY_AUTHOR)),
                    cursor.getString(cursor.getColumnIndex(SQLUserDataHelper.KEY_MSG)),
                    calendar));
            i++;
            cursor.moveToNext();
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
    public static final String KEY_AUTHOR = "author";
    public static final String KEY_TIME = "time";
    public static final String KEY_MSG = "msg";

    public SQLUserDataHelper(@Nullable Context context, String id) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        TABLE_NAME = "PK" + id + "_HISTORY";
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_AUTHOR + " TEXT,"
                + KEY_TIME + " INTEGER,"
                + KEY_MSG + " TEXT);");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_AUTHOR + " TEXT,"
                + KEY_TIME + " INTEGER,"
                + KEY_MSG + " TEXT);");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //do nothing
    }
}
