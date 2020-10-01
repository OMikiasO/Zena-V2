package com.chaosapps.zena.utils;

import android.content.Context;
import android.util.Log;

import com.chaosapps.zena.App;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

public class AdUtils {

    private static final String TAG = "AdUtils";
    private static final String AD_UNIT_ID = "ca-app-pub-1449277214264730/2254917696";
    private static final String TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/8691691433";

    private static AdUtils INSTANCE;
    private InterstitialAd interstitialAd;
    private int noOfNewsSeen = 0;
    public boolean adsInitialized = false;
    public boolean audioWasPlaying = false;

    public static AdUtils getInstance() {
        if (INSTANCE == null) INSTANCE = new AdUtils();
        return INSTANCE;
    }

    public void initAds(Context context) {
        try {
            if (!App.dynamicVariables.getValue().showAds) return;
            MobileAds.initialize(context, initializationStatus -> {
                adsInitialized = true;
                Log.e(TAG, "ADs initialized");
            });

            interstitialAd = new InterstitialAd(context);
            interstitialAd.setAdUnitId(AD_UNIT_ID);
            interstitialAd.loadAd(new AdRequest.Builder().addTestDevice("DDD36311860647F892E4518142E3C1AB").build());
            Controller.getInstance().detailsFragment.observeForever(aBoolean -> {
                if(!App.dynamicVariables.getValue().showAds) return;
                if (aBoolean) {
                    if (interstitialAd.isLoaded()) {
                        if (noOfNewsSeen % App.dynamicVariables.getValue().gapBetweenAds == 0) interstitialAd.show();
                    } else {
                        interstitialAd.loadAd(new AdRequest.Builder().addTestDevice("DDD36311860647F892E4518142E3C1AB").build());
                    }
                    noOfNewsSeen++;
                } else {
                    if (!interstitialAd.isLoaded()) {
                        interstitialAd.loadAd(new AdRequest.Builder().addTestDevice("DDD36311860647F892E4518142E3C1AB").build());
                    }
                }
            });


            interstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                    PlayerUtils.getInstance().auto_play = false;
                    if (PlayerUtils.getInstance().isPlaying.getValue()) {
                        PlayerUtils.getInstance().player.setPlayWhenReady(false);
                    }
                }
            });
        } catch (Exception e) {
            Utils.getInstance().recordException(e);
        }
    }
}
