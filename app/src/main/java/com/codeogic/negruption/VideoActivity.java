package com.codeogic.negruption;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class VideoActivity extends AppCompatActivity implements View.OnClickListener{

    VideoView videoView;
    Button btnChoose,btnNext;
    StoryBean rcvStory;
    String  selectedPath;

    ProgressDialog progressDialog;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        videoView = (VideoView)findViewById(R.id.videoView);
        btnChoose = (Button)findViewById(R.id.buttonVideoChoose);
        btnNext = (Button)findViewById(R.id.buttonNext4);
        Intent rcv = getIntent();
        rcvStory =(StoryBean)rcv.getSerializableExtra("rcvStory");

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Uploading...");


        btnChoose.setOnClickListener(this);
        btnNext.setOnClickListener(this);

        sharedPreferences = getSharedPreferences(Util.PREFS_NAME,MODE_PRIVATE);

        editor = sharedPreferences.edit();

        String img = sharedPreferences.getString(Util.PREFS_KEYIMAGE,null);
        String aud = sharedPreferences.getString(Util.PREFS_KEYAUDIO,null);

        Log.i("keys",img + aud);





    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (selectedPath!=null)
        videoView.stopPlayback();
    }


    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.buttonVideoChoose){
            openGalleryVideo();

        }else if (v.getId()==R.id.buttonNext4){
            if (selectedPath!=null) {
                MyTask myTask = new MyTask();
                myTask.execute();
            }else {
                if((sharedPreferences.contains(Util.PREFS_KEYIMAGE)||sharedPreferences.contains(Util.PREFS_KEYAUDIO))){
                    Intent intent = new Intent(VideoActivity.this,HomeActivity.class);
                    startActivity(intent);
                }
                else
                    showAlertDialog();
            }
        }


    }


    void showAlertDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage("Story would not be entertained without any proofs. Upload image, audio or a video.");
        builder.setCancelable(false); // If user will press the back key dialog will not be dismissed
        builder.setPositiveButton("Ok", null);
         builder.create().show();

    }

    public void openGalleryVideo(){

        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Video "),103);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if(requestCode== 103){
                System.out.println("SELECT_VIDEO");
                Uri selectedVideoUri = data.getData();
                selectedPath = Util.getPath(this,selectedVideoUri);
                Log.i("Videouri",selectedVideoUri.toString());
               // System.out.println("SELECT_AUDIO Path : " + selectedPath);



                videoView.setMediaController(new MediaController(this));
                videoView.setVideoURI(selectedVideoUri);
                videoView.requestFocus();

                if (selectedPath!=null)
                videoView.start();


            }

        }
    }




class MyTask extends AsyncTask {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
            Toast.makeText(VideoActivity.this,"Upload started",Toast.LENGTH_LONG).show();
        }

        @Override
        protected Object doInBackground(Object[] params) {

            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            DataInputStream inStream = null;
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
                URL url = new URL(Util.urlVideoUpload);
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
            Toast.makeText(VideoActivity.this,"Upload Finished",Toast.LENGTH_LONG).show();
           finish();
        }
    }

}
