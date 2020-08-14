package com.mike.zenaplusplus.ui.headlines;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.mike.zenaplusplus.App;
import com.mike.zenaplusplus.R;
import com.mike.zenaplusplus.adapter.MyFragmentPagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeadlinesFragment extends Fragment {
    private final static String TAG = "HeadlinesFragment";

    //Objects
    private HeadlinesViewModel headlinesViewModel;

    //vars

    //views
    private ViewPager viewPager;
    private TabLayout tab;
    private ProgressBar loadingProgressBar;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        headlinesViewModel = ViewModelProviders.of(requireActivity()).get(HeadlinesViewModel.class);
        headlinesViewModel.init();
        return inflater.inflate(R.layout.fragment_hedlines, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        allFindViewByIds(view);
        synchronizer();
    }

    private void synchronizer(){
        headlinesViewModel.trigger.observe(getViewLifecycleOwner(), aBoolean -> {
            if(aBoolean) setUpViewPagerAndTab();
        });
    }


    private void allFindViewByIds(View view){
        viewPager = view.findViewById(R.id.viewPager);
        tab = view.findViewById(R.id.tabs);
        loadingProgressBar = view.findViewById(R.id.loadingFeedProgressBar);
    }

    private void setUpViewPagerAndTab(){
        //required vars
        HashMap<String, Map<String, Object>> categories = App.dynamicVariables.getValue().categories;
        List<String> categoryNames = new ArrayList<>();
        List<Fragment> fragments = new ArrayList<>();

        for (String categoryKey : categories.keySet()) {
            Map<String, Object> singleCatMap = categories.get(categoryKey);
            categoryNames.add((String) singleCatMap.get("englishName"));
            fragments.add(SingleCategoryFeedFragment.newInstance(categoryKey));
        }

        MyFragmentPagerAdapter myFragmentPagerAdapter = new MyFragmentPagerAdapter(getChildFragmentManager(), fragments, categoryNames);
        viewPager.setAdapter(myFragmentPagerAdapter);
        viewPager.setOffscreenPageLimit(App.dynamicVariables.getValue().categories.size()-1);
        tab.setupWithViewPager(viewPager, true);
        headlinesViewModel.loadingProgressBarVisibility.observe(getViewLifecycleOwner(), loadingProgressBar::setVisibility);
    }
}