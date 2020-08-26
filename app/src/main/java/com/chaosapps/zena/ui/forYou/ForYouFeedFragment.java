package com.chaosapps.zena.ui.forYou;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chaosapps.zena.adapter.NewsAdapter;
import com.chaosapps.zena.repository.NewsRepo;
import com.paginate.Paginate;

public class ForYouFeedFragment extends Fragment {
    private static final String TAG = "ForYouFeedFragment";

    private ForYouViewModel forYouViewModel;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        forYouViewModel = ViewModelProviders.of(requireActivity()).get(ForYouViewModel.class);
        swipeRefreshLayout = new SwipeRefreshLayout(requireContext());
        recyclerView = new RecyclerView(requireContext());
        swipeRefreshLayout.addView(recyclerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return swipeRefreshLayout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
       setUpRecyclerView(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("position",((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition()); // get current recycle view position here.
        super.onSaveInstanceState(savedInstanceState);
    }

    private void setUpRecyclerView(Bundle savedInstanceState){
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        NewsAdapter newsAdapter = new NewsAdapter(this);
        NewsRepo.getInstance().mainFeedNewsList.observe(getViewLifecycleOwner(), newsAdapter::setMainFeed);
        recyclerView.setAdapter(newsAdapter);
        if (savedInstanceState != null) {
            recyclerView.scrollToPosition(savedInstanceState.getInt("position"));
        }
        Paginate.with(recyclerView, callbacks)
                .setLoadingTriggerThreshold(0)
                .addLoadingListItem(true)
                .build();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            NewsRepo.getInstance().fetchNewsForMainFeed(getContext(), false);
            swipeRefreshLayout.setRefreshing(false);
        });
    }



    private Paginate.Callbacks callbacks = new Paginate.Callbacks() {
        @Override
        public void onLoadMore() {
            Log.e(TAG, "OnLoadMore");
            NewsRepo.getInstance().fetchNewsForMainFeed(getContext(), true);
        }

        @Override
        public boolean isLoading() {
            // Indicate whether new page loading is in progress or
            return NewsRepo.getInstance().loadingMainFeed.getValue();
        }

        @Override
        public boolean hasLoadedAllItems() {
            return false;
        }
    };

}
