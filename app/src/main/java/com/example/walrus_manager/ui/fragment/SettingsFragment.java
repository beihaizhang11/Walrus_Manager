package com.example.walrus_manager.ui.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;

import com.example.walrus_manager.R;
import com.example.walrus_manager.data.model.Event;
import com.example.walrus_manager.data.model.Schedule;
import com.example.walrus_manager.util.RemoteDatabaseUtil;
import com.example.walrus_manager.util.ScheduleShareUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class SettingsFragment extends Fragment {
    private static final String PREF_DARK_THEME = "dark_theme";
    private static final String PREF_DB_URL = "remote_db_url";
    private SharedPreferences preferences;
    private RemoteDatabaseUtil remoteDatabaseUtil;
    private MaterialButton btnBrowseSchedules;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        remoteDatabaseUtil = new RemoteDatabaseUtil();
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

        // 深色模式开关
        SwitchMaterial themeSwitch = view.findViewById(R.id.switchTheme);
        boolean isDarkTheme = preferences.getBoolean(PREF_DARK_THEME, false);
        themeSwitch.setChecked(isDarkTheme);
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean(PREF_DARK_THEME, isChecked).apply();
            updateTheme(isChecked);
        });

        // 远程数据库配置
        TextInputEditText etDatabaseUrl = view.findViewById(R.id.etDatabaseUrl);
        MaterialButton btnTestConnection = view.findViewById(R.id.btnTestConnection);
        MaterialButton btnSaveConnection = view.findViewById(R.id.btnSaveConnection);
        btnBrowseSchedules = view.findViewById(R.id.btnBrowseSchedules);

        // 加载保存的配置
        etDatabaseUrl.setText(preferences.getString(PREF_DB_URL, ""));

        // 测试连接
        btnTestConnection.setOnClickListener(v -> {
            String url = etDatabaseUrl.getText().toString().trim();

            if (url.isEmpty()) {
                Toast.makeText(requireContext(), "请输入数据库URL", Toast.LENGTH_SHORT).show();
                return;
            }

            btnTestConnection.setEnabled(false);
            RemoteDatabaseUtil.saveCredentials(requireContext(), url);
            remoteDatabaseUtil.testConnection(requireContext(), new RemoteDatabaseUtil.RemoteCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    btnTestConnection.setEnabled(true);
                    btnBrowseSchedules.setEnabled(true);
                    Toast.makeText(requireContext(), "连接成功", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String message) {
                    btnTestConnection.setEnabled(true);
                    btnBrowseSchedules.setEnabled(false);
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                }
            });
        });

        // 保存配置
        btnSaveConnection.setOnClickListener(v -> {
            String url = etDatabaseUrl.getText().toString().trim();

            if (url.isEmpty()) {
                Toast.makeText(requireContext(), "请输入数据库URL", Toast.LENGTH_SHORT).show();
                return;
            }

            preferences.edit().putString(PREF_DB_URL, url).apply();
            Toast.makeText(requireContext(), "配置已保存", Toast.LENGTH_SHORT).show();
        });

        // 浏览远程日程
        btnBrowseSchedules.setOnClickListener(v -> {
            remoteDatabaseUtil.getRemoteSchedules(requireContext(), new RemoteDatabaseUtil.RemoteCallback<List<Schedule>>() {
                @Override
                public void onSuccess(List<Schedule> schedules) {
                    showScheduleSelectionDialog(schedules);
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void updateTheme(boolean isDarkTheme) {
        AppCompatDelegate.setDefaultNightMode(
                isDarkTheme ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    private void showScheduleSelectionDialog(List<Schedule> schedules) {
        if (schedules.isEmpty()) {
            Toast.makeText(requireContext(), "没有可用的远程日程", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] titles = schedules.stream()
                .map(schedule -> schedule.getTitle() + " (by " + schedule.getCreatedBy() + ")")
                .toArray(String[]::new);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("选择要导入的日程")
                .setItems(titles, (dialog, which) -> {
                    Schedule selectedSchedule = schedules.get(which);
                    importRemoteSchedule(selectedSchedule);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void importRemoteSchedule(Schedule schedule) {
        // 获取该日程的所有事件
        remoteDatabaseUtil.getRemoteEvents(requireContext(), schedule.getId(), new RemoteDatabaseUtil.RemoteCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> events) {
                ScheduleShareUtil.ShareData shareData = new ScheduleShareUtil.ShareData(schedule, events);
                ScheduleShareUtil.importSchedule(requireContext(), shareData, new ScheduleShareUtil.ImportCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(requireContext(), "导入成功", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView()).navigate(R.id.action_settings_to_scheduleList);
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(requireContext(), "导入失败：" + message, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }
} 