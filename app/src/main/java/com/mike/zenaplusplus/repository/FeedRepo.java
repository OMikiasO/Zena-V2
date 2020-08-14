package com.mike.zenaplusplus.repository;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.mike.zenaplusplus.daos.FeedDao;
import com.mike.zenaplusplus.daos.FeedDataBase;
import com.mike.zenaplusplus.models.FeedElementModel;
import com.mike.zenaplusplus.models.FeedModel;
import com.mike.zenaplusplus.models.NewsModel;
import com.mike.zenaplusplus.utils.CacheUtils;
import com.mike.zenaplusplus.utils.Singletons;
import com.mike.zenaplusplus.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedRepo {
    private FeedDao feedDao;
    private static FeedRepo Instance;
    private LiveData<List<FeedElementModel>> mainFeedElements;
    private LiveData<List<FeedElementModel>> savedFeedElements;
    public MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> refreshing = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> hasLoadedAllItems = new MutableLiveData<>(false);

    private FeedRepo(Application application) {
        FeedDataBase dataBase = FeedDataBase.getInstance(application);
        feedDao = dataBase.feedDao();
        mainFeedElements = feedDao.getMainFeedElements();
        savedFeedElements = feedDao.getSavedFeedElements();
        init();
    }

    public static synchronized FeedRepo getInstance(Application application) {
        if (Instance == null) {
            Instance = new FeedRepo(application);
        }
        return Instance;
    }

    private void init() {
        mainFeedCF(false, false, false);
        mainFeedElements.observeForever(feedElementModels -> {
            try {
                CacheUtils.getInstance().saveFirstAndLastIds(feedElementModels, "mainFeed");
            } catch (Exception e) {
                Log.e("saveFirstAndLastIds", e.getMessage());
            }
        });
    }

    public void mainFeedCF(boolean loadMore, boolean reload, boolean refresh) {
        // Create the arguments to the callable function.
        if (refresh) refreshing.setValue(true);
        else loading.setValue(true);

        final long time = System.currentTimeMillis();
        Map<String, Object> data = new HashMap<>();
        data.put("firstAndLastIds", CacheUtils.getInstance().getSavedFirstAndLastIds("mainFeed"));
        data.put("loadMore", loadMore);
        data.put("reload", reload);
        data.put("userId", CacheUtils.getInstance().userSharedPref.getString("userId", null));
        FirebaseFunctions.getInstance("europe-west1")
                .getHttpsCallable("mainFeed")
                .call(data)
                .continueWith(task -> {
                    loading.setValue(false);
                    refreshing.setValue(false);
                    if (task.isSuccessful()) {
                        String result = (String) task.getResult().getData();
                        Log.e("MainFeedRepo", result);
                        Log.e("MainFeedRepo", result.length() + "");
                        FeedModel feedModel = Singletons.gson().fromJson(result, FeedModel.class);
                        if (feedModel.getStatus() == -1) {
                            Log.e("MainFeedRepo", "no new news");
                            return "no new news";
                        } else {
                            if (!loadMore) deleteAll();
                            if (feedModel.getObjectList().size() < 60)
                                hasLoadedAllItems.setValue(true);
                            insertAll(feedModel.getObjectList());
                        }
                        Log.e("MainFeedRepo", "" + (System.currentTimeMillis() - time));
                        return result;
                    } else {
                        Log.e("MainFeedRepo", task.getException().getMessage());
                        Log.e("MainFeedRepo", "" + (System.currentTimeMillis() - time));
                        return "err";
                    }
                });
    }

    public void saveNews(Context context, NewsModel newsModel) {
        if (newsModel.getId() == null) {
            Utils.getInstance().makeToast(context, "News hasn't loaded yet");
            return;
        }
        Utils.getInstance().makeToast(context, "Saved");

        for (FeedElementModel elementModel : savedFeedElements.getValue()) {
            NewsModel savedNewsModel = Singletons.gson().fromJson(elementModel.getItemJson(), NewsModel.class);
            if (savedNewsModel.getId().equals(newsModel.getId())) return;
        }
        newsModel.setNumber(-1);
        FeedElementModel feedElementModel = new FeedElementModel(FeedElementModel.SMALL_NEWS_ITEM, Singletons.gson().toJson(newsModel));
        feedElementModel.setFeedId("savedNewsFeed");
        feedElementModel.setRankingScore(0);
        insert(feedElementModel);
    }

    public void saveNews(Context context,String id) {
        Utils.getInstance().makeToast(context, "Saved");

        for (FeedElementModel elementModel : savedFeedElements.getValue()) {
            NewsModel savedNewsModel = Singletons.gson().fromJson(elementModel.getItemJson(), NewsModel.class);
            if (savedNewsModel.getId().equals(id)) return;
        }

        DocumentReference documentReference = FirebaseFirestore.getInstance().document("News/"+id);
        documentReference.get().addOnCompleteListener(task->{
            if(task.isSuccessful()){
                NewsModel newsModel = task.getResult().toObject(NewsModel.class);
                newsModel.setNumber(-1);
                FeedElementModel feedElementModel = new FeedElementModel(FeedElementModel.SMALL_NEWS_ITEM, Singletons.gson().toJson(newsModel));
                feedElementModel.setFeedId("savedNewsFeed");
                feedElementModel.setRankingScore(0);
                insert(feedElementModel);
            }
        });
    }


    public void unSaveNews(NewsModel newsModel) {
        for (FeedElementModel elementModel : savedFeedElements.getValue()) {
            NewsModel savedNewsModel = Singletons.gson().fromJson(elementModel.getItemJson(), NewsModel.class);
            if (savedNewsModel.getId().equals(newsModel.getId())) delete(elementModel);
        }
    }

    public void insert(FeedElementModel feedElementModel) {
        new InsertFeedAsyncTask(feedDao).execute(feedElementModel);
    }

    private void insertAll(List<FeedElementModel> feedElementModels) {
        new InsertAllFeedAsyncTask(feedDao).execute(feedElementModels);
    }

    public void update(FeedElementModel feedElementModel) {
        new UpdateFeedAsyncTask(feedDao).execute(feedElementModel);
    }

    public void delete(FeedElementModel feedElementModel) {
        new DeleteFeedAsyncTask(feedDao).execute(feedElementModel);
    }

    private void deleteAll() {
        new DeleteAllFeedAsyncTask(feedDao).execute();
    }

    public LiveData<List<FeedElementModel>> getMainFeedElements() {
        return mainFeedElements;
    }

    public LiveData<List<FeedElementModel>> getSavedFeedElements() {
        return savedFeedElements;
    }

    private static class InsertFeedAsyncTask extends AsyncTask<FeedElementModel, Void, Void> {
        private FeedDao feedDao;

        private InsertFeedAsyncTask(FeedDao feedDao) {
            this.feedDao = feedDao;
        }

        @Override
        protected Void doInBackground(FeedElementModel... feedElementModels) {
            feedDao.insert(feedElementModels[0]);
            return null;
        }
    }

    private static class InsertAllFeedAsyncTask extends AsyncTask<List<FeedElementModel>, Void, Void> {
        private FeedDao feedDao;

        private InsertAllFeedAsyncTask(FeedDao feedDao) {
            this.feedDao = feedDao;
        }

        @SafeVarargs
        @Override
        protected final Void doInBackground(List<FeedElementModel>... lists) {
            feedDao.insertAll(lists[0]);
            return null;
        }
    }

    private static class UpdateFeedAsyncTask extends AsyncTask<FeedElementModel, Void, Void> {
        private FeedDao feedDao;

        private UpdateFeedAsyncTask(FeedDao feedDao) {
            this.feedDao = feedDao;
        }

        @Override
        protected Void doInBackground(FeedElementModel... feedElementModels) {
            feedDao.update(feedElementModels[0]);
            return null;
        }
    }

    private static class DeleteFeedAsyncTask extends AsyncTask<FeedElementModel, Void, Void> {
        private FeedDao feedDao;

        private DeleteFeedAsyncTask(FeedDao feedDao) {
            this.feedDao = feedDao;
        }

        @Override
        protected Void doInBackground(FeedElementModel... feedElementModels) {
            feedDao.delete(feedElementModels[0]);
            return null;
        }
    }

    private static class DeleteAllFeedAsyncTask extends AsyncTask<Void, Void, Void> {
        private FeedDao feedDao;

        private DeleteAllFeedAsyncTask(FeedDao feedDao) {
            this.feedDao = feedDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            feedDao.deleteAll();
            return null;
        }
    }

}
