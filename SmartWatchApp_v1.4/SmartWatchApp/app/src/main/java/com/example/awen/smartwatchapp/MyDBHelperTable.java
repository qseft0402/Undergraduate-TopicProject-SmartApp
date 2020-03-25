package com.example.awen.smartwatchapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by awen on 2018/2/28.
 * 用來儲存所有Data的檔名的清單
 */

public class MyDBHelperTable extends SQLiteOpenHelper {
    protected  static String TABLE_LIST="smartWatchList";
    protected  static String TIME="time";
    protected  static String ID="_id";

    public MyDBHelperTable(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
//        String creatTableList="create table "+TABLE_LIST +
//                " ("+ID+" integer primary key autoincrement,"+
//                YEAR+" text,"+MONTH+" text,"+DAY+" text,"+HOUR+" text,"+
//                MINUTE+" text,"+SECOND+" text)";

        String creatTableList="create table "+TABLE_LIST +
                " ("+ID+" integer primary key autoincrement,"+
                TIME+" text)";


        sqLiteDatabase.execSQL(creatTableList);
        Log.d("SQLite","List資料表建立成功");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table if exists "+TABLE_LIST);
        onCreate(sqLiteDatabase);
    }
    public void RebuildAllTable(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("drop table if exists " + TABLE_LIST);
        Log.d("drop","成功刪除"+TABLE_LIST+"的 Table");
        onCreate(sqLiteDatabase);
    }

    public long InsertData(String time){
        ContentValues contentValues=new ContentValues();
        SQLiteDatabase db=getWritableDatabase();
        contentValues.put(TIME,time);
        return db.insert(TABLE_LIST,null,contentValues);
    }
    public Cursor getData(){
        SQLiteDatabase db=getWritableDatabase();
        Cursor cursor=db.query(TABLE_LIST,new String[]{TIME},
                null,null,
                null,null,null);
        if(cursor!=null){
            cursor.moveToLast();
        }
        return cursor;
    }
    public void delete (SQLiteDatabase db,String whereClause){
        Cursor cursor=db.query(TABLE_LIST,new String[]{TIME},
                null,null,
                null,null,null);
        if(cursor!=null){
            cursor.moveToFirst();
        }
        for(int i=0;i<cursor.getCount();i++) {
            Log.d("myLog", cursor.getString(0) + "");
            cursor.moveToNext();
        }

        String sql="DELETE FROM "+MyDBHelperTable.TABLE_LIST +" WHERE time = '"+whereClause+"'";
        db.execSQL(sql);
        Log.d("myLog","已刪除"+whereClause);
    }
    public static boolean isExist(SQLiteDatabase db,String time){
        Cursor cursor=db.query(TABLE_LIST,new String[]{TIME},
                null,null,
                null,null,null);
        if(cursor!=null){
            cursor.moveToFirst();
        }
        for(int i=0;i<cursor.getCount();i++) {
            Log.d("myLog", cursor.getString(0) + "");
            if(cursor.getString(0).equals(time)) return true;//當有該時間 則回傳true
            cursor.moveToNext();
        }


        return false;//沒有該時間
    }

}
