package com.mike.zenaplusplus.ui.forYou;

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

import com.mike.zenaplusplus.adapter.FeedAdapter;
import com.paginate.Paginate;

public class ForYouFeedFragment extends Fragment {
    private static final String TAG = "ForYouFeedFragment";

    private ForYouViewModel forYouViewModel;
    private RecyclerView recyclerView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        forYouViewModel = ViewModelProviders.of(requireActivity()).get(ForYouViewModel.class);
        recyclerView = new RecyclerView(requireContext());
        return recyclerView;
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
        FeedAdapter feedAdapter = new FeedAdapter(requireActivity().getApplication(), requireActivity());
        forYouViewModel.getMainFeedElements().observe(getViewLifecycleOwner(), feedAdapter::setFeedElementModelList);
        recyclerView.setAdapter(feedAdapter);
        if(savedInstanceState != null){
            recyclerView.scrollToPosition(savedInstanceState.getInt("position"));
        }
        Paginate.with(recyclerView, callbacks)
                .setLoadingTriggerThreshold(0)
                .addLoadingListItem(true)
                .build();
    }



    private Paginate.Callbacks callbacks = new Paginate.Callbacks() {
        @Override
        public void onLoadMore() {
            Log.e(TAG, "OnLoadMore");
            forYouViewModel.feedRepo.mainFeedCF(true,false,false);
        }

        @Override
        public boolean isLoading() {
            // Indicate whether new page loading is in progress or
            return forYouViewModel.feedRepo.loading.getValue();
        }

        @Override
        public boolean hasLoadedAllItems() {
            return false;
        }
    };

}
