package com.chaosapps.zena.ui.details;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chaosapps.zena.R;
import com.chaosapps.zena.adapter.DetailsAdapter;
import com.chaosapps.zena.repository.NewsRepo;
import com.chaosapps.zena.utils.Controller;
import com.chaosapps.zena.utils.Utils;

public class DetailsFragment extends Fragment {

    //objects
    private DetailsViewModel detailsViewModel;

    //views
    private RecyclerView recyclerView;
    private ImageView actionBarIV;
    private ProgressBar progressBar;
    private ConstraintLayout errorStateCL;
    private Button retryBtn;
    private ImageView shareImageView;
    private ImageView visitWebsiteImageView;
    private ImageView saveImageView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        detailsViewModel = ViewModelProviders.of(this).get(DetailsViewModel.class);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        allFindViewByIds(view);
        setUpRecyclerView(savedInstanceState);
        setUpViews();
        linkWithController();
    }

    private void allFindViewByIds(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        actionBarIV = view.findViewById(R.id.actionBarIV);
        progressBar = view.findViewById(R.id.progressBar);
        errorStateCL = view.findViewById(R.id.errorStateCL);
        retryBtn = view.findViewById(R.id.retryBtn);
        shareImageView = view.findViewById(R.id.shareImageView);
        visitWebsiteImageView = view.findViewById(R.id.visitWebSiteImageView);
        saveImageView = view.findViewById(R.id.saveImageView);
    }

    private void setUpViews() {
        actionBarIV.setOnClickListener(v -> Controller.getInstance().detailsFragment.setValue(false));
        detailsViewModel.progressBarVisibility.observe(getViewLifecycleOwner(), progressBar::setVisibility);
        detailsViewModel.errorStateVisibility.observe(getViewLifecycleOwner(), errorStateCL::setVisibility);
        retryBtn.setOnClickListener(v -> {
            NewsRepo.getInstance().selectedNewsId.setValue(NewsRepo.getInstance().selectedNewsId.getValue());
        });
        shareImageView.setOnClickListener(v -> Utils.getInstance().share(requireContext(), NewsRepo.getInstance().selectedNewsModel.getValue().getLink()));
        visitWebsiteImageView.setOnClickListener(v -> Utils.getInstance().openLink(requireContext(), NewsRepo.getInstance().selectedNewsModel.getValue().getLink()));

        detailsViewModel.saveImageSourceId.observe(getViewLifecycleOwner(), saveImageView::setImageDrawable);

        detailsViewModel.saveImageColor.observe(getViewLifecycleOwner(), colorId -> saveImageView.setColorFilter(ContextCompat.getColor(requireContext(), colorId)));
        detailsViewModel.onSaveClickListener.observe(getViewLifecycleOwner(), saveImageView::setOnClickListener);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("position", ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition()); // get current recycle view position here.
        super.onSaveInstanceState(savedInstanceState);
    }

    private void setUpRecyclerView(Bundle savedInstanceState) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setVerticalScrollBarEnabled(false);
        DetailsAdapter detailsAdapter = new DetailsAdapter(getContext());
        NewsRepo.getInstance().selectedNewsModel.observe(getViewLifecycleOwner(), newsDetailsModel -> {
            if (!newsDetailsModel.getBody().isEmpty()) detailsAdapter.setNews(newsDetailsModel);
        });
        recyclerView.setAdapter(detailsAdapter);
        if (savedInstanceState != null) {
            recyclerView.scrollToPosition(savedInstanceState.getInt("position"));
        }
    }

    private void linkWithController() {
        Controller.getInstance().detailsFragment.observe(getViewLifecycleOwner(), aBoolean -> {
            if (!aBoolean) {
                getParentFragmentManager().beginTransaction().remove(DetailsFragment.this).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
            }
        });
    }
}
