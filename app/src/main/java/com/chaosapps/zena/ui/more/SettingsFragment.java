package com.chaosapps.zena.ui.more;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.chaosapps.zena.R;
import com.chaosapps.zena.utils.Controller;
import com.chaosapps.zena.utils.PlayerUtils;

public class SettingsFragment extends PreferenceFragmentCompat {
    private final static String TAG = "SettingsFragment";
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PreferenceManager.getDefaultSharedPreferences(requireActivity()).registerOnSharedPreferenceChangeListener(listener);


        findPreference("categories").setOnPreferenceClickListener(preference -> {
            Controller.getInstance().navController.setValue(1);
            if(!Controller.getInstance().showCategories.getValue()){
                Controller.getInstance().showCategories.setValue(true);
            }
            return false;
        });
    }

    SharedPreferences.OnSharedPreferenceChangeListener listener = (prefs, key) -> {
        // Implementation
        switch (key) {
            case "auto_play_audio":
                PlayerUtils.getInstance().auto_play = prefs.getBoolean(key, true);
                break;
            case "dark_theme":
                String darkTheme = prefs.getString(key, "3");
                setUpTheme(darkTheme);
                break;
        }
    };



    private void setUpTheme(String darkTheme) {
        switch (darkTheme) {
            case "1":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "2":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "3":
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }



}
