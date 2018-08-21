package com.tecapps.AnimeC;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.item.ItemAbout;
import com.example.util.Constant;
import com.example.util.JsonUtils;
import com.google.ads.consent.ConsentForm;
import com.google.ads.consent.ConsentFormListener;
import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import cn.gavinliu.android.lib.shapedimageview.ShapedImageView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private FragmentManager fragmentManager;
    NavigationView navigationView;
    private AdView mAdView;
    Toolbar toolbar;
    LinearLayout lay_dev;
    ArrayList<ItemAbout> mListItem;
    TextView txt_develop, txt_devname;
    JsonUtils jsonUtils;
    MyApplication myApplication;
    LinearLayout adLayout;
    private ConsentForm form;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        myApplication = MyApplication.getAppInstance();

        jsonUtils = new JsonUtils(this);
        jsonUtils.forceRTLIfSupported(getWindow());

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        lay_dev = (LinearLayout) findViewById(R.id.dev_lay);
        txt_develop = (TextView) findViewById(R.id.text_develop);
        txt_devname = (TextView) findViewById(R.id.dev_name);
        adLayout = (LinearLayout) findViewById(R.id.adview);

        Typeface tf = Typeface.createFromAsset(getAssets(), "myfonts/custom.ttf");
        Typeface tfbold = Typeface.createFromAsset(getAssets(), "myfonts/custom.ttf");
        txt_devname.setTypeface(tfbold);
        txt_develop.setTypeface(tf);


        mListItem = new ArrayList<>();
        if (JsonUtils.isNetworkAvailable(MainActivity.this)) {
            new MyTaskDev().execute(Constant.ABOUT_US_URL);
        } else {
            showToast(getString(R.string.network_msg));
        }
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        mDrawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }
        fragmentManager = getSupportFragmentManager();
        HomeFragment currenthome = new HomeFragment();
        fragmentManager.beginTransaction().replace(R.id.fragment1, currenthome).commit();

    }

    private void setupDrawerContent(final NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        int id = menuItem.getItemId();
                        switch (id) {
                            case R.id.nav_home:
                                HomeFragment currenthome = new HomeFragment();
                                fragmentManager.beginTransaction().replace(R.id.fragment1, currenthome).commit();
                                toolbar.setTitle(getString(R.string.menu_home));

                                mDrawerLayout.closeDrawers();
                                break;
                            case R.id.nav_latest:
                                Intent intent_latest = new Intent(MainActivity.this, LatestListActivity.class);
                                startActivity(intent_latest);
                                mDrawerLayout.closeDrawers();
                                break;
                            case R.id.nav_cat:
                                CategoryFragment catfragment = new CategoryFragment();
                                fragmentManager.beginTransaction().replace(R.id.fragment1, catfragment).commit();
                                toolbar.setTitle(getString(R.string.menu_category));
                                mDrawerLayout.closeDrawers();
                                break;
                            case R.id.nav_fav:
                                FavoriteFragment favfragment = new FavoriteFragment();
                                fragmentManager.beginTransaction().replace(R.id.fragment1, favfragment).commit();
                                toolbar.setTitle(getString(R.string.menu_favorite));
                                mDrawerLayout.closeDrawers();
                                break;

                            case R.id.sub_abus:
                                Intent intentab = new Intent(MainActivity.this, AboutUsActivity.class);
                                startActivity(intentab);
                                mDrawerLayout.closeDrawers();
                                break;

                            case R.id.sub_shareapp:
                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.shareapp_msg) + "\n" + "\n" + "https://play.google.com/store/apps/details?id=" + getPackageName());
                                sendIntent.setType("text/plain");
                                startActivity(sendIntent);
                                mDrawerLayout.closeDrawers();
                                break;
                            case R.id.sub_rateapp:
                                final String appName = MainActivity.this.getPackageName();
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW,
                                            Uri.parse("market://details?id="
                                                    + appName)));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    startActivity(new Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("http://play.google.com/store/apps/details?id="
                                                    + appName)));
                                }
                                mDrawerLayout.closeDrawers();
                                break;
                            case R.id.sub_privacy:
                                Intent intenpri = new Intent(MainActivity.this, Privacy_Activity.class);
                                startActivity(intenpri);
                                mDrawerLayout.closeDrawers();
                                break;
                        }
                        return true;
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private class MyTaskDev extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... params) {
            return JsonUtils.getJSONString(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

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

                        itemAbout.setappDevelop(objJson.getString(Constant.APP_DEVELOP));
                        itemAbout.setappBannerId(objJson.getString(Constant.ADS_BANNER_ID));
                        itemAbout.setappFullId(objJson.getString(Constant.ADS_FULL_ID));
                        itemAbout.setappBannerOn(objJson.getString(Constant.ADS_BANNER_ON_OFF));
                        itemAbout.setappFullOn(objJson.getString(Constant.ADS_FULL_ON_OFF));
                        itemAbout.setappFullPub(objJson.getString(Constant.ADS_PUB_ID));
                        mListItem.add(itemAbout);
                        Constant.mListItem.add(itemAbout);
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
        txt_develop.setText(itemAbout.getappDevelop());
        Constant.SAVE_ADS_BANNER_ID = itemAbout.getappBannerId();
        Constant.SAVE_ADS_FULL_ID = itemAbout.getappFullId();
        Constant.SAVE_ADS_BANNER_ON_OFF = itemAbout.getappBannerOn();
        Constant.SAVE_ADS_FULL_ON_OFF = itemAbout.getappFullOn();
        Constant.SAVE_ADS_PUB_ID=itemAbout.getappFullPub();

        checkForConsent();
    }

    public void showToast(String msg) {
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            AlertDialog.Builder alert = new AlertDialog.Builder(
                    MainActivity.this);
            alert.setTitle(getString(R.string.app_name));
            alert.setIcon(R.mipmap.app_icon);
            alert.setMessage("Você tem certeza que quer sair amigo(a)?");

            alert.setPositiveButton("Sim",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {

                            finish();
                        }

                    });
            alert.setNegativeButton("Não",
                    new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub

                        }
                    });
            alert.show();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search, menu);

        final SearchView searchView = (SearchView) menu.findItem(R.id.search)
                .getActionView();

        final MenuItem searchMenuItem = menu.findItem(R.id.search);
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                if (!hasFocus) {
                    searchMenuItem.collapseActionView();
                    searchView.setQuery("", false);
                }
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                // Do something
                Intent intent = new Intent(MainActivity.this, SearchListActivity.class);
                intent.putExtra("search", query);
                startActivity(intent);
                searchView.clearFocus();
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void setHeader() {
        View header = navigationView.getHeaderView(0);
        TextView txtHeaderName = (TextView) header.findViewById(R.id.text_username);
        TextView txtHeaderEmail = (TextView) header.findViewById(R.id.text_user_email);
        ImageView fabProfile = (ImageView) header.findViewById(R.id.image_profile);
        ImageView fabLogout = (ImageView) header.findViewById(R.id.image_logout);
        ShapedImageView image = (ShapedImageView) header.findViewById(R.id.profile_image);
        if (myApplication.getIsLogin()) {
            txtHeaderName.setText(myApplication.getUserName());
            txtHeaderEmail.setText(myApplication.getUserEmail());
            fabProfile.setVisibility(View.VISIBLE);
            fabLogout.setVisibility(View.VISIBLE);
        } else {
            fabProfile.setVisibility(View.GONE);
            fabLogout.setVisibility(View.GONE);
        }

        fabProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
                mDrawerLayout.closeDrawers();
            }
        });

        fabLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myApplication.saveIsLogin(false);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setHeader();
    }

    public void checkForConsent() {


       // ConsentInformation.getInstance(MainActivity.this).addTestDevice("65C855CE481F45A609DAC8C6E8951D53");
        // Geography appears as in EEA for test devices.
     //   ConsentInformation.getInstance(MainActivity.this).setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_EEA);
        // Geography appears as not in EEA for debug devices.
        ConsentInformation consentInformation = ConsentInformation.getInstance(MainActivity.this);
        String[] publisherIds = {Constant.SAVE_ADS_PUB_ID};
        consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
            @Override
            public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                Log.d("consentStatus", consentStatus.toString());
                // User's consent status successfully updated.
                switch (consentStatus) {
                    case PERSONALIZED:
                        JsonUtils.personalization_ad = true;
                        JsonUtils.showPersonalizedAds(adLayout, MainActivity.this);
                        break;
                    case NON_PERSONALIZED:
                        JsonUtils.personalization_ad = false;
                        JsonUtils.showNonPersonalizedAds(adLayout, MainActivity.this);
                        break;
                    case UNKNOWN:
                        if (ConsentInformation.getInstance(getBaseContext())
                                .isRequestLocationInEeaOrUnknown()) {
                            requestConsent();
                        } else {
                            JsonUtils.personalization_ad = true;
                            JsonUtils.showPersonalizedAds(adLayout, MainActivity.this);
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onFailedToUpdateConsentInfo(String errorDescription) {
                // User's consent status failed to update.
            }
        });

    }

    public void requestConsent() {
        URL privacyUrl = null;
        try {
            // TODO: Replace with your app's privacy policy URL.
            privacyUrl = new URL("https://www.your.com/privacyurl");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            // Handle error.
        }
        form = new ConsentForm.Builder(MainActivity.this, privacyUrl)
                .withListener(new ConsentFormListener() {
                    @Override
                    public void onConsentFormLoaded() {
                        showForm();
                        // Consent form loaded successfully.
                    }

                    @Override
                    public void onConsentFormOpened() {
                        // Consent form was displayed.
                    }

                    @Override
                    public void onConsentFormClosed(ConsentStatus consentStatus, Boolean userPrefersAdFree) {
                        Log.d("consentStatus_form", consentStatus.toString());
                        switch (consentStatus) {
                            case PERSONALIZED:
                                JsonUtils.personalization_ad = true;
                                JsonUtils.showPersonalizedAds(adLayout, MainActivity.this);
                                break;
                            case NON_PERSONALIZED:
                                JsonUtils.personalization_ad = false;
                                JsonUtils.showNonPersonalizedAds(adLayout, MainActivity.this);
                                break;
                            case UNKNOWN:
                                JsonUtils.personalization_ad = false;
                                JsonUtils.showNonPersonalizedAds(adLayout, MainActivity.this);
                        }
                    }

                    @Override
                    public void onConsentFormError(String errorDescription) {
                        Log.d("errorDescription", errorDescription);
                    }
                })
                .withPersonalizedAdsOption()
                .withNonPersonalizedAdsOption()
                .build();
        form.load();
    }

    private void showForm() {
        if (form != null) {
            form.show();
        }
    }

}