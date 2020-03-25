package com.example.awen.smartwatchapp;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class DataDetailedActivity extends AppCompatActivity {
    MyDBHelper myDBHelper;
    Cursor cursor[];//0 血氧 1血壓 2脈搏 3電量
    DataPoint[] Oxygen_datas;
    DataPoint[] pulse_datas;
    DataPoint[] power_datas;
    String tableName;
    DecimalFormat df;
    int sportsTime=0;
    int oxAvg,plusAVG;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_detailed);
        Oxygen_datas=new DataPoint[24];
        pulse_datas=new DataPoint[24];
        power_datas=new DataPoint[24];
        Intent intent=getIntent();
        tableName=intent.getStringExtra("tableName");
        Log.d("myLog",tableName);
        setTitle(tableName+"紀錄");

        main();

    }
    private void main(){
        df = new DecimalFormat("#.##");  //取道小數點第二位
        //DataPoint ddd = new DataPoint(25,26);

        GraphView graph = (GraphView) findViewById(R.id.graph);
        TextView text_Oxygen_max = findViewById(R.id.text_Oxygen_max);
        TextView text_Oxygen_min = findViewById(R.id.text_Oxygen_min);
        TextView text_Oxygen_avg = findViewById(R.id.text_Oxygen_avg);
        TextView text_pulse_max = findViewById(R.id.text_pulse_max);
        TextView text_pulse_min = findViewById(R.id.text_pulse_min);
        TextView text_pulse_avg = findViewById(R.id.text_pulse_avg);
        TextView tvSportTime=findViewById(R.id.tvSportTime);

        insetDatas();
        LineGraphSeries<DataPoint> Oxygen = new LineGraphSeries<DataPoint>(Oxygen_datas);
        LineGraphSeries<DataPoint> pulse = new LineGraphSeries<DataPoint>(pulse_datas);
//        LineGraphSeries<DataPoint> power = new LineGraphSeries<DataPoint>(power_datas);
        //Oxygen.appendData(ddd,false,24);  //測試
        Oxygen.setColor(Color.RED);
        Oxygen.setTitle("血氧濃度");
        pulse.setColor(Color.GREEN);
        pulse.setTitle("心跳脈搏");
//        power.setColor(Color.YELLOW);
//        power.setTitle("電量");


        graph.addSeries(Oxygen);
        graph.addSeries(pulse);
//        graph.addSeries(power);
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.BOTTOM);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setYAxisBoundsManual(true);

        graph.getViewport().setMinY(0);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxY(200);
        graph.getViewport().setMaxX(24);
        graph.getViewport().setXAxisBoundsManual(false);

        text_Oxygen_max.append((int)Oxygen.getHighestValueY()+"");
//        if(Oxygen.getLowestValueY()==0) //因測試時只有某一小時 因此最小會為0 若最小我將它設為最大
//            text_Oxygen_min.append(Oxygen.getHighestValueY()+"");
//        else
        text_Oxygen_min.append((int)Oxygen.getLowestValueY()+"");
        oxAvg=(int)getAvgValueY(Oxygen_datas);
        text_Oxygen_avg.append(oxAvg+"");

        text_pulse_max.append((int)pulse.getHighestValueY()+"");
//        if(pulse.getLowestValueY()==0)
//            text_pulse_min.append( pulse.getHighestValueY()+"");
//        else

        text_pulse_min.append((int)pulse.getLowestValueY()+"");
        plusAVG=(int)(getAvgValueY(pulse_datas));
        text_pulse_avg.append(plusAVG+"");
        insetAvgDatas();
        tvSportTime.setText("運動時間約:"+sportsTime/60+"小時"+sportsTime%60+"分鐘");
    }
    ///////////////////////////////////
    public double getAvgValueY(DataPoint[] dPoints)
    {
        double total = 0;
        int count=0;
        for (int i = 0; i < dPoints.length ; i++)
        {
            if(dPoints[i].getY()!=0)count++;
            total += dPoints[i].getY();
        }
        double avg = (total/count);
        return avg;
    }
    public void insetDatas(){

        myDBHelper=new MyDBHelper(this,tableName,null,1);
        cursor=myDBHelper.getData();
        Log.d("deteal","\t血氧\t脈搏\t時間");
        for(int i=0;i<cursor[0].getCount();i++) {
//            Log.d("myLog", "\t"+cursor[0].getInt(0) + "\t" + cursor[1].getInt(0) +"\t"+cursor[1].getString(1));
            for(int k=0;k<cursor.length;k++) cursor[k].moveToNext();
        }
        DataIntoIf1(cursor[0],Oxygen_datas,false);
        DataIntoIf1(cursor[1],pulse_datas,true);
//        DataIntoIf1(cursor[2],power_datas,false);
    }
    public void insetAvgDatas(){
        Cursor avgCursor=cursor[3];
        avgCursor.moveToFirst();
        myDBHelper.DeleteAVGDate();
        for (int i=0;i<avgCursor.getCount();i++) {
            Log.d("AVG", avgCursor.getCount() + "");
            Log.d("AVG", avgCursor.getInt(0) + " " + avgCursor.getInt(1));
            avgCursor.moveToNext();
        }
        myDBHelper.InsertAvgData(oxAvg,plusAVG);
    }

    public void DataIntoIf1(final Cursor cursor, DataPoint[] dataPoints,boolean isCount){//為了要計算脈搏跳動140以上有幾分鐘
            int tempArray[]=new int[24];
            int tempArrayTime[]=new int[24];//把每個同一個時間 例如下午1點 也就是 13點中所有資料加總放入tempArrayTime[12]中 以便計算平均
            for(int i=0;i<tempArray.length;i++){
                tempArray[i]=0;
                tempArrayTime[i]=0;
            }

            Log.d("detal","DataIntoIf1() "+cursor.getCount());

            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                int data=cursor.getInt(0);
                String time = cursor.getString(1);//
                //Log.d("detal1","time="+time);
                String[] time1 = time.split("_");
                int timeHour = Integer.parseInt(time1[0]);
                Log.d("detal1", "第" + i + " " + timeHour);

                tempArray[timeHour] += data;//一天24小時中 每個小時的加總 存入個小時中的陣列
                Log.d("detal2", tempArray[timeHour]+" "+data);
                if (isCount && data>140) sportsTime++;
                tempArrayTime[timeHour]++;
                cursor.moveToNext();
            }

            for(int i=0;i<tempArray.length;i++) {

                if (tempArrayTime[i] > 0) {
                    int avg=tempArray[i] / tempArrayTime[i];
                    Log.d("detal3", tempArray[i] + " " + tempArrayTime[i] + " avg=" +avg);
                    dataPoints[i]=new DataPoint(i,avg);
                }else
                    dataPoints[i]=new DataPoint(i,0);
            }
    }
    public void insetDatas2()
    {
        Oxygen_datas = new DataPoint[]{
                new DataPoint(1, 96),
                new DataPoint(2, 97),
                new DataPoint(3, 99),
                new DataPoint(4, 98),
                new DataPoint(5, 98),
                new DataPoint(6, 96),
                new DataPoint(7, 97),
                new DataPoint(8, 99),
                new DataPoint(9, 98),
                new DataPoint(10, 98),
                new DataPoint(11, 98),
                new DataPoint(12, 97),
                new DataPoint(13, 99),
                new DataPoint(14, 96),
                new DataPoint(15, 98),
                new DataPoint(16, 96),
                new DataPoint(17, 96),
                new DataPoint(18, 97),
                new DataPoint(19, 98),
                new DataPoint(20, 98),
                new DataPoint(21, 96),
                new DataPoint(22, 97),
                new DataPoint(23, 99),
                new DataPoint(24, 98),};

        pulse_datas = new DataPoint[]{
                new DataPoint(0, 81),
                new DataPoint(1, 78),
                new DataPoint(2, 67),
                new DataPoint(3, 59),
                new DataPoint(4, 56),
                new DataPoint(5, 55),
                new DataPoint(6, 54),
                new DataPoint(7, 57),
                new DataPoint(8, 55),
                new DataPoint(9, 80),
                new DataPoint(10, 81),
                new DataPoint(11, 87),
                new DataPoint(12, 92),
                new DataPoint(13, 82),
                new DataPoint(14, 71),
                new DataPoint(15, 79),
                new DataPoint(16, 78),
                new DataPoint(17, 156),
                new DataPoint(18, 148),
                new DataPoint(19, 94),
                new DataPoint(20, 87),
                new DataPoint(21, 89),
                new DataPoint(22, 87),
                new DataPoint(23, 81),

        };
    }
}
