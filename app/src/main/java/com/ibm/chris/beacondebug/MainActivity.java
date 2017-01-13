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

    TextView beaconRssiView;
    EditText beaconIdView;
    long last = System.currentTimeMillis();
    int rssi = 0;
    int readings = 0;

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
                final String text = "RSSI:" + Integer.toString(rssi) + "\nfrq: " + Long.toString(System.currentTimeMillis() - last) + "ms\nreadings:" + readings;
                rssi = 0;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        beaconRssiView.setText(text);
                    }
                });
            }
        }, 100, 100);
    }


    private void startBluetoothScan(){
        //requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        // setup bluetooth
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        // wait for bluetooth to be ready
        while(bluetoothLeScanner == null) {
            try {
                Thread.sleep(10);                 //1000 milliseconds is one second.
                bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        List<ScanFilter> filters = new ArrayList<>();
        bluetoothLeScanner.startScan(filters, settings, new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                if(result.getDevice().getAddress().equals(beaconIdView.getText().toString())) {
                    last = System.currentTimeMillis();
                    rssi = result.getRssi();
                    readings++;
                }
            }
        });
    }
}

