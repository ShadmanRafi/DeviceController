package com.rafi.app.devicecontroller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class HomeActivity extends AppCompatActivity {

    ImageButton bBT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bBT = (ImageButton) findViewById(R.id.b_bt);
        bBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent setup = new Intent(HomeActivity.this,BluetoothSetup.class);
                startActivity(setup);
            }
        });
    }
}
