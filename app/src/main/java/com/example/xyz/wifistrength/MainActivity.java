package com.example.xyz.wifistrength;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void onStart(){
        super.onStart();
        onClickSetValue(findViewById(R.id.cardView));

    }
    public void onClickNavigate(View view){
        startActivity(new Intent(this,GraphActivity.class));
    }
    public void onClickSetValue(View view){
        TextView status=findViewById(R.id.wifiStatus);
        TextView name=findViewById(R.id.wifiName);
        ImageView iview=findViewById(R.id.navigateImage);
        WifiManager wifiManager=(WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo=wifiManager.getConnectionInfo();
        if(wifiInfo.getSupplicantState()==SupplicantState.COMPLETED){
            status.setText("You Are Connected");
            status.setVisibility(View.VISIBLE);
            name.setText(wifiInfo.getSSID());
            iview.setVisibility(View.VISIBLE);
        }else{
            status.setVisibility(View.INVISIBLE);
            name.setText("Wifi Not Connected:(");
            iview.setVisibility(View.INVISIBLE);
        }

    }
}
