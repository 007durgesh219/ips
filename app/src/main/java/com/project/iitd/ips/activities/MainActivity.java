package com.project.iitd.ips.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;

import com.project.iitd.ips.R;

import java.util.ArrayList;
import java.util.List;

import com.project.iitd.ips.utils.CommonUtils;

public class MainActivity extends AppCompatActivity {
    private static final int WIFI_PERMISSION_REQ_CODE = 101;
    private WifiManager wifiManager;
    private WifiReceiver wifiReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiReceiver = new WifiReceiver();
        registerReceiver(wifiReceiver,new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        if (wifiManager.isWifiEnabled()) {
            initScan();
        } else {
            final Button button = (Button)findViewById(R.id.btn_start);
            button.setText(" Enable Wifi ");
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    wifiManager.setWifiEnabled(true);
                    button.setText("Start");
                    initScan();
                }
            });
        }
    }


    private void initScan() {
        CommonUtils.toast(getApplicationContext(), "Please wait scanning wifis...");
        if (CommonUtils.checkPermissions(getApplicationContext(),
                new String[]{Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION})) {
            CommonUtils.log("Granted");
            wifiManager.startScan();
            //setupWifiList();
        } else {
            CommonUtils.log("Requesting");
            /*ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION},
                    WIFI_PERMISSION_REQ_CODE);*/
            wifiManager.startScan();
        }
    }

    private List<ScanResult> scanResults;
    private List<Integer> aps = new ArrayList<>();
    private List<Pair<Float, Float>> apCoordinats = new ArrayList<>();
    private void setupWifiList() {

        scanResults = wifiManager.getScanResults();
        ArrayList<String> wifis  = new ArrayList<>();
        CommonUtils.log("Size: "+scanResults.size());
        for (ScanResult scanResult : scanResults) {
            wifis.add(scanResult.SSID+"("+scanResult.level+")");
            CommonUtils.log(scanResult.SSID+":"+scanResult.level);
        }
        final ListView listView = (ListView)findViewById(R.id.list_wifi);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_multiple_choice,wifis);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                CheckedTextView textView = (CheckedTextView)view.findViewById(android.R.id.text1);
                if (!textView.isChecked()) {
                    int loc = aps.indexOf(position);
                    aps.remove(loc);
                    apCoordinats.remove(loc);
                    return;
                }

                aps.add(position);
                if (aps.size() > 3)
                    aps.remove(0);


                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Enter Coordinate of this ap");

                final View dialogView = LayoutInflater.from(MainActivity.this)
                        .inflate(R.layout.coordinate_dialog_layout, null);
                builder.setView(dialogView);

// Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editText = (EditText)dialogView.findViewById(R.id.x_cord);
                        float xcord = Float.parseFloat(editText.getText().toString());
                        editText = (EditText)dialogView.findViewById(R.id.y_cord);
                        float ycord = Float.parseFloat(editText.getText().toString());
                        apCoordinats.add(new Pair<Float, Float>(xcord,ycord));
                        if (apCoordinats.size() > 3)
                            apCoordinats.remove(0);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        int loc = aps.indexOf(position);
                        aps.remove(loc);
                        apCoordinats.remove(loc);
                    }
                });

                builder.show();
            }
        });

        Button button = (Button)findViewById(R.id.btn_start);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (apCoordinats.size() != 3 || aps.size() != 3) {
                    CommonUtils.toast(getApplicationContext(), "Please select exactly 3 aps");
                    return;
                }
                Intent tracker = new Intent(MainActivity.this, TrackerActivity.class);
                tracker.putExtra(TrackerActivity.FIRST_X_CORD, apCoordinats.get(0).first);
                tracker.putExtra(TrackerActivity.FIRST_Y_CORD, apCoordinats.get(0).second);
                tracker.putExtra(TrackerActivity.SECOND_X_CORD, apCoordinats.get(1).first);
                tracker.putExtra(TrackerActivity.SECOND_Y_CORD, apCoordinats.get(1).second);
                tracker.putExtra(TrackerActivity.THIRD_X_CORD, apCoordinats.get(2).first);
                tracker.putExtra(TrackerActivity.THIRD_Y_CORD, apCoordinats.get(2).second);
                tracker.putExtra(TrackerActivity.FIRST_AP, scanResults.get(aps.get(0)));
                tracker.putExtra(TrackerActivity.SECOND_AP, scanResults.get(aps.get(1)));
                tracker.putExtra(TrackerActivity.THIRD_AP, scanResults.get(aps.get(2)));
                startActivity(tracker);
            }
        });

        wifiManager.startScan();
    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == WIFI_PERMISSION_REQ_CODE) {
            if (grantResults.length < 2
                    || grantResults[0] != PackageManager.PERMISSION_GRANTED
                    || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                CommonUtils.toast(getApplicationContext(), "Please give wifi access permission");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_WIFI_STATE,Manifest.permission.CHANGE_WIFI_STATE},
                        WIFI_PERMISSION_REQ_CODE);
            } else {
                //setupWifiList();
                wifiManager.startScan();
            }
        }
    }*/

    public class WifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            unregisterReceiver(wifiReceiver);
            CommonUtils.log("Received");
            //CommonUtils.toast(getApplicationContext(), "Refreshed");
            setupWifiList();
        }
    }
}
