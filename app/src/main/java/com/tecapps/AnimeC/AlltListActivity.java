package com.tecapps.AnimeC;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.adapter.LatestGridAdapter;
import com.example.item.ItemLatest;
import com.example.util.AlertDialogManager;
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


public class AlltListActivity extends AppCompatActivity {

	GridView lsv_cat;
	List<ItemLatest> arrayOfLatestVideo;
	LatestGridAdapter objAdapter;
	AlertDialogManager alert = new AlertDialogManager();
	private ItemLatest objAllBean;
	JsonUtils util;
	int textlength = 0;
 	Toolbar toolbar;
	ProgressBar pbar;
	private int columnWidth;
	InterstitialAd mInterstitial;
	JsonUtils jsonUtils;
	LinearLayout adLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_categorylist);
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setTitle(getString(R.string.all_video));
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
		}

		jsonUtils = new JsonUtils(this);
		jsonUtils.forceRTLIfSupported(getWindow());

		lsv_cat=(GridView)findViewById(R.id.gridcat);
		pbar = (ProgressBar)findViewById(R.id.progressBar);
		adLayout = (LinearLayout) findViewById(R.id.adview);
		arrayOfLatestVideo=new ArrayList<ItemLatest>();

		util=new JsonUtils(getApplicationContext());


		if (JsonUtils.isNetworkAvailable(AlltListActivity.this)) {
			new MyTask().execute(Constant.ALL_VIDEO_URL);
		} else {
			showToast(getString(R.string.network_msg));
		}
		if (JsonUtils.personalization_ad) {
			JsonUtils.showPersonalizedAds(adLayout, AlltListActivity.this);
		} else {
			JsonUtils.showNonPersonalizedAds(adLayout, AlltListActivity.this);
		}

		lsv_cat.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, final int position,
									long arg3) {
				// TODO Auto-generated method stub

				if(Constant.SAVE_ADS_FULL_ON_OFF.equals("true"))
				{
					Constant.AD_COUNT++;
					if (Constant.AD_COUNT == Constant.AD_COUNT_SHOW) {
						Constant.AD_COUNT = 0;
						mInterstitial = new InterstitialAd(AlltListActivity.this);
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
								objAllBean=arrayOfLatestVideo.get(position);
								Constant.VIDEO_IDD=objAllBean.getId();
								Intent intplay=new Intent(getApplicationContext(),VideoPlay.class);
								startActivity(intplay);

							}

							@Override
							public void onAdFailedToLoad(int errorCode) {
								objAllBean=arrayOfLatestVideo.get(position);
								Constant.VIDEO_IDD=objAllBean.getId();
								Intent intplay=new Intent(getApplicationContext(),VideoPlay.class);
								startActivity(intplay);
							}
						});
					} else {
						objAllBean=arrayOfLatestVideo.get(position);
						Constant.VIDEO_IDD=objAllBean.getId();
						Intent intplay=new Intent(getApplicationContext(),VideoPlay.class);
						startActivity(intplay);
					}

				}
				else {
					objAllBean=arrayOfLatestVideo.get(position);
					Constant.VIDEO_IDD=objAllBean.getId();
					Intent intplay=new Intent(getApplicationContext(),VideoPlay.class);
					startActivity(intplay);
				}


			}
		});
 	}

	private	class MyTask extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			pbar.setVisibility(View.VISIBLE);
			lsv_cat.setVisibility(View.GONE);

		}

		@Override
		protected String doInBackground(String... params) {
			return JsonUtils.getJSONString(params[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			pbar.setVisibility(View.INVISIBLE);
			lsv_cat.setVisibility(View.VISIBLE);

			if (null == result || result.length() == 0) {
				showToast(getString(R.string.no_data_found));

			} else {

				try {
					JSONObject mainJson = new JSONObject(result);
					JSONArray jsonArray = mainJson.getJSONArray(Constant.CATEGORY_ITEM_ARRAY_NAME);
					JSONObject objJson = null;
					for (int i = 0; i < jsonArray.length(); i++) {
						objJson = jsonArray.getJSONObject(i);

						ItemLatest objItem = new ItemLatest();

						objItem.setId(objJson.getString(Constant.CATEGORY_ITEM_ID));
						objItem.setCategoryId(objJson.getString(Constant.CATEGORY_ITEM_CATID));
						objItem.setCategoryName(objJson.getString(Constant.CATEGORY_ITEM_CAT_NAME));
						objItem.setVideoUrl(objJson.getString(Constant.CATEGORY_ITEM_VIDEO_URL));
						objItem.setVideoId(objJson.getString(Constant.CATEGORY_ITEM_VIDEO_ID));
						objItem.setVideoName(objJson.getString(Constant.CATEGORY_ITEM_VIDEO_NAME));
						objItem.setDuration(objJson.getString(Constant.CATEGORY_ITEM_VIDEO_DURATION));
						objItem.setDescription(objJson.getString(Constant.LATEST_VIDEO_DESCRIPTION));
						objItem.setImageUrl(objJson.getString(Constant.LATEST_IMAGE_URL));
						objItem.setVideoType(objJson.getString(Constant.LATEST_VIDEOTYPE));
						objItem.setVideoRate(objJson.getString(Constant.LATEST_RATE));
						arrayOfLatestVideo.add(objItem);

 					}

				} catch (JSONException e) {
					e.printStackTrace();
				}


				setAdapterToListview();
			}

		}
	}

	public void setAdapterToListview() {
		objAdapter = new LatestGridAdapter(AlltListActivity.this, R.layout.latest_row_item,
				arrayOfLatestVideo,columnWidth);
		lsv_cat.setAdapter(objAdapter);
	}

	public void showToast(String msg) {
		Toast.makeText(AlltListActivity.this, msg, Toast.LENGTH_LONG).show();
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
				if(!hasFocus) {
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

				Intent intent = new Intent(AlltListActivity.this, SearchListActivity.class);
				intent.putExtra("search", query);
				startActivity(intent);
				searchView.clearFocus();
				return true;
			}
		});

 		return super.onCreateOptionsMenu(menu);
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
