package com.example.p2pchat.dataTools;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class SQLUserInfo {
    SQLUserInfoHelper helper;

    public SQLUserInfo(Context context, String tableName) {
        helper = new SQLUserInfoHelper(context, tableName);
    }

    public void WriteDB (String name, String ip, String publicKey, String encryptAESKey) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(SQLUserInfoHelper.KEY_NAME, name);
        contentValues.put(SQLUserInfoHelper.KEY_IP_ADDRESS, ip);
        contentValues.put(SQLUserInfoHelper.KEY_PUBLIC_KEY, publicKey);
        contentValues.put(SQLUserInfoHelper.KEY_ENCRYPT_AES_KEY, encryptAESKey);

        SQLiteDatabase dataBase = helper.getWritableDatabase();
        dataBase.insert(SQLUserInfoHelper.TABLE_NAME, null, contentValues);

        helper.close();
    }

    public void WriteDB (String name, String ip, String publicKey) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(SQLUserInfoHelper.KEY_NAME, name);
        contentValues.put(SQLUserInfoHelper.KEY_IP_ADDRESS, ip);
        contentValues.put(SQLUserInfoHelper.KEY_PUBLIC_KEY, publicKey);

        SQLiteDatabase dataBase = helper.getWritableDatabase();
        dataBase.insert(SQLUserInfoHelper.TABLE_NAME, null, contentValues);

        helper.close();
    }

    public Cursor getAllData () {
        SQLiteDatabase db = helper.getReadableDatabase();
        String[] cols = new String[] { SQLUserInfoHelper.KEY_NAME };
        Cursor c = db.query(SQLUserInfoHelper.TABLE_NAME, cols, null, null, null, null, null);
        c.moveToFirst();
        return c;
    }

    private ArrayList<String> getAllRowsInColumn (String column) {
        ArrayList<String> res = new ArrayList<>();

        SQLiteDatabase db = helper.getReadableDatabase();
        String[] columns = new String[] { column };

        Cursor cursor = db.query(SQLUserInfoHelper.TABLE_NAME, columns,
                null, null, null, null, SQLUserInfoHelper.KEY_ID);

        if (cursor.getCount() == 0) {

            cursor.close();
            res.add("No suggestions");
            return res;

        } else {
            if (!cursor.moveToFirst()) {
                cursor.close();
                res.add("No strings");
                return res;
            }

            do {
                res.add(cursor.getString(cursor.getColumnIndex(column)));
            } while (cursor.moveToNext());

            cursor.close();
            return res;
        }
    }

    public ArrayList<String> getAllNames () {
        return getAllRowsInColumn(SQLUserInfoHelper.KEY_NAME);
    }

    public ArrayList<String> getAllPublicKeys () {
        return getAllRowsInColumn(SQLUserInfoHelper.KEY_PUBLIC_KEY);
    }

    public ArrayList<String> getAllIpAddresses () {
        return getAllRowsInColumn(SQLUserInfoHelper.KEY_IP_ADDRESS);
    }

    public ArrayList<String> getAllAESKeys () {
        return getAllRowsInColumn(SQLUserInfoHelper.KEY_ENCRYPT_AES_KEY);
    }

    private ArrayList<String> getCells (String Column, String row, String rowArg) {

        ArrayList<String> res = new ArrayList<>();

        SQLiteDatabase db = helper.getReadableDatabase();

        String selection = row + " = ?";
        String[] selectionArgs = new String[] { rowArg };

        Cursor cursor = db.query(SQLUserInfoHelper.TABLE_NAME,
                null, selection, selectionArgs,
                null, null, null);

        if (cursor.getCount() == 0) {
            cursor.close();
            db.close();
            return res;

        } else {

            int index;

            if ((index = cursor.getColumnIndex(Column)) == -1) {
                cursor.close();
                db.close();
                return res;

            } else {

                if (!cursor.moveToFirst()) {
                    cursor.close();
                    db.close();
                    return res;
                }

                do {
                    res.add(cursor.getString(index));
                } while (cursor.moveToNext());

                cursor.close();
                db.close();
                return res;
            }
        }
    }

    public ArrayList<String> getPublicKeyByName (String Name) {
        return getCells(SQLUserInfoHelper.KEY_PUBLIC_KEY, SQLUserInfoHelper.KEY_NAME, Name);
    }

    public ArrayList<String> getIpAddressByName (String Name) {
        return getCells(SQLUserInfoHelper.KEY_IP_ADDRESS, SQLUserInfoHelper.KEY_NAME, Name);
    }

    public ArrayList<String> getIdByName (String Name) {
        return getCells(SQLUserInfoHelper.KEY_ID, SQLUserInfoHelper.KEY_NAME, Name);
    }

    public ArrayList<String> getIdByPublicKey (String publicKey) {
        return getCells(SQLUserInfoHelper.KEY_ID, SQLUserInfoHelper.KEY_PUBLIC_KEY, publicKey);
    }

    public ArrayList<String> getPublicKeyById (String id) {
        return getCells(SQLUserInfoHelper.KEY_PUBLIC_KEY, SQLUserInfoHelper.KEY_ID, id);
    }

    public ArrayList<String> getNameById (String id) {
        return getCells(SQLUserInfoHelper.KEY_NAME, SQLUserInfoHelper.KEY_ID, id);
    }

    public ArrayList<String> getIpAddressById (String id) {
        return getCells(SQLUserInfoHelper.KEY_IP_ADDRESS, SQLUserInfoHelper.KEY_ID, id);
    }

    public ArrayList<String> getNameByPublicKey (String publicKey) {
        return getCells(SQLUserInfoHelper.KEY_NAME, SQLUserInfoHelper.KEY_PUBLIC_KEY, publicKey);
    }

    public ArrayList<String> getNameByIpAddress (String ip) {
        return getCells(SQLUserInfoHelper.KEY_NAME, SQLUserInfoHelper.KEY_IP_ADDRESS, ip);
    }

    public ArrayList<String> getPublicKeyByIpAddress (String ip) {
        return getCells(SQLUserInfoHelper.KEY_PUBLIC_KEY, SQLUserInfoHelper.KEY_IP_ADDRESS, ip);
    }


    public ArrayList<String> getAESKeyByPublicKey (String publicKey) {
        return getCells(SQLUserInfoHelper.KEY_ENCRYPT_AES_KEY, SQLUserInfoHelper.KEY_PUBLIC_KEY, publicKey);
    }

    public ArrayList<String> getAESKeyByName (String name) {
        return getCells(SQLUserInfoHelper.KEY_ENCRYPT_AES_KEY, SQLUserInfoHelper.KEY_NAME, name);
    }


    public ArrayList<String> getAESKeyByIpAddress (String ip) {
        return getCells(SQLUserInfoHelper.KEY_ENCRYPT_AES_KEY, SQLUserInfoHelper.KEY_IP_ADDRESS, ip);
    }

    public int updateAESKeyByPublicKey (String aesKey, String publicKey) {
        SQLiteDatabase db = helper.getReadableDatabase();
        ContentValues val = new ContentValues ();
        val.put (SQLUserInfoHelper.KEY_ENCRYPT_AES_KEY, aesKey);

        String selection = SQLUserInfoHelper.KEY_PUBLIC_KEY + " = ?";
        String[] selectionArgs = new String[] { publicKey };

        return db.update(SQLUserInfoHelper.TABLE_NAME, val, selection, selectionArgs);
    }

    public int updateAESKeyByIpAddress (String aesKey, String ip) {
        SQLiteDatabase db = helper.getReadableDatabase();
        ContentValues val = new ContentValues ();
        val.put (SQLUserInfoHelper.KEY_ENCRYPT_AES_KEY, aesKey);

        String selection = SQLUserInfoHelper.KEY_IP_ADDRESS + " = ?";
        String[] selectionArgs = new String[] { ip };

        return db.update(SQLUserInfoHelper.TABLE_NAME, val, selection, selectionArgs);
    }

    public int updateIpByPublicKey (String ip, String publicKey) {
        SQLiteDatabase db = helper.getReadableDatabase();
        ContentValues val = new ContentValues ();
        val.put (SQLUserInfoHelper.KEY_IP_ADDRESS, ip);

        String selection = SQLUserInfoHelper.KEY_PUBLIC_KEY + " = ?";
        String[] selectionArgs = new String[] { publicKey };

        return db.update(SQLUserInfoHelper.TABLE_NAME, val, selection, selectionArgs);
    }

    public boolean isPublicKeyInTable (String publicKey) {
        return !getIdByPublicKey(publicKey).isEmpty();
    }

    public boolean deleteInfoBySign (String row, String rowArg) {
        SQLiteDatabase db = helper.getWritableDatabase();

        String selection = row + " = ?";
        String[] selectionArgs = new String[] { rowArg };
        db.delete(SQLUserInfoHelper.TABLE_NAME, selection, selectionArgs);
        helper.close();
        return true;
    }

    public boolean deleteInfoByName (String Name) {
        return deleteInfoBySign(SQLUserInfoHelper.KEY_NAME, Name);
    }

    public boolean deleteInfoByIpAddress (String ip) {
        return deleteInfoBySign(SQLUserInfoHelper.KEY_IP_ADDRESS, ip);
    }

    public boolean deleteInfoById (String id) {
        return deleteInfoBySign(SQLUserInfoHelper.KEY_ID, id);
    }

    public boolean deleteInfoByPublicKey (String publicKey) {
        return deleteInfoBySign(SQLUserInfoHelper.KEY_PUBLIC_KEY, publicKey);
    }

}

class SQLUserInfoHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "USERS_INFO";
    //public static final String TABLE_NAME = "UsersContacts";
    public static String TABLE_NAME;
    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_IP_ADDRESS = "ipAddress";
    public static final String KEY_PUBLIC_KEY = "publicKey";
    public static final String KEY_ENCRYPT_AES_KEY = "encryptAESKey";

    public SQLUserInfoHelper(@Nullable Context context, String tableName) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        TABLE_NAME = tableName;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + "(" + KEY_ID + " integer primary key," + KEY_NAME + " text," +
                KEY_IP_ADDRESS + " text," + KEY_PUBLIC_KEY + " text," + KEY_ENCRYPT_AES_KEY + " text);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        //do nothing
    }
}