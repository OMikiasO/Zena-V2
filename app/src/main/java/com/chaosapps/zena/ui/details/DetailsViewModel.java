package com.chaosapps.zena.ui.details;

import android.content.Context;
import android.view.View;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.chaosapps.zena.R;
import com.chaosapps.zena.models.NewsDetailsModel;
import com.chaosapps.zena.repository.NewsRepo;
import com.chaosapps.zena.utils.CacheUtils;
import com.chaosapps.zena.utils.Utils;
import com.google.firebase.firestore.Source;

import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class DetailsViewModel extends ViewModel {
    public static final String TAG = "DetailsViewModel";

    MutableLiveData<Integer> progressBarVisibility = new MutableLiveData<>(GONE);
    MutableLiveData<Integer> errorStateVisibility = new MutableLiveData<>(GONE);

    MutableLiveData<Integer> saveImageSourceId = new MutableLiveData<>(R.drawable.save);
    MutableLiveData<Integer> saveImageColor = new MutableLiveData<>(R.color.secondary);
    MutableLiveData<View.OnClickListener> onSaveClickListener = new MutableLiveData<>(v -> {
    });

    public void updateViewItems(Context context) {
        try{
        //required views
        boolean loadingNews = NewsRepo.getInstance().loadingNews.getValue();
        boolean failedToFetch = NewsRepo.getInstance().failedToFetch.getValue();
        NewsDetailsModel newsDetailsModel = NewsRepo.getInstance().selectedNewsModel.getValue();
        List<String> savedNewsIds = CacheUtils.getInstance().savedNewsIds.getValue();

        //logic
        if (loadingNews) progressBarVisibility.setValue(VISIBLE);
        else progressBarVisibility.setValue(GONE);

        if (failedToFetch) errorStateVisibility.setValue(VISIBLE);
        else errorStateVisibility.setValue(GONE);

        setSaveBtnAttribs(context, savedNewsIds, newsDetailsModel);
        } catch (Exception e) {
            Utils.getInstance().recordException(e);
        }
    }

    private void setSaveBtnAttribs(Context context, List<String> savedNewsIds, NewsDetailsModel newsDetailsModel) {
        try{
        for (String savedNewsId : savedNewsIds) {
            if (savedNewsId.equals(newsDetailsModel.getId())) {
                saveImageSourceId.setValue(R.drawable.unsave);
                saveImageColor.setValue(R.color.accentPrimary);
                onSaveClickListener.setValue(v -> CacheUtils.getInstance().unSaveNewsId(savedNewsId));
                return;
            }
        }
        onSaveClickListener.setValue(v -> CacheUtils.getInstance().saveNewsId(context, newsDetailsModel.getId(), Source.CACHE));
        saveImageSourceId.setValue(R.drawable.save);
        saveImageColor.setValue(R.color.secondary);
        } catch (Exception e) {
            Utils.getInstance().recordException(e);
        }
    }
}