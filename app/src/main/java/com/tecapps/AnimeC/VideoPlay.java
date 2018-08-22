package com.tecapps.AnimeC;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adapter.RelatedGridAdapter;
import com.example.dailymotion.DailyMotionPlay;
import com.example.favorite.DatabaseHandler;
import com.example.favorite.ItemDb;
import com.example.item.ItemLatest;
import com.example.item.ItemRelated;
import com.example.play.MyPlayerActivity;
import com.example.play.OpenYouTubePlayerActivity;
import com.example.util.Constant;
import com.example.util.ItemOffsetDecoration;
import com.example.util.JsonUtils;
import com.example.vimeo.Vimeo;
import com.example.youtube.YoutubePlay;
import com.github.ornolfr.ratingview.RatingView;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.IntroductoryOverlay;
import com.squareup.picasso.Picasso;
import com.zanjou.http.debug.Logger;
import com.zanjou.http.request.FileUploadListener;
import com.zanjou.http.request.Request;
import com.zanjou.http.request.RequestStateListener;
import com.zanjou.http.response.JsonResponseListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class VideoPlay extends AppCompatActivity {

    public DatabaseHandler db;
    private Menu menu;
    String vid, video_cat_name, vrate, video_type, video_title, video_url, video_playid, video_thumbnail_b, video_thumbnail_s, video_duration, video_description;
    Toolbar toolbar;
    List<ItemLatest> arrayOfLatestVideo;
    ArrayList<ItemRelated> arrayOfRelated;
    private ItemLatest objAllBean;
    private ItemRelated objAllBeanrelated;
    ImageView vp_imageview, img_play;
    TextView txt_name;
    WebView webdesc;
    LinearLayout linearContent;
    RecyclerView gridViewrela;
    RelatedGridAdapter objAdapterrelated;
    private int columnWidth;
    JsonUtils jsonUtils;
    private FragmentManager fragmentManager;
    RatingView ratingView;
    String rateMsg, strMessage;
    ProgressDialog pDialog;
    MyApplication MyApp;
    String deviceId;
    LinearLayout adLayout;
    private CastContext mCastContext;
    private MenuItem mediaRouteMenuItem;
    private IntroductoryOverlay mIntroductoryOverlay;
    private CastStateListener mCastStateListener;
    private static final String TAG = "VideoPlay";
    private boolean mIsHoneyCombOrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        MyApp = MyApplication.getAppInstance();
        pDialog = new ProgressDialog(VideoPlay.this);

        mCastStateListener = new CastStateListener() {
            @Override
            public void onCastStateChanged(int newState) {
                if (newState != CastState.NO_DEVICES_AVAILABLE) {
                    showIntroductoryOverlay();
                }
            }
        };

        mCastContext = CastContext.getSharedInstance(this);

        jsonUtils = new JsonUtils(this);
        jsonUtils.forceRTLIfSupported(getWindow());

        db = new DatabaseHandler(this);
        arrayOfLatestVideo = new ArrayList<>();
        arrayOfRelated = new ArrayList<>();
        fragmentManager = getSupportFragmentManager();

        vp_imageview = (ImageView) findViewById(R.id.img_gmain);
        txt_name = (TextView) findViewById(R.id.text_title);
        img_play = (ImageView) findViewById(R.id.img_play);
        webdesc = (WebView) findViewById(R.id.desweb);
        gridViewrela = (RecyclerView) findViewById(R.id.vertical_courses_list);
        gridViewrela.setHasFixedSize(true);
        gridViewrela.setNestedScrollingEnabled(false);
        gridViewrela.setLayoutManager(new LinearLayoutManager(VideoPlay.this, LinearLayoutManager.HORIZONTAL, false));
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(VideoPlay.this, R.dimen.item_offset);
        gridViewrela.addItemDecoration(itemDecoration);
        linearContent = (LinearLayout) findViewById(R.id.rel_c_content);
        adLayout = (LinearLayout) findViewById(R.id.adview);

        if (JsonUtils.isNetworkAvailable(VideoPlay.this)) {
            new MyTask().execute(Constant.SINGLE_VIDEO_URL + Constant.VIDEO_IDD + "&api_key=" + Constant.SERVER_API_KEY);
        } else {
            showToast(getString(R.string.network_msg));
        }
        if (JsonUtils.personalization_ad) {
            JsonUtils.showPersonalizedAds(adLayout, VideoPlay.this);
        } else {
            JsonUtils.showNonPersonalizedAds(adLayout, VideoPlay.this);
        }

    }

    private class MyTask extends AsyncTask<String, Void, String> {

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
                        objItem.setVideoImgBig(objJson.getString(Constant.LATEST_IMAGE_URL_BIG));
                        objItem.setVideoRate(objJson.getString(Constant.LATEST_RATE));

                        arrayOfLatestVideo.add(objItem);

                        JSONArray jsonArraychild = objJson.getJSONArray(Constant.RELATED_ARRAY);
                        if (jsonArraychild.length() == 0) {

                        } else {
                            for (int j = 0; j < jsonArraychild.length(); j++) {
                                JSONObject objChild = jsonArraychild.getJSONObject(j);
                                ItemRelated item = new ItemRelated();
                                item.setRId(objChild.getString(Constant.RELATED_ID));
                                item.setRVideoName(objChild.getString(Constant.RELATED_NAME));
                                item.setRVideoType(objChild.getString(Constant.RELATED_TYPE));
                                item.setRCategoryName(objChild.getString(Constant.RELATED_CNAME));
                                item.setRVideoId(objChild.getString(Constant.RELATED_PID));
                                item.setRImageUrl(objChild.getString(Constant.RELATED_IMG));
                                item.setRDuration(objChild.getString(Constant.RELATED_TIME));
                                item.setRVideoRate(objJson.getString(Constant.RELATED_RATE));
                                arrayOfRelated.add(item);
                            }
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                setAdapterToListview();
            }

        }

        public void setAdapterToListview() {

            objAllBean = arrayOfLatestVideo.get(0);

            vid = objAllBean.getId();
            video_cat_name = objAllBean.getCategoryName();
            video_type = objAllBean.getVideoType();
            video_title = objAllBean.getVideoName();
            video_url = objAllBean.getVideoUrl();
            video_playid = objAllBean.getVideoId();
            video_thumbnail_b = objAllBean.getVideoImgBig();
            video_thumbnail_s = objAllBean.getImageUrl();
            video_duration = objAllBean.getDuration();
            video_description = objAllBean.getDescription();
            vrate = objAllBean.getVideoRate();

            if (video_type.equals("local")) {
                Picasso.with(VideoPlay.this).load(video_thumbnail_b).placeholder(R.drawable.placeholder).into(vp_imageview);
            } else if (video_type.equals("server_url")) {
                Picasso.with(VideoPlay.this).load(video_thumbnail_b).placeholder(R.drawable.placeholder).into(vp_imageview);
            } else if (video_type.equals("youtube")) {
                Picasso.with(VideoPlay.this).load(Constant.YOUTUBE_IMAGE_FRONT + video_playid + Constant.YOUTUBE_SMALL_IMAGE_BACK).placeholder(R.drawable.placeholder).into(vp_imageview);
            } else if (video_type.equals("dailymotion")) {
                Picasso.with(VideoPlay.this).load(Constant.DAILYMOTION_IMAGE_PATH + video_playid).placeholder(R.drawable.placeholder).into(vp_imageview);
            } else if (video_type.equals("vimeo")) {
                Picasso.with(VideoPlay.this).load(video_thumbnail_b).placeholder(R.drawable.placeholder).into(vp_imageview);
            }

            txt_name.setText(video_title);

            webdesc.setBackgroundColor(0);
            webdesc.setFocusableInTouchMode(false);
            webdesc.setFocusable(false);
            webdesc.getSettings().setDefaultTextEncodingName("UTF-8");

            String mimeType = "text/html;charset=UTF-8";
            String encoding = "utf-8";
            String htmlText = video_description;

            String text = "<html><head>"
                    + "<style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/myfonts/custom.ttf\")}body{font-family: MyFont;color: #545454;text-align:justify}"
                    + "</style></head>"
                    + "<body>"
                    + htmlText
                    + "</body></html>";

            webdesc.loadDataWithBaseURL(null, text, mimeType, encoding, null);
            img_play.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub

                    if (video_type.equals("local")) {
                        Intent lVideoIntent = new Intent(null, Uri.parse("file://" + video_url), VideoPlay.this, MyPlayerActivity.class);
                        startActivity(lVideoIntent);
                    } else if (video_type.equals("server_url")) {
                        Intent lVideoIntent = new Intent(null, Uri.parse("file://" + video_url), VideoPlay.this, MyPlayerActivity.class);
                        startActivity(lVideoIntent);
                    } else if (video_type.equals("youtube")) {
                        Intent i = new Intent(VideoPlay.this, YoutubePlay.class);
                        i.putExtra("id", video_playid);
                        startActivity(i);
                    } else if (video_type.equals("dailymotion")) {
                        Intent i = new Intent(VideoPlay.this, DailyMotionPlay.class);
                        i.putExtra("id", video_playid);
                        startActivity(i);
                    } else if (video_type.equals("vimeo")) {
                        Intent i = new Intent(VideoPlay.this, Vimeo.class);
                        i.putExtra("id", video_playid);
                        startActivity(i);
                    }
                }
            });

            RelatedVideoContent();

            if (MyApp.getIsLogin()) {
                CommentFragment commentFragment = CommentFragment.newInstance(vid);
                fragmentManager.beginTransaction().replace(R.id.comment_container, commentFragment).commitAllowingStateLoss();
            }


        }
    }

    public void RelatedVideoContent() {

        objAdapterrelated = new RelatedGridAdapter(VideoPlay.this, arrayOfRelated);
        gridViewrela.setAdapter(objAdapterrelated);

    }

    public void showToast(String msg) {
        Toast.makeText(VideoPlay.this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        mCastContext.addCastStateListener(mCastStateListener);
        super.onResume();
    }

    @Override
    protected void onPause() {
        mCastContext.removeCastStateListener(mCastStateListener);
        super.onPause();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.details_menu, menu);
        this.menu = menu;
        mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu, R.id.media_route_menu_item);
        FirstFav();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;


            case R.id.menu_fav:
                List<ItemDb> pojolist = db.getFavRow(Constant.VIDEO_IDD);
                if (pojolist.size() == 0) {
                    AddtoFav();//if size is zero i.e means that record not in database show add to favorite
                } else {
                    if (pojolist.get(0).getvid().equals(Constant.VIDEO_IDD)) ;
                    {
                        RemoveFav();
                    }

                }

                return true;

            case R.id.menu_rate:
                if (MyApp.getIsLogin()) {
                    showRating();
                } else {
                    showLogin();
                }

                return true;

            case R.id.menu_share:

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_video_msg) + "\n" + video_title + "\n" + video_url + "\n" + "https://play.google.com/store/apps/details?id=" + getPackageName());
                sendIntent.setType("text/plain");
                startActivity(sendIntent);

                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }


    public void AddtoFav() {

        db.AddtoFavorite(new ItemDb(Constant.VIDEO_IDD, video_cat_name, video_type, video_playid, video_title, video_thumbnail_s, video_duration, vrate));
        Toast.makeText(getApplicationContext(), getString(R.string.add_favorite_msg), Toast.LENGTH_SHORT).show();
        menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.fav_hover));
    }

    public void RemoveFav() {

        db.RemoveFav(new ItemDb(Constant.VIDEO_IDD));
        Toast.makeText(getApplicationContext(), getString(R.string.remove_favorite_msg), Toast.LENGTH_SHORT).show();
        menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.fav));

    }

    public void FirstFav() {

        List<ItemDb> pojolist = db.getFavRow(Constant.VIDEO_IDD);
        if (pojolist.size() == 0) {
            menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.fav));

        } else {
            if (pojolist.get(0).getvid().equals(Constant.VIDEO_IDD)) {
                menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.fav_hover));

            }

        }
    }

    private void showRating() {

        final Dialog mDialog = new Dialog(VideoPlay.this, R.style.Theme_AppCompat_Translucent);
        mDialog.setContentView(R.layout.rate_dialog);
        ratingView = (RatingView) mDialog.findViewById(R.id.ratingView);
        ratingView.setRating(Float.parseFloat(vrate));
        Button button = (Button) mDialog.findViewById(R.id.btn_submit);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (JsonUtils.isNetworkAvailable(VideoPlay.this)) {
                    uploadData();
                } else {
                    showToast(getString(R.string.network_msg));
                }
                mDialog.dismiss();
            }
        });
        mDialog.show();
    }


    private void showLogin() {

        final Dialog mDialog = new Dialog(VideoPlay.this, R.style.Theme_AppCompat_Translucent);
        mDialog.setContentView(R.layout.need_login_dialog);

        Button button_yes = (Button) mDialog.findViewById(R.id.btn_yes);
        Button button_no = (Button) mDialog.findViewById(R.id.btn_no);

        button_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent_go = new Intent(VideoPlay.this, SignInActivity.class);
                intent_go.putExtra("isfromdetail", true);
                intent_go.putExtra("isvideoid", vid);
                startActivity(intent_go);
                mDialog.dismiss();
            }
        });

        button_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
            }
        });
        mDialog.show();
    }

    public void uploadData() {
        deviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        Request request = Request.create(Constant.RATE_URL);
        request.setMethod("POST")
                .setTimeout(120)
                .setLogger(new Logger(Logger.ERROR))
                .addParameter("post_id", vid)
                .addParameter("device_id", deviceId)
                .addParameter("user_id", MyApp.getUserId())
                .addParameter("rate", ratingView.getRating());


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

    private void showIntroductoryOverlay() {
        if (mIntroductoryOverlay != null) {
            mIntroductoryOverlay.remove();
        }
        if ((mediaRouteMenuItem != null) && mediaRouteMenuItem.isVisible()) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    mIntroductoryOverlay = new IntroductoryOverlay.Builder(
                            VideoPlay.this, mediaRouteMenuItem)
                            .setTitleText("Introducing Cast")
                            .setSingleTime()
                            .setOnOverlayDismissedListener(
                                    new IntroductoryOverlay.OnOverlayDismissedListener() {
                                        @Override
                                        public void onOverlayDismissed() {
                                            mIntroductoryOverlay = null;
                                        }
                                    })
                            .build();
                    mIntroductoryOverlay.show();
                }
            });
        }
    }

}
