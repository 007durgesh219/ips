package com.project.iitd.ips.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.widget.TextView;

import com.project.iitd.ips.R;
import com.project.iitd.ips.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

public class TrackerActivity extends AppCompatActivity {
    public static final String FIRST_X_CORD = "first_ap_x_coordinate";
    public static final String FIRST_Y_CORD = "first_ap_y_coordinate";
    public static final String SECOND_X_CORD = "second_ap_x_coordinate";
    public static final String SECOND_Y_CORD = "second_ap_y_coordinate";
    public static final String THIRD_X_CORD = "third_ap_x_coordinate";
    public static final String THIRD_Y_CORD = "third_ap_y_coordinate";
    public static final String FIRST_AP = "first_ap";
    public static final String SECOND_AP = "second_ap";
    public static final String THIRD_AP = "third_ap";
    private static final double m = -33.44 ;
    private static final double c = -55.62 ;

    private ArrayList<Pair<Float, Float>> apCoordinates = new ArrayList<>();
    private ArrayList<ScanResult> scanResults = new ArrayList<>();

    private TextView result, ap1dist, ap2dist, ap3dist, ap1level, ap2level, ap3level;

    private WifiManager wifiManager;
    private WifiReceiver wifiReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);
        if (getIntent() == null) {
            finish();
            CommonUtils.toast(getApplicationContext(), "Please send proper arguments");
            return;
        }
        apCoordinates.add(new Pair<Float, Float>(getIntent().getFloatExtra(FIRST_X_CORD,0),
                getIntent().getFloatExtra(FIRST_Y_CORD,0)));
        apCoordinates.add(new Pair<Float, Float>(getIntent().getFloatExtra(SECOND_X_CORD,0),
                getIntent().getFloatExtra(SECOND_Y_CORD,0)));
        apCoordinates.add(new Pair<Float, Float>(getIntent().getFloatExtra(THIRD_X_CORD,0),
                getIntent().getFloatExtra(THIRD_Y_CORD,0)));
        scanResults.add((ScanResult)getIntent().getParcelableExtra(FIRST_AP));
        scanResults.add((ScanResult)getIntent().getParcelableExtra(SECOND_AP));
        scanResults.add((ScanResult)getIntent().getParcelableExtra(THIRD_AP));

        result = (TextView) findViewById(R.id.result);
        ap1dist = (TextView) findViewById(R.id.ap1_distance);
        ap2dist = (TextView) findViewById(R.id.ap2_distance);
        ap3dist = (TextView) findViewById(R.id.ap3_distance);
        ap1level = (TextView) findViewById(R.id.ap1_level);
        ap2level = (TextView) findViewById(R.id.ap2_level);
        ap3level = (TextView) findViewById(R.id.ap3_level);

        TextView ap1name = (TextView) findViewById(R.id.ap1_name);
        TextView ap2name = (TextView) findViewById(R.id.ap2_name);
        TextView ap3name = (TextView) findViewById(R.id.ap3_name);

        TextView ap1coord = (TextView) findViewById(R.id.ap1_coord);
        TextView ap2coord = (TextView) findViewById(R.id.ap2_coord);
        TextView ap3coord = (TextView) findViewById(R.id.ap3_coord);

        ap1name.setText(scanResults.get(0).SSID);
        ap2name.setText(scanResults.get(2).SSID);
        ap3name.setText(scanResults.get(2).SSID);

        ap1coord.setText(apCoordinates.get(0).first+","+apCoordinates.get(0).second);
        ap2coord.setText(apCoordinates.get(1).first+","+apCoordinates.get(1).second);
        ap3coord.setText(apCoordinates.get(2).first+","+apCoordinates.get(2).second);

        ap1level.setText(scanResults.get(0).level+" dBm");
        ap2level.setText(scanResults.get(1).level+" dBm");
        ap3level.setText(scanResults.get(2).level+" dBm");


        calculateResult();

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiReceiver = new TrackerActivity.WifiReceiver();
        registerReceiver(wifiReceiver,new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
    }

    private void calculateResult() {
        double r0 = Math.pow(10, (scanResults.get(0).level - c)/m);
        double r1 = Math.pow(10, (scanResults.get(1).level - c)/m);
        double r3 = Math.pow(10, (scanResults.get(2).level - c)/m);
        double d = Math.sqrt(Math.pow(apCoordinates.get(0).first-apCoordinates.get(1).first, 2) +
                        Math.pow(apCoordinates.get(0).second-apCoordinates.get(1).second, 2));
        double a = (r0*r0 - r1*r1 + d*d) / 2*d ;
        double h = Math.sqrt(r0*r0-a*a);
        Pair<Float, Float> p2 = new Pair<>((float)(apCoordinates.get(0).first + a*(apCoordinates.get(1).first - apCoordinates.get(0).first)/d),
                (float)(apCoordinates.get(0).second + a*(apCoordinates.get(1).second - apCoordinates.get(0).second)/d));
        Pair<Float, Float> p31 = new Pair<>((float)(p2.first+h*(apCoordinates.get(1).second-apCoordinates.get(0).second)/d),
                (float)(p2.second-h*(apCoordinates.get(1).first-apCoordinates.get(0).first)/d));
        Pair<Float, Float> p32 = new Pair<>((float)(p2.first-h*(apCoordinates.get(1).second-apCoordinates.get(0).second)/d),
                (float)(p2.second+h*(apCoordinates.get(1).first-apCoordinates.get(0).first)/d));
        double r31 = Math.sqrt(Math.pow(p31.first-apCoordinates.get(2).first,2)
                +Math.pow(p31.second-apCoordinates.get(2).second, 2));
        double r32 = Math.sqrt(Math.pow(p32.first-apCoordinates.get(2).first,2)
                +Math.pow(p32.second-apCoordinates.get(2).second, 2));
        if (Math.abs(r32-r3) < Math.abs(r31-r3)) {
            result.setText(p32.first+", "+p32.second);
        } else {
            result.setText(p31.first+", "+p31.second);
        }
    }

    public class WifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            CommonUtils.log("Received");
            List<ScanResult> scanResults0 = wifiManager.getScanResults();
            for (ScanResult scanResult : scanResults0) {
                if (scanResult.BSSID == scanResults.get(0).BSSID) {
                    scanResults.set(0, scanResult);
                } else if (scanResult.BSSID == scanResults.get(1).BSSID) {
                    scanResults.set(0, scanResult);
                } else if (scanResult.BSSID == scanResults.get(2).BSSID) {
                    scanResults.set(1, scanResult);
                }
            }
            calculateResult();
        }
    }
}
