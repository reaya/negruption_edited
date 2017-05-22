package com.codeogic.negruption;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

public class SecretAudioService extends Service implements SensorEventListener {
    Sensor sensor;
    SensorManager sensorManager;
    MediaRecorder recorder;
    float last_x=0;
    float last_y=0;
    float last_z=0;
    static int SHAKE_THRESHOLD = 800;
    long lastUpdate=0;
    Boolean flag= false;

    public SecretAudioService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        Toast.makeText(getApplicationContext(), "Registered", Toast.LENGTH_LONG).show();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        sensorManager.unregisterListener(this);
        Toast.makeText(getApplicationContext(), "Unregistered", Toast.LENGTH_LONG).show();
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] values = event.values;
            long curTime = System.currentTimeMillis();
            // only allow one update every 100ms.
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float x = values[0];
                float y = values[1];
                float z = values[2];

                float speed = Math.abs(x+y+z - last_x - last_y - last_z) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    Log.d("sensor", "shake detected w/ speed: " + speed);
                    Toast.makeText(this, "shake detected w/ speed: " + speed, Toast.LENGTH_SHORT).show();
                    onShake();
                }
                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    void onShake() {
        if (flag == false) {
            startRecording();
            flag = true;
        } else {
            stopRecording();
            flag = false;
        }
    }

    void startRecording(){
        recorder = new MediaRecorder();
        recorder.reset();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.OutputFormat.DEFAULT);
        String storageDir = Environment.getExternalStorageDirectory()+ File.separator+"Negruption";

        File folder = new File(storageDir);
        if(!folder.exists()){
            folder.mkdirs();}
        //Date date = new Date();
        String date = DateFormat.getTimeInstance().format(new Date());
        File file = new File(storageDir+"/"+date +"-Audio.mp3");
        recorder.setOutputFile(file.getAbsolutePath());
        try {
            recorder.prepare();

            recorder.start();

            Toast.makeText(getApplicationContext(), " Recording Started", Toast.LENGTH_LONG).show();
            Log.i("recordStart", "start");


        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    void stopRecording(){
        if (null != recorder) {

            recorder.stop();
            recorder.reset();
            recorder.release();

            recorder = null;
            Toast.makeText(getApplicationContext(), "Recording Stopped", Toast.LENGTH_LONG).show();
            Log.i("recordStop", "stop");

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
