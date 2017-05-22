package com.codeogic.negruption;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ManageAccountActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    ListView listView;
    ArrayList<String> accountList;
    ArrayAdapter<String> adapter;
    User user;
    int pos;
    RequestQueue requestQueue;
    int userId;
    String username;
    ProgressDialog progressDialog;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_account);
        initViews();
    }

    void initViews(){
        listView= (ListView)findViewById(R.id.listView_account);
        accountList = new ArrayList<>();
        accountList.add("Update Your Account Details");
        accountList.add("Delete Your Account");

        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,accountList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);

        requestQueue = Volley.newRequestQueue(this);
        sharedPreferences=getSharedPreferences(Util.PREFS_NAME,MODE_PRIVATE);
        editor= sharedPreferences.edit();

        //username=sharedPreferences.getString(Util.PREFS_KEYUSERNAME,"");
        userId = sharedPreferences.getInt(Util.PREFS_KEYUSERID,0);

        Log.i("uid",String.valueOf(userId));

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        pos= position;
        String account = accountList.get(position);
        //Toast.makeText(this,"You Clicked: "+account.toString(),Toast.LENGTH_LONG).show();
        if(position == 0){

            /*UserTask u =new UserTask();
            u.execute();*/
            retrieveUser();
            /*Intent rcv = getIntent();
            User current = (User)rcv.getSerializableExtra("currentUser");
            Intent intent = new Intent(ManageAccountActivity.this,RegisterActivity.class);
            intent.putExtra("keyUser",current);
            startActivity(intent);*/


        }else if(position ==1){
           deleteUser();
        }
    }

    void deleteUser(){
        //User user = new User();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete ");
        builder.setMessage("Do you wish to Delete?");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                deleteFromCloud();

            }
        });
        builder.setNegativeButton("Cancel",null);
        builder.create().show();
    }

    void deleteFromCloud(){
        progressDialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, Util.DELETE_USER, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject jsonObject = new JSONObject(response);
                    int success = jsonObject.getInt("success");
                    String message = jsonObject.getString("message");

                    if(success == 1){

                        Toast.makeText(ManageAccountActivity.this,message,Toast.LENGTH_LONG).show();
                        Intent i = new Intent(ManageAccountActivity.this,SplashActivity.class);
                        startActivity(i);
                        finish();
                    }else{
                        Toast.makeText(ManageAccountActivity.this,message,Toast.LENGTH_LONG).show();
                    }
                    progressDialog.dismiss();
                }catch (Exception e){
                    e.printStackTrace();
                    progressDialog.dismiss();
                    Toast.makeText(ManageAccountActivity.this,"Some Exception"+e,Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(ManageAccountActivity.this,"Some Volley Error: "+error.getMessage(),Toast.LENGTH_LONG).show();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> map = new HashMap<>();
                map.put("uid",String.valueOf(userId));

                return map;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(this).add(request);
        requestQueue.add(request);

    }
    void retrieveUser(){
        progressDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Util.RETRIEVE_USER, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("users");

                    int  id=0;
                    String name="",phone="",email="",gender="",username="",password="";

                    for(int i=0;i<jsonArray.length();i++) {
                        JSONObject jObj = jsonArray.getJSONObject(i);

                        id= jObj.getInt("id");
                        name = jObj.getString("name");
                        phone = jObj.getString("phone");
                        email = jObj.getString("email");
                        gender = jObj.getString("gender");
                        username = jObj.getString("username");
                        password = jObj.getString("password");


                    }
                    if(id==userId){
                    user = new User(id,name,phone,email,gender,username,password);
                    Intent intent = new Intent(ManageAccountActivity.this,RegisterActivity.class);
                    intent.putExtra("keyUser",user);
                    startActivity(intent);}


                }catch (Exception e){
                    e.printStackTrace();
                    progressDialog.dismiss();
                    Toast.makeText(ManageAccountActivity.this,"Some Exception"+ e,Toast.LENGTH_LONG).show();

                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                progressDialog.dismiss();
                Toast.makeText(ManageAccountActivity.this,"Some Error"+error,Toast.LENGTH_LONG).show();

            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(this).add(stringRequest);
        requestQueue.add(stringRequest);
    }

    /* class UserTask extends AsyncTask{
        @Override
        protected void onPreExecute() {
            progressDialog.show();

        }

        @Override
        protected Object doInBackground(Object[] params) {
            StringRequest stringRequest = new StringRequest(Request.Method.GET, Util.RETRIEVE_USER, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray jsonArray = jsonObject.getJSONArray("users");

                        int  id=0;
                        String name="",phone="",email="",gender="",username="",password="";

                        for(int i=0;i<jsonArray.length();i++) {
                            JSONObject jObj = jsonArray.getJSONObject(i);

                            id= jObj.getInt("id");
                            name = jObj.getString("name");
                            phone = jObj.getString("phone");
                            email = jObj.getString("email");
                            gender = jObj.getString("gender");
                            username = jObj.getString("username");
                            password = jObj.getString("password");

                            user = new User(id,name,phone,email,gender,username,password);
                        }


                    }catch (Exception e){
                        e.printStackTrace();

                    }


                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.i("Error",error.getMessage());

                }
            });
            requestQueue.add(stringRequest);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            progressDialog.dismiss();
            Intent intent = new Intent(ManageAccountActivity.this,RegisterActivity.class);
            intent.putExtra("keyUser",user);
            startActivity(intent);
        }
    }*/
}
