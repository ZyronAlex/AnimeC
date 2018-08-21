package com.tecapps.AnimeC;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.adapter.CategoryAdapter;
import com.example.item.ItemCategory;
import com.example.util.Constant;
import com.example.util.JsonUtils;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class CategoryFragment extends Fragment {

    GridView lsv_allvideos;
    List<ItemCategory> arrayOfAllvideos;
    CategoryAdapter objAdapter;
    private ItemCategory objAllBean;
    ArrayList<String> allListCatid, allListCatName, allListCatImageUrl;
    String[] allArrayCatid, allArrayCatname, allArrayCatImageurl;
    ProgressBar pbar;
    InterstitialAd mInterstitial;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_latest, container, false);

        lsv_allvideos = (GridView) rootView.findViewById(R.id.gridcat);
        arrayOfAllvideos = new ArrayList<ItemCategory>();
        pbar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        setHasOptionsMenu(true);
        getActivity().setTitle(getString(R.string.menu_category));
        allListCatid = new ArrayList<String>();
        allListCatImageUrl = new ArrayList<String>();
        allListCatName = new ArrayList<String>();

        allArrayCatid = new String[allListCatid.size()];
        allArrayCatname = new String[allListCatName.size()];
        allArrayCatImageurl = new String[allListCatImageUrl.size()];

        lsv_allvideos.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, final int position,
                                    long arg3) {
                // TODO Auto-generated method stub
                if (Constant.SAVE_ADS_FULL_ON_OFF.equals("true")) {
                    Constant.AD_COUNT++;
                    if (Constant.AD_COUNT == Constant.AD_COUNT_SHOW) {
                        Constant.AD_COUNT = 0;
                        mInterstitial = new InterstitialAd(getActivity());
                        mInterstitial.setAdUnitId(Constant.SAVE_ADS_FULL_ID);
                        AdRequest adRequest;
                        if (JsonUtils.personalization_ad) {
                            adRequest = new AdRequest.Builder()
                                    .build();
                        } else {
                            Bundle extras = new Bundle();
                            extras.putString("npa", "1");
                            adRequest = new AdRequest.Builder()
                                    .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                                    .build();
                        }
                        mInterstitial.loadAd(adRequest);
                        mInterstitial.setAdListener(new AdListener() {
                            @Override
                            public void onAdLoaded() {
                                // TODO Auto-generated method stub
                                super.onAdLoaded();
                                if (mInterstitial.isLoaded()) {
                                    mInterstitial.show();
                                }
                            }

                            public void onAdClosed() {
                                objAllBean = arrayOfAllvideos.get(position);
                                int Catid = objAllBean.getCategoryId();
                                Constant.CATEGORY_ID = objAllBean.getCategoryId();
                                Constant.CATEGORY_TITLE = objAllBean.getCategoryName();
                                Intent intcat = new Intent(getActivity(), CategoryListActivity.class);
                                startActivity(intcat);

                            }

                            @Override
                            public void onAdFailedToLoad(int errorCode) {
                                objAllBean = arrayOfAllvideos.get(position);
                                int Catid = objAllBean.getCategoryId();
                                Constant.CATEGORY_ID = objAllBean.getCategoryId();
                                Constant.CATEGORY_TITLE = objAllBean.getCategoryName();
                                Intent intcat = new Intent(getActivity(), CategoryListActivity.class);
                                startActivity(intcat);
                            }
                        });
                    } else {
                        objAllBean = arrayOfAllvideos.get(position);
                        int Catid = objAllBean.getCategoryId();
                        Constant.CATEGORY_ID = objAllBean.getCategoryId();
                        Constant.CATEGORY_TITLE = objAllBean.getCategoryName();
                        Intent intcat = new Intent(getActivity(), CategoryListActivity.class);
                        startActivity(intcat);
                    }
                } else {
                    objAllBean = arrayOfAllvideos.get(position);
                    int Catid = objAllBean.getCategoryId();
                    Constant.CATEGORY_ID = objAllBean.getCategoryId();
                    Constant.CATEGORY_TITLE = objAllBean.getCategoryName();
                    Intent intcat = new Intent(getActivity(), CategoryListActivity.class);
                    startActivity(intcat);
                }

            }
        });

        if (JsonUtils.isNetworkAvailable(getActivity())) {
            new MyTask().execute(Constant.CATEGORY_URL);
        } else {
            showToast(getString(R.string.network_msg));

        }

        return rootView;
    }

    private class MyTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pbar.setVisibility(View.VISIBLE);
            lsv_allvideos.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(String... params) {
            return JsonUtils.getJSONString(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            pbar.setVisibility(View.INVISIBLE);
            lsv_allvideos.setVisibility(View.VISIBLE);

            if (null == result || result.length() == 0) {
                showToast(getString(R.string.no_data_found));
            } else {

                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.CATEGORY_ARRAY_NAME);
                    JSONObject objJson = null;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);

                        ItemCategory objItem = new ItemCategory();
                        objItem.setCategoryName(objJson.getString(Constant.CATEGORY_NAME));
                        objItem.setCategoryId(objJson.getInt(Constant.CATEGORY_CID));
                        objItem.setCategoryImageurl(objJson.getString(Constant.CATEGORY_IMAGE));
                        arrayOfAllvideos.add(objItem);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                for (int j = 0; j < arrayOfAllvideos.size(); j++) {
                    objAllBean = arrayOfAllvideos.get(j);

                    allListCatid.add(String.valueOf(objAllBean.getCategoryId()));
                    allArrayCatid = allListCatid.toArray(allArrayCatid);

                    allListCatName.add(objAllBean.getCategoryName());
                    allArrayCatname = allListCatName.toArray(allArrayCatname);

                    allListCatImageUrl.add(objAllBean.getCategoryImageurl());
                    allArrayCatImageurl = allListCatImageUrl.toArray(allArrayCatImageurl);

                }

                setAdapterToListview();
            }

        }
    }

    public void setAdapterToListview() {
        objAdapter = new CategoryAdapter(getActivity(), R.layout.category_raw_item,
                arrayOfAllvideos);
        lsv_allvideos.setAdapter(objAdapter);
    }

    public void showToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

}
