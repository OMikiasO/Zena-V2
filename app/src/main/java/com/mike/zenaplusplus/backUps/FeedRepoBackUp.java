//package com.mike.zenaplusplus.repository;
//
//import android.app.Application;
//import android.os.AsyncTask;
//import android.util.Log;
//
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.MutableLiveData;
//
//import com.google.firebase.functions.FirebaseFunctions;
//import com.mike.zenaplusplus.App;
//import com.mike.zenaplusplus.daos.FeedDao;
//import com.mike.zenaplusplus.daos.FeedDataBase;
//import com.mike.zenaplusplus.models.FeedElementModel;
//import com.mike.zenaplusplus.models.FeedModel;
//import com.mike.zenaplusplus.utils.CacheUtils;
//import com.mike.zenaplusplus.utils.Singletons;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class FeedRepo {
//    //constants
//    public final static String HAS_LOADED_ALL_ITEMS = "HasLoadedAllItems";
//    public final static String REFRESHING = "Refreshing";
//    public final static String LOADING = "Loading";
//
//    private FeedDao feedDao;
//    private static FeedRepo Instance;
//    private LiveData<List<FeedElementModel>> mainFeedElements;
//    private LiveData<List<FeedElementModel>> politicsFeedElements;
//    public Map<String, MutableLiveData<Boolean>> listStateObjects = new HashMap<>();
//
//    private FeedRepo(Application application) {
//        initListStateObjects();
//        FeedDataBase dataBase = FeedDataBase.getInstance(application);
//        feedDao = dataBase.feedDao();
//        mainFeedElements = feedDao.getMainFeedElements();
//        politicsFeedElements = feedDao.getPoliticsFeedElements();
//        init();
//    }
//
//    public static synchronized FeedRepo getInstance(Application application) {
//        if (Instance == null) {
//            Instance = new FeedRepo(application);
//        }
//        return Instance;
//    }
//
//    private void init() {
//        mainFeedCF(false, false, false);
//        singleCategoryCF("politics", false, false, false);
//        mainFeedElements.observeForever(feedElementModels -> {
//            try {
//                CacheUtils.getInstance().saveFirstAndLastIds(feedElementModels, "mainFeed");
//            } catch (Exception e) {
//                Log.e("saveFirstAndLastIds", e.getMessage());
//            }
//        });
//        politicsFeedElements.observeForever(feedElementModels -> {
//            try {
//                CacheUtils.getInstance().saveFirstAndLastIds(feedElementModels, "politics");
//            } catch (Exception e) {
//                Log.e("saveFirstAndLastIds", e.getMessage());
//            }
//        });
//    }
//
//    public void mainFeedCF(boolean loadMore, boolean reload, boolean refresh) {
//        String feedId = "mainFeed";
//        // Create the arguments to the callable function.
//        if (refresh) listStateObjects.get(feedId + REFRESHING).setValue(true);
//        else listStateObjects.get(feedId + LOADING).setValue(true);
//
//        final long time = System.currentTimeMillis();
//        Map<String, Object> data = new HashMap<>();
//        data.put("firstAndLastIds", CacheUtils.getInstance().getSavedFirstAndLastIds("mainFeed"));
//        data.put("loadMore", loadMore);
//        data.put("reload", reload);
//        data.put("userId", CacheUtils.getInstance().userSharedPref.getString("userId", null));
//        FirebaseFunctions.getInstance("europe-west1")
//                .getHttpsCallable("mainFeed")
//                .call(data)
//                .continueWith(task -> {
//                    listStateObjects.get(feedId + LOADING).setValue(false);
//                    listStateObjects.get(feedId + REFRESHING).setValue(false);
//                    if (task.isSuccessful()) {
//                        String result = (String) task.getResult().getData();
//                        Log.e("MainFeedRepo", result);
//                        Log.e("MainFeedRepo", result.length() + "");
//                        FeedModel feedModel = Singletons.gson().fromJson(result, FeedModel.class);
//                        if (feedModel.getStatus() == -1) {
//                            Log.e("MainFeedRepo", "no new news");
//                            return "no new news";
//                        } else {
//                            if (!loadMore) deleteAll("mainFeed");
//                            if (feedModel.getObjectList().size() < 60)
//                                listStateObjects.get(feedId + HAS_LOADED_ALL_ITEMS).setValue(true);
//                            insertAll(feedModel.getObjectList());
//                        }
//                        Log.e("MainFeedRepo", "" + (System.currentTimeMillis() - time));
//                        return result;
//                    } else {
//                        Log.e("MainFeedRepo", task.getException().getMessage());
//                        Log.e("MainFeedRepo", "" + (System.currentTimeMillis() - time));
//                        return "err";
//                    }
//                });
//    }
//
//    public void singleCategoryCF(String feedId, boolean loadMore, boolean reload, boolean refresh) {
//        // Create the arguments to the callable function.
//        if (refresh) listStateObjects.get(feedId + REFRESHING).setValue(true);
//        else listStateObjects.get(feedId + HAS_LOADED_ALL_ITEMS).setValue(true);
//
//        final long time = System.currentTimeMillis();
//        Map<String, Object> data = new HashMap<>();
//        data.put("firstAndLastIds", CacheUtils.getInstance().getSavedFirstAndLastIds(feedId));
//        data.put("loadMore", loadMore);
//        data.put("reload", reload);
//        data.put("categoryKey", feedId);
//        data.put("userId", CacheUtils.getInstance().userSharedPref.getString("userId", null));
//        FirebaseFunctions.getInstance("europe-west1")
//                .getHttpsCallable("singleCategoryFeed")
//                .call(data)
//                .continueWith(task -> {
//                    listStateObjects.get(feedId + LOADING).setValue(false);
//                    listStateObjects.get(feedId + REFRESHING).setValue(false);
//                    if (task.isSuccessful()) {
//                        String result = (String) task.getResult().getData();
//                        Log.e("Single", result);
//                        Log.e("Single", result.length() + "");
//                        FeedModel feedModel = Singletons.gson().fromJson(result, FeedModel.class);
//                        if (feedModel.getStatus() == -1) {
//                            Log.e("Single", "no new news");
//                            return "no new news";
//                        } else {
//                            if (!loadMore) deleteAll(feedId);
//                            if (feedModel.getObjectList().size() < 20)
////                                listStateObjects.get(feedId + HAS_LOADED_ALL_ITEMS).setValue(true);
//                                insertAll(feedModel.getObjectList());
//                        }
//                        Log.e("Single", "" + (System.currentTimeMillis() - time));
//                        return result;
//                    } else {
//                        Log.e("Single", task.getException().getMessage());
//                        Log.e("Single", "" + (System.currentTimeMillis() - time));
//                        return "err";
//                    }
//                });
//    }
//
//    private void initListStateObjects() {
//        listStateObjects.put("mainFeedLoading", new MutableLiveData<>(false));
//        listStateObjects.put("mainFeedRefreshing", new MutableLiveData<>(false));
//        listStateObjects.put("mainFeedHasLoadedAllItems", new MutableLiveData<>(false));
//        for (String key : App.categoriesMap.keySet()) {
//            listStateObjects.put(key + LOADING, new MutableLiveData<>(false));
//            listStateObjects.put(key + REFRESHING, new MutableLiveData<>(false));
//            listStateObjects.put(key + HAS_LOADED_ALL_ITEMS, new MutableLiveData<>(false));
//        }
//    }
//    public void insert(FeedElementModel feedElementModel) {
//        new InsertFeedAsyncTask(feedDao).execute(feedElementModel);
//    }
//
//    private void insertAll(List<FeedElementModel> feedElementModels) {
//        new InsertAllFeedAsyncTask(feedDao).execute(feedElementModels);
//    }
//
//    public void update(FeedElementModel feedElementModel) {
//        new UpdateFeedAsyncTask(feedDao).execute(feedElementModel);
//    }
//
//    public void delete(FeedElementModel feedElementModel) {
//        new DeleteFeedAsyncTask(feedDao).execute(feedElementModel);
//    }
//
//    private void deleteAll(String feedId) {
//        new DeleteAllFeedAsyncTask(feedDao).execute(feedId);
//    }
//
//    public LiveData<List<FeedElementModel>> getMainFeedElements() {
//        return mainFeedElements;
//    }
//
//    public LiveData<List<FeedElementModel>> getPoliticsFeedElements() {
//        return politicsFeedElements;
//    }
//
//    private static class InsertFeedAsyncTask extends AsyncTask<FeedElementModel, Void, Void> {
//        private FeedDao feedDao;
//
//        private InsertFeedAsyncTask(FeedDao feedDao) {
//            this.feedDao = feedDao;
//        }
//
//        @Override
//        protected Void doInBackground(FeedElementModel... feedElementModels) {
//            feedDao.insert(feedElementModels[0]);
//            return null;
//        }
//    }
//
//    private static class InsertAllFeedAsyncTask extends AsyncTask<List<FeedElementModel>, Void, Void> {
//        private FeedDao feedDao;
//
//        private InsertAllFeedAsyncTask(FeedDao feedDao) {
//            this.feedDao = feedDao;
//        }
//
//        @SafeVarargs
//        @Override
//        protected final Void doInBackground(List<FeedElementModel>... lists) {
//            feedDao.insertAll(lists[0]);
//            return null;
//        }
//    }
//
//    private static class UpdateFeedAsyncTask extends AsyncTask<FeedElementModel, Void, Void> {
//        private FeedDao feedDao;
//
//        private UpdateFeedAsyncTask(FeedDao feedDao) {
//            this.feedDao = feedDao;
//        }
//
//        @Override
//        protected Void doInBackground(FeedElementModel... feedElementModels) {
//            feedDao.update(feedElementModels[0]);
//            return null;
//        }
//    }
//
//    private static class DeleteFeedAsyncTask extends AsyncTask<FeedElementModel, Void, Void> {
//        private FeedDao feedDao;
//
//        private DeleteFeedAsyncTask(FeedDao feedDao) {
//            this.feedDao = feedDao;
//        }
//
//        @Override
//        protected Void doInBackground(FeedElementModel... feedElementModels) {
//            feedDao.delete(feedElementModels[0]);
//            return null;
//        }
//    }
//
//    private static class DeleteAllFeedAsyncTask extends AsyncTask<String, Void, Void> {
//        private FeedDao feedDao;
//
//        private DeleteAllFeedAsyncTask(FeedDao feedDao) {
//            this.feedDao = feedDao;
//        }
//
//        @Override
//        protected Void doInBackground(String... strings) {
//            switch (strings[0]) {
//                case "mainFeed":
//                    Log.e("DeleteAllFeedAsyncTask", strings[0]);
//                    feedDao.deleteAllMainFeed();
//                    break;
//                case "politics":
//                    Log.e("DeleteAllFeedAsyncTask", strings[0]);
//                    feedDao.deleteAllPolitics();
//                default:
//                    Log.e("DeleteAllFeedAsyncTask", "default");
//                    break;
//            }
//            return null;
//        }
//    }
//
//}
