package com.chaosapps.zena;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import com.chaosapps.zena.utils.Account;
import com.chaosapps.zena.utils.CacheUtils;
import com.chaosapps.zena.utils.ConnectionUtils;
import com.chaosapps.zena.utils.Controller;
import com.chaosapps.zena.utils.PlayerUtils;
import com.chaosapps.zena.utils.Utils;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class App extends Application {
    public static final String TAG = "App";

    public static final String CHANNEL_1_ID = "channel1";
    public static MutableLiveData<DynamicVariables> dynamicVariables = new MutableLiveData<>(new DynamicVariables());


    @Override
    public void onCreate() {
        fetchDynamicVariables();
        syncSettings();
        super.onCreate();
        createNotificationChannels();
        CacheUtils.getInstance().userSharedPref = this.getSharedPreferences(CacheUtils.USER_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        CacheUtils.getInstance().getSavedNewsIds(); // init
        CacheUtils.getInstance().getSavedSearchTerms(); //init
        Account.getInstance().signIn();
        Account.getInstance().syncUserData(this);
    }

    private void syncSettings() {
        try {
            String darkTheme = PreferenceManager.getDefaultSharedPreferences(this).getString("dark_theme", "3");
            switch (darkTheme) {
                case "1":
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    break;
                case "2":
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    break;
                case "3":
                default:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    break;
            }

            boolean audioAutoPlay = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("auto_play_audio", true);
            PlayerUtils.getInstance().auto_play = audioAutoPlay;
        } catch (Exception e) {
            Utils.getInstance().recordException(e);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public void fetchDynamicVariables() {
        try {
            DocumentReference dynamicVariablesDocRef = FirebaseFirestore.getInstance().document("Public Files/dynamicVariables");
            dynamicVariablesDocRef.get(Source.CACHE).addOnCompleteListener(task -> {

                if (task.isSuccessful()) {
                    dynamicVariables.setValue(task.getResult().toObject(DynamicVariables.class));
                } else {
                    Log.e(TAG, task.getException().getMessage());
                    if (!ConnectionUtils.isConnected(this)) {
                        Controller.getInstance().noInternet.setValue(true);
                        return;
                    }
                }

                dynamicVariablesDocRef.get().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful())
                        dynamicVariables.setValue(task1.getResult().toObject(DynamicVariables.class));
                    else Log.e(TAG, task1.getException().getMessage());

                });
            });
        } catch (Exception e) {
            Utils.getInstance().recordException(e);
        }
    }

    public static class DynamicVariables {
        public HashMap<String, String> sourceLogos = new LinkedHashMap<>();
        public HashMap<String, Map<String, Object>> categories = new HashMap<>();
        public HashMap<String, Double> rankingVariables = new HashMap<>();
        public int gapBetweenAds = 2;
        public boolean showAds = true;

        public DynamicVariables(){
            rankingVariables.put("P", 30d);
            rankingVariables.put("freshnessExponent", 1.5);
            rankingVariables.put("relevanceScoreExponent", 2d);
        }
    }

    private void createNotificationChannels() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel1 = new NotificationChannel(
                        CHANNEL_1_ID,
                        "News notification",
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel1.setDescription("This is Channel 1");
                NotificationManager manager = getSystemService(NotificationManager.class);
                manager.createNotificationChannel(channel1);
            }
        } catch (Exception e) {
            Utils.getInstance().recordException(e);
        }
    }
}
