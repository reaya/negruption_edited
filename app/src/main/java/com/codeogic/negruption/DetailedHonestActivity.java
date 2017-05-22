package com.codeogic.negruption;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DetailedHonestActivity extends AppCompatActivity {
    @InjectView(R.id.tVName_Honest)
    TextView txtUserName;

    @InjectView(R.id.tVStoryTitle_honest)
    TextView txtStoryTitle;

    @InjectView(R.id.tVStoryDesc_honest)
    TextView txtStoryDesc;

    @InjectView(R.id.tVDepartment_honest)
    TextView txtDepartment;

    @InjectView(R.id.tVPlace_honest)
    TextView txtPlace;


    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_honest);
        ButterKnife.inject(this);

        Intent rcv = getIntent();
        StoryBean story = (StoryBean)rcv.getSerializableExtra("keyHonestStory");

        txtUserName.setText(story.getUsername());
        txtStoryTitle.setText(story.getStoryTitle());
        txtDepartment.setText(story.getDepartment());
        txtPlace.setText(story.getPlace());
        txtStoryDesc.setText(story.getStoryDesc());

       /* progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");

        progressDialog.show();*/

        Log.i("info",story.toString());
    }
}
