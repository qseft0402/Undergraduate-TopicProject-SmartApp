package com.example.awen.smartwatchapp;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Enter extends AppCompatActivity implements View.OnClickListener {
    MyDBHelperTable myDBHelperList;
    MyDBHelper myDBHelper;
    Cursor cursor;
    Button btnGoToNowState,btnGoToHistory,btnDropAllData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);
        setTitle("選單");
        FindView();
        myDBHelperList=new MyDBHelperTable(this,MyDBHelperTable.TABLE_LIST,null,1);

    }
    void FindView(){
        btnGoToNowState=findViewById(R.id.btnGoToNowState);
        btnGoToNowState.setOnClickListener(this);
        btnGoToHistory=findViewById(R.id.btnGoToHistory);
        btnGoToHistory.setOnClickListener(this);
        btnDropAllData=findViewById(R.id.btnDropAllData);
        btnDropAllData.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnGoToNowState:
                Intent intent=new Intent();
                intent.setClass(this,MainActivity.class);
                startActivity(intent);
                break;
            case R.id.btnGoToHistory:
                Intent intent2=new Intent();
                intent2.setClass(this,HistoryActivity.class);
                startActivity(intent2);
                break;
            case R.id.btnDropAllData:
                cursor=myDBHelperList.getData();
                cursor.moveToFirst();
                String tableName;
                for (int i=0;i<cursor.getCount();i++){
                    tableName=cursor.getString(0);
                    cursor.moveToNext();
                    Log.d("myLog","刪除"+tableName);
                    myDBHelper=new MyDBHelper(this,tableName,null,1);
                    myDBHelper.DropAllTable(myDBHelper.getWritableDatabase());

                }
                myDBHelper=null;
                myDBHelperList.RebuildAllTable(myDBHelperList.getWritableDatabase());
                break;


        }
    }
}
