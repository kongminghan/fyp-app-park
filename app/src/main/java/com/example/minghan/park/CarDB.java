package com.example.minghan.park;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.minghan.park.Modal.History;

/**
 * Created by MingHan on 5/13/2017.
 */
public class CarDB extends SQLiteOpenHelper{

    public static final String dbName = "carDB";
    public static final String tblName = "record";

    public CarDB(Context context){
        super(context, dbName, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS record(record_id INTEGER PRIMARY KEY AUTOINCREMENT, carNum VARCHAR, carAmount VARCHAR, carEntDate VARCHAR, carEntTime VARCHAR, carExtDate VARCHAR, carExtTime VARCHAR, carDuration VARCHAR, carLocation VARCHAR);");
        db.execSQL("CREATE TABLE IF NOT EXISTS searchRecord(record_id INTEGER PRIMARY KEY AUTOINCREMENT, carNum VARCHAR, searchDate DATETIME);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS record");
        onCreate(db);
    }

    public Cursor getDataById(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select * from record where record_id =" +id, null);
        return cursor;
    }

    public Cursor getSearchById(String car){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select * from searchRecord where carNum ='" +car+"'", null);
        return cursor;
    }

    public void updateSearch(String car){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("UPDATE searchRecord set searchDate = datetime() where carNum = '"+car+"'");
    }

    public void insertHistory(History history){
        int row = fnTotalRow();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("carAmount", history.Payment);
        contentValues.put("carNum", history.CarNumber);
        contentValues.put("carEntDate", history.EntDate);
        contentValues.put("carEntTime", history.EntTime);
        contentValues.put("carExtDate", history.ExtDate);
        contentValues.put("carExtTime", history.ExtTime);
        contentValues.put("carDuration", history.Duration);
        contentValues.put("carLocation", history.CarLocation);
        db.insert("record", null, contentValues);
    }

    public void insertSearch(String carNum){
        int row = fnTotalRow();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("carNum", carNum);
        contentValues.put("searchDate", "datetime()");
        db.insert("searchRecord", null, contentValues);
    }

    public Cursor getAllRecord(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select * from record;", null);
        return cursor;
    }

    public Cursor getAllSearches(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select * from searchRecord order by searchDate DESC limit 3;", null);
        return cursor;
    }

    public void deleteSearch(String carNum){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM searchRecord where carNum = '"+carNum+"'");
    }

    public int fnTotalRow(){
        int row = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        row = (int) DatabaseUtils.queryNumEntries(db, tblName);
        return row;
    }
}
