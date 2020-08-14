package com.mike.zenaplusplus.ui.details;

import android.app.Application;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.mike.zenaplusplus.R;
import com.mike.zenaplusplus.models.FeedElementModel;
import com.mike.zenaplusplus.models.NewsModel;
import com.mike.zenaplusplus.repository.FeedRepo;
import com.mike.zenaplusplus.repository.NewsRepo;
import com.mike.zenaplusplus.utils.Singletons;

import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class DetailsViewModel extends AndroidViewModel {

    FeedRepo feedRepo;

    MutableLiveData<Integer> progressBarVisibility = new MutableLiveData<>(GONE);
    MutableLiveData<Integer> errorStateVisibility = new MutableLiveData<>(GONE);

    MutableLiveData<Drawable> saveImageSourceId = new MutableLiveData<>(ContextCompat.getDrawable(getApplication(), R.drawable.save));
    MutableLiveData<Integer> saveImageColor = new MutableLiveData<>(ContextCompat.getColor(getApplication(), R.color.secondary));
    MutableLiveData<View.OnClickListener> onSaveClickListener = new MutableLiveData<>(v -> {});

    public DetailsViewModel(@NonNull Application application) {
        super(application);
        feedRepo = FeedRepo.getInstance(application);
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

        feedRepo.getSavedFeedElements().observeForever(feedElementModels -> {
            updateViewItems(application);
        });
    }

    private void updateViewItems(Application application) {
        //required views
        boolean loadingNews = NewsRepo.getInstance().loadingNews.getValue();
        boolean failedToFetch = NewsRepo.getInstance().failedToFetch.getValue();
        NewsModel newsModel = NewsRepo.getInstance().selectedNewsModel.getValue();
        List<FeedElementModel> feedElementModels = feedRepo.getSavedFeedElements().getValue();

        //logic
        if (loadingNews) progressBarVisibility.setValue(VISIBLE);
        else progressBarVisibility.setValue(GONE);

        if (failedToFetch) errorStateVisibility.setValue(VISIBLE);
        else errorStateVisibility.setValue(GONE);

        if(feedElementModels!=null)setSaveBtnAttribs(application,feedElementModels,newsModel);
    }

    private void setSaveBtnAttribs(Application application,List<FeedElementModel> feedElementModels, NewsModel newsModel){

        for (FeedElementModel feedElementModel : feedElementModels) {
            NewsModel savedNewsModel = Singletons.gson().fromJson(feedElementModel.getItemJson(), NewsModel.class);
            if(savedNewsModel.getId().equals(newsModel.getId())){
                saveImageSourceId.setValue(ContextCompat.getDrawable(getApplication(), R.drawable.unsave));
                saveImageColor.setValue(ContextCompat.getColor(getApplication(), R.color.accentPrimary));
                onSaveClickListener.setValue(v-> feedRepo.unSaveNews(newsModel));
                return;
            }
        }
        onSaveClickListener.setValue(v-> feedRepo.saveNews(application, newsModel));
        saveImageSourceId.setValue(ContextCompat.getDrawable(getApplication(), R.drawable.save));
        saveImageColor.setValue(ContextCompat.getColor(getApplication(), R.color.secondary));
    }
}