package com.example.walrus_manager.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.walrus_manager.R;
import com.example.walrus_manager.data.model.Schedule;
import com.example.walrus_manager.ui.adapter.ScheduleAdapter;
import com.example.walrus_manager.viewmodel.ScheduleViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

public class ScheduleListFragment extends Fragment implements ScheduleAdapter.ScheduleActionListener {
    private ScheduleViewModel viewModel;
    private ScheduleAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);
        adapter = new ScheduleAdapter(this);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.fabAddSchedule);
        fab.setOnClickListener(v -> showAddScheduleDialog());

        viewModel.getAllSchedules().observe(getViewLifecycleOwner(), schedules -> {
            adapter.submitList(schedules);
        });

        viewModel.getDataChanged().observe(getViewLifecycleOwner(), changed -> {
            if (changed) {
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_schedule_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Navigation.findNavController(requireView()).navigate(R.id.action_scheduleList_to_settings);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onScheduleClick(Schedule schedule) {
        Bundle bundle = new Bundle();
        bundle.putLong("scheduleId", schedule.getId());
        Navigation.findNavController(requireView())
                .navigate(R.id.action_scheduleList_to_scheduleDetail, bundle);
    }

    @Override
    public void onEditSchedule(Schedule schedule) {
        showEditScheduleDialog(schedule);
    }

    @Override
    public void onDeleteSchedule(Schedule schedule) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("删除日程")
                .setMessage("确定要删除这个日程吗？这将同时删除该日程下的所有事件。")
                .setPositiveButton("确定", (dialog, which) -> {
                    viewModel.delete(schedule, result -> {
                        // 删除成功
                    });
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showAddScheduleDialog() {
        showScheduleDialog(null);
    }

    private void showEditScheduleDialog(Schedule schedule) {
        showScheduleDialog(schedule);
    }

    private void showScheduleDialog(@Nullable Schedule schedule) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_schedule, null);
        TextInputEditText titleInput = dialogView.findViewById(R.id.titleInput);
        TextInputEditText descriptionInput = dialogView.findViewById(R.id.descriptionInput);

        if (schedule != null) {
            titleInput.setText(schedule.getTitle());
            descriptionInput.setText(schedule.getDescription());
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(schedule == null ? "添加新日程" : "编辑日程")
                .setView(dialogView)
                .setPositiveButton("确定", (dialog, which) -> {
                    String title = titleInput.getText().toString().trim();
                    String description = descriptionInput.getText().toString().trim();
                    if (!title.isEmpty()) {
                        if (schedule == null) {
                            // 添加新日程
                            Schedule newSchedule = new Schedule(title, description, 1); // 临时使用userId=1
                            viewModel.insert(newSchedule, id -> {
                                // 插入成功
                            });
                        } else {
                            // 更新现有日程
                            schedule.setTitle(title);
                            schedule.setDescription(description);
                            viewModel.update(schedule, result -> {
                                // 更新成功
                            });
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }
} 