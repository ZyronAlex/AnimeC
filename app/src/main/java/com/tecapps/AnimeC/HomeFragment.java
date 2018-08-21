package com.tecapps.AnimeC;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adapter.HomeAllGridAdapter;
import com.example.adapter.HomeLatestGridAdapter;
import com.example.item.ItemLatest;
import com.example.item.ItemSlider;
import com.example.util.Constant;
import com.example.util.JsonUtils;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator;


public class HomeFragment extends Fragment {

    List<ItemSlider> arrayofSlider;
    ItemSlider itemSlider;
    RelativeLayout mainlay;
    ProgressBar pbar;
    List<ItemLatest> arrayofLatest;
    List<ItemLatest> arrayofLatestVideoall;
    GridView gridView, gridViewallvideo;
    private int columnWidth;
    Button btn_more, btn_moreall;
    private FragmentManager fragmentManager;
    private ItemLatest objAllBean;
    TextView txt_latest, txt_latestall;
    ViewPager viewpager_main;
    ImagePagerAdapter adapter;
    CircleIndicator circleIndicator;
    int currentCount = 0;
    HomeLatestGridAdapter homeLatestGridAdapter;
    HomeAllGridAdapter homeAllGridAdapter;
    TextView txt_no_latest, txt_no_all;
    InterstitialAd mInterstitial;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        setHasOptionsMenu(true);

        arrayofSlider = new ArrayList<>();
        arrayofLatest = new ArrayList<>();
        arrayofLatestVideoall = new ArrayList<>();
        getActivity().setTitle(getString(R.string.app_name));
        viewpager_main = (ViewPager) rootView.findViewById(R.id.viewPager);
        mainlay = (RelativeLayout) rootView.findViewById(R.id.main);
        pbar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        fragmentManager = getActivity().getSupportFragmentManager();
        circleIndicator = (CircleIndicator) rootView.findViewById(R.id.indicator_unselected_background);

        gridView = (GridView) rootView.findViewById(R.id.gridcat);
        gridViewallvideo = (GridView) rootView.findViewById(R.id.gridcat_allvideo);
        btn_more = (Button) rootView.findViewById(R.id.btn_more);
        btn_moreall = (Button) rootView.findViewById(R.id.btn_moreall);
        txt_latest = (TextView) rootView.findViewById(R.id.text_title_latest);
        txt_latestall = (TextView) rootView.findViewById(R.id.text_title_latestall);
        txt_no_latest = (TextView) rootView.findViewById(R.id.text_title_latest2);
        txt_no_all = (TextView) rootView.findViewById(R.id.text_title_latestall2);

        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "myfonts/custom.ttf");
        Typeface tfbold = Typeface.createFromAsset(getActivity().getAssets(), "myfonts/custom.ttf");
        btn_more.setTypeface(tf);
        btn_moreall.setTypeface(tf);
        txt_latest.setTypeface(tfbold);
        txt_latestall.setTypeface(tfbold);

        if (JsonUtils.isNetworkAvailable(getActivity())) {
            new MyTaskSlider().execute(Constant.SLIDER_URL);
        } else {
            showToast(getString(R.string.network_msg));
        }

        btn_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_latest = new Intent(getActivity(), LatestListActivity.class);
                startActivity(intent_latest);

            }
        });

        btn_moreall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_all = new Intent(getActivity(), AlltListActivity.class);
                startActivity(intent_all);
            }
        });
        return rootView;
    }

    public class MyTaskSlider extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pbar.setVisibility(View.VISIBLE);
            mainlay.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(String... params) {
            return JsonUtils.getJSONString(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            pbar.setVisibility(View.INVISIBLE);
            mainlay.setVisibility(View.VISIBLE);

            if (null == result || result.length() == 0) {
                showToast(getString(R.string.no_data_found));

            } else {

                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.SLIDER_ARRAY);
                    JSONObject objJson = null;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);

                        ItemSlider objItem = new ItemSlider();
                        objItem.setName(objJson.getString(Constant.SLIDER_NAME));
                        objItem.setImage(objJson.getString(Constant.SLIDER_IMAGE));
                        objItem.setLink(objJson.getString(Constant.SLIDER_LINK));
                        arrayofSlider.add(objItem);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                setAdapterToFeatured();
            }

        }
    }

    public void setAdapterToFeatured() {

        adapter = new ImagePagerAdapter();
        viewpager_main.setAdapter(adapter);
        circleIndicator.setViewPager(viewpager_main);
        autoPlay(viewpager_main);

        if (JsonUtils.isNetworkAvailable(getActivity())) {
            new MyTaskVideo().execute(Constant.HOME_VIDOE_URL);

        } else {
            showToast(getString(R.string.network_msg));
        }

    }

    private void autoPlay(final ViewPager viewPager) {

        viewPager.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (adapter != null && viewpager_main.getAdapter().getCount() > 0) {
                        int position = currentCount % adapter.getCount();
                        currentCount++;
                        viewpager_main.setCurrentItem(position);
                        autoPlay(viewpager_main);
                    }
                } catch (Exception e) {
                    Log.e("TAG", "auto scroll pager error.", e);
                }
            }
        }, 2500);
    }

    private class ImagePagerAdapter extends PagerAdapter {

        private LayoutInflater inflater;

        public ImagePagerAdapter() {
            // TODO Auto-generated constructor stub

            inflater = getActivity().getLayoutInflater();
        }

        @Override
        public int getCount() {
            return arrayofSlider.size();

        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {

            View imageLayout = inflater.inflate(R.layout.viewpager_item, container, false);
            assert imageLayout != null;
            itemSlider = arrayofSlider.get(position);
            ImageView imageview = (ImageView) imageLayout.findViewById(R.id.imageView_viewitem);
            TextView textView_catname = (TextView) imageLayout.findViewById(R.id.text_slider_name);

            textView_catname.setText(itemSlider.getName());

            Picasso.with(getActivity()).load(itemSlider.getImage()).placeholder(R.drawable.placeholder).into(imageview);

            imageview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemSlider = arrayofSlider.get(position);
                    String id = itemSlider.getLink();

                    startActivity(new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(id)));

                }
            });

            container.addView(imageLayout, 0);
            return imageLayout;

        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((View) object);
        }
    }

    private class MyTaskVideo extends AsyncTask<String, Void, String> {

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
                    JSONObject mainJsonob = mainJson.getJSONObject(Constant.LATEST_ARRAY_NAME);
                    JSONArray jsonArray = mainJsonob.getJSONArray(Constant.HOME_LATEST_ARRAY_NAME);
                    JSONObject objJson = null;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);

                        ItemLatest objItem = new ItemLatest();

                        objItem.setId(objJson.getString(Constant.LATEST_ID));
                        objItem.setCategoryId(objJson.getString(Constant.LATEST_CATID));
                        objItem.setCategoryName(objJson.getString(Constant.LATEST_CAT_NAME));
                        objItem.setVideoUrl(objJson.getString(Constant.LATEST_VIDEO_URL));
                        objItem.setVideoId(objJson.getString(Constant.LATEST_VIDEO_ID));
                        objItem.setVideoName(objJson.getString(Constant.LATEST_VIDEO_NAME));
                        objItem.setDuration(objJson.getString(Constant.LATEST_VIDEO_DURATION));
                        objItem.setDescription(objJson.getString(Constant.LATEST_VIDEO_DESCRIPTION));
                        objItem.setImageUrl(objJson.getString(Constant.LATEST_IMAGE_URL));
                        objItem.setVideoType(objJson.getString(Constant.LATEST_VIDEOTYPE));
                        objItem.setVideoRate(objJson.getString(Constant.LATEST_RATE));

                        arrayofLatest.add(objItem);

                    }
                    JSONArray jsonArray2 = mainJsonob.getJSONArray(Constant.HOME_ALLVIDEO_ARRAY_NAME);
                    JSONObject objJson2 = null;
                    for (int i = 0; i < jsonArray2.length(); i++) {
                        objJson2 = jsonArray2.getJSONObject(i);

                        ItemLatest objItem = new ItemLatest();

                        objItem.setId(objJson2.getString(Constant.LATEST_ID));
                        objItem.setCategoryId(objJson2.getString(Constant.LATEST_CATID));
                        objItem.setCategoryName(objJson2.getString(Constant.LATEST_CAT_NAME));
                        objItem.setVideoUrl(objJson2.getString(Constant.LATEST_VIDEO_URL));
                        objItem.setVideoId(objJson2.getString(Constant.LATEST_VIDEO_ID));
                        objItem.setVideoName(objJson2.getString(Constant.LATEST_VIDEO_NAME));
                        objItem.setDuration(objJson2.getString(Constant.LATEST_VIDEO_DURATION));
                        objItem.setDescription(objJson2.getString(Constant.LATEST_VIDEO_DESCRIPTION));
                        objItem.setImageUrl(objJson2.getString(Constant.LATEST_IMAGE_URL));
                        objItem.setVideoType(objJson2.getString(Constant.LATEST_VIDEOTYPE));
                        objItem.setVideoRate(objJson.getString(Constant.LATEST_RATE));

                        arrayofLatestVideoall.add(objItem);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setAdapterToListview();
            }

        }
    }

    public void setAdapterToListview() {
        homeLatestGridAdapter = new HomeLatestGridAdapter(getActivity(), R.layout.home_latest_row_item,
                arrayofLatest, columnWidth);
        gridView.setAdapter(homeLatestGridAdapter);

        txt_no_latest.setText(String.valueOf(arrayofLatest.size()) + "\u0020" + getResources().getString(R.string.home_videos));
        txt_no_all.setText(String.valueOf(arrayofLatestVideoall.size()) + "\u0020" + getResources().getString(R.string.home_videos));

        homeAllGridAdapter = new HomeAllGridAdapter(getActivity(), R.layout.home_latest_row_item,
                arrayofLatestVideoall, columnWidth);
        gridViewallvideo.setAdapter(homeAllGridAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
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
                                objAllBean = arrayofLatest.get(position);
                                Constant.VIDEO_IDD = objAllBean.getId();
                                Intent intplay = new Intent(getActivity(), VideoPlay.class);
                                startActivity(intplay);


                            }

                            @Override
                            public void onAdFailedToLoad(int errorCode) {
                                objAllBean = arrayofLatest.get(position);
                                Constant.VIDEO_IDD = objAllBean.getId();
                                Intent intplay = new Intent(getActivity(), VideoPlay.class);
                                startActivity(intplay);

                            }
                        });
                    } else {
                        objAllBean = arrayofLatest.get(position);
                        Constant.VIDEO_IDD = objAllBean.getId();
                        Intent intplay = new Intent(getActivity(), VideoPlay.class);
                        startActivity(intplay);
                    }
                } else {
                    objAllBean = arrayofLatestVideoall.get(position);
                    Constant.VIDEO_IDD = objAllBean.getId();
                    Intent intplay = new Intent(getActivity(), VideoPlay.class);
                    startActivity(intplay);
                }

            }
        });

        gridViewallvideo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
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
                                objAllBean = arrayofLatestVideoall.get(position);
                                Constant.VIDEO_IDD = objAllBean.getId();
                                Intent intplay = new Intent(getActivity(), VideoPlay.class);
                                startActivity(intplay);
                            }

                            @Override
                            public void onAdFailedToLoad(int errorCode) {
                                objAllBean = arrayofLatestVideoall.get(position);
                                Constant.VIDEO_IDD = objAllBean.getId();
                                Intent intplay = new Intent(getActivity(), VideoPlay.class);
                                startActivity(intplay);
                            }
                        });
                    } else {
                        objAllBean = arrayofLatestVideoall.get(position);
                        Constant.VIDEO_IDD = objAllBean.getId();
                        Intent intplay = new Intent(getActivity(), VideoPlay.class);
                        startActivity(intplay);
                    }
                } else {
                    objAllBean = arrayofLatestVideoall.get(position);
                    Constant.VIDEO_IDD = objAllBean.getId();
                    Intent intplay = new Intent(getActivity(), VideoPlay.class);
                    startActivity(intplay);
                }

            }
        });

    }

    public void showToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }


}
