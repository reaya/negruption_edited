package com.codeogic.negruption;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class ImageActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView imageView;
    EditText txtImgTitle;
    Button btnImageChoose,btnNext;

    String selectedPath;
    StoryBean rcvStory;

    RequestQueue requestQueue;
    String encodedImage, imageTitle;

    Bitmap image;

    ProgressDialog progressDialog;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        imageView = (ImageView)findViewById(R.id.imageView);
        Intent rcv = getIntent();
        rcvStory =(StoryBean)rcv.getSerializableExtra("keyStoryBean");


        txtImgTitle= (EditText)findViewById(R.id.editTextImageTitle);
        btnImageChoose = (Button) findViewById(R.id.buttonImageChoose);
        btnNext = (Button) findViewById(R.id.buttonNext2);
        btnImageChoose.setOnClickListener(this);
        btnNext.setOnClickListener(this);

        requestQueue = Volley.newRequestQueue(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Uploading...");

        sharedPreferences=getSharedPreferences(Util.PREFS_NAME,MODE_PRIVATE);
        editor=sharedPreferences.edit();



    }

    @Override
    public void onClick(View v) {

        if (v.getId()==R.id.buttonImageChoose){
            openGalleryImage();
        }
        else if (v.getId()==R.id.buttonNext2){

            imageTitle = txtImgTitle.getText().toString().trim();

            image = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

            encodedImage = getStringImage(image);

            if (selectedPath != null){
            uploadImage();
            }
            else {
                Intent intent = new Intent(ImageActivity.this, AudioActivity.class);
                intent.putExtra("rcvStory", rcvStory);
                //intent.putExtra("ImageTitle",txtImgTitle.getText().toString().trim());
                startActivity(intent);
            }

        }

    }



    public void openGalleryImage(){

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Image "),101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {

            if (requestCode == 101)
            {
                System.out.println("SELECT_IMAGE");
                Uri selectedImageUri = data.getData();

                selectedPath = Util.getPath(this,selectedImageUri);
                rcvStory.setImageProof(selectedPath);
                imageView.setImageURI(selectedImageUri);


            }

        }
    }






    void uploadImage(){
        progressDialog.show();


        StringRequest stringRequest = new StringRequest(Request.Method.POST, Util.urlImageUpload, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.i("imgRespone",response);
               Toast.makeText(getApplication(), "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                // progressDialog.dismiss();

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int success=jsonObject.getInt("success");
                    String message=jsonObject.getString("message");

                    if (success == 1){

                        //progressDialog.dismiss();

                        editor.putString(Util.PREFS_KEYIMAGE,selectedPath);
                        editor.commit();

                        Intent intent = new Intent(ImageActivity.this,AudioActivity.class);
                        intent.putExtra("keyStoryBean",rcvStory);
                        startActivity(intent);

                        Toast.makeText(getApplication(), "Story Uploaded Success"  + message , Toast.LENGTH_SHORT).show();
                    }else {
                       // progressDialog.dismiss();

                        Toast.makeText(getApplication(), "Story Uploaded Failure"  + message , Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                  progressDialog.dismiss();


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();

                Toast.makeText(getApplication(), "Some Volley Error" + error.getMessage(), Toast.LENGTH_SHORT).show();


            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("imageName",imageTitle);
                map.put("imageData", encodedImage);
                return map;
            }

        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                100000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);

    }


    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);

    }




}
