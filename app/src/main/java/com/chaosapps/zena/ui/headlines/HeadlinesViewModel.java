package com.chaosapps.zena.ui.headlines;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.chaosapps.zena.App;
import com.chaosapps.zena.models.NewsModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class HeadlinesViewModel extends ViewModel {

    private int skipper = 0;

    Map<String, MutableLiveData<List<NewsModel>>> liveDataMap = new HashMap<>();
    Map<String, MutableLiveData<Boolean>> loadingStatesMap = new HashMap<>();
    Map<String, MutableLiveData<Boolean>> refreshingStatesMap = new HashMap<>();
    Map<String, MutableLiveData<Boolean>> hasLoadedAllItemsStatesMap = new HashMap<>();
    MutableLiveData<Integer> loadingProgressBarVisibility = new MutableLiveData<>(GONE);
    MutableLiveData<Boolean> trigger = new MutableLiveData<>(false);
    MutableLiveData<Integer> currentPage = new MutableLiveData<>(0);

    public void initLiveDataMaps(App.DynamicVariables dynamicVariables){
        if(!dynamicVariables.categories.isEmpty() && skipper==0){
            skipper++;
            for (String key : dynamicVariables.categories.keySet()) {
                liveDataMap.put(key, new MutableLiveData<>(new ArrayList<>()));
                loadingStatesMap.put(key, new MutableLiveData<>(false));
                refreshingStatesMap.put(key, new MutableLiveData<>(false));
                hasLoadedAllItemsStatesMap.put(key, new MutableLiveData<>(false));
            }
            trigger.setValue(true);
            initLoadingProgressBar();
        }
    }

    private void initLoadingProgressBar(){
        for (String key : loadingStatesMap.keySet()) {
            loadingStatesMap.get(key).observeForever(aBoolean -> {
                if(aBoolean) loadingProgressBarVisibility.setValue(VISIBLE);
                else loadingProgressBarVisibility.setValue(GONE);
            });
        }
    }



}