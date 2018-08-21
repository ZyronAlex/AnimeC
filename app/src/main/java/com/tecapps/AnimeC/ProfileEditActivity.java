package com.tecapps.AnimeC;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.util.Constant;
import com.example.util.JsonUtils;
import com.zanjou.http.debug.Logger;
import com.zanjou.http.request.FileUploadListener;
import com.zanjou.http.request.Request;
import com.zanjou.http.request.RequestStateListener;
import com.zanjou.http.response.JsonResponseListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;


public class ProfileEditActivity extends AppCompatActivity {


    EditText edtFullName;
    EditText edtEmail;
    EditText edtMobile;
    EditText edtPass;
    Button btnSave;
    MyApplication MyApp;
    Toolbar toolbar;
    String Name,Email,Phone,strMessage;
    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        toolbar = (Toolbar) findViewById(R.id.toolbar_profile);
        toolbar.setTitle(getString(R.string.profile_edit_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        MyApp = MyApplication.getAppInstance();
        pDialog = new ProgressDialog(ProfileEditActivity.this);

        Intent intent=getIntent();
        Name=intent.getStringExtra("u_name");
        Email=intent.getStringExtra("u_email");
        Phone=intent.getStringExtra("u_phone");

        edtFullName = (EditText) findViewById(R.id.textView_name_profile);
        edtEmail = (EditText) findViewById(R.id.textView_email_profile);
        edtMobile = (EditText) findViewById(R.id.textView_phone_profile);
        edtPass = (EditText) findViewById(R.id.textView_pass);
        btnSave = (Button) findViewById(R.id.button_edit);

        edtFullName.setText(Name);
        edtEmail.setText(Email);
        edtMobile.setText(Phone);


        btnSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (JsonUtils.isNetworkAvailable(ProfileEditActivity.this)) {
                    uploadData();
                } else {
                    showToast(getString(R.string.network_msg));
                }
            }
        });
    }

    public void uploadData() {
         Request request = Request.create(Constant.PROFILE_EDIT_URL);
        request.setMethod("POST")
                .setTimeout(120)
                .setLogger(new Logger(Logger.ERROR))
                .addParameter("user_id", MyApp.getUserId())
                .addParameter("name", edtFullName.getText().toString())
                .addParameter("email", edtEmail.getText().toString())
                .addParameter("password", edtPass.getText().toString())
                .addParameter("phone", edtMobile.getText().toString());


        request.setFileUploadListener(new FileUploadListener() {
            @Override
            public void onUploadingFile(File file, long size, long uploaded) {

            }
        })
                .setRequestStateListener(new RequestStateListener() {
                    @Override
                    public void onStart() {
                        showProgressDialog();
                    }

                    @Override
                    public void onFinish() {
                        dismissProgressDialog();
                    }

                    @Override
                    public void onConnectionError(Exception e) {
                        e.printStackTrace();
                    }
                })
                .setResponseListener(new JsonResponseListener() {
                    @Override
                    public void onOkResponse(JSONObject jsonObject) throws JSONException {
                        JSONArray jsonArray = jsonObject.getJSONArray(Constant.LATEST_ARRAY_NAME);
                        JSONObject objJson = jsonArray.getJSONObject(0);
                        if (objJson.has(Constant.MSG)) {
                            strMessage = objJson.getString(Constant.MSG);
                            Constant.GET_SUCCESS_MSG = objJson.getInt(Constant.SUCCESS);
                        } else {
                            Constant.GET_SUCCESS_MSG = objJson.getInt(Constant.SUCCESS);

                        }
                        setResult();

                    }

                    @Override
                    public void onErrorResponse(JSONObject jsonObject) throws JSONException {

                    }

                    @Override
                    public void onParseError(JSONException e) {
                    }
                }).execute();
    }

    public void setResult() {

        showToast(strMessage);
        finish();


    }

    public void showProgressDialog() {
        pDialog.setMessage(getResources().getString(R.string.loading));
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();
    }

    public void dismissProgressDialog() {
        pDialog.dismiss();
    }

    public void showToast(String msg) {
        Toast.makeText(ProfileEditActivity.this, msg, Toast.LENGTH_LONG).show();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
        switch (menuItem.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                break;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }
}
