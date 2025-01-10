package com.example.walrus_manager.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.walrus_manager.data.AppDatabase;
import com.example.walrus_manager.data.model.Event;
import com.example.walrus_manager.repository.EventRepository;
import com.example.walrus_manager.repository.ScheduleRepository;
import com.example.walrus_manager.data.model.Schedule;

import java.util.List;

public class EventViewModel extends AndroidViewModel {
    private final EventRepository repository;
    private final MutableLiveData<List<Event>> eventsForSchedule = new MutableLiveData<>();
    private final MutableLiveData<Boolean> dataChanged = new MutableLiveData<>(false);
    private long currentScheduleId = -1;

    public EventViewModel(Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(application);
        repository = new EventRepository(database.eventDao());
    }

    public void setScheduleId(long scheduleId) {
        if (currentScheduleId != scheduleId) {
            currentScheduleId = scheduleId;
            loadEvents();
        }
    }

    private void loadEvents() {
        if (currentScheduleId != -1) {
            repository.getEventsForSchedule(currentScheduleId, events -> {
                eventsForSchedule.postValue(events);
            });
        }
    }

    public LiveData<List<Event>> getEventsForSchedule() {
        return eventsForSchedule;
    }

    public LiveData<Boolean> getDataChanged() {
        return dataChanged;
    }

    public void insert(Event event, EventRepository.Callback<Long> callback) {
        repository.insert(event, id -> {
            loadEvents();
            dataChanged.postValue(true);
            callback.onComplete(id);
        });
    }

    public void update(Event event, EventRepository.Callback<Void> callback) {
        repository.update(event, result -> {
            loadEvents();
            dataChanged.postValue(true);
            callback.onComplete(result);
        });
    }

    public void delete(Event event, EventRepository.Callback<Void> callback) {
        repository.delete(event, result -> {
            loadEvents();
            dataChanged.postValue(true);
            callback.onComplete(result);
        });
    }

    public void getCurrentSchedule(long scheduleId, ScheduleRepository.Callback<Schedule> callback) {
        AppDatabase database = AppDatabase.getInstance(getApplication());
        ScheduleRepository scheduleRepository = new ScheduleRepository(database.scheduleDao());
        scheduleRepository.getScheduleById(scheduleId, callback);
    }
} 