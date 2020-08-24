package com.chaosapps.zena.ui.following;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chaosapps.zena.R;
import com.chaosapps.zena.adapter.NewsAdapter;
import com.chaosapps.zena.repository.NewsRepo;
import com.chaosapps.zena.utils.Controller;
import com.paginate.Paginate;

public class SingleSourceFragment extends Fragment {
    private static final String TAG = "SourcesListFragment";

    private FollowingViewModel followingViewModel;
    private RecyclerView recyclerView;
    private ImageView actionBarIV;
    private TextView fragment_title_textView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        followingViewModel = ViewModelProviders.of(requireActivity()).get(FollowingViewModel.class);
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        allFindViewByIds(view);
        setUpRecyclerView(savedInstanceState);
        setUpViews();
        linkWithController();
        NewsRepo.getInstance().fetchBySource(false);
    }

    private void allFindViewByIds(View view){
        recyclerView = view.findViewById(R.id.recyclerView);
        actionBarIV = view.findViewById(R.id.actionBarIV);
        fragment_title_textView = view.findViewById(R.id.fragment_title_textView);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("position",((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition()); // get current recycle view position here.
        super.onSaveInstanceState(savedInstanceState);
    }

    private void setUpViews(){
        actionBarIV.setOnClickListener(v-> Controller.getInstance().singleSourceFragment.setValue(false));
        fragment_title_textView.setText(NewsRepo.getInstance().selectedSource.getValue());
    }

    private void setUpRecyclerView(Bundle savedInstanceState){
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        NewsAdapter newsAdapter = new NewsAdapter(requireActivity().getApplication());
        NewsRepo.getInstance().newsModelsBySource.observe(getViewLifecycleOwner(), newsAdapter::setNewsList);
        recyclerView.setAdapter(newsAdapter);
        if(savedInstanceState != null){
            recyclerView.scrollToPosition(savedInstanceState.getInt("position"));
        }

        Paginate.with(recyclerView, callbacks)
                .setLoadingTriggerThreshold(0)
                .addLoadingListItem(true)
                .build();
    }

    private void linkWithController() {
        Controller.getInstance().singleSourceFragment.observe(getViewLifecycleOwner(), aBoolean -> {
            if (!aBoolean) {
                getParentFragmentManager().beginTransaction().remove(SingleSourceFragment.this).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
            }
        });
    }
    private Paginate.Callbacks callbacks = new Paginate.Callbacks() {
        @Override
        public void onLoadMore() {
            NewsRepo.getInstance().fetchBySource(true);

        }

        @Override
        public boolean isLoading() {
            // Indicate whether new page loading is in progress or
            return  NewsRepo.getInstance().loadingNewsBySource.getValue();
        }

        @Override
        public boolean hasLoadedAllItems() {
            return false;
        }
    };


}
