package com.chaosapps.zena.ui.more;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.chaosapps.zena.R;

public class MoreFragment extends Fragment {

    private Fragment settingsFragment;

    private MoreViewModel moreViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        moreViewModel = ViewModelProviders.of(this).get(MoreViewModel.class);
        return inflater.inflate(R.layout.fragment_more, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpFragments();
    }

    private void setUpFragments() {
        if (settingsFragment == null) {
            settingsFragment = new SettingsFragment();
            getChildFragmentManager().beginTransaction().add(R.id.fragmentHost, settingsFragment, "1").setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
        }
    }
}
