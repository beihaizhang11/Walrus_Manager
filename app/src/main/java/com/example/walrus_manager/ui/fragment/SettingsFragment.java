package com.example.walrus_manager.ui.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.example.walrus_manager.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsFragment extends Fragment {
    private static final String PREF_DARK_THEME = "dark_theme";
    private SharedPreferences preferences;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SwitchMaterial themeSwitch = view.findViewById(R.id.switchTheme);
        
        // 加载保存的主题设置
        boolean isDarkTheme = preferences.getBoolean(PREF_DARK_THEME, false);
        themeSwitch.setChecked(isDarkTheme);

        // 监听主题切换
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean(PREF_DARK_THEME, isChecked).apply();
            updateTheme(isChecked);
        });
    }

    private void updateTheme(boolean isDarkTheme) {
        AppCompatDelegate.setDefaultNightMode(
                isDarkTheme ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
} 