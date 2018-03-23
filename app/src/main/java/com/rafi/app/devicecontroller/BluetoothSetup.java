package com.rafi.app.devicecontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BluetoothSetup extends AppCompatActivity {

    TextView tvStatus;
    ImageButton bConnect;
    ListView lvDevices;

    private BluetoothAdapter bluetoothAdapter = null;
    private Set<BluetoothDevice> devices;
    private ArrayList<BluetoothDevice> deviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_setup);

        deviceList = new ArrayList<>();

        tvStatus = (TextView) findViewById(R.id.tv_status);
        bConnect = (ImageButton) findViewById(R.id.b_bt_connect);
        lvDevices = (ListView) findViewById(R.id.lv_devices);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null){
            tvStatus.setText("No Device Found !");
            ShowToast("Turn On Your Device First");
        }
        else {
            if(bluetoothAdapter.isEnabled()){
            }
            else {
                Intent btOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(btOn, 1);
                ShowToast("Pokkat");
            }
        }

        bConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lvDevices.setAdapter(null);
                loadDeviceList();
            }
        });

    }

    void ShowToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    void loadDeviceList(){
        devices = bluetoothAdapter.getBondedDevices();
        ArrayList<String> deviceAL = new ArrayList();
        if(devices.size() > 0){
            for (BluetoothDevice btNow : devices){
                deviceAL.add(btNow.getName() + "\n" + btNow.getAddress());
                deviceList.add(btNow);
            }

            final ArrayAdapter adapter = new ArrayAdapter(this, R.layout.device_card, deviceAL);
            lvDevices.setAdapter(adapter);
            lvDevices.setOnItemClickListener(selectListener);
        }
        else{
            ShowToast("No Paired Devices Found");
        }

    }

    AdapterView.OnItemClickListener selectListener = new AdapterView.OnItemClickListener(){

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String address = deviceList.get(position).getAddress();
            BluetoothControl.address = address;
            BluetoothControl.rBluetoothDevice = deviceList.get(position);
            BluetoothControl.bluetoothAdapter = bluetoothAdapter;

            Intent startControl = new Intent(getBaseContext(), BluetoothControl.class);
            startActivity(startControl);
        }
    };
}
