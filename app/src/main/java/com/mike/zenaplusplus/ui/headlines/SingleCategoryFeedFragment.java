package com.mike.zenaplusplus.ui.headlines;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.mike.zenaplusplus.App;
import com.mike.zenaplusplus.adapter.NewsAdapter;
import com.mike.zenaplusplus.models.NewsModel;
import com.mike.zenaplusplus.repository.NewsRepo;
import com.paginate.Paginate;

import java.util.List;

public class SingleCategoryFeedFragment extends Fragment {
    private static final String TAG = "ForYouFeedFragment";

    //vars
    private HeadlinesViewModel headlinesViewModel;
    private String key;

    //views
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    static SingleCategoryFeedFragment newInstance(String key) {
        SingleCategoryFeedFragment f = new SingleCategoryFeedFragment();
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString("key", key);
        f.setArguments(args);
        return f;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        key = args.getString("key", "-1");
        headlinesViewModel = ViewModelProviders.of(requireActivity()).get(HeadlinesViewModel.class);
        swipeRefreshLayout = new SwipeRefreshLayout(requireContext());
        recyclerView = new RecyclerView(requireContext());
        swipeRefreshLayout.addView(recyclerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return swipeRefreshLayout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpRecyclerView(savedInstanceState);
        setUpViews();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("position",((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition()); // get current recycle view position here.
        super.onSaveInstanceState(savedInstanceState);
    }

    private void setUpRecyclerView(Bundle savedInstanceState) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        NewsAdapter newsAdapter = new NewsAdapter(requireActivity().getApplication());
        List<String> queries = (List<String>) (App.dynamicVariables.getValue().categories.get(key).get("queries"));
        if (!queries.isEmpty())
            NewsRepo.getInstance().fetchByCategory(false ,headlinesViewModel.liveDataMap.get(key), queries, 0, headlinesViewModel.loadingStatesMap.get(key),headlinesViewModel.hasLoadedAllItemsStatesMap.get(key),headlinesViewModel.refreshingStatesMap.get(key));
        headlinesViewModel.liveDataMap.get(key).observe(getViewLifecycleOwner(), newsAdapter::setNewsList);
        recyclerView.setAdapter(newsAdapter);
        if(savedInstanceState != null){
            recyclerView.scrollToPosition(savedInstanceState.getInt("position"));
        }
        Paginate.with(recyclerView, callbacks)
                .setLoadingTriggerThreshold(0)
                .addLoadingListItem(true)
                .build();
    }

    private void setUpViews(){
        headlinesViewModel.refreshingStatesMap.get(key).observe(getViewLifecycleOwner(), swipeRefreshLayout::setRefreshing);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            List<String> queries = (List<String>) (App.dynamicVariables.getValue().categories.get(key).get("queries"));
            if (!queries.isEmpty()){
                NewsRepo.getInstance().fetchByCategory(true ,headlinesViewModel.liveDataMap.get(key), queries, 0, headlinesViewModel.loadingStatesMap.get(key),headlinesViewModel.hasLoadedAllItemsStatesMap.get(key),headlinesViewModel.refreshingStatesMap.get(key));
                swipeRefreshLayout.setRefreshing(true);
            }
        });
    }


    private Paginate.Callbacks callbacks = new Paginate.Callbacks() {
        @Override
        public void onLoadMore() {
            Log.e(TAG, "OnLoadMore");
            try {
                List<String> queries = (List<String>) App.dynamicVariables.getValue().categories.get(key).get("queries");
                if (!queries.isEmpty()){
                    MutableLiveData<List<NewsModel>> liveData = headlinesViewModel.liveDataMap.get(key);
                    NewsRepo.getInstance().fetchByCategory(false,liveData, queries, liveData.getValue().get(liveData.getValue().size() - 1).getPostedTime(), headlinesViewModel.loadingStatesMap.get(key),headlinesViewModel.hasLoadedAllItemsStatesMap.get(key),headlinesViewModel.refreshingStatesMap.get(key));
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }

        }

        @Override
        public boolean isLoading() {
            // Indicate whether new page loading is in progress or
            return headlinesViewModel.loadingStatesMap.get(key).getValue();
        }

        @Override
        public boolean hasLoadedAllItems() {
            return headlinesViewModel.hasLoadedAllItemsStatesMap.get(key).getValue();
        }
    };

}
