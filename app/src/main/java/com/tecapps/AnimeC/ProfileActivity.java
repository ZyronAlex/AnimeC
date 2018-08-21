package com.tecapps.AnimeC;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.util.Constant;
import com.example.util.JsonUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class ProfileActivity extends AppCompatActivity {


    TextView edtFullName;
    TextView edtEmail;
    TextView edtMobile;
    Button btnEdit;
    MyApplication MyApp;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        toolbar = (Toolbar) findViewById(R.id.toolbar_profile);
        toolbar.setTitle(getString(R.string.profile_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        MyApp = MyApplication.getAppInstance();

        edtFullName = (TextView) findViewById(R.id.textView_name_profile);
        edtEmail = (TextView) findViewById(R.id.textView_email_profile);
        edtMobile = (TextView) findViewById(R.id.textView_phone_profile);
        btnEdit = (Button) findViewById(R.id.button_edit);

        if (JsonUtils.isNetworkAvailable(ProfileActivity.this)) {
            new MyTask().execute(Constant.PROFILE_URL + MyApp.getUserId());
        } else {
            showToast(getString(R.string.network_msg));
        }

        btnEdit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent_edit=new Intent(ProfileActivity.this,ProfileEditActivity.class);
                intent_edit.putExtra("u_name",edtFullName.getText().toString());
                intent_edit.putExtra("u_email",edtEmail.getText().toString());
                intent_edit.putExtra("u_phone",edtMobile.getText().toString());
                startActivity(intent_edit);
            }
        });
    }

    private class MyTask extends AsyncTask<String, Void, String> {

        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(ProfileActivity.this);
            pDialog.setMessage(getResources().getString(R.string.loading));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            return JsonUtils.getJSONString(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (null != pDialog && pDialog.isShowing()) {
                pDialog.dismiss();
            }

            if (null == result || result.length() == 0) {
                showToast(getString(R.string.no_data_found));
            } else {
                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.LATEST_ARRAY_NAME);
                    JSONObject objJson;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);
                        edtFullName.setText(objJson.getString(Constant.USER_NAME));
                        edtEmail.setText(objJson.getString(Constant.USER_EMAIL));
                        edtMobile.setText(objJson.getString(Constant.USER_PHONE));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void showToast(String msg) {
        Toast.makeText(ProfileActivity.this, msg, Toast.LENGTH_LONG).show();
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
