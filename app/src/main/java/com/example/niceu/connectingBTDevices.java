package com.example.niceu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;
import java.lang.*;

public class connectingBTDevices extends AppCompatActivity {

    private ListView listView;
    private static final int REQUEST_ENABLE_BT = 1;
    private ArrayList<String> mDeviceList = new ArrayList<String>();
    private ArrayList<String> mDeviceList2 = new ArrayList<String>();

    BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connecting_btdevices);

        listView = (ListView) findViewById(R.id.listView);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        CheckBluetoothState();

        if(!mDeviceList.isEmpty()) {
//            listView.setOnItemClickListener((adapterView, view, position, id) -> Toast.makeText(
//                    connectingBTDevices.this, "clicked " + mDeviceList.get(position),
//                    Toast.LENGTH_SHORT));
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Toast.makeText(getApplicationContext(), mDeviceList2.get(i), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(connectingBTDevices.this, pump.class);
                    intent.putExtra("id",mDeviceList2.get(i));
                    startActivity(intent);
                }
            });
        }
    }

    /* It is called when an activity completes.*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            CheckBluetoothState();
        }
    }

    @Override
    protected void onDestroy() {
//        unregisterReceiver(mReceiver);
        super.onDestroy();
    }
    private void CheckBluetoothState() {
        // Checks for the Bluetooth support and then makes sure it is turned on
        // If it isn't turned on, request to turn it on
        // List paired devices
        if(bluetoothAdapter==null) {
            Toast.makeText(getApplicationContext(), "Device doesnt Support Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        } else {
            if (bluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "Bluetooth adapter is enabled", Toast.LENGTH_SHORT).show();
                // Listing paired devices
                Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
                for (BluetoothDevice device : devices) {
                    mDeviceList.add(device.getName() + "\n" + device.getAddress());
                    mDeviceList2.add(device.getAddress());
                    Log.i("BT", device.getName() + "\n" + device.getAddress());
                    listView.setAdapter(new ArrayAdapter<String>(this,
                            android.R.layout.simple_list_item_1, mDeviceList));
                }
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }
}

