package com.example.play;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.example.chromecast.ExpandedControlsActivity;
import com.example.item.ItemLatest;
import com.example.util.Utils;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;
import com.halilibo.bettervideoplayer.BetterVideoCallback;
import com.halilibo.bettervideoplayer.BetterVideoPlayer;
import com.tecapps.AnimeC.R;

import static com.example.play.MyPlayerActivity.PlaybackState.IDLE;


public class MyPlayerActivity extends AppCompatActivity implements BetterVideoCallback {

    private static final String TAG = "MyPlayerActivity";
    private BetterVideoPlayer player;
    private CastContext mCastContext;
    private MenuItem mediaRouteMenuItem;
    private CastSession mCastSession;
    private PlaybackState mPlaybackState;
    private ItemLatest mSelectedMedia;
    private SessionManagerListener<CastSession> mSessionManagerListener;

    /**
     * List of various states that we can be in
     */
    public enum PlaybackState {
        PLAYING, PAUSED, BUFFERING, IDLE
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_player);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // set the flag to keep the screen ON so that the video can play without the screen being turned off
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Set ChromeCast
        mCastContext = CastContext.getSharedInstance(this);

        // Grabs a reference to the player view
        player = (BetterVideoPlayer) findViewById(R.id.player);

        //Enable SwipeGestures
        player.enableSwipeGestures(this.getWindow());

        // Sets the callback to this Activity, since it inherits EasyVideoCallback
        player.setCallback(this);

        // Sets the function of Chrome Cast SDK
        setupCastListener();
        mCastContext = CastContext.getSharedInstance(this);
        mCastSession = mCastContext.getSessionManager().getCurrentCastSession();


        // see what we need to play and where
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            mSelectedMedia = (ItemLatest) bundle.getSerializable("media");
            setupActionBar();
            Log.d(TAG, "Setting url of the VideoView to: " + mSelectedMedia.getVideoUrl());
            // To play files, you can use Uri.fromFile(new File("..."))
            player.setSource(Uri.parse(mSelectedMedia.getVideoUrl()));

            if (mCastSession != null && mCastSession.isConnected()) {
                // we should load the video but pause it
                // and show the album art.
                loadRemoteMedia(player.getCurrentPosition(), true);
            } else {
                // Set Immediately starts playback when the player becomes prepared.
                player.setAutoPlay(true);
            }
        }

        // From here, the player view will show a progress indicator until the player is prepared.
        // Once it's prepared, the progress indicator goes away and the controls become enabled.
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() was called");
        mCastContext.getSessionManager().removeSessionManagerListener(
                mSessionManagerListener, CastSession.class);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop() was called");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy() is called");
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart was called");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume() was called");
        mCastContext.getSessionManager().addSessionManagerListener(
                mSessionManagerListener, CastSession.class);
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.browse, menu);
        mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu,
                R.id.media_route_menu_item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            ActivityCompat.finishAfterTransition(this);
            onBackPressed();
        }
        return true;
    }


    // Methods for the implemented EasyVideoCallback
    @Override
    public void onStarted(BetterVideoPlayer player) {
        Log.i(TAG, "Started");
        mPlaybackState = PlaybackState.PLAYING;
    }

    @Override
    public void onPaused(BetterVideoPlayer player) {
        Log.i(TAG, "Paused");
        mPlaybackState = PlaybackState.PAUSED;
    }

    @Override
    public void onPreparing(BetterVideoPlayer player) {
        Log.i(TAG, "Preparing");
    }

    @Override
    public void onPrepared(BetterVideoPlayer player) {
        Log.i(TAG, "Prepared");
        mPlaybackState = PlaybackState.IDLE;
        updatePlayButton(mPlaybackState);
    }

    @Override
    public void onBuffering(int percent) {
        Log.i(TAG, "Buffering " + percent);
        mPlaybackState = PlaybackState.BUFFERING;
    }

    @Override
    public void onError(BetterVideoPlayer player, Exception e) {
        Log.i(TAG, "Error " + e.getMessage());
        Utils.showErrorDialog(MyPlayerActivity.this, e.getLocalizedMessage());
        player.stop();
        mPlaybackState = PlaybackState.IDLE;
        updatePlayButton(mPlaybackState);
    }

    @Override
    public void onCompletion(BetterVideoPlayer player) {
        Log.i(TAG, "Completed");
        mPlaybackState = PlaybackState.IDLE;
        updatePlayButton(mPlaybackState);
    }

    @Override
    public void onToggleControls(BetterVideoPlayer player, boolean isShowing) {
        Log.i(TAG, "Controls toggled " + isShowing);

    }


    //Methods for Chrome Cast SDK
    private void setupCastListener() {
        mSessionManagerListener = new SessionManagerListener<CastSession>() {

            @Override
            public void onSessionEnded(CastSession session, int error) {
                onApplicationDisconnected();
            }

            @Override
            public void onSessionResumed(CastSession session, boolean wasSuspended) {
                onApplicationConnected(session);
            }

            @Override
            public void onSessionResumeFailed(CastSession session, int error) {
                onApplicationDisconnected();
            }

            @Override
            public void onSessionStarted(CastSession session, String sessionId) {
                onApplicationConnected(session);
            }

            @Override
            public void onSessionStartFailed(CastSession session, int error) {
                onApplicationDisconnected();
            }

            @Override
            public void onSessionStarting(CastSession session) {
            }

            @Override
            public void onSessionEnding(CastSession session) {
            }

            @Override
            public void onSessionResuming(CastSession session, String sessionId) {
            }

            @Override
            public void onSessionSuspended(CastSession session, int reason) {
            }

            private void onApplicationConnected(CastSession castSession) {
                mCastSession = castSession;
                if (null != mSelectedMedia) {
                    if (mPlaybackState == PlaybackState.PLAYING) {
                        player.pause();
                        loadRemoteMedia(player.getCurrentPosition(), true);
                        finish();
                        return;
                    }
                }
                supportInvalidateOptionsMenu();
            }

            private void onApplicationDisconnected() {
                mPlaybackState = IDLE;
                supportInvalidateOptionsMenu();
            }
        };
    }

    private MediaInfo buildMediaInfo() {
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, mSelectedMedia.getDescription());
        movieMetadata.putString(MediaMetadata.KEY_TITLE, mSelectedMedia.getVideoName());
        movieMetadata.addImage(new WebImage(Uri.parse(mSelectedMedia.getImageUrl())));
        movieMetadata.addImage(new WebImage(Uri.parse(mSelectedMedia.getVideoImgBig())));


        return new MediaInfo.Builder(mSelectedMedia.getVideoUrl())
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType(mSelectedMedia.getVideoType())
                .setMetadata(movieMetadata)
                .setStreamDuration(player.getDuration() * 1000)
                .build();
    }

    private void loadRemoteMedia(int position, boolean autoPlay) {
        if (mCastSession == null) {
            return;
        }
        final RemoteMediaClient remoteMediaClient = mCastSession.getRemoteMediaClient();
        if (remoteMediaClient == null) {
            return;
        }
        remoteMediaClient.addListener(new RemoteMediaClient.Listener() {
            @Override
            public void onStatusUpdated() {
                Intent intent = new Intent(MyPlayerActivity.this, ExpandedControlsActivity.class);
                startActivity(intent);
                remoteMediaClient.removeListener(this);
            }

            @Override
            public void onMetadataUpdated() {
            }

            @Override
            public void onQueueStatusUpdated() {
            }

            @Override
            public void onPreloadStatusUpdated() {
            }

            @Override
            public void onSendingRemoteMediaRequest() {
            }

            @Override
            public void onAdBreakStatusUpdated() {

            }
        });
        remoteMediaClient.load(buildMediaInfo(), autoPlay, position);
    }

    private void setupActionBar() {
        Toolbar toolbar = player.getToolbar();
        toolbar.setTitle(mSelectedMedia.getVideoName());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void updatePlayButton(PlaybackState state) {
        Log.d(TAG, "Controls: PlayBackState: " + state);
        boolean isConnected = (mCastSession != null)
                && (mCastSession.isConnected() || mCastSession.isConnecting());
        if (isConnected)
            player.hideControls();
        else
            player.showControls();
    }

}