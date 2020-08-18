package com.mike.zenaplusplus.ui.details;

import android.app.Application;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.Source;
import com.mike.zenaplusplus.R;
import com.mike.zenaplusplus.models.NewsDetailsModel;
import com.mike.zenaplusplus.repository.NewsRepo;
import com.mike.zenaplusplus.utils.CacheUtils;

import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class DetailsViewModel extends AndroidViewModel {

    MutableLiveData<Integer> progressBarVisibility = new MutableLiveData<>(GONE);
    MutableLiveData<Integer> errorStateVisibility = new MutableLiveData<>(GONE);

    MutableLiveData<Drawable> saveImageSourceId = new MutableLiveData<>(ContextCompat.getDrawable(getApplication(), R.drawable.save));
    MutableLiveData<Integer> saveImageColor = new MutableLiveData<>(R.color.secondary);
    MutableLiveData<View.OnClickListener> onSaveClickListener = new MutableLiveData<>(v -> {
    });

    public DetailsViewModel(@NonNull Application application) {
        super(application);
        init(application);
    }

    void init(Application application) {
        synchronizer(application);
    }

    private void synchronizer(Application application) {
        NewsRepo.getInstance().loadingNews.observeForever(aBoolean -> {
            updateViewItems(application);
        });

        NewsRepo.getInstance().failedToFetch.observeForever(aBoolean -> {
            updateViewItems(application);
        });

        NewsRepo.getInstance().selectedNewsModel.observeForever(newsModel -> {
            updateViewItems(application);
        });

        CacheUtils.getInstance().savedNewsIds.observeForever(strings -> {
            updateViewItems(application);
        });
    }

    private void updateViewItems(Application application) {
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

        setSaveBtnAttribs(application, savedNewsIds, newsDetailsModel);
    }

    private void setSaveBtnAttribs(Application application, List<String> savedNewsIds, NewsDetailsModel newsDetailsModel) {

        for (String savedNewsId : savedNewsIds) {
            if (savedNewsId.equals(newsDetailsModel.getId())) {
                saveImageSourceId.setValue(ContextCompat.getDrawable(getApplication(), R.drawable.unsave));
                saveImageColor.setValue(R.color.accentPrimary);
                onSaveClickListener.setValue(v -> CacheUtils.getInstance().unSaveNewsId(savedNewsId));
                return;
            }
        }
        onSaveClickListener.setValue(v -> CacheUtils.getInstance().saveNewsId(application.getApplicationContext(), newsDetailsModel.getId(), Source.CACHE));
        saveImageSourceId.setValue(ContextCompat.getDrawable(getApplication(), R.drawable.save));
        saveImageColor.setValue(R.color.secondary);
    }
}