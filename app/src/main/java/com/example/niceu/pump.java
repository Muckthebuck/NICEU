package com.example.niceu;

import static java.lang.Math.abs;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import java.lang.*;
public class pump extends AppCompatActivity {
    //converted using angle, between -1 and 1
    private int sentCount=0;
    private int speed = 0; //0%
    private boolean reachedThreashold = false;
    private int stop = 0;
    private int speedIncrement = 5; //5%

    private boolean c;
    private boolean z;

    // Importing also other views
    //bluetooth variables
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");//Serial Port Service ID
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private String Id;

    ImageButton upButton, downButton, connectButton, homeButton, sendButton;
    Button stopButton;
    ImageView topImage, bottomImage;
    TextView textView;
    EditText editText;
//    TextView textConn;
    boolean deviceConnected = false;
    Thread thread;
    byte buffer[];
    int bufferPosition;
    boolean stopThread;
    private String mode;

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pump);
        Bundle bundle = getIntent().getExtras();
        Id = bundle.getString("id");
        initalise_setup();


        connectButton.setOnClickListener(view -> {
            onClickConnect();
        });

        upButton.setOnClickListener(view -> {
            if(speed<=100 && speed+speedIncrement <=100){
                speed += speedIncrement;
                sendData();
            }
        });

        downButton.setOnClickListener(view -> {
            if(speed>=-100 && speed-speedIncrement>=-100){
                speed -= speedIncrement;
                sendData();
            }
        });

        homeButton.setOnClickListener(view -> {
            closeConnection();
            Intent intent = new Intent( pump.this, MainActivity.class );
            intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
            startActivity( intent );
        });

        sendButton.setOnClickListener(view -> {
            int _speed=-1;
            try{
                _speed = Integer.parseInt(editText.getText().toString());
                System.out.println(_speed);
            }
            catch (NumberFormatException ex){
                ex.printStackTrace();
            }
            if( _speed >= 0 && _speed <=100 && speed!=_speed){
                speed = _speed;
                sendData();
            }

        });

        stopButton.setOnClickListener(view -> {
            stop = 1;
            sendData();
        });

    }

    public void sendData() {
        checkBT();
        String _stop = String.format(Locale.getDefault(), "%d", (int) stop);
        int sign = (speed<0)?1:0;
        String _sign = String.format(Locale.getDefault(),"%d", (int) sign );
        String _speed = String.format(Locale.getDefault(), "%03d", (int) abs(speed));

        String string = _stop + _sign + _speed + "!\0";
        try {
            outputStream.write(string.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public void clearScreen(){
//        sentCount++;
//        if(sentCount>=2){
//            textView.setText("");
//            sentCount=0;
//        }
//    }


    public boolean BTinit() {
        boolean found = false;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Device doesnt Support Bluetooth", Toast.LENGTH_SHORT).show();
        }

        assert bluetoothAdapter != null;
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if (bondedDevices.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please Pair the Device first", Toast.LENGTH_SHORT).show();
        } else {
            for (BluetoothDevice iterator : bondedDevices) {
                if (iterator.getAddress().equals(getDeviceId())) {
                    device = iterator;
                    found = true;
                    break;
                }
            }
        }

        return found;
    }

    public boolean BTconnect() {
        boolean connected = true;
        try {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            connected=false;
        }
        if(connected)
        {
            try {
                outputStream=socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream=socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        return connected;
    }

    public void checkBT() {
        if(!deviceConnected){
            if(BTinit())
            {
                if(BTconnect())
                {
                    deviceConnected=true;
//                    Toast.makeText(getApplicationContext(), "\nConnection Opened!\n", Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    public void onClickConnect(){
        if(BTinit())
        {
            if(BTconnect())
            {
                deviceConnected=true;
                Toast.makeText(getApplicationContext(), "\nConnection Opened!\n", Toast.LENGTH_SHORT).show();
            }

        }
    }

    void beginListenForData()
    {
        final Handler handler = new Handler();
        stopThread = false;
        buffer = new byte[1024];
        Thread thread  = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopThread)
                {
                    try
                    {
                        int byteCount = inputStream.available();
                        if(byteCount > 0)
                        {
                            byte[] rawBytes = new byte[byteCount];
                            inputStream.read(rawBytes);
                            final String string=new String(rawBytes,"UTF-8");
                            if (string.contains("STOP\0")){
                                stopMotors();
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopThread = true;
                    }
                }
            }
        });

        thread.start();
    }

    public  String getDeviceId(){
        return Id;
    }

    public void stopMotors(){
        stop = 1;
        upButton.setImageResource(R.drawable.up2);
        downButton.setImageResource(R.drawable.down2);
        sendButton.setImageResource(R.drawable.send2);
        textView.clearComposingText();
        topImage.setImageResource(R.drawable.green_text);
        bottomImage.setImageResource(R.drawable.comp_green);
        upButton.setEnabled(false);
        downButton.setEnabled(false);
        sendButton.setEnabled(false);
        sendData();
    }
    public void initalise_setup(){
        textView = (TextView) findViewById(R.id.textView);
        stopButton = (Button) findViewById(R.id.button_stop);
        sendButton = (ImageButton) findViewById(R.id.button_send);
        homeButton =  (ImageButton) findViewById(R.id.button_home);
        downButton = (ImageButton) findViewById(R.id.button_down);
        upButton = (ImageButton) findViewById(R.id.button_up);
        connectButton = (ImageButton) findViewById(R.id.button_bt_connect);
        editText = (EditText) findViewById(R.id.editTextNumber);
        topImage = (ImageView) findViewById(R.id.itop_circle);
        topImage = (ImageView) findViewById(R.id.bottom_colorBlob);
        editText.setTextColor(Color.BLACK);

        upButton.setImageResource(R.drawable.up1);
        downButton.setImageResource(R.drawable.down1);
        sendButton.setImageResource(R.drawable.send);
        upButton.setEnabled(true);
        downButton.setEnabled(true);
        sendButton.setEnabled(true);

    }

    public void closeConnection(){
        if(deviceConnected){
            stopThread = true;
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            deviceConnected=false;
            Toast.makeText(getApplicationContext(), "\nConnection Closed!\n", Toast.LENGTH_SHORT).show();
        }
    }

}
