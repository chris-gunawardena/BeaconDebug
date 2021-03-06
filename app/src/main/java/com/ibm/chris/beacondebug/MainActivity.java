package com.ibm.chris.beacondebug;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Double.NaN;

public class MainActivity extends AppCompatActivity {

    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;
    BluetoothLeScanner bluetoothLeScanner;

    TextView beaconRssiView;
    EditText beaconIdView;
    long last = System.currentTimeMillis();
    int rssi = 0;
    int readings = 0;
    long higest_diff = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        beaconRssiView = (TextView) findViewById(R.id.beaconRssi);
        beaconIdView = (EditText) findViewById(R.id.beaconId);
        startBluetoothScan();
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long diff = System.currentTimeMillis() - last;
                if(higest_diff < diff) {
                    higest_diff = diff;
                }
                final String text = "RSSI:" + Integer.toString(rssi) +
                        "\nfrq: " + Long.toString(diff) +
                        "ms\nhighest:" + higest_diff +
                        "ms\nreadings:" + readings;
                rssi = 0;
                if(diff>10000) {
                    bluetoothLeScanner.stopScan(scanCallback);

                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        beaconRssiView.setText(text);
                    }
                });
            }
        }, 0, 100);
    }


    private void startBluetoothScan(){
        //requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        // setup bluetooth
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        // wait for bluetooth to be ready
        while(bluetoothLeScanner == null) {
            try {
                Thread.sleep(10);                 //1000 milliseconds is one second.
                bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        scanBluetooth();
    }

    ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if(result.getDevice().getAddress().equals(beaconIdView.getText().toString())) {
                last = System.currentTimeMillis();
                rssi = result.getRssi();
                readings++;
            }
        }
    };

    private void scanBluetooth() {
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(0)
                .build();
        List<ScanFilter> filters = new ArrayList<>();
        bluetoothLeScanner.startScan(filters, settings, scanCallback);
    }


}

