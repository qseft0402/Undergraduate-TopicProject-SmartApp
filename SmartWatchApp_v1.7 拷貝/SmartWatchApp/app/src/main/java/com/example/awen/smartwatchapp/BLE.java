package com.example.awen.smartwatchapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by awen on 2017/11/22.
 */

public class BLE {
    Context context;
    BluetoothAdapter mBtAdapter;
    BLEEvent bleEvent;
    BluetoothLeScanner mLEScanner;
    ScanSettings settings;
    private ArrayList<ScanFilter> filters;
    ScanCallback mScanCallback;
    BluetoothGattCharacteristic watchOxChar;//血氧
    BluetoothGattCharacteristic watchPsChar;//血壓
    BluetoothGattCharacteristic watchPlusChar;//脈搏
    BluetoothGattCharacteristic watchPower;//板子電量
    static final String OX_CHAR_UUID = "4e38e0c4-ab04-4c5d-b54a-852900379bc1";
    static final String PS_CHAR_UUID = "4e38e0c4-ab04-4c5d-b54a-852900379bc2";
    static final String PLUS_CHAR_UUID = "4e38e0c4-ab04-4c5d-b54a-852900379bc3";
    static final String POWER_CHAR_UUID = "4e38e0c4-ab04-4c5d-b54a-852900379bc4";
    static final String WATCH_SERVICE1_UUID = "4e38e0c3-ab04-4c5d-b54a-852900379ba3";
    static final String WATCH_SERVICE2_UUID = "4e38e0c3-ab04-4c5d-b54a-852900379ba4";


    public BLE(Context context, BluetoothAdapter mBluetoothAdapter, BLEEvent bleEvent){
        this.context=context;
        this.mBtAdapter=mBluetoothAdapter;
        this.bleEvent=bleEvent;

        if (Build.VERSION.SDK_INT >= 21) {//SDK>21用這邊掃描裝置
            mLEScanner =
                    mBluetoothAdapter.getBluetoothLeScanner();
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            filters = new ArrayList<>();
            mScanCallback = new ScanCallback() { //掃描到裝置就做這個
                public void onScanResult(int callbackType, ScanResult result) {
                    Log.i("callbackType", String.valueOf(callbackType)+" 搜到藍牙裝置");
                    BluetoothDevice btDevice = result.getDevice();
                    BLE.this.bleEvent.bleDevice(btDevice);
                    //Log.i(MainActivity.TAG,"===="+result.getScanRecord());
                }
                public void onBatchScanResults(List<ScanResult> results) {
                    for (ScanResult sr : results) {
                        Log.i("ScanResult - Results", sr.toString());
                    }
                }
                public void onScanFailed(int errorCode) {
                    Log.e("Scan Failed", "Error Code: " + errorCode);
                }
            };
        }


    }
    BluetoothAdapter.LeScanCallback mLeScanCallback=new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {//掃到裝置來做這邊
            BLE.this.bleEvent.bleDevice(bluetoothDevice);
        }
    }; //舊版的callback
    boolean scanning;
    private Handler mHandler= new Handler();
    public void startScan(final boolean enable) {

        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT < 21) {
                        mBtAdapter.stopLeScan(mLeScanCallback);
                    } else {
                        mLEScanner.stopScan(mScanCallback);
                    }
                    scanning=false;
                }
            }, 15000);
            if (Build.VERSION.SDK_INT < 21) {
                mBtAdapter.startLeScan(mLeScanCallback);
                scanning=true;
            } else {
                mLEScanner.startScan(filters, settings, mScanCallback);
                scanning=true;
            }
        } else {
            if (Build.VERSION.SDK_INT < 21) {
                mBtAdapter.stopLeScan(mLeScanCallback);
            } else {
                mLEScanner.stopScan(mScanCallback);
            }
            scanning=false;
        }

    }
    public void connectDevice(BluetoothDevice peripheral) {
        if (peripheral != null) {
            peripheral.connectGatt(context, false, mGattCallback);
            Log.i("mylog", "Connected to Ble." + peripheral);
        }
    }
    BluetoothGatt peripheral;
    BluetoothGattCallback mGattCallback=new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                bleEvent.isConnected(true);
                peripheral=gatt;
                Log.d("mylog", "連線成功");

                peripheral.discoverServices();//開始撈service 若撈到執行onServicesDiscovered()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                bleEvent.isConnected(false);
                peripheral=null;
                Log.d("mylog", "斷線");
            }
            super.onConnectionStateChange(gatt, status, newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) { //若撈到service則執行
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    List<BluetoothGattService> sss = null;

                    sss = gatt.getServices();

                    for (BluetoothGattService bgs : sss) {

                        String serviceName = bgs.getUuid().toString();

                        if (serviceName.equalsIgnoreCase(WATCH_SERVICE1_UUID) || serviceName.equalsIgnoreCase(WATCH_SERVICE2_UUID)) {
                            Log.e("mylog", " onServicesDiscovered : " + serviceName + " status:" + status);
                            for (BluetoothGattCharacteristic c : bgs.getCharacteristics()) {
                                    String charName = c.getUuid().toString();

                                if (charName.equalsIgnoreCase(OX_CHAR_UUID)) {
                                    Log.i("mylog", " 血氧");
                                    watchOxChar=c;
                                }
                                else if (charName.equalsIgnoreCase(PS_CHAR_UUID)) {
                                    Log.i("mylog", " 血壓");
                                    watchPsChar=c;
                                }
                                else if (charName.equalsIgnoreCase(PLUS_CHAR_UUID)) {
                                    Log.i("mylog", " 脈搏");
                                    watchPlusChar=c;
                                }
                                else if (charName.equalsIgnoreCase(POWER_CHAR_UUID)) {
                                    Log.i("mylog", " 電量");
                                    watchPower=c;
                                }
                                Log.d("mylog", "charName " + charName);

                            }
                        }
                    }

                    setNotification();
                } else {
                    Log.d("mylog", "onServicesDiscovered received: " + status);
                }

            }
            super.onServicesDiscovered(gatt, status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            int what=0;
            if(characteristic==watchOxChar)what=3;
            else if (characteristic==watchPsChar)what=4;
            else if (characteristic==watchPlusChar)what=5;
            else if (characteristic==watchPower)what=6;
            Log.d("mylog","Ble.onChar what:"+what);
            bleEvent.notificationData(characteristic,what);
        }
    };
    public void setNotification()
    {
        boolean value1= false,value2 = false,value3=false,value4=false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            value1 = peripheral.setCharacteristicNotification(watchOxChar, true);
            value2 = peripheral.setCharacteristicNotification(watchPsChar, true);
            value3 = peripheral.setCharacteristicNotification(watchPlusChar, true);
            value4 = peripheral.setCharacteristicNotification(watchPower, true);
        }
        ////////
        Log.i("mylog","setNotification:watchOxChar:"+value1);
        Log.i("mylog","setNotification:watchPsChar:"+value2);
        Log.i("mylog","setNotification:watchPlusChar:"+value3);
        Log.i("mylog","setNotification:watchPower:"+value4);

    }
    boolean isScanning(){
            return scanning;
    }

    public void disConnect() {
        if (peripheral != null) {
            peripheral.disconnect();
            peripheral.close();
            mBtAdapter.disable();
            peripheral=null;
        }
    }
}
interface BLEEvent{
    void bleDevice(BluetoothDevice blePeripheral);
    void isConnected(boolean connected);
    void notificationData( BluetoothGattCharacteristic characteristic,int what);
}