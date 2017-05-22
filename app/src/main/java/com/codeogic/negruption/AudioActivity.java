package com.codeogic.negruption;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class AudioActivity extends AppCompatActivity implements View.OnClickListener {

    SeekBar seekBar;
    EditText txtAudioTitle;
    Button play, stop, btnChoose, btnNext;
    MediaPlayer mediaPlayer;
    StoryBean rcvStory;
    String  selectedPath;
    Uri selectedAudioUri;

    ProgressDialog progressDialog;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);


        play = (Button) findViewById(R.id.buttonPlay);
        stop = (Button) findViewById(R.id.buttonStop);
        btnChoose =(Button)findViewById(R.id.buttonAudioChoose);
        btnNext = (Button)findViewById(R.id.buttonNext3) ;
        txtAudioTitle=(EditText)findViewById(R.id.editTextAudioTitle);

        play.setOnClickListener(this);
        stop.setOnClickListener(this);
        btnChoose.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        Intent rcv = getIntent();
        rcvStory =(StoryBean)rcv.getSerializableExtra("rcvStory");


        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Uploading...");

        sharedPreferences=getSharedPreferences(Util.PREFS_NAME,MODE_PRIVATE);
        editor=sharedPreferences.edit();





    }

    @Override
    public void onClick(View v) {


        if (v.getId() == R.id.buttonAudioChoose) {
            openGalleryAudio();

        }
        else if (v.getId() == R.id.buttonPlay) {

            if (selectedPath!=null){
            mediaPlayer.start();}
            else {
                Toast.makeText(this, "No Audio Selected", Toast.LENGTH_LONG).show();
            }


        } else if (v.getId() == R.id.buttonStop) {

            if (selectedPath!=null) {

                mediaPlayer.stop();
            }



        }
        else if (v.getId()==R.id.buttonNext3){

            if (selectedPath!=null){
            MyTask myTask = new MyTask();
            myTask.execute();
                editor.putString(Util.PREFS_KEYAUDIO,selectedPath);
                editor.commit();
            }
            else {
                Intent intent = new Intent(AudioActivity.this,VideoActivity.class);
                intent.putExtra("rcvStory",rcvStory);
                startActivity(intent);
            }


        }

    }

    public void openGalleryAudio(){

        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Audio "),102);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {

            if(requestCode == 102){
                System.out.println("SELECT_AUDIO");
                Uri selectedAudioUri = data.getData();
                selectedPath =Util.getPath(this,selectedAudioUri);
                Log.i("uri",selectedAudioUri.toString());
                Log.i("path",selectedPath);
               // rcvStory.setAudioProof(selectedPath);

                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(this, selectedAudioUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }




            }
        }
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (selectedPath!= null)
           mediaPlayer.release();

    }


    class MyTask extends AsyncTask {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
            Toast.makeText(AudioActivity.this,"Upload started",Toast.LENGTH_LONG).show();
        }

        @Override
        protected Object doInBackground(Object[] params) {

            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            DataInputStream inStream = null;
            //String existingFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/mypic.png";
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 5 * 1024 * 1024;
            String responseFromServer = "";
            //String urlString = "http://mywebsite.com/directory/upload.php";

            try {

                //------------------ CLIENT REQUEST
                FileInputStream fileInputStream = new FileInputStream(new File(selectedPath));
                // open a URL connection to the Servlet
                URL url = new URL(Util.urlAudioUpload);
                // Open a HTTP connection to the URL
                conn = (HttpURLConnection) url.openConnection();
                // Allow Inputs
                conn.setDoInput(true);
                // Allow Outputs
                conn.setDoOutput(true);
                // Don't use a cached copy.
                conn.setUseCaches(false);
                // Use a post method.
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + selectedPath + "\""  + lineEnd);
                dos.writeBytes(lineEnd);
                // create a buffer of maximum size
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];
                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                // close streams
                Log.e("Debug", "File is written");
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {
                Log.e("Debug", "error: " + ex.getMessage(), ex);
            } catch (IOException ioe) {
                Log.e("Debug", "error: " + ioe.getMessage(), ioe);
            }

            //------------------ read the SERVER RESPONSE
            try {

                inStream = new DataInputStream(conn.getInputStream());
                String str;

                while ((str = inStream.readLine()) != null) {

                    Log.e("Debug", "Server Response " + str);

                }

                inStream.close();
                //dialog.dismiss();

            } catch (IOException ioex) {
                Log.e("Debug", "error: " + ioex.getMessage(), ioex);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
             progressDialog.dismiss();
              Toast.makeText(AudioActivity.this,"Upload Finished",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(AudioActivity.this,VideoActivity.class);
            intent.putExtra("rcvStory",rcvStory);
            startActivity(intent);
        }
    }

}
