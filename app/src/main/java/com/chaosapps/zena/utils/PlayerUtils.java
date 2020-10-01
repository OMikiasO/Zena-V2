package com.chaosapps.zena.utils;

import android.app.Activity;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.chaosapps.zena.models.NewsDetailsModel;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

public class PlayerUtils {
    private static final String TAG = "PlayerUtils";
    private static PlayerUtils INSTANCE;

    public boolean auto_play = true;

    public static synchronized PlayerUtils getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PlayerUtils();
        }
        return INSTANCE;
    }

    private String currentUrl;
    public MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(false);
    public MutableLiveData<NewsDetailsModel> currentlyPlayingNewsModel = new MutableLiveData<>();
    public MutableLiveData<Integer> position = new MutableLiveData<>(0);

    public void initMediaPlayer(Activity activity, NewsDetailsModel newsDetailsModel) {
        try {
            currentlyPlayingNewsModel.setValue(newsDetailsModel);
            if (newsDetailsModel.getAudio().equals(currentUrl)) {
                if (player != null) return;
            } else {
                if (player != null) player.release();
            }

            RenderersFactory renderersFactory = new DefaultRenderersFactory(activity);
            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
            TrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
            TrackSelector trackSelector = new DefaultTrackSelector(trackSelectionFactory);
            LoadControl loadControl = new DefaultLoadControl();

            player = ExoPlayerFactory.newSimpleInstance(activity, renderersFactory, trackSelector, loadControl);

            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(activity, "ExoplayerDemo");
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            Handler mainHandler = new Handler();
            MediaSource mediaSource = new ExtractorMediaSource(Uri.parse(newsDetailsModel.getAudio()),
                    dataSourceFactory,
                    extractorsFactory,
                    mainHandler,
                    null);

            player.prepare(mediaSource);
            player.setPlayWhenReady(auto_play);
            addListeners();
            currentUrl = newsDetailsModel.getAudio();
        } catch (Exception e) {
            Utils.getInstance().recordException(e);
        }
    }

    private void addListeners() {
        try {
            player.addListener(new Player.EventListener() {
                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    Log.e(TAG, "Player state changed");
                    Log.e(TAG, (playbackState == PlaybackState.STATE_STOPPED) + "");
                    if (playbackState == PlaybackState.STATE_STOPPED) {
                        isPlaying.setValue(false);
                        currentUrl = "";
                    } else {
                        isPlaying.setValue(true);
                    }
                }
            });
        } catch (Exception e) {
            Utils.getInstance().recordException(e);
        }
    }

    public SimpleExoPlayer player;


}
