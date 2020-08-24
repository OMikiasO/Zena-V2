package com.chaosapps.zena.utils;

import android.content.Context;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

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
import com.chaosapps.zena.models.NewsDetailsModel;

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
    private int skipper = 0;
    public MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(false);
    public MutableLiveData<NewsDetailsModel> currentlyPlayingNewsModel = new MutableLiveData<>();
    public MutableLiveData<Integer> position = new MutableLiveData<>(0);

    public void initMediaPlayer(Context context, NewsDetailsModel newsDetailsModel) {
        currentlyPlayingNewsModel.setValue(newsDetailsModel);
        if (newsDetailsModel.getAudio().equals(currentUrl)) {
            if (player != null) return;
        } else {
            if (player != null) player.release();
        }

        renderersFactory = new DefaultRenderersFactory(context.getApplicationContext());
        bandwidthMeter = new DefaultBandwidthMeter();
        trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        trackSelector = new DefaultTrackSelector(trackSelectionFactory);
        loadControl = new DefaultLoadControl();

        player = ExoPlayerFactory.newSimpleInstance(context, renderersFactory, trackSelector, loadControl);

        dataSourceFactory = new DefaultDataSourceFactory(context.getApplicationContext(), "ExoplayerDemo");
        extractorsFactory = new DefaultExtractorsFactory();
        mainHandler = new Handler();
        mediaSource = new ExtractorMediaSource(Uri.parse(newsDetailsModel.getAudio()),
                dataSourceFactory,
                extractorsFactory,
                mainHandler,
                null);

        player.prepare(mediaSource);
        player.setPlayWhenReady(auto_play);
        addListeners();
        currentUrl = newsDetailsModel.getAudio();
    }

    private void addListeners() {
        player.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.e(TAG, "Player state changed");
                Log.e(TAG,  (playbackState == PlaybackState.STATE_STOPPED) +"");
                if (playbackState == PlaybackState.STATE_STOPPED) {
                    isPlaying.setValue(false);
                    currentUrl = "";
                } else {
                    isPlaying.setValue(true);
                }
            }
        });
    }

    private Handler mainHandler;
    private RenderersFactory renderersFactory;
    private BandwidthMeter bandwidthMeter;
    private LoadControl loadControl;
    private DataSource.Factory dataSourceFactory;
    private ExtractorsFactory extractorsFactory;
    private MediaSource mediaSource;
    private TrackSelection.Factory trackSelectionFactory;
    public SimpleExoPlayer player;
    private TrackSelector trackSelector;


}
