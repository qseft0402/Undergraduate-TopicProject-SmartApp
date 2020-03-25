package com.example.awen.smartwatchapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by awen on 2018/2/23.
 * 不同日中的細節資料 以日期命名MyDBHelperTable資料
 */

public class MyDBHelper extends SQLiteOpenHelper {

    protected  static String TABLE_OX="table_ox";
    //protected  static String TABLE_PRESS="table_perss";
    protected  static String TABLE_PLUS="table_plus";
    protected  static String TABLE_POWER="table_power";
    protected  static String TABLE_AVG="table_avg";
    protected  static String ID="_id";
    protected  static String OX="ox";//血氧
    //protected  static String PRESS="press";//血壓
    protected  static String PLUS="plus";//脈搏
    protected  static String POWER="power";//電力
    protected  static String TIME="time";
    protected  static String OX_AVG="oxAvg";
    protected  static String PLUS_AVG="plusAvg";



    public MyDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d("SQLite","SQLiteOnCreate");
        String creatTableOX="create table "+TABLE_OX +
                " ("+ID+" integer primary key autoincrement,"+
                OX+" integer,"+
                TIME+" text)";
//        String creatTablePress="create table "+TABLE_PRESS+
//                " ("+ID+" integer primary key autoincrement,"+
//                PRESS+" integer," +
//                TIME+" text)";
        String creatTablePlus="create table "+TABLE_PLUS +
                " ("+ID+" integer primary key autoincrement,"+
                PLUS+" integer,"+
                TIME+" text)";
        String creatTablePower="create table "+TABLE_POWER +
                " ("+ID+" integer primary key autoincrement,"+
                POWER+" integer,"+
                TIME+" text)";
        String creatTableAvg="create table "+TABLE_AVG +
                " ("+ID+" integer primary key autoincrement,"+
                OX_AVG+" integer,"+
                PLUS_AVG+" integer)";


        sqLiteDatabase.execSQL(creatTableOX);
        Log.d("SQLite","OX資料表建立成功");
//        sqLiteDatabase.execSQL(creatTablePress);
//        Log.d("SQLite","PRESS資料表建立成功");
        sqLiteDatabase.execSQL(creatTablePlus);
        Log.d("SQLite","PLUS資料表建立成功");
        sqLiteDatabase.execSQL(creatTablePower);
        Log.d("SQLite","POWER資料表建立成功");
        sqLiteDatabase.execSQL(creatTableAvg);
        Log.d("SQLite","AVG資料表建立成功");

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        sqLiteDatabase.execSQL("drop table if exists "+TABLE_OX);
        sqLiteDatabase.execSQL("drop table if exists "+TABLE_POWER);
        sqLiteDatabase.execSQL("drop table if exists "+TABLE_PLUS);
        sqLiteDatabase.execSQL("drop table if exists "+TABLE_AVG);
        //sqLiteDatabase.execSQL("drop table if exists "+TABLE_PRESS);
        onCreate(sqLiteDatabase);
    }
    public int DeleteAllDate(){
       SQLiteDatabase db=getWritableDatabase();
        //db.delete(TABLE_LIST,null,null);
        db.delete(TABLE_OX,null,null);
        //db.delete(TABLE_PRESS,null,null);
        db.delete(TABLE_PLUS,null,null);
        db.delete(TABLE_POWER,null,null);
        db.delete(TABLE_AVG,null,null);

        return db.delete(TABLE_POWER,null,null);

    }
    public int DeleteAVGDate(){
        SQLiteDatabase db=getWritableDatabase();
        return db.delete(TABLE_AVG,null,null);
    }
    public void DropAllTable(SQLiteDatabase sqLiteDatabase){
        sqLiteDatabase.execSQL("drop table if exists "+TABLE_OX);
        sqLiteDatabase.execSQL("drop table if exists "+TABLE_POWER);
        sqLiteDatabase.execSQL("drop table if exists "+TABLE_PLUS);
        sqLiteDatabase.execSQL("drop table if exists "+TABLE_AVG);
        //sqLiteDatabase.execSQL("drop table if exists "+TABLE_PRESS);
        Log.d("drop","成功drop ox plus power 的 Table");
        onCreate(sqLiteDatabase);
    }
    public void RebuildAllTable(SQLiteDatabase sqLiteDatabase){
        sqLiteDatabase.execSQL("drop table if exists "+TABLE_OX);
        sqLiteDatabase.execSQL("drop table if exists "+TABLE_POWER);
        sqLiteDatabase.execSQL("drop table if exists "+TABLE_PLUS);
        sqLiteDatabase.execSQL("drop table if exists "+TABLE_AVG);
       // sqLiteDatabase.execSQL("drop table if exists "+TABLE_PRESS);
        Log.d("drop","成功drop ox plus power 的 Table");
        onCreate(sqLiteDatabase);
    }





    public long InsertData(int what,int value){
        SimpleDateFormat sdfY = new SimpleDateFormat("HH_mm");
        String currentDateandTime = sdfY.format(new Date());
        ContentValues contentValues=new ContentValues();
        SQLiteDatabase db=getWritableDatabase();
        switch (what){
            case 3:
                contentValues.put(OX,value);
                contentValues.put(TIME,currentDateandTime);
                return db.insert(TABLE_OX,null,contentValues);
//                break;
           // case 4:
//                contentValues.put(PRESS,value);
//                return db.insert(TABLE_PRESS,null,contentValues);
//                break;

            case 5:
                contentValues.put(PLUS,value);
                contentValues.put(TIME,currentDateandTime);
                return db.insert(TABLE_PLUS,null,contentValues);
//                break;
            case 6:
                contentValues.put(POWER,value);
                contentValues.put(TIME,currentDateandTime);
                return db.insert(TABLE_POWER,null,contentValues);
//                break;
            }
//        return  db.insert(TABLE,null,contentValues);
        return -1;
    }
    public long InsertAvgData(int oxAvg,int plusAvg){
        ContentValues contentValues=new ContentValues();
        SQLiteDatabase db=getWritableDatabase();
        contentValues.put(OX_AVG,oxAvg);
        contentValues.put(PLUS_AVG,plusAvg);
        return db.insert(TABLE_AVG,null,contentValues);

    }

    public Cursor[] getData(){

        SQLiteDatabase db=getWritableDatabase();
        Cursor cursorOX=db.query(TABLE_OX,new String[]{OX,TIME},
                null,null,
                null,null,ID);

//        Cursor cursorPRESS=db.query(TABLE_PRESS,new String[]{ID,PRESS,TIME},
//                null,null,
//                null,null,ID);

        Cursor cursorPlus=db.query(TABLE_PLUS,new String[]{PLUS,TIME},
                null,null,
                null,null,ID);

        Cursor cursorPower=db.query(TABLE_POWER,new String[]{POWER,TIME},
                null,null,
                null,null,ID);
        Cursor cursorAvg=db.query(TABLE_AVG,new String[]{OX_AVG,PLUS_AVG},
                null,null,
                null,null,ID);

        cursorOX.moveToFirst();
        //cursorPRESS.moveToFirst();
        cursorPlus.moveToFirst();
        cursorPower.moveToFirst();
        cursorAvg.moveToFirst();
        Cursor[] cursor=new Cursor[]{cursorOX,cursorPlus,cursorPower,cursorAvg};
        return cursor;
    }

}
