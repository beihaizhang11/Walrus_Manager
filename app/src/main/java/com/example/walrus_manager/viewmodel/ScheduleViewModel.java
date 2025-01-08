package com.example.walrus_manager.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.walrus_manager.data.AppDatabase;
import com.example.walrus_manager.data.model.Schedule;
import com.example.walrus_manager.repository.ScheduleRepository;

import java.util.List;

public class ScheduleViewModel extends AndroidViewModel {
    private final ScheduleRepository repository;
    private final MutableLiveData<List<Schedule>> allSchedules = new MutableLiveData<>();
    private final MutableLiveData<Boolean> dataChanged = new MutableLiveData<>(false);

    public ScheduleViewModel(Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(application);
        repository = new ScheduleRepository(database.scheduleDao());
        loadSchedules();
    }

    private void loadSchedules() {
        repository.getAllSchedules(schedules -> {
            allSchedules.postValue(schedules);
        });
    }

    public LiveData<List<Schedule>> getAllSchedules() {
        return allSchedules;
    }

    public LiveData<Boolean> getDataChanged() {
        return dataChanged;
    }

    public void insert(Schedule schedule, ScheduleRepository.Callback<Long> callback) {
        repository.insert(schedule, id -> {
            loadSchedules();
            dataChanged.postValue(true);
            callback.onComplete(id);
        });
    }

    public void update(Schedule schedule, ScheduleRepository.Callback<Void> callback) {
        repository.update(schedule, result -> {
            loadSchedules();
            dataChanged.postValue(true);
            callback.onComplete(result);
        });
    }

    public void delete(Schedule schedule, ScheduleRepository.Callback<Void> callback) {
        repository.delete(schedule, result -> {
            loadSchedules();
            dataChanged.postValue(true);
            callback.onComplete(result);
        });
    }
} 