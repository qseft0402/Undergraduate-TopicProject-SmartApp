package com.example.awen.smartwatchapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.LocaleDisplayNames;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ReplacementTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class HistoryActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener {
    MyDBHelper myDBHelper;
    MyDBHelperTable myDBHelperList;
    Cursor cursor;
    ListView listView;
    SimpleAdapter adapter;
    ArrayList<HashMap<String,String>> arrayList;
    String oxAvg="",plusAvg="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        setTitle("歷史清單");
        FindView();

//        String[] name=new String[]{"2018_02_14","2018_02_15","2018_02_16","2018_02_17"};
//        String[] oxAvg=new String[]{"血氧平均:98","血氧平均:97","血氧平均:97","血氧平均:99"};
//        String[] plusAvg=new String[]{"心率平均:76","心率平均:87","心率平均:81","心率平均:79"};
        reStartData();

    }

    @Override
    protected void onResume() {
        super.onResume();
        reStartData();
    }
    private void reStartData(){
        myDBHelperList=new MyDBHelperTable(this,MyDBHelperTable.TABLE_LIST,null,1);
        cursor=myDBHelperList.getData();
        Log.d("search",cursor.getCount()+" "+cursor.getPosition());

        arrayList=new ArrayList<>();
        cursor.moveToLast();//因為最舊的資料在下面 因此要從後面往前讀 才能將最新維持在list的最上面
        for(int i=0;i<cursor.getCount();i++) {
            HashMap<String,String> map=new HashMap<>();
            String[] avg=getAvg(cursor.getString(0));
            map.put(MyDBHelperTable.TIME,cursor.getString(0));
            map.put(MyDBHelper.OX_AVG,avg[0]);
            map.put(MyDBHelper.PLUS_AVG,avg[1]);
            Log.d("search",cursor.getString(0)+" 總共："+cursor.getCount());
            arrayList.add(map);
            if(cursor.moveToPrevious()){}
            else Log.d("search","cursor搜尋結束");
        }
//        for(int i=0;i<name.length;i++) {
//            HashMap<String,String> map=new HashMap<>();
//            map.put("name",name[i]);
//            map.put("oxAvg",oxAvg[i]);
//            map.put("plusAvg",plusAvg[i]);
//            arrayList.add(map);
//        }
        adapter=new SimpleAdapter(this,arrayList,
                R.layout.mylayout,
                new String[]{MyDBHelperTable.TIME,MyDBHelper.OX_AVG,MyDBHelper.PLUS_AVG},
                new int[]{R.id.historyName,R.id.historyOxAvg,R.id.historyPlusAvg});
        listView.setAdapter(adapter);
    }
    public String[] getAvg(String tableName){
        try {
            myDBHelper = new MyDBHelper(this, tableName, null, 1);
            Cursor[] cursor1 = (myDBHelper.getData());
            cursor1[3].moveToFirst();

                oxAvg = "血氧平均:" + String.valueOf(cursor1[3].getInt(0));
                plusAvg = "心率平均:" + String.valueOf(cursor1[3].getInt(1));
        } catch (CursorIndexOutOfBoundsException e) {
                e.printStackTrace();
                return new String[]{"", ""};
        }
        catch (RuntimeException e1){
            e1.printStackTrace();
        }
        return new String[]{oxAvg, plusAvg};
    }
    void FindView(){
        listView=findViewById(R.id.listHistory);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
//            case R.id.btnCleanData:
////                Log.d("myLog","已刪除 "+myDBHelper.DeleteAllDate()+"筆資料");
//
//                myDBHelper.DropAllTable(myDBHelper.getWritableDatabase());
//                myDBHelperList.RebuildAllTable(myDBHelperList.getWritableDatabase());
//                arrayList.clear();
//                adapter.notifyDataSetChanged();
//
//                break;

        }


    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        Intent intent=new Intent();
        intent.setClass(this,DataDetailedActivity.class);
        cursor.moveToPosition(cursor.getCount()-i-1);//因顯示是顛倒 因此反過來
        String tableName=cursor.getString(0);
        intent.putExtra("tableName",tableName);
        startActivity(intent);


    }

    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View v,
                                   int index, long arg3) {
        final int index1=index;

        Log.d("long clicked","pos: " + index);
        new AlertDialog.Builder(this)
                .setTitle("是否確定刪除？")
                .setCancelable(false).setPositiveButton(
                "確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d("long clicked","確定 ");
                        cursor.moveToPosition(cursor.getCount()-index1-1);//因顯示是顛倒 因此反過來
                        Log.d("long clicked",index1+" "+(cursor.getCount()-index1-1)+"");
                        String tableName=cursor.getString(0);


                        myDBHelper=new MyDBHelper(HistoryActivity.this,tableName,null,1);
                        myDBHelper.DropAllTable(myDBHelper.getWritableDatabase());
                        Log.d("long clicked",tableName+"刪除成功 ");
                        Toast.makeText(HistoryActivity.this,tableName+"刪除成功",Toast.LENGTH_LONG).show();
                        myDBHelper=null;
                        myDBHelperList.delete(myDBHelperList.getWritableDatabase(),tableName);
                        reStartData();

                    }
                }).setNegativeButton(
                "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d("long clicked","取消 ");
                    }
                }).show();
        return true;
    }
}

