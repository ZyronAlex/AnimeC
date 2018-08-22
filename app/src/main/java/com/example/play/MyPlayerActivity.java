package com.example.play;

import android.media.session.PlaybackState;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.halilibo.bettervideoplayer.BetterVideoCallback;
import com.halilibo.bettervideoplayer.BetterVideoPlayer;
import com.tecapps.AnimeC.R;


public class MyPlayerActivity extends AppCompatActivity implements BetterVideoCallback {

    private BetterVideoPlayer player;
    private CastContext mCastContext;
    private MenuItem mediaRouteMenuItem;
    private CastSession mCastSession;
    private PlaybackState mPlaybackState;
    private PlaybackLocation mLocation;
    private SessionManagerListener<CastSession> mSessionManagerListener;
    /**
     * indicates whether we are doing a local or a remote playback
     */
    public enum PlaybackLocation {
        LOCAL,
        REMOTE
    }

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

        //Set Immediately starts playback when the player becomes prepared.
        player.setAutoPlay(true);

        //Enable SwipeGestures
        player.enableSwipeGestures();
        player.enableSwipeGestures(this.getWindow());

        // Sets the callback to this Activity, since it inherits EasyVideoCallback
        player.setCallback(this);

        // Show Toolbar
        player.getToolbar().inflateMenu(R.menu.details_menu);
        player.showToolbar();

        // To play files, you can use Uri.fromFile(new File("..."))
        Uri lVideoIdUri = this.getIntent().getData();

        if (lVideoIdUri == null) {
            Log.i(this.getClass().getSimpleName(), "No video ID was specified in the intent.  Closing video activity.");
            finish();
        }
        String lVideoIdStr = lVideoIdUri.getEncodedSchemeSpecificPart();
        if (lVideoIdStr == null) {
            Log.i(this.getClass().getSimpleName(), "No video ID was specified in the intent.  Closing video activity.");
            finish();
        }
        if (lVideoIdStr.startsWith("//")) {
            if (lVideoIdStr.length() > 2) {
                lVideoIdStr = lVideoIdStr.substring(2);
            } else {
                Log.i(this.getClass().getSimpleName(), "No video ID was specified in the intent.  Closing video activity.");
                finish();
            }
        }
        player.setSource(Uri.parse(lVideoIdStr));

        // From here, the player view will show a progress indicator until the player is prepared.
        // Once it's prepared, the progress indicator goes away and the controls become enabled.
    }

    @Override
    public void onPause() {
        super.onPause();
        // Make sure the player stops playing if the user presses the home button.
        player.pause();
    }

    // Methods for the implemented EasyVideoCallback

    @Override
    public void onStarted(BetterVideoPlayer player) {
        //Log.i(TAG, "Started");
    }

    @Override
    public void onPaused(BetterVideoPlayer player) {
        //Log.i(TAG, "Paused");
    }

    @Override
    public void onPreparing(BetterVideoPlayer player) {
        //Log.i(TAG, "Preparing");
    }

    @Override
    public void onPrepared(BetterVideoPlayer player) {
        //Log.i(TAG, "Prepared");
    }

    @Override
    public void onBuffering(int percent) {
        //Log.i(TAG, "Buffering " + percent);
    }

    @Override
    public void onError(BetterVideoPlayer player, Exception e) {
        //Log.i(TAG, "Error " +e.getMessage());
    }

    @Override
    public void onCompletion(BetterVideoPlayer player) {
        //Log.i(TAG, "Completed");
    }

    @Override
    public void onToggleControls(BetterVideoPlayer player, boolean isShowing) {
        //Log.i(TAG, "Controls toggled " + isShowing);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu, R.id.media_route_menu_item);

        return true;
    }

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
            public void onSessionStarting(CastSession session) {}

            @Override
            public void onSessionEnding(CastSession session) {}

            @Override
            public void onSessionResuming(CastSession session, String sessionId) {}

            @Override
            public void onSessionSuspended(CastSession session, int reason) {}

            private void onApplicationConnected(CastSession castSession) {
                mCastSession = castSession;
                if (null != mSelectedMedia) {

                    if (mPlaybackState == PlaybackState.PLAYING) {
                        mVideoView.pause();
                        loadRemoteMedia(mSeekbar.getProgress(), true);
                        finish();
                        return;
                    } else {
                        mPlaybackState = PlaybackState.IDLE;
                        updatePlaybackLocation(PlaybackLocation.REMOTE);
                    }
                }
                updatePlayButton(mPlaybackState);
                supportInvalidateOptionsMenu();
            }

            private void onApplicationDisconnected() {
                updatePlaybackLocation(PlaybackLocation.LOCAL);
                mPlaybackState = PlaybackState.IDLE;
                mLocation = PlaybackLocation.LOCAL;
                updatePlayButton(mPlaybackState);
                supportInvalidateOptionsMenu();
            }
        };
    }
}