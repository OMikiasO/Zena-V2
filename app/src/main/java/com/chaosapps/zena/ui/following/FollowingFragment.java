package com.chaosapps.zena.ui.following;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chaosapps.zena.App;
import com.chaosapps.zena.R;
import com.chaosapps.zena.adapter.NewsAdapter;
import com.chaosapps.zena.adapter.SourceAdapter;
import com.chaosapps.zena.repository.NewsRepo;
import com.chaosapps.zena.utils.Controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FollowingFragment extends Fragment {
    private static final String TAG = "FollowingFragment";

    private FollowingViewModel followingViewModel;

    //view
    private RecyclerView savedNewsRecyclerView;
    private LinearLayout savedNewsLL;
    private TextView seeAllSavedTextView;
    private RecyclerView sourcesRecyclerView;
    private TextView seeAllSourcesTextView;
    private ImageView noSavedNewsImageView;
    private TextView noSavedNewsTextView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        followingViewModel = ViewModelProviders.of(this).get(FollowingViewModel.class);
        return inflater.inflate(R.layout.fragment_following, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        allFindViewByIds(view);
        setUpSavedNewsViews();
        setUpSourcesViews();
    }

    private void allFindViewByIds(View view) {
        savedNewsRecyclerView = view.findViewById(R.id.savedNewsRecyclerView);
        savedNewsLL = view.findViewById(R.id.savedNewsLL);
        seeAllSavedTextView = view.findViewById(R.id.seeAllSavedTextView);
        sourcesRecyclerView = view.findViewById(R.id.sourcesRecyclerView);
        seeAllSourcesTextView = view.findViewById(R.id.seeAllSourcesTextView);
        noSavedNewsImageView = view.findViewById(R.id.noSavedNewsImageView);
        noSavedNewsTextView = view.findViewById(R.id.noSavedNewsTextView);
    }

    private void setUpSavedNewsViews() {
        savedNewsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        NewsAdapter newsAdapter = new NewsAdapter(this);
        NewsRepo.getInstance().savedNewsModels.observe(getViewLifecycleOwner(), newsModelList -> {
            Collections.reverse(newsModelList);
            if (newsModelList.isEmpty()) {
                noSavedNewsImageView.setVisibility(View.VISIBLE);
                noSavedNewsTextView.setVisibility(View.VISIBLE);
                savedNewsRecyclerView.setVisibility(View.GONE);
            } else {
                noSavedNewsImageView.setVisibility(View.GONE);
                noSavedNewsTextView.setVisibility(View.GONE);
                savedNewsRecyclerView.setVisibility(View.VISIBLE);
            }
            if (newsModelList.size() > 2) {
                seeAllSavedTextView.setVisibility(View.VISIBLE);
                newsModelList = newsModelList.subList(0, 2);
            } else seeAllSavedTextView.setVisibility(View.GONE);
            newsAdapter.setNewsList(newsModelList);
        });
        savedNewsRecyclerView.setAdapter(newsAdapter);
        seeAllSavedTextView.setOnClickListener(v -> Controller.getInstance().savedNewsFragment.setValue(true));
    }

    private void setUpSourcesViews() {
        sourcesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        SourceAdapter sourceAdapter = new SourceAdapter(this);
        App.dynamicVariables.observe(getViewLifecycleOwner(), dynamicVariables -> {
            Map<String, String> sourcesMap = new HashMap<>();
            for (int i = 0; i < 3; i++) {
                try {
                    String key = new ArrayList<>(dynamicVariables.sourceLogos.keySet()).get(i);
                    String value = dynamicVariables.sourceLogos.get(key);
                    sourcesMap.put(key, value);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    break;
                }
            }
            sourceAdapter.setSourcesMap(sourcesMap);
        });
        sourcesRecyclerView.setAdapter(sourceAdapter);
        seeAllSourcesTextView.setOnClickListener(v -> Controller.getInstance().sourcesFragment.setValue(true));
    }
}
