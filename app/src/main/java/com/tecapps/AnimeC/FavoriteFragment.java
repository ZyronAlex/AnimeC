package com.tecapps.AnimeC;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.example.adapter.FavoriteGridAdapter;
import com.example.favorite.DatabaseHandler;
import com.example.favorite.ItemDb;
import com.example.util.Constant;
import com.example.util.JsonUtils;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.List;


public class FavoriteFragment extends Fragment {

    GridView lsv_latest;
    DatabaseHandler db;
    private DatabaseHandler.DatabaseManager dbManager;
    FavoriteGridAdapter adapterfav;
    TextView txt_no;
    private int columnWidth;
    List<ItemDb> allData;
    ItemDb pojo;
    InterstitialAd mInterstitial;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_favorite, container, false);
        setHasOptionsMenu(true);
        getActivity().setTitle(getString(R.string.menu_favorite));
        lsv_latest = (GridView) rootView.findViewById(R.id.gridcat);

        txt_no = (TextView) rootView.findViewById(R.id.textView1);
        db = new DatabaseHandler(getActivity());
        dbManager = DatabaseHandler.DatabaseManager.INSTANCE;
        dbManager.init(getActivity());

        txt_no = (TextView) rootView.findViewById(R.id.textView1);
        db = new DatabaseHandler(getActivity());
        dbManager = DatabaseHandler.DatabaseManager.INSTANCE;
        dbManager.init(getActivity());

        allData = db.getAllData();
        adapterfav = new FavoriteGridAdapter(getActivity(), R.layout.latest_row_item,
                allData, columnWidth);
        lsv_latest.setAdapter(adapterfav);
        if (allData.size() == 0) {
            txt_no.setVisibility(View.VISIBLE);
        } else {
            txt_no.setVisibility(View.INVISIBLE);
        }

        lsv_latest.setOnItemClickListener(new AdapterView.OnItemClickListener() {

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
                                pojo = allData.get(position);
                                Constant.VIDEO_IDD = pojo.getvid();
                                Intent intplay = new Intent(getActivity(), VideoPlay.class);
                                startActivity(intplay);


                            }

                            @Override
                            public void onAdFailedToLoad(int errorCode) {
                                pojo = allData.get(position);
                                Constant.VIDEO_IDD = pojo.getvid();
                                Intent intplay = new Intent(getActivity(), VideoPlay.class);
                                startActivity(intplay);

                            }
                        });
                    } else {
                        pojo = allData.get(position);
                        Constant.VIDEO_IDD = pojo.getvid();
                        Intent intplay = new Intent(getActivity(), VideoPlay.class);
                        startActivity(intplay);
                     }
                } else {
                    pojo = allData.get(position);
                    Constant.VIDEO_IDD = pojo.getvid();
                    Intent intplay = new Intent(getActivity(), VideoPlay.class);
                    startActivity(intplay);
                }
            }
        });

        return rootView;
    }

    public void onDestroyView() {

        if (!dbManager.isDatabaseClosed())
            dbManager.closeDatabase();
        super.onDestroyView();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!dbManager.isDatabaseClosed())
            dbManager.closeDatabase();
    }

    @Override
    public void onResume() {
        super.onResume();

        allData = db.getAllData();
        adapterfav = new FavoriteGridAdapter(getActivity(), R.layout.latest_row_item,
                allData, columnWidth);
        lsv_latest.setAdapter(adapterfav);

        if (allData.size() == 0) {
            txt_no.setVisibility(View.VISIBLE);
        } else {
            txt_no.setVisibility(View.INVISIBLE);
        }

    }


}