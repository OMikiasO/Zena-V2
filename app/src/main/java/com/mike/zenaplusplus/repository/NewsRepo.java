package com.mike.zenaplusplus.repository;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Source;
import com.mike.zenaplusplus.models.NewsModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NewsRepo {
    private static final String TAG = "NewsRepo";
    private static NewsRepo INSTANCE;

    public static synchronized NewsRepo getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NewsRepo();
            INSTANCE.init();
        }
        return INSTANCE;
    }

    private void init() {
        fetchNewsById();
        fetchRelatedNews();
    }

    public MutableLiveData<NewsModel> selectedNewsModel = new MutableLiveData<>(new NewsModel());
    public MutableLiveData<List<NewsModel>> relatedNewsList = new MutableLiveData<>(new ArrayList<>());
    public MutableLiveData<String> selectedNewsId = new MutableLiveData<>("");

    //stateObjects
    public MutableLiveData<Boolean> failedToFetch = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> loadingNews = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> failedToFetchRelatedNews = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> loadingRelatedNews = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> loadingSuggestions = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> loadingSearchResult = new MutableLiveData<>(false);
    public MutableLiveData<Boolean> noSearchResultFound = new MutableLiveData<>(false);
    public MutableLiveData<ArrayList<String>> searchSuggestions = new MutableLiveData<>();
    public MutableLiveData<List<NewsModel>> searchResults = new MutableLiveData<>();

    public void fetchByCategory(boolean refresh, MutableLiveData<List<NewsModel>> liveData, List<String> queries, long lastPostedTime, MutableLiveData<Boolean> isLoading, MutableLiveData<Boolean> hasLoadedAllItems, MutableLiveData<Boolean> refreshing) {
        if (refresh) refreshing.setValue(true);
        else isLoading.setValue(true);
        Query query;
        Log.e(TAG, "QUERIES > " + queries.toString());
        if (lastPostedTime != 0) {
            query = FirebaseFirestore.getInstance().collection("News").whereIn("category", queries).orderBy("postedTime", Query.Direction.DESCENDING).startAfter(lastPostedTime).limit(10);
        } else {
            query = FirebaseFirestore.getInstance().collection("News").whereIn("category", queries).orderBy("postedTime", Query.Direction.DESCENDING).limit(10);
        }
        List<NewsModel> newsModels = liveData.getValue();
        if (lastPostedTime == 0) {
            query.get(Source.CACHE).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        isLoading.setValue(false);
                    }
                    liveData.setValue(task.getResult().toObjects(NewsModel.class));
                }
                query.get().addOnCompleteListener(task1 -> {
                    isLoading.setValue(false);
                    refreshing.setValue(false);
                    if (task1.isSuccessful()) {
                        if (task.getResult().size() % 10 != 0) hasLoadedAllItems.setValue(true);
                        liveData.setValue(task1.getResult().toObjects(NewsModel.class));
                    }
                });
            });
        } else {
            query.get().addOnCompleteListener(task1 -> {
                isLoading.setValue(false);
                refreshing.setValue(false);
                if (task1.isSuccessful()) {
                    if (task1.getResult().size() % 10 != 0) hasLoadedAllItems.setValue(true);
                    newsModels.addAll(task1.getResult().toObjects(NewsModel.class));
                    liveData.setValue(newsModels);
                }
            });
        }
    }

    private void fetchNewsById() {
        selectedNewsId.observeForever(id -> {
            if (id.isEmpty() || id.equals(selectedNewsModel.getValue().getId())) return;
            DocumentReference documentReference = FirebaseFirestore.getInstance().document("News/" + id);
            selectedNewsModel.setValue(new NewsModel());
            loadingNews.setValue(true);
            failedToFetch.setValue(false);
            documentReference.get().addOnCompleteListener(task -> {
                loadingNews.setValue(false);
                if (task.isSuccessful()) {
                    selectedNewsModel.setValue(task.getResult().toObject(NewsModel.class));
                } else {
                    failedToFetch.setValue(true);
                }
            });
        });
    }

    private void fetchRelatedNews() {
        selectedNewsModel.observeForever(newsModel -> {
            if (newsModel == null || newsModel.getCategory() == null) return;
            Query query = FirebaseFirestore.getInstance()
                    .collection("News")
                    .whereEqualTo("category", newsModel.getCategory())
                    .whereEqualTo("source", newsModel.getSource())
                    .limit(4);
            relatedNewsList.setValue(new ArrayList<>());
            loadingRelatedNews.setValue(true);
            failedToFetchRelatedNews.setValue(false);
            query.get().addOnCompleteListener(task -> {
                loadingRelatedNews.setValue(false);
                if (task.isSuccessful()) {
                    List<NewsModel> newsModels = task.getResult().toObjects(NewsModel.class);
                    for (int i = 0; i < newsModels.size(); i++) {
                        if (newsModels.get(i).getId().equals(newsModel.getId())) {
                            newsModels.remove(i);
                        }
                    }
                    newsModels.remove(newsModel);
                    relatedNewsList.setValue(newsModels);

                } else {
                    failedToFetchRelatedNews.setValue(true);
                }
            });
        });
    }

//    private void initSavedSearchSuggestions() {
//        ArrayList<String> items = new ArrayList<>();
//        Set searchQueries = new HashSet<>(Account.getInstance().userSharedPref.getStringSet("searchQueries", new HashSet<>()));
//        items.addAll(searchQueries);
//        Collections.reverse(items);
//        savedSearchSuggestions.setValue(new ArrayList<>(items));
//    }

    public void createSuggestions(String query) {
        //required vars
        loadingSuggestions.setValue(false);
        if (query.isEmpty()) return;

        loadingSuggestions.setValue(true);
        query = query.toUpperCase();

        Query q = FirebaseFirestore.getInstance().collection("News").whereEqualTo("searchKeyWords." + query, true);
        q.limit(5).get().addOnCompleteListener(task -> {
            loadingSuggestions.setValue(false);
            if (task.isSuccessful()) {
                List<NewsModel> newsModels = task.getResult().toObjects(NewsModel.class);
                ArrayList<String> suggestionList = new ArrayList<>();
                for (int i = 0; i < newsModels.size(); i++) {
                    String suggestion = newsModels.get(i).getTitle();
                    suggestionList.add(suggestion);
                }
                searchSuggestions.setValue(suggestionList);
            }
        });
    }

    public void searchProduct(String query) {
        //required vars

        loadingSuggestions.setValue(false);
        loadingSearchResult.setValue(false);
        Set<String> searchQueries = new HashSet<>();
//        Set<String> savedSearchQueries = new HashSet<>(Account.getInstance().userSharedPref.getStringSet("searchQueries", new HashSet<>()));

//        searchQueries.addAll(savedSearchQueries);

        searchQueries.add(query);
//        savedSearchSuggestions.setValue(new ArrayList<>(searchQueries)); // This updates teh list of the saved suggestion when search screen is empty.
//        Account.getInstance().userSharedPref.edit().putStringSet("searchQueries", searchQueries).apply();
        query = query.trim();
        if (query.isEmpty()) return;
        query = query.toUpperCase();
        String[] queries = query.trim().split(" ");

        loadingSearchResult.setValue(true);
        Query q = FirebaseFirestore.getInstance().collection("News").limit(5);
        Log.e(TAG, q.toString());
        for (String s : queries) {
            q = q.whereEqualTo("searchKeyWords." + s, true);
        }

        q.get().addOnCompleteListener(task -> {
            loadingSearchResult.setValue(false);
            if (task.isSuccessful()) {
                List<NewsModel> newsModels = task.getResult().toObjects(NewsModel.class);
                if (newsModels.isEmpty()) noSearchResultFound.setValue(true);
                else noSearchResultFound.setValue(false);
                searchResults.setValue(task.getResult().toObjects(NewsModel.class));
            } else {
                Log.e(TAG, task.getException().getMessage());
            }
        });
    }

}
