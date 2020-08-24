package com.chaosapps.zena.adapter;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.List;

public class MyFragmentPagerAdapter extends androidx.fragment.app.FragmentPagerAdapter {
    private List<Fragment> fragments;
    private List<String> titles/* = new ArrayList<>()*/;
    public MyFragmentPagerAdapter(FragmentManager fm, List<Fragment> fragments, List<String> titles) {
        super(fm);
        this.fragments = fragments;
        this.titles = titles;
    }
//    public MyFragmentPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
//        super(fm);
//        this.fragments = fragments;
//    }

//    public void setTitles(List<String> titles){
//        this.titles.clear();
//        this.titles.addAll(titles);
//        notifyDataSetChanged();
//    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if(titles!=null && !titles.isEmpty()) return titles.get(position);
        else return null;
    }
}