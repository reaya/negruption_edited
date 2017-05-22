package com.codeogic.negruption;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

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

public class RetrieveHonestStory extends AppCompatActivity implements AdapterView.OnItemClickListener {
    ListView honestStories;
    ArrayList<StoryBean> stories;
    HonestStoryAdapter adapter;
    StoryBean storyBean;
    RequestQueue requestQueue;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retrieve_honest_story);
        honestStories = (ListView)findViewById(R.id.listStories_honest);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);

        requestQueue = Volley.newRequestQueue(this);
        retrieveHonestStory();
    }

    void retrieveHonestStory(){
        progressDialog.show();
        stories = new ArrayList<>();

        StringRequest request = new StringRequest(Request.Method.GET, Util.RETRIEVE_STORY, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("stories");

                    int  sid=0,views=0;
                    String username="",title="",description="",privacy="",u="Anonymous",category ="";

                    for(int i=0;i<jsonArray.length();i++) {
                        JSONObject jObj = jsonArray.getJSONObject(i);

                        username = jObj.getString("name");
                        sid = jObj.getInt("storyId");
                        title = jObj.getString("storyTitle");
                        description = jObj.getString("storyDesc");
                        privacy = jObj.getString("privacy");
                        category = jObj.getString("category");
                        views = jObj.getInt("views");

                        if (category.equals("Honest")) {

                            if (privacy.equals("Anonymous")) {
                                stories.add(new StoryBean(0, sid, title, null, null, description, null, null, null, u,views));
                            } else {
                                Log.i("name", username);
                                stories.add(new StoryBean(0, sid, title, null, null, description, null, null, null, username,views));
                            }

                        }
                    }

                    adapter = new HonestStoryAdapter(RetrieveHonestStory.this,R.layout.honest_list_item,stories);

                    honestStories.setAdapter(adapter);
                    honestStories.setOnItemClickListener(RetrieveHonestStory.this);
                    progressDialog.dismiss();

                }catch (Exception e){
                    e.printStackTrace();
                    progressDialog.dismiss();
                    Toast.makeText(RetrieveHonestStory.this,"Some Exception"+ e,Toast.LENGTH_LONG).show();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(RetrieveHonestStory.this,"Some Error"+error,Toast.LENGTH_LONG).show();

            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(this).add(request);
        requestQueue.add(request);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        storyBean = stories.get(position);
        Toast.makeText(RetrieveHonestStory.this,"You clicked"+storyBean.getUsername(),Toast.LENGTH_LONG).show();
        //int c = count++;

    }
}
