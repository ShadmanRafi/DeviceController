package com.rafi.app.devicecontroller;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public class BluetoothControl extends AppCompatActivity {
    String TAG = "BTC";
    Switch sD1, sD2;
    TextView statusD1, statusD2, descriptionD1, descriptionD2;
    Button bVoice;

    static boolean isD1On, isD2On;
    static String address;

    static BluetoothAdapter bluetoothAdapter;
    BluetoothSocket bluetoothSocket = null;

    BluetoothConnectionService mBluetoothConnection;
    BluetoothDevice bluetoothDevice;
    static BluetoothDevice rBluetoothDevice;
    boolean isBtConnected = false;

    static final UUID thisUUID = UUID.fromString("113feb42-c7c6-11e7-abc4-cec278b6b50a");


    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(bluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, bluetoothAdapter.ERROR);

                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d("BTC", "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d("BTC", "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d("BTC", "mBroadcastReceiver1: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d("BTC", "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };


    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d("BTC", "BroadcastReceiver: BOND_BONDED.");
                    //inside BroadcastReceiver4
                    bluetoothDevice = mDevice;
                }
                //case2: creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d("BTC", "BroadcastReceiver: BOND_BONDING.");
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d("BTC", "BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    //Device is in Discoverable Mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled.");
                        break;
                    //Device not in discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcastReceiver2: Connecting....");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadcastReceiver2: Connected.");
                        break;
                }

            }
        }
    };



    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_control);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4,filter);
        rBluetoothDevice.createBond();
        bluetoothDevice = rBluetoothDevice;
        mBluetoothConnection= new BluetoothConnectionService(BluetoothControl.this,bluetoothAdapter);
        StartBtConnection(bluetoothDevice,thisUUID);

        //progress = new ProgressDialog(getApplicationContext());

        sD1 = (Switch) findViewById(R.id.s_device1);
        sD2 = (Switch) findViewById(R.id.s_device2);

        bVoice = (Button) findViewById(R.id.b_voice);

        statusD1 = (TextView) findViewById(R.id.tv_status_d1);
        statusD2 = (TextView) findViewById(R.id.tv_status_d2);

        //new ConnectBT().execute();

        init();
        setStatus();

        sD1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String d1Data = "";
                isD1On = sD1.isChecked();
                if(isD1On){
                    d1Data = "A";
                }else {
                    d1Data = "a";
                }
                setStatus();
                SendData(d1Data);
            }
        });

        sD2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String d2Data = "";
                isD1On = sD1.isChecked();
                if(isD1On){
                    d2Data = "21";
                }else {
                    d2Data = "20";
                }
                setStatus();
                SendData(d2Data);
            }
        });

        bVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });
    }

    void init(){
        if(isD1On!=true && isD2On!=true){
            isD1On = false;
            isD2On = false;
        }
    }

    void setStatus(){
        if(isD1On){
            statusD1.setText("ON");
        }else {
            statusD1.setText("OFF");
        }

        if(isD2On){
            statusD2.setText("ON");
        }else {
            statusD2.setText("OFF");
        }
    }

    void SendData(String data){
        mBluetoothConnection.write(data.getBytes());
        ShowToast("Sending to serial...");
    }
/*

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(BluetoothControl.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (bluetoothSocket == null || !isBtConnected)
                {
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = bluetoothAdapter.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    bluetoothSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(thisUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    bluetoothSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                ShowToast("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                ShowToast("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }

*/
    void ShowToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");
        try {
            startActivityForResult(intent, 100);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "Sorry!!!..your device does not support s", Toast.LENGTH_LONG).show();
        }

    }

    public void onActivityResult(int reqst_code, int result_code, Intent intent) {
        super.onActivityResult(reqst_code, result_code, intent);
        switch (reqst_code) {
            case 100:
                if (result_code == RESULT_OK && intent != null) {
                    ArrayList<String> result = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    SendVoiceCommand(result.get(0));
                }
                break;
        }
    }

    void SendVoiceCommand(String cmd){
        switch (cmd){
            case "Turn on first device":
                SendData("A");
            case "Turn off first device":
                SendData("a");
            case "Turn on second device":
                SendData("21");
            case "Turn off second device":
                SendData("20");
        }
    }

    void StartBtConnection(BluetoothDevice device, UUID uuid){
        mBluetoothConnection.startClient(device,uuid);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver4);
        unregisterReceiver(mBroadcastReceiver1);
        unregisterReceiver(mBroadcastReceiver2);

    }
}
