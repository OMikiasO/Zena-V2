package com.mike.zenaplusplus.ui.forYou;

import android.app.Application;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mike.zenaplusplus.App;
import com.mike.zenaplusplus.models.FeedElementModel;
import com.mike.zenaplusplus.models.UserModel;
import com.mike.zenaplusplus.repository.FeedRepo;
import com.mike.zenaplusplus.utils.Account;
import com.mike.zenaplusplus.utils.CacheUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForYouViewModel extends AndroidViewModel {
    private static final String TAG = "ForYouViewModel";
    static final int SHOW_NON = 0;
    static final int SHOW_CATEGORIES = 1;
    static final int SHOW_FEED = 2;
    private String feedId = "mainFeed";

    FeedRepo feedRepo;

    //LiveData
    private MutableLiveData<List<String>> selectedCategories = new MutableLiveData<>(new ArrayList<>());
    MutableLiveData<Integer> visibleFragment = new MutableLiveData<>(SHOW_NON);
    MutableLiveData<Integer> loadingProgressBarVisibility = new MutableLiveData<>(View.GONE);
    MutableLiveData<Map<String, Map<String, Object>>> categoriesMap = new MutableLiveData<>(new HashMap<>());
    public MutableLiveData<Boolean> boneBtnEnabled = new MutableLiveData<>(true);
    private int skipper = 0;

    public ForYouViewModel(@NonNull Application application) {
        super(application);
        feedRepo = FeedRepo.getInstance(application);
        synchronizer();
    }

    private void synchronizer() {
        App.dynamicVariables.observeForever(dynamicVariables -> {
            categoriesMap.setValue(dynamicVariables.categories);
        });
        Account.getInstance().user.observeForever(userModel -> {
            updateSelectedCategories();
        });
        feedRepo.loading.observeForever(aBoolean -> {
            if (aBoolean) loadingProgressBarVisibility.postValue(View.VISIBLE);
            else loadingProgressBarVisibility.postValue(View.GONE);
        });
    }

    private void updateSelectedCategories() {
        //required vars
        UserModel userModel = Account.getInstance().user.getValue();

        //logic
        List<String> existingSelectedCategories = selectedCategories.getValue();
        List<String> newlySelectedCategories = userModel.getSelectedCategories();
        Log.e(TAG, existingSelectedCategories.toString() + "__" + newlySelectedCategories.toString());
        String cachedUserId = CacheUtils.getInstance().userSharedPref.getString("userId", null);
        if (cachedUserId == null) {
            visibleFragment.setValue(SHOW_CATEGORIES);
            Log.e(TAG, "SHOW CATEGORIES - cachedUserId == nul");
        } else if (existingSelectedCategories.isEmpty() && newlySelectedCategories.isEmpty() && skipper <= 0) {
            visibleFragment.setValue(SHOW_NON);
            Log.e(TAG, "SHOW NON");
            skipper++;
        } else if (newlySelectedCategories.isEmpty() || !existingSelectedCategories.equals(newlySelectedCategories) && !userModel.getUserId().isEmpty()) {
            if (newlySelectedCategories.isEmpty()) {
                visibleFragment.setValue(SHOW_CATEGORIES);
                Log.e(TAG, "SHOW CATEGORIES");
            } else {
                visibleFragment.setValue(SHOW_FEED);
                Log.e(TAG, "SHOW FEED");
            }
        }

        if(userModel.getSelectedCategories().isEmpty()) boneBtnEnabled.setValue(false);
        else boneBtnEnabled.setValue(true);

    }

    LiveData<List<FeedElementModel>> getMainFeedElements() {
        return feedRepo.getMainFeedElements();
    }
}