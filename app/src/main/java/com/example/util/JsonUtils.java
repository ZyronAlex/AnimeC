package com.example.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.tecapps.AnimeC.R;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class JsonUtils {

    private Context _context;
    public static boolean personalization_ad = false;

    // constructor
    public JsonUtils(Context context) {
        this._context = context;
    }

    public static String getJSONString(String url) {
        String jsonString = null;
        HttpURLConnection linkConnection = null;
        try {
            URL linkurl = new URL(url);
            linkConnection = (HttpURLConnection) linkurl.openConnection();
            int responseCode = linkConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream linkinStream = linkConnection.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int j = 0;
                while ((j = linkinStream.read()) != -1) {
                    baos.write(j);
                }
                byte[] data = baos.toByteArray();
                jsonString = new String(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (linkConnection != null) {
                linkConnection.disconnect();
            }
        }
        return jsonString;
    }

    public static boolean isNetworkAvailable(Activity activity) {
        ConnectivityManager connectivity = (ConnectivityManager) activity
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public int getScreenWidth() {
        int columnWidth;
        WindowManager wm = (WindowManager) _context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        final Point point = new Point();

        point.x = display.getWidth();
        point.y = display.getHeight();

        columnWidth = point.x;
        return columnWidth;
    }

    public void forceRTLIfSupported(Window window) {
        if (_context.getResources().getString(R.string.isRTL).equals("true")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                window.getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }
    }

    public static void showPersonalizedAds(LinearLayout adLayout, Activity activity) {

        if (Constant.SAVE_ADS_BANNER_ON_OFF.equals("true")) {
            AdView mAdView = new AdView(activity);
            mAdView.setAdSize(AdSize.BANNER);
            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            mAdView.setAdUnitId(Constant.mListItem.get(0).getappBannerId());
            mAdView.loadAd(new AdRequest.Builder().build());
            adLayout.addView(mAdView);
            mAdView.loadAd(adRequest);
        }
    }

    public static void showNonPersonalizedAds(LinearLayout adLayout, Activity activity) {

        Bundle extras = new Bundle();
        extras.putString("npa", "1");
        if (Constant.SAVE_ADS_BANNER_ON_OFF.equals("true")) {
            AdView mAdView = new AdView(activity);
            mAdView.setAdSize(AdSize.BANNER);
            AdRequest adRequest = new AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                    .build();
            mAdView.setAdUnitId(Constant.mListItem.get(0).getappBannerId());
            adLayout.addView(mAdView);
            mAdView.loadAd(adRequest);
        }
    }

}
