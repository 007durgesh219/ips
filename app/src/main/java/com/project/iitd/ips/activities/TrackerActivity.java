package com.project.iitd.ips.activities;

import android.net.wifi.ScanResult;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;

import com.project.iitd.ips.R;
import com.project.iitd.ips.utils.CommonUtils;

import java.util.ArrayList;

public class TrackerActivity extends AppCompatActivity {
    public static final String FIRST_X_CORD = "first_ap_x_coordinate";
    public static final String FIRST_Y_CORD = "first_ap_y_coordinate";
    public static final String SECOND_X_CORD = "second_ap_x_coordinate";
    public static final String SECOND_Y_CORD = "second_ap_y_coordinate";
    public static final String FIRST_AP = "first_ap";
    public static final String SECOND_AP = "second_ap";

    private ArrayList<Pair<Float, Float>> apCoordinates = new ArrayList<>();
    private ArrayList<ScanResult> scanResults = new ArrayList<>();

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
        scanResults.add((ScanResult)getIntent().getParcelableExtra(FIRST_AP));
        scanResults.add((ScanResult)getIntent().getParcelableExtra(SECOND_AP));
        CommonUtils.log(scanResults.get(0).SSID);
        CommonUtils.log(scanResults.get(1).SSID);
    }
}
