package com.mike.zenaplusplus.ui.following;

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

import com.mike.zenaplusplus.R;
import com.mike.zenaplusplus.adapter.NewsAdapter;
import com.mike.zenaplusplus.repository.NewsRepo;
import com.mike.zenaplusplus.utils.Controller;

public class SavedNewsFragment extends Fragment {
    private static final String TAG = "SavedNewsFragment";

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
        actionBarIV.setOnClickListener(v-> Controller.getInstance().savedNewsFragment.setValue(false));
        fragment_title_textView.setText("Saved News");
    }

    private void setUpRecyclerView(Bundle savedInstanceState){
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        NewsAdapter newsAdapter = new NewsAdapter(requireActivity().getApplication());
        NewsRepo.getInstance().savedNewsModels.observe(getViewLifecycleOwner(), newsAdapter::setNewsList);
        recyclerView.setAdapter(newsAdapter);
        if(savedInstanceState != null){
            recyclerView.scrollToPosition(savedInstanceState.getInt("position"));
        }
    }

    private void linkWithController() {
        Controller.getInstance().savedNewsFragment.observe(getViewLifecycleOwner(), aBoolean -> {
            if (!aBoolean) {
                getParentFragmentManager().beginTransaction().remove(SavedNewsFragment.this).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
            }
        });
    }

}
