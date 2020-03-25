package com.example.awen.smartwatchapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Queue;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, BLEEvent, AdapterView.OnItemClickListener {
    BluetoothAdapter mbluetoothAdapter;
    BluetoothManager bluetoothManager;
    MyDBHelper myDBHelper;
    MyDBHelperTable myDBHelperList;
    String currentDateandTime;
    BLE ble=null;
    Button btnScan,btnDisconnet,btnGoToHistory,btnGoToSetting,btnDropAllData;
    TextView tvBleState,tvTargetDeviceName,tvShowOx,tvShowPlus,tvShowPower,tvShowV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("手環狀態");
        FindView();
        CheckSetting();
        if(setPermission(this)){
            Toast.makeText(this,"允許",Toast.LENGTH_LONG).show();
        }
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);



    }
    private void CheckSetting(){//先檢查是否有先設定過資料 若無跳至設定頁面
        //String data=SettingImformation.readFromSDcard();
        //Log.d("main",data);
        if(SettingImformation.isDataExist() && SettingImformation.readFromSDcard()!="") {
            Log.d("main", "存在");
        }

        else {
            Log.d("main", "不存在");
            Intent intent2=new Intent();
            intent2.setClass(MainActivity.this,SettingImformation.class);
            startActivity(intent2);
        }
    }
    private void FindView(){
        btnScan=findViewById(R.id.btnScan);
        btnScan.setOnClickListener(this);
        btnDisconnet=findViewById(R.id.btnDisconnet);
        btnDisconnet.setOnClickListener(this);
        btnGoToHistory=findViewById(R.id.btnGoToHistory);
        btnGoToHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2=new Intent();
                intent2.setClass(MainActivity.this,HistoryActivity.class);
                startActivity(intent2);
            }
        });
        btnGoToSetting=findViewById(R.id.btnSetting);
        btnGoToSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2=new Intent();
                intent2.setClass(MainActivity.this,SettingImformation.class);
                startActivity(intent2);
            }
        });
        btnDropAllData=findViewById(R.id.btnDropAllData);

        btnDropAllData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cursor cursor;
                myDBHelperList=new MyDBHelperTable(MainActivity.this,MyDBHelperTable.TABLE_LIST,null,1);
                cursor=myDBHelperList.getData();
                cursor.moveToFirst();
                String tableName;
                for (int i=0;i<cursor.getCount();i++){
                    tableName=cursor.getString(0);
                    cursor.moveToNext();
                    Log.d("myLog","刪除"+tableName);
                    myDBHelper=new MyDBHelper(MainActivity.this,tableName,null,1);
                    myDBHelper.DropAllTable(myDBHelper.getWritableDatabase());
                }
                myDBHelper=null;
                myDBHelperList.RebuildAllTable(myDBHelperList.getWritableDatabase());
            }
        });
        tvBleState=findViewById(R.id.bleState);
        tvBleState.setTextColor(Color.RED);
        tvTargetDeviceName=findViewById(R.id.tvTargetDeviceName);
        tvShowOx=findViewById(R.id.tvShowOx);
        tvShowPlus=findViewById(R.id.tvShowPlus);
        tvShowPower=findViewById(R.id.tvShowPower);
        tvShowV=findViewById(R.id.tvShowV);


    }




    private static boolean setPermission(Activity activity){
        if(ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED)
            return true;
        ActivityCompat.requestPermissions(activity,new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE},123);
        return false;
    }

    @Override//權限回結果值
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==123){
            if(grantResults!=null){
                for(int i=0;i<grantResults.length;i++){
                    if(grantResults[i]!=PackageManager.PERMISSION_GRANTED){
                        new AlertDialog.Builder(this)
                                .setTitle("必須允許存取")
                                .setCancelable(false).setPositiveButton(
                                "OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        MainActivity.this.finish();
                                    }
                                }).show();
                        return;
                    }
                }
                //startBluetooth();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    @Override//詢問藍芽要不要開啟
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if(requestCode==2){
                if(resultCode==Activity.RESULT_CANCELED){
                    new AlertDialog.Builder(this)
                            .setTitle("必須開啟藍芽")
                            .setCancelable(false).setPositiveButton(
                            "OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            }).show();
                }
                else{

                    ble=new BLE(this,this.mbluetoothAdapter,this);
                    showAlert();
                    ble.startScan(true);
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    void ReadyToScan(){
        mbluetoothAdapter=bluetoothManager.getAdapter();
        ble=new BLE(this,this.mbluetoothAdapter,this);
        showAlert();
        ble.startScan(true);

    }


    Dialog dialog,dialog1,dialog2,dialog3,dialog4;//1=alert1 2=alert2 4=alert4 個別去儲存 這樣才能和下按鈕關閉
    ArrayList<BluetoothDevice> blePeripherals=new ArrayList<>();
    ArrayList<String> peripheralName=new ArrayList<>();
    ArrayAdapter<String> adapter;

    public void showAlert(){//點選掃描後呈現出小畫面
        blePeripherals.clear();
        peripheralName.clear();
        android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(this);
        alertDialog.setCancelable(false);
        LayoutInflater inflater = getLayoutInflater();
        View convertView =  inflater.inflate(R.layout.devicelist, null);
        alertDialog.setView(convertView);
        alertDialog.setTitle("搜尋藍牙裝置");
        ListView lv = convertView.findViewById(R.id.deviceList);
        adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,peripheralName);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);
        convertView.findViewById(R.id.btnReScan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!ble.isScanning()) {
                    blePeripherals.clear();
                    peripheralName.clear();
                    adapter.notifyDataSetChanged();
                    ReadyToScan();
                }
            }
        });
        alertDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ble.startScan(false);
            }
        });
        dialog=alertDialog.show();
    }
    @Override//藉由BLE 的 callBack當掃描到時來做這邊
    public void bleDevice(BluetoothDevice blePeripheral) {
        if(blePeripheral != null
                && !blePeripherals.contains(blePeripheral)
                && blePeripheral.getName()!=null){
        blePeripherals.add(blePeripheral);
        peripheralName.add(blePeripheral.getName());
            Log.d("myLog","搜尋到:"+blePeripheral.getName());
            adapter.notifyDataSetChanged();
        }

    }
    Handler uiHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {

                case 0:
                    //ble.startScan(false);
                    tvBleState.setTextColor(getResources().getColor(R.color.red));
                    tvBleState.setText("尚未連線");
                    tvTargetDeviceName.setText("無");
                    tvShowOx.setText("0");
                    tvShowPlus.setText("0");
                    tvShowPower.setText("0");
                    tvShowOx.setTextColor(getResources().getColor(R.color.gray));
                    tvShowPlus.setTextColor(getResources().getColor(R.color.gray));
                    tvShowPower.setTextColor(getResources().getColor(R.color.gray));
                    break;
                case 1:
                    tvBleState.setText("已連線");
                    tvBleState.setTextColor(getResources().getColor(R.color.green));
                    tvTargetDeviceName.setText(targetDeviceName);
                    //showAlert1();
                    //showAlert2();
                    //showAlert3();
                    //showAlert4();
                    break;
                case 2:
                    tvBleState.setText("連線中...");
                    tvBleState.setTextColor(getResources().getColor(R.color.orange));
                    //showSongName.setText(msg.obj+"");
                    break;
                case 3://血氧
                    if (msg.arg1==254) {
                        tvShowOx.setTextColor(getResources().getColor(R.color.gray));
                        tvShowOx.setText("X");
                    }
                    else if (msg.arg1>94) {
                        tvShowOx.setTextColor(getResources().getColor(R.color.green));
                        tvShowOx.setText(msg.arg1+"");
                    }
                    else if (msg.arg1<=93){
                        tvShowOx.setTextColor(getResources().getColor(R.color.orange));
                        showAlert4(msg.arg1);
                        tvShowOx.setText(msg.arg1+"");
                    }



                    //showSongName.setText(msg.obj+"");
                    break;
//                case 4:
//                    if (msg.arg1>=100 && msg.arg1<=120)tvShowPs.setTextColor(Color.GREEN);
//                    else if(msg.arg1>=120)tvShowPs.setTextColor(getResources().getColor(R.color.orange));
//                    else tvShowPs.setTextColor(Color.RED);
//                    tvShowPs.setText(msg.arg1+"");
//                    //showSongName.setText(msg.obj+"");
//                    break;
                case 5://脈搏
                    if (msg.arg1==254) {
                        tvShowPlus.setTextColor(getResources().getColor(R.color.gray));
                        tvShowPlus.setText("X");
                    }
                    else if (msg.arg1>=65 && msg.arg1<=100) {
                        tvShowPlus.setTextColor(getResources().getColor(R.color.green));
                        tvShowPlus.setText(msg.arg1+"");
                    }
                    else if (msg.arg1 >130 ) {
                        tvShowPlus.setTextColor(getResources().getColor(R.color.red));
                        Log.d("isbool","ox="+isOxAlertShow+" plus="+isPlusAlertShow);
                        showAlert1(msg.arg1);
                        Log.d("isbool","ox="+isOxAlertShow+" plus="+isPlusAlertShow);
                        tvShowPlus.setText(msg.arg1+"");
                    }
                    else if (msg.arg1>100 || msg.arg1<50){
                        tvShowPlus.setTextColor(getResources().getColor(R.color.orange));
                        showAlert2(msg.arg1);
                        tvShowPlus.setText(msg.arg1+"");
                    }
                    else if (msg.arg1==0) {
                        tvShowPlus.setTextColor(getResources().getColor(R.color.gray));
                        tvShowPlus.setText(msg.arg1+"");
                    }

                    //showSongName.setText(msg.obj+"");
                    break;
                case 6://電量
                    int mV=msg.arg1;

                    int power=(int)((mV-31)/0.08);
                    double V=mV/10.0;
                    Log.d("power1",mV+" "+V);
                    if(power>100) power=100;
                    if (power>=70)tvShowPower.setTextColor(getResources().getColor(R.color.green));
                    else if(power>=40)tvShowPower.setTextColor(getResources().getColor(R.color.orange));
                    else tvShowPower.setTextColor(getResources().getColor(R.color.red));
                    tvShowPower.setText(power+"");
                    tvShowV.setText(V+"");
                    //showSongName.setText(msg.obj+"");
                    break;


            }
            super.handleMessage(msg);
        }
    };
    @Override
    public void isConnected(boolean connected) {
       if (connected) {
           uiHandler.sendEmptyMessage(1);
           SimpleDateFormat sdfY = new SimpleDateFormat("yyyy_MM_dd");
           currentDateandTime = sdfY.format(new Date());
           //currentDateandTime="2018_08_21";
           myDBHelper=new MyDBHelper(this,currentDateandTime,null,1);
//           myDBHelper=new MyDBHelper(this,"Data",null,1);
           myDBHelperList =new MyDBHelperTable(this,MyDBHelperTable.TABLE_LIST,null,1);
           if(!MyDBHelperTable.isExist(myDBHelperList.getWritableDatabase(),currentDateandTime)) {
               if (myDBHelperList.InsertData(currentDateandTime) > 0)
                   Log.d("time", "加入" + currentDateandTime + " 成功");
           }
           else Log.d("time",  currentDateandTime + " 已存在 加入失敗");



       }
       else uiHandler.sendEmptyMessage(0);
    }
    int OxSum=0,pulseSum=0,receiveCount=0;
    int OxCount=0,pulseCount=0;
    @Override
    public void notificationData(BluetoothGattCharacteristic characteristic,int what) {
//        byte[] value=characteristic.getValue();
        Message msg=uiHandler.obtainMessage();
        int flag = characteristic.getProperties();
        int format = -1;
        if ((flag & 0x01) != 0) {
            format = BluetoothGattCharacteristic.FORMAT_UINT16;
            Log.d("logFormat", "format UINT16.");
        } else {
            format = BluetoothGattCharacteristic.FORMAT_UINT8;
            Log.d("logFormat", "format UINT8.");
        }
        int value = characteristic.getIntValue(format, 0);
        msg.what=what;// 血氧 what=3,血壓 what=4,脈搏 what=5,電量 what=6
        msg.arg1=value;
       switch (what){
           case 3:
               OxSum+=value;
               OxCount++;
               Log.d("OxCount", "OxCount="+OxCount+" Value="+value);
               break;
           case 4:
               pulseSum+=value;
               pulseCount++;//每接收20次存入一次減少數值量
               Log.d("pulseCount", "pulseCount="+pulseCount+" Value="+value);
               break;
           case 5:
               Log.d("Power", "mV="+msg.arg1);
               break;
       }
        if(value!=254 && (OxCount==20 ||pulseCount==20) ) {
           if(OxCount==20) {
               value+=OxSum/OxCount;
               OxCount = 0;
           }
           else if (pulseCount==20) {
               value=pulseSum/pulseCount;
               pulseCount = 0;
           }
            if (myDBHelper.InsertData(what, value) > 0)
                Log.d("SQLite", "儲存成功 What:" + what + " value:" + value);
            else Log.d("SQLite", "儲存失敗 What:" + what + " value:" + value);
            Log.d("mylog", "進來做notifi");
            Log.d("mylog", value + "");
        }
        uiHandler.sendMessage(msg);
    }
    String targetDeviceName=null;
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        ble.connectDevice(blePeripherals.get(i));
        targetDeviceName=blePeripherals.get(i).getName();
        uiHandler.sendEmptyMessage(2);
        dialog.dismiss();

    }
    private static Boolean isPlusAlertShow=new Boolean(true);//設定心率是否要顯示出提醒
    private static Boolean isOxAlertShow=new Boolean(true);//設定血氧是否要顯示出提醒
    Button btnA1_1,btnA1_2,btnA1_3,btnA1_4,
            btnA2_1,btnA2_2,btnA2_3,btnA2_4,btnA2_5,
            btnA3_1,btnA3_2,btnA3_3,btnA3_4,
            btnA4_1;
    View convertView1,convertView2,convertView3,convertView4;
    public void showAlert1(int value){//當心率>130時 屬於過高 跳出提醒
        if(isPlusAlertShow) {
            final android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(this);
            alertDialog.setCancelable(true);
            LayoutInflater inflater = getLayoutInflater();
            convertView1 = inflater.inflate(R.layout.alert1, null);

            alertDialog.setView(convertView1);
            alertDialog.setTitle("心率提醒").setCancelable(false);
            TextView tvAlert1Plus = convertView1.findViewById(R.id.tvAlert1Plus);
            TextView tvAlert1PlusLV = convertView1.findViewById(R.id.tvAlert1PlusLV);
            tvAlert1Plus.setText(value + "");
            tvAlert1Plus.setTextColor(getResources().getColor(R.color.red));
            tvAlert1PlusLV.setText("過高");
            tvAlert1PlusLV.setTextColor(getResources().getColor(R.color.red));

            btnA1_1=convertView1.findViewById(R.id.btnAlert1_1);
            btnA1_1.setOnClickListener(this);
            btnA1_2=convertView1.findViewById(R.id.btnAlert1_2);
            btnA1_2.setOnClickListener(this);
            btnA1_3=convertView1.findViewById(R.id.btnAlert1_3);
            btnA1_3.setOnClickListener(this);
            btnA1_4=convertView1.findViewById(R.id.btnAlert1_4);
            btnA1_4.setOnClickListener(this);
            dialog1 = alertDialog.show();

            isPlusAlertShow=false;

        }
    }
    public void showAlert2(int value){//當心率>100時 屬於偏高 跳出提醒
        if(isPlusAlertShow) {
            android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(this);
            alertDialog.setCancelable(true);
            LayoutInflater inflater = getLayoutInflater();
             convertView2 = inflater.inflate(R.layout.alert2, null);
            alertDialog.setView(convertView2);
            TextView tvAlert2Plus = convertView2.findViewById(R.id.tvAlert2Plus);
            TextView tvAlert2PlusLV = convertView2.findViewById(R.id.tvAlert2PlusLV);
            alertDialog.setTitle("心率提醒").setCancelable(false);
            tvAlert2Plus.setText(value + "");
            tvAlert2Plus.setTextColor(getResources().getColor(R.color.orange));
            tvAlert2PlusLV.setText("偏高");
            tvAlert2PlusLV.setTextColor(getResources().getColor(R.color.orange));

            btnA2_1=convertView2.findViewById(R.id.btnAlert2_1);
            btnA2_1.setOnClickListener(this);
            btnA2_2=convertView2.findViewById(R.id.btnAlert2_2);
            btnA2_2.setOnClickListener(this);
            btnA2_3=convertView2.findViewById(R.id.btnAlert2_3);
            btnA2_3.setOnClickListener(this);
            btnA2_4=convertView2.findViewById(R.id.btnAlert2_4);
            btnA2_4.setOnClickListener(this);
            btnA2_5=convertView2.findViewById(R.id.btnAlert2_5);
            btnA2_5.setOnClickListener(this);
            isPlusAlertShow=false;
            dialog2 = alertDialog.show();
        }
    }
    public void showAlert3(){//沒有用
        Button btnA3_1,btnA3_2,btnA3_3,btnA3_4;
        android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(this);
        alertDialog.setCancelable(true);
        LayoutInflater inflater = getLayoutInflater();
         convertView3 =  inflater.inflate(R.layout.alert3, null);
        alertDialog.setView(convertView3).setCancelable(false);
        //alertDialog.setTitle("搜尋藍牙裝置");

        dialog=alertDialog.show();
    }
    public void showAlert4(int value){//當血氧低於94時跳出提醒血氧偏低
        if(isOxAlertShow) {
            android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(this);
            alertDialog.setCancelable(true);
            LayoutInflater inflater = getLayoutInflater();
            convertView4 = inflater.inflate(R.layout.alert4, null);
            alertDialog.setView(convertView4);
            TextView tvAlert4Ox = convertView4.findViewById(R.id.tvAlert4Ox);
            TextView tvAlert4OxLV = convertView4.findViewById(R.id.tvAlert4OxLV);
            alertDialog.setTitle("血氧提醒").setCancelable(false);
            tvAlert4Ox.setText(value + "");
            tvAlert4Ox.setTextColor(getResources().getColor(R.color.orange));
            tvAlert4OxLV.setText("偏低");
            tvAlert4OxLV.setTextColor(getResources().getColor(R.color.orange));

            btnA4_1=convertView4.findViewById(R.id.btnAlert4_1);
            btnA4_1.setOnClickListener(this);
            dialog4 = alertDialog.show();
            isOxAlertShow=false;
        }
    }
    class CountToSetIsShowAlert implements Runnable{
        int time,what;
        Boolean[] isAlertShow;
        CountToSetIsShowAlert(int time ,int what,Boolean[] isAlertShow){
            this.time=time;
            this.isAlertShow=isAlertShow;
            this.what=what;//what 1=plus 2=ox
        }
        @Override
        public void run() {
            try {
                Log.d("isbool",time+" "+what);

                Thread.sleep((time*60)*1000);//time*60秒*1豪秒
                if(what==1) MainActivity.isPlusAlertShow=true;
                else MainActivity.isOxAlertShow=true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View view) {
        //Toast.makeText(this,"view",Toast.LENGTH_LONG).show();
        switch (view.getId()){

            case R.id.btnScan :
                //Toast.makeText(this,"btnScan",Toast.LENGTH_LONG).show();
                mbluetoothAdapter=bluetoothManager.getAdapter();
                if(mbluetoothAdapter == null) {
                    new AlertDialog.Builder(this)
                            .setTitle("此裝置無藍芽功能")
                            .setCancelable(false).setPositiveButton(
                            "OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    MainActivity.this.finish();
                                }
                            }).show();
                }
                else if(!mbluetoothAdapter.isEnabled()){
                    Intent enableBtIntent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, 2);
                }
                else{
                    ReadyToScan();
                }
                break;
            case R.id.btnDisconnet:
                try {
                    ble.disConnect();
                }catch (NullPointerException e){
                    //尚未連線按下斷線鈕產生例外 不理會
                }
                uiHandler.sendEmptyMessage(0);
                break;
            case R.id.btnAlert1_1://what 1=plus 2=ox
                Log.d("mainBtnTest","btnAlert1_1");
                btnSendBack(1,btnA1_1);
                dialog1.cancel();
                break;
            case R.id.btnAlert1_2:
                Log.d("mainBtnTest","btnAlert1_2");
                btnSendBack(1,btnA1_2);
                dialog1.cancel();
                break;
            case R.id.btnAlert1_3:
                btnSendBack(1,btnA1_3);
                dialog1.cancel();
                break;
            case R.id.btnAlert1_4:
                btnSendBack(1,btnA1_4);
                dialog1.cancel();
                break;
            case R.id.btnAlert2_1:
                btnSendBack(2,btnA2_1);
                dialog2.cancel();
                break;
            case R.id.btnAlert2_2:
                btnSendBack(2,btnA2_2);
                dialog2.cancel();
                break;
            case R.id.btnAlert2_3:
                btnSendBack(2,btnA2_3);
                dialog2.cancel();
                break;
            case R.id.btnAlert2_4:
                btnSendBack(2,btnA2_4);
                dialog2.cancel();
                break;
            case R.id.btnAlert2_5:
                btnSendBack(2,btnA2_5);
                dialog2.cancel();
                break;
            case R.id.btnAlert4_1:
                btnSendBack(4,btnA4_1);
                dialog4.cancel();
                break;

        }
    }

    private void btnSendBack(int whatAlert,Button btnAlert){//what=1 是心率 what=2 是血氧
        Spinner spinner = null;
        Boolean[] boolList=new Boolean[]{isPlusAlertShow,isOxAlertShow};//是哪種狀況 血氧或心率 用來判斷是否要提醒 t代表可以顯示  f代表被禁止提醒
        //Log.d("mainBtnTest",spinner.toString());
        int boolWhat=1;//1=plus 2=ox
        switch (whatAlert){
            case 1:
                spinner=convertView1.findViewById(R.id.spAlert1Late);
                boolWhat=1;
                break;
            case 2:
                spinner=convertView2.findViewById(R.id.spAlert2Late);
                boolWhat=1;
                break;
            case 4:
                spinner=convertView4.findViewById(R.id.spAlert4Late);
                boolWhat=2;
                break;
        }
        String[] time=getResources().getStringArray(R.array.time);
        int LateTime=Integer.parseInt(time[spinner.getSelectedItemPosition()]);//找到該提醒的sp 要將該提醒停止多久
        Log.d("mainBtnTest","Send..."+btnAlert.getText()+" stop for "+LateTime+" mins");
        Thread countTimeThread=new Thread(new CountToSetIsShowAlert(LateTime,boolWhat,boolList));
        countTimeThread.start();
    }

}

