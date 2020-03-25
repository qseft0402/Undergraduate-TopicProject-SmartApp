package com.example.awen.smartwatchapp;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SettingImformation extends AppCompatActivity implements View.OnClickListener {
    RadioButton radioBtnBoy,radioBtnGirl;
    EditText etName,etHight,etWeight,etAge,etPhone;
    Button btnComfirm;
    String path,data;
    File file ;
    String sex,name,hight,weight,age,phone;
    boolean non=false;//資料欄位是否為空白 t=空白 f=非空白
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_imformation);
        setTitle("個人資訊設定");
        FindView();
        getPath();
        refoundData();


    }
    void getPath(){
        //先取得sdcard目錄
        path = Environment.getExternalStorageDirectory().getPath();
        Log.d("setting",path);
        //利用File來設定目錄的名稱(myappdir)
        File dir = new File(path + "/SmartBandApp");
        //先檢查該目錄是否存在
        if (!dir.exists()){
            //若不存在則建立它
            dir.mkdir();
            Log.d("setting","SmartBandApp建立成功");
        }
        else Log.d("setting","SmartBandApp已存在");
        Log.d("setting",dir.getPath());
        file=new File(path + "/SmartBandApp/" + "SettingData.txt");
    }
    void writeFromSDcard(){

        try {
            FileOutputStream fout = new FileOutputStream(file,false);
            fout.write(data.getBytes());
            fout.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

     static String readFromSDcard(){
        String path= Environment.getExternalStorageDirectory().getPath();
        File file=new File(path + "/SmartBandApp/" + "SettingData.txt");
        StringBuilder sb = new StringBuilder();
        try {
            FileInputStream fin = new FileInputStream(file);
            byte[] data = new byte[fin.available()];
            while (fin.read(data) != -1) {
                sb.append(new String(data));
            }
            fin.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d( "setting1",sb.toString());
        return sb.toString();
    }
    static boolean isDataExist(){//判斷資料是否存在  f=不存在 t=存在
        String path= Environment.getExternalStorageDirectory().getPath();
        File file=new File(path + "/SmartBandApp/" + "SettingData.txt");
        StringBuilder sb = new StringBuilder();
        try {
            FileInputStream fin = new FileInputStream(file);
            byte[] data = new byte[fin.available()];
            while (fin.read(data) != -1) {
                sb.append(new String(data));
            }
            fin.close();
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        Log.d( "setting1",sb.toString());
        return true;
    }
    private void refoundData(){
        try {
            String data = readFromSDcard();
            String[] sTemp = data.split(" ");
            if (sTemp[0].equals("男")) radioBtnBoy.setChecked(true);
            else radioBtnGirl.setChecked(true);
            etName.setText(sTemp[1]);
            etHight.setText(sTemp[2]);
            etWeight.setText(sTemp[3]);
            etAge.setText(sTemp[4]);
            etPhone.setText(sTemp[5]);
        }catch (ArrayIndexOutOfBoundsException e){

        }
    }
    void FindView(){
        radioBtnBoy=findViewById(R.id.radioBtnBoy);
        radioBtnGirl=findViewById(R.id.radioBtnGirl);
        etName=findViewById(R.id.etName);
        etHight=findViewById(R.id.etHight);
        etWeight=findViewById(R.id.etWeight);
        etAge=findViewById(R.id.etAge);
        etPhone=findViewById(R.id.etPhone);
        btnComfirm=findViewById(R.id.btnComfirm);
        btnComfirm.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        if(v==btnComfirm){
            non=false;
            if(radioBtnBoy.isChecked())sex="男";
            else sex="女";

            if(etName.getText()+"" != "")name=etName.getText()+"";
            else non=true;
            if(etHight.getText()+"" != "")hight=etHight.getText()+"";
            else non=true;
            if(etWeight.getText()+"" != "")weight=etWeight.getText()+"";
            else non=true;
            if(etAge.getText()+"" != "")age=etAge.getText()+"";
            else non=true;
            if(etPhone.getText()+"" != "")phone=etPhone.getText()+"";
            else non=true;


            Log.d("setting",sex+" "+name+" "+hight+" "+weight+" "+age+" "+phone);
            if(non) Toast.makeText(this,"不可為空白",Toast.LENGTH_SHORT).show();
            else{
                data=sex+" "+name+" "+hight+" "+weight+" "+age+" "+phone;
                writeFromSDcard();
                Toast.makeText(this,"資料已儲存",Toast.LENGTH_SHORT).show();
                finish();

            }
        }
    }
}
