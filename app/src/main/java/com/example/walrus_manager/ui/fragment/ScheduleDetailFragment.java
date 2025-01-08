package com.example.walrus_manager.ui.fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.walrus_manager.R;
import com.example.walrus_manager.data.model.Event;
import com.example.walrus_manager.ui.adapter.EventAdapter;
import com.example.walrus_manager.viewmodel.EventViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ScheduleDetailFragment extends Fragment implements EventAdapter.EventActionListener {
    private EventViewModel viewModel;
    private EventAdapter adapter;
    private long scheduleId;
    private Calendar selectedDateTime;
    private SimpleDateFormat dateTimeFormat;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scheduleId = getArguments().getLong("scheduleId", -1);
        selectedDateTime = Calendar.getInstance();
        dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(EventViewModel.class);
        adapter = new EventAdapter(this);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.fabAddEvent);
        fab.setOnClickListener(v -> showAddEventDialog());

        // 设置当前日程ID并观察数据变化
        viewModel.setScheduleId(scheduleId);
        viewModel.getEventsForSchedule().observe(getViewLifecycleOwner(), events -> {
            adapter.submitList(events);
        });

        // 观察数据更新状态
        viewModel.getDataChanged().observe(getViewLifecycleOwner(), changed -> {
            if (changed) {
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onEditEvent(Event event) {
        showEditEventDialog(event);
    }

    @Override
    public void onDeleteEvent(Event event) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("删除事件")
                .setMessage("确定要删除这个事件吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    viewModel.delete(event, result -> {
                        // 删除成功
                    });
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showAddEventDialog() {
        showEventDialog(null);
    }

    private void showEditEventDialog(Event event) {
        showEventDialog(event);
    }

    private void showEventDialog(@Nullable Event event) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_event, null);
        TextInputEditText titleInput = dialogView.findViewById(R.id.titleInput);
        TextInputEditText descriptionInput = dialogView.findViewById(R.id.descriptionInput);
        TextView dateTimeText = dialogView.findViewById(R.id.dateTimeText);

        if (event != null) {
            titleInput.setText(event.getTitle());
            descriptionInput.setText(event.getDescription());
            selectedDateTime.setTimeInMillis(event.getTargetDate());
        }
        dateTimeText.setText(dateTimeFormat.format(selectedDateTime.getTime()));

        dateTimeText.setOnClickListener(v -> showDateTimePicker(dateTimeText));

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(event == null ? "添加新事件" : "编辑事件")
                .setView(dialogView)
                .setPositiveButton("确定", (dialog, which) -> {
                    String title = titleInput.getText().toString().trim();
                    String description = descriptionInput.getText().toString().trim();
                    if (!title.isEmpty()) {
                        if (event == null) {
                            // 添加新事件
                            Event newEvent = new Event(title, description, scheduleId, selectedDateTime.getTimeInMillis());
                            viewModel.insert(newEvent, id -> {
                                // 插入成功
                            });
                        } else {
                            // 更新现有事件
                            event.setTitle(title);
                            event.setDescription(description);
                            event.setTargetDate(selectedDateTime.getTimeInMillis());
                            viewModel.update(event, result -> {
                                // 更新成功
                            });
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showDateTimePicker(TextView dateTimeText) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // 选择时间
                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            requireContext(),
                            (view1, hourOfDay, minute) -> {
                                selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                selectedDateTime.set(Calendar.MINUTE, minute);
                                dateTimeText.setText(dateTimeFormat.format(selectedDateTime.getTime()));
                            },
                            selectedDateTime.get(Calendar.HOUR_OF_DAY),
                            selectedDateTime.get(Calendar.MINUTE),
                            true
                    );
                    timePickerDialog.show();
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
} 