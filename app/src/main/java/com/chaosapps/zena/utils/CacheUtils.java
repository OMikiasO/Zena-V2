package com.chaosapps.zena.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.chaosapps.zena.models.FeedElementModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class CacheUtils {
    private final static String TAG = "CacheUtil";
    private static CacheUtils INSTANCE;

    public final static String USER_PREFERENCE_FILE_KEY = "com.mike.zenaplusplus.utils.USER_PREFERENCE_FILE_KEY";
    public SharedPreferences userSharedPref;

    //LiveData
    public MutableLiveData<List<String>> savedNewsIds = new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<List<String>> savedSearchTerms = new MutableLiveData<>(new ArrayList<>());

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

    /////////////////////////////////SAVED NEWS ID CACHE FUNCTIONS//////////////////////////////////

    public List<String> getSavedNewsIds() {
        List<String> savedNewsIds = new ArrayList<>();
        try {
            savedNewsIds.addAll(userSharedPref.getStringSet("savedNewsIds", new HashSet<>()));
            this.savedNewsIds.setValue(savedNewsIds); // save the value to the live data so that i don't need to this multiple times per session
            Log.e(TAG, savedNewsIds + " - getSavedNewsIds");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return savedNewsIds;
    }

    public void saveNewsId(Context context, String newsId, Source source) {
        if (newsId == null) {
            Utils.getInstance().makeToast(context, "News hasn't loaded yet");
            return;
        }
        FirebaseFirestore.getInstance().document("NewsDetails/" + newsId)
                .get(source).addOnCompleteListener(task -> {
            if (task.isSuccessful())
                try {
                    List<String> savedNewsIds = getSavedNewsIds();
                    Utils.getInstance().makeToast(context, "Saved");
                    if (savedNewsIds.contains(newsId)) return;
                    savedNewsIds.add(newsId);
                    this.savedNewsIds.setValue(savedNewsIds); // post it the new value to the live data too
                    userSharedPref.edit().putStringSet("savedNewsIds", new HashSet<>(savedNewsIds)).apply();
                    Log.e(TAG, savedNewsIds + " - saveNewsId");
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            else
                Utils.getInstance().makeToast(context, "Unable to save the News");
        });

    }

    public void unSaveNewsId(String newsId) {
        try {
            List<String> savedNewsIds = getSavedNewsIds();
            savedNewsIds.remove(newsId);
            this.savedNewsIds.setValue(savedNewsIds); // post the new value to the live data too
            userSharedPref.edit().putStringSet("savedNewsIds", new HashSet<>(savedNewsIds)).apply();
            Log.e(TAG, savedNewsIds + " - saveNewsId");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /////////////////////////////////SEARCH TERM CACHE FUNCTIONS////////////////////////////////////

    public List<String> getSavedSearchTerms() {
        List<String> savedSearchTerms = new ArrayList<>();
        try {
            savedSearchTerms.addAll(userSharedPref.getStringSet("savedSearchTerms", new HashSet<>()));
            this.savedSearchTerms.setValue(savedSearchTerms); // save the value to the live data so that i don't need to this multiple times per session
            Log.e(TAG, savedSearchTerms + " - getSavedSearchTerms");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return savedSearchTerms;
    }


    public void saveSearchTerm(String searchTerm) {
        try {
            List<String> savedSearchTerms = getSavedSearchTerms();
            if (savedSearchTerms.contains(searchTerm)) return;
            savedSearchTerms.add(searchTerm);
            this.savedSearchTerms.setValue(savedSearchTerms); // post the new value to the live data too
            userSharedPref.edit().putStringSet("savedSearchTerms", new HashSet<>(savedSearchTerms)).apply();
            Log.e(TAG, savedSearchTerms + " - savedSearchTerms");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void unSaveSearchTerm(String searchTerm) {
        try {
            List<String> savedSearchTerms = getSavedNewsIds();
            savedSearchTerms.remove(searchTerm);
            this.savedSearchTerms.setValue(savedSearchTerms); // post the new value to the live data too
            userSharedPref.edit().putStringSet("savedSearchTerms", new HashSet<>(savedSearchTerms)).apply();
            Log.e(TAG, savedSearchTerms + " - savedSearchTerms");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }


}
