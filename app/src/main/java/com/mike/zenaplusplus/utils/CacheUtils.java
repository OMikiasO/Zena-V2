package com.mike.zenaplusplus.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.mike.zenaplusplus.models.FeedElementModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CacheUtils {
    private final static String TAG = "CacheUtil";
    private static CacheUtils INSTANCE;

    public final static String USER_PREFERENCE_FILE_KEY = "com.mike.zenaplusplus.utils.USER_PREFERENCE_FILE_KEY";
    public SharedPreferences userSharedPref;

    public static synchronized CacheUtils getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CacheUtils();
        }
        return INSTANCE;
    }

    public String getSavedFirstAndLastIds(String feedId) {
        String firstAndLastIds = null;
        try {
            firstAndLastIds = userSharedPref.getString("firstAndLastIds" + feedId, null);
            Log.e(TAG, firstAndLastIds + " - get" + feedId);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return firstAndLastIds;
    }

    public void saveFirstAndLastIds(List<FeedElementModel> feedElementModels, String feedId) {
        String firstAndLastIds = Utils.getInstance().getFirstAndLastId(feedElementModels);
        try {
            userSharedPref.edit().putString("firstAndLastIds" + feedId, firstAndLastIds).apply();
            Log.e(TAG, firstAndLastIds + " - save" + feedId);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public List<String> getSavesNewsIds() {
        List<String> savedNewsIds = new ArrayList<>();
        try {
            savedNewsIds.addAll(userSharedPref.getStringSet("savedNewsIds", new HashSet<>()));
            Log.e(TAG, savedNewsIds + " - getSavedNewsIds");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return savedNewsIds;
    }

    public void saveNewsId(Context context, String newsId) {
        if (newsId == null) {
            Utils.getInstance().makeToast(context, "News hasn't loaded yet");
            return;
        }
        try {
            List<String> savedNewsIds = getSavesNewsIds();
            Utils.getInstance().makeToast(context, "Saved");
            if (savedNewsIds.contains(newsId)) return;
            savedNewsIds.add(newsId);
            userSharedPref.edit().putStringSet("savedNewsIds", new HashSet<>(savedNewsIds)).apply();
            Log.e(TAG, savedNewsIds + " - saveNewsId");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public Map<String, String> getSavedSourceLogos() {
        Map<String, String> sourceLogos = new LinkedHashMap<>();
        List<String> keysAndValues = new ArrayList<>();
        try {
            keysAndValues = new ArrayList<>(userSharedPref.getStringSet("sourceLogosKeys", new HashSet<>()));
            Log.e(TAG, keysAndValues.toString() + " - get");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        for (int i = 0; i < keysAndValues.size(); i++) {
            String key = keysAndValues.get(i).split(",")[0];
            String value = keysAndValues.get(i).split(",")[1];
            sourceLogos.put(key, value);
        }
        return sourceLogos;
    }

    public void saveSourceLogos(Map<String, String> sourceLogos) {
        List<String> keysAndValues = new ArrayList<>();
        for (String key : sourceLogos.keySet()) {
            String value = sourceLogos.get(key);
            keysAndValues.add(key + "," + value);
        }
        Log.e(getClass().getSimpleName(), keysAndValues.toString());
        try {
            userSharedPref.edit().putStringSet("sourceLogosKeysAndValues", new HashSet<>(keysAndValues)).apply();
            Log.e(TAG, "sourceLogos - save");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
