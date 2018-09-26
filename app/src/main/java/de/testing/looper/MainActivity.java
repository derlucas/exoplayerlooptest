package de.testing.looper;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private SimpleExoPlayer player;
    private DataSource.Factory mediaDataSourceFactory;
    private DefaultTrackSelector trackSelector;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        mediaDataSourceFactory = ((ApplicationState) getApplication()).buildDataSourceFactory(null);
    }

    private void initializePlayer() {

        PlayerView playerView = findViewById(R.id.player_view);
        playerView.requestFocus();

        trackSelector = new DefaultTrackSelector();

        player = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(this), trackSelector);
        player.setPlayWhenReady(true);
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        player.addListener(new EventLogger());

        // uncomment this for a workaround
//        player.addListener(new EventListernerThingi());

        playerView.setPlayer(player);
        playerView.showController();
        playerView.setControllerShowTimeoutMs(1000);

        loadPlaylist();

    }

    private void loadPlaylist() {
        List<String> videos = new ArrayList<>();
        videos.add("http://www.example.com/xx_H265_65s_v2.mp4");
        videos.add("http://www.example.com/xx_H265_30s_v1.mp4");


        List<MediaSource> mediaSourceList = new ArrayList<>();

        for (String video : videos) {
            mediaSourceList.add(new ExtractorMediaSource.Factory(mediaDataSourceFactory).createMediaSource(Uri.parse(video)));
        }


        if (mediaSourceList.size() == 1) {
            player.prepare(mediaSourceList.get(0), true, true);
        } else if (mediaSourceList.size() > 1) {
            player.prepare(new ConcatenatingMediaSource(mediaSourceList.toArray(new MediaSource[]{})));
        }
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
            trackSelector = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }

        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }

    }

    @Override
    public void onStop() {
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }

        super.onStop();
    }

    private class EventListernerThingi extends Player.DefaultEventListener {
        private int changes = 0;

        @Override
        public void onPositionDiscontinuity(int reason) {
            super.onPositionDiscontinuity(reason);

            if (reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION) {
                changes++;

                if (changes >= 50) {
                    Log.d(TAG, String.format("re-loading the playlist after %d loops", changes));
                    changes = 0;
                    loadPlaylist();
                }
            }
        }
    }

}
