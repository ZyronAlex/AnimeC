package com.tecapps.AnimeC;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.item.ItemAbout;
import com.example.util.Constant;
import com.example.util.JsonUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class Privacy_Activity extends AppCompatActivity {

    ArrayList<ItemAbout> mListItem;
    ScrollView mScrollView;
    ProgressBar mProgressBar;
    WebView webView;
    JsonUtils jsonUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.menu_privacy));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        jsonUtils = new JsonUtils(this);
        jsonUtils.forceRTLIfSupported(getWindow());

        webView = (WebView) findViewById(R.id.webView);

        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mListItem = new ArrayList<>();

         if (JsonUtils.isNetworkAvailable(Privacy_Activity.this)) {
            new MyTaskAbout().execute(Constant.ABOUT_US_URL);
        }else {
            showToast(getString(R.string.network_msg));
        }
     }

    private class MyTaskAbout extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
            mScrollView.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(String... params) {
            return JsonUtils.getJSONString(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            mProgressBar.setVisibility(View.GONE);
            mScrollView.setVisibility(View.VISIBLE);
            if (null == result || result.length() == 0) {
                showToast(getString(R.string.no_data_found));
            } else {

                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.LATEST_ARRAY_NAME);
                    JSONObject objJson;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);
                        ItemAbout itemAbout = new ItemAbout();

                        itemAbout.setappPrivacy(objJson.getString(Constant.APP_PRIVACY));
                        mListItem.add(itemAbout);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setResult();
            }
        }
    }

    private void setResult() {

        ItemAbout itemAbout = mListItem.get(0);

        String mimeType = "text/html;charset=UTF-8";
        String encoding = "utf-8";
        String htmlText = itemAbout.getappPrivacy();

        String text = "<html><head>"
                + "<style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/myfonts/custom.ttf\")}body{font-family: MyFont;color: #545454;text-align:justify}"
                + "</style></head>"
                + "<body>"
                +  htmlText
                + "</body></html>";

        webView.loadDataWithBaseURL(null,text, mimeType, encoding,null);
    }


    public void showToast(String msg) {
        Toast.makeText(Privacy_Activity.this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }
}