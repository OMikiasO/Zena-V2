package com.mike.zenaplusplus;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.mike.zenaplusplus.utils.Account;
import com.mike.zenaplusplus.utils.CacheUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class App extends Application {
    public static final String TAG = "App";

    public static final String CHANNEL_1_ID = "channel1";
    public static final String CHANNEL_2_ID = "channel2";

    public static MutableLiveData<DynamicVariables> dynamicVariables = new MutableLiveData<>(new DynamicVariables());


    @Override
    public void onCreate() {
        setUpTheme();
        super.onCreate();
        createNotificationChannels();
        CacheUtils.getInstance().userSharedPref = this.getSharedPreferences(CacheUtils.USER_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        Account.getInstance().signIn();
        Account.getInstance().syncUserData();
        fetchDynamicVariables();

    }

    private void setUpTheme() {
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
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    private void fetchDynamicVariables() {
        DocumentReference dynamicVariablesDocRef = FirebaseFirestore.getInstance().document("Public Files/dynamicVariables");
        dynamicVariablesDocRef.get(Source.CACHE).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                dynamicVariables.setValue(task.getResult().toObject(DynamicVariables.class));
            } else {
                Log.e(TAG, task.getException().getMessage());
            }
            dynamicVariablesDocRef.get().addOnCompleteListener(task1 -> {
                if (task1.isSuccessful()) {
                    dynamicVariables.setValue(task1.getResult().toObject(DynamicVariables.class));
                } else {
                    Log.e(TAG, task1.getException().getMessage());
                }
            });
        });
    }

    public static class DynamicVariables {
        public HashMap<String, String> sourceLogos = new LinkedHashMap<>();
        public HashMap<String, Map<String, Object>> categories = new HashMap<>();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("This is Channel 1");
            NotificationChannel channel2 = new NotificationChannel(
                    CHANNEL_2_ID,
                    "Channel 2",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel2.setDescription("This is Channel 2");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
            manager.createNotificationChannel(channel2);
        }
    }
}
