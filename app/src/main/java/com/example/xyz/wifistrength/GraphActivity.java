package com.example.xyz.wifistrength;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Calendar;

public class GraphActivity extends AppCompatActivity {
    WifiManager wifiManager;
    WifiInfo wifiInfo;
    GraphView graph;
    LineGraphSeries<DataPoint> series;
    DataPoint[] dataPoints=new DataPoint[20];
    Button b;
    ProgressBar progressBar;
    long millis;
    int counter=0;
    File contentFile,file;
    OutputStream outputStream;
    InputStream inputStream;
    TextView titleText,contentText;
    String content="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(GraphActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        file=new File(getExternalFilesDir(null),"WifiStrength");
        contentFile=new File(file,"info.txt");
        if(!file.exists()){
            file.mkdir();
            try {
                contentFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(!contentFile.exists()){
            try {
                contentFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            outputStream=new FileOutputStream(contentFile,true);
            inputStream=new FileInputStream(contentFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        wifiManager=(WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        graph=findViewById(R.id.graph);
        titleText=findViewById(R.id.fileDisplayTitle);
        contentText=findViewById(R.id.fileContentsView);
        progressBar=findViewById(R.id.progressBar);
        b=findViewById(R.id.reverifyButton);

        progressBar.setMax(20);
        progressBar.setVisibility(View.INVISIBLE);
        if(TextUtils.isEmpty(contentText.getText()))
            contentText.setText("Refresh to see contents");

        LegendRenderer legendRenderer=graph.getLegendRenderer();
        legendRenderer.setFixedPosition(600,0);
        legendRenderer.setMargin(0);
        legendRenderer.setTextSize(30);
        legendRenderer.setBackgroundColor(0);
        legendRenderer.setVisible(true);
        GridLabelRenderer gridLabel=graph.getGridLabelRenderer();
        gridLabel.setHorizontalAxisTitle("Counts");
        gridLabel.setVerticalAxisTitle("Strength");
        graph.setTitle("Signal Strength");
        Viewport viewport=graph.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMinX(0);
        viewport.setMaxY(100);
        viewport.setMaxX(25);
        viewport.setScalable(true);
        viewport.setScalableY(true);
        viewport.setScrollableY(true);
        viewport.setScrollable(true);
}
    public void onClickVerify(View view){
        wifiInfo=wifiManager.getConnectionInfo();
        if(wifiInfo.getSupplicantState()== SupplicantState.COMPLETED) {
            progressBar.setProgress(0);
            progressBar.setVisibility(View.VISIBLE);
            b.setEnabled(false);
            b.setText("Checking Signal!!");
            content="";

            new Thread() {
                public void run() {
                    Calendar time=Calendar.getInstance();
                    millis = time.getTimeInMillis();
                    try {
                        counter = 0;
                        String text="At "+Calendar.getInstance().getTime()+"\n************\nIP address:"+wifiInfo.getIpAddress()+"\n*********\n";
                        outputStream.write(text.getBytes());
                        while (counter < 20) {
                            Thread.sleep(1000);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    wifiInfo = wifiManager.getConnectionInfo();
                                    try {
                                        addEntry(wifiInfo, counter);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    counter++;
                                }
                            });
                        }
                        text="\n\n\n\n";
                        outputStream.write(text.getBytes());
                        series = new LineGraphSeries<>(dataPoints);
                        series.setAnimated(true);
                        series.setColor(Color.rgb(getRandomColor(), getRandomColor(), getRandomColor()));
                        int hrs,mins,secs;
                        hrs=time.get(Calendar.HOUR_OF_DAY);
                        mins=time.get(Calendar.MINUTE);
                        secs=time.get(Calendar.SECOND);
                        series.setTitle(String.valueOf(hrs) + "hrs " + String.valueOf(mins) + "mins"+ String.valueOf(secs) + "secs");
                        series.setOnDataPointTapListener(new OnDataPointTapListener() {
                            int h1,m1,s1;

                            @Override
                            public void onTap(Series series, DataPointInterface dataPoint) {
                                Calendar temp=Calendar.getInstance();
                                temp.setTimeInMillis(millis+(int)(dataPoint.getX()*1000));
                                int h1=temp.get(Calendar.HOUR_OF_DAY);
                                int m1=temp.get(Calendar.MINUTE);
                                int s1=temp.get(Calendar.SECOND);
                                Toast.makeText(GraphActivity.this, "At " +h1+" hrs "+m1+" mins "+s1+" secs " + " value was " + dataPoint.getY(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        series.setDrawDataPoints(true);
                        graph.addSeries(series);
                    } catch (Exception e) {

                    }
                }

            }.start();
//            onClickFetchDetails(titleText);
        }else{
            Toast.makeText(GraphActivity.this, "Wifi Not Connected", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickFetchDetails(View view){
        InputStreamReader inputStreamReader=new InputStreamReader(inputStream);
        BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
        String line;
        try {
            while((line=bufferedReader.readLine())!=null){
                content=content+line+"\n";
            }
            contentText.setText(content);
            bufferedReader.close();
            inputStreamReader.close();

        }catch (IOException io){
            io.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    private int getRandomColor() {
        return  (int)(Math.random()*255);
    }

    private void addEntry(WifiInfo wifiInfo, int counter) throws IOException {
        if(counter<20) {
            Calendar temp=Calendar.getInstance();
            temp.setTimeInMillis(millis+(int)(counter*1000));
            Log.v("ENTRY TIME",temp.toString());
            int h1=temp.get(Calendar.HOUR_OF_DAY);
            int m1=temp.get(Calendar.MINUTE);
            int s1=temp.get(Calendar.SECOND);
            progressBar.setProgress(counter+1);
            String text=h1+" hrs "+m1+" mins "+s1+" secs :"+wifiInfo.getLinkSpeed()+"mbps"+ "\n";
            outputStream.write(text.getBytes());
            dataPoints[counter] = new DataPoint(counter, WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 100));
        }else{
            progressBar.setVisibility(View.INVISIBLE);
            b.setText("Re Check Signal");
            b.setEnabled(true);
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //do here
                } else {
                    Toast.makeText(this, "The app was not allowed to write in your storage", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
    //        fileOutputStream.close();
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void onClickOpenFile(View view){
        Intent openFileIntent=new Intent(Intent.ACTION_VIEW );
        openFileIntent.setDataAndType(Uri.fromFile(contentFile),"text/plain");
        startActivity(Intent.createChooser(openFileIntent,"Open File Using...:p"));

    }
}
