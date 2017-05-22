package com.codeogic.negruption;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class SecretAudioReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if(action.equals("Start.Service")){
            Toast.makeText(context,"Service Started",Toast.LENGTH_LONG).show();
            Intent i = new Intent(context,SecretAudioService.class);
            context.startService(i);
        }
        if(action.equals("Stop.Service")){
            Toast.makeText(context,"Service Stopped",Toast.LENGTH_LONG).show();
            Intent i1 = new Intent(context,SecretAudioService.class);
            context.stopService(i1);
        }
    }
}
