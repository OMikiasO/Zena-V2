package com.chaosapps.zena.ui.forYou;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.chaosapps.zena.App;
import com.chaosapps.zena.R;
import com.chaosapps.zena.repository.NewsRepo;
import com.chaosapps.zena.utils.Account;
import com.chaosapps.zena.utils.Controller;
import com.chaosapps.zena.utils.Utils;

public class ForYouFragment extends Fragment {

    //objects
    private ForYouViewModel forYouViewModel;
    private ForYouFeedFragment forYouFeedFragment;
    private CategoriesFragment categoriesFragment;
    private FragmentManager fm;
    private Fragment active;

    //view
    private ImageView feedSettingsImageView;
    private ProgressBar loadingFeedProgressBar;
//    private SwipeRefreshLayout feedRefreshView;
    private ImageView searchImageView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        forYouViewModel = ViewModelProviders.of(requireActivity()).get(ForYouViewModel.class);
        return inflater.inflate(R.layout.fragment_for_you, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        allFindViewByIds(view);
        setUpViews();
        setUpFragments(savedInstanceState);
        setUpSwitcher();
        synchronizer();
    }

    private void allFindViewByIds(View view) {
        feedSettingsImageView = view.findViewById(R.id.feedSettingsImageView);
        loadingFeedProgressBar = view.findViewById(R.id.loadingFeedProgressBar);
        searchImageView = view.findViewById(R.id.searchImageView);
    }

    private void synchronizer() {
        App.dynamicVariables.observe(getViewLifecycleOwner(),dynamicVariables -> {
            forYouViewModel.categoriesMap.setValue(dynamicVariables.categories);
        });
        Account.getInstance().user.observe(getViewLifecycleOwner(),userModel -> {
            forYouViewModel.updateSelectedCategories();
        });
        NewsRepo.getInstance().loadingMainFeed.observe(getViewLifecycleOwner(),aBoolean -> {
            if (aBoolean) forYouViewModel.loadingProgressBarVisibility.postValue(View.VISIBLE);
            else forYouViewModel.loadingProgressBarVisibility.postValue(View.GONE);
        });
    }

    private void setUpViews() {
        feedSettingsImageView.setOnClickListener(v-> Controller.getInstance().showCategories.setValue(true));
        Controller.getInstance().showCategories.observe(getViewLifecycleOwner(), aBoolean -> {
            if(aBoolean){
                if (forYouViewModel.visibleFragment.getValue() == ForYouViewModel.SHOW_FEED) {
                    categoriesFragment.categoriesAdapter.setSelectedKeys(Account.getInstance().user.getValue().getSelectedCategories());
                    forYouViewModel.visibleFragment.setValue(ForYouViewModel.SHOW_CATEGORIES);
                } else {
                    if (!Account.getInstance().user.getValue().getSelectedCategories().isEmpty()) {
                        forYouViewModel.visibleFragment.setValue(ForYouViewModel.SHOW_FEED);
                        Controller.getInstance().showCategories.setValue(false);
                    } else
                        Utils.getInstance().makeToast(requireContext(), "Please choose categories before you continue");
                }
            }
        });
        forYouViewModel.loadingProgressBarVisibility.observe(getViewLifecycleOwner(), loadingFeedProgressBar::setVisibility);
        searchImageView.setOnClickListener(v -> Controller.getInstance().searchFragment.setValue(true));
    }

    private void setUpSwitcher() {
        forYouViewModel.visibleFragment.observe(getViewLifecycleOwner(), integer -> {
            if (integer == ForYouViewModel.SHOW_NON) {
                fm.beginTransaction().hide(categoriesFragment);
                fm.beginTransaction().hide(forYouFeedFragment);
            } else if (integer == ForYouViewModel.SHOW_CATEGORIES) {
                fm.beginTransaction().hide(active).show(categoriesFragment).commit();
                active = categoriesFragment;
            } else if (integer == ForYouViewModel.SHOW_FEED) {
                fm.beginTransaction().hide(active).show(forYouFeedFragment).commit();
                active = forYouFeedFragment;
            }
        });
    }


    private void setUpFragments(Bundle savedInstanceState) {
        fm = getChildFragmentManager();
        forYouFeedFragment = (ForYouFeedFragment) fm.findFragmentByTag("1");
        if (forYouFeedFragment == null) {
            forYouFeedFragment = new ForYouFeedFragment();
            fm.beginTransaction().add(R.id.fragmentHost, forYouFeedFragment, "1").hide(forYouFeedFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
        }

        categoriesFragment = (CategoriesFragment) fm.findFragmentByTag("2");
        if (categoriesFragment == null) {
            categoriesFragment = new CategoriesFragment();
            fm.beginTransaction().add(R.id.fragmentHost, categoriesFragment, "2").hide(categoriesFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
        }

        if (savedInstanceState != null) {
            Log.e(getClass().getSimpleName(), savedInstanceState.toString());
            active = fm.findFragmentByTag(savedInstanceState.getString("activeTag"));
            if (active == null) active = forYouFeedFragment;
        } else {
            active = forYouFeedFragment;
        }
    }

}
