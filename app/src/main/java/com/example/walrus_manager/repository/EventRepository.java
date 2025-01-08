package com.example.walrus_manager.repository;

import com.example.walrus_manager.data.dao.EventDao;
import com.example.walrus_manager.data.model.Event;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventRepository {
    private final EventDao eventDao;
    private final ExecutorService executorService;

    public interface Callback<T> {
        void onComplete(T result);
    }

    public EventRepository(EventDao eventDao) {
        this.eventDao = eventDao;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void getEventsForSchedule(long scheduleId, Callback<List<Event>> callback) {
        executorService.execute(() -> {
            List<Event> events = eventDao.getEventsBySchedule(scheduleId);
            callback.onComplete(events);
        });
    }

    public void insert(Event event, Callback<Long> callback) {
        executorService.execute(() -> {
            long id = eventDao.insert(event);
            callback.onComplete(id);
        });
    }

    public void update(Event event, Callback<Void> callback) {
        executorService.execute(() -> {
            eventDao.update(event);
            callback.onComplete(null);
        });
    }

    public void delete(Event event, Callback<Void> callback) {
        executorService.execute(() -> {
            eventDao.delete(event);
            callback.onComplete(null);
        });
    }
} 