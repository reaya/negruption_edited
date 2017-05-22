package com.codeogic.negruption;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

public class SecretAudioActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    Switch aSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secret_audio);
        aSwitch = (Switch) findViewById(R.id.switchAudio);
        aSwitch.setOnCheckedChangeListener(this);
        aSwitch.setTextOn("ON");
        aSwitch.setTextOff("OFF");
        aSwitch.setChecked(false);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(buttonView.getId() == R.id.switchAudio){
            if(isChecked){
                Intent start = new Intent("Start.Service");
                sendBroadcast(start);
            }else{
                Intent stop = new Intent("Stop.Service");
                sendBroadcast(stop);
            }
        }

    }
}
