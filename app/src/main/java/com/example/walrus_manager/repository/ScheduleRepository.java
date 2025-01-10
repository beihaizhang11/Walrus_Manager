package com.example.walrus_manager.repository;

import com.example.walrus_manager.data.dao.ScheduleDao;
import com.example.walrus_manager.data.model.Schedule;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScheduleRepository {
    private final ScheduleDao scheduleDao;
    private final ExecutorService executorService;

    public interface Callback<T> {
        void onComplete(T result);
    }

    public ScheduleRepository(ScheduleDao scheduleDao) {
        this.scheduleDao = scheduleDao;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void getAllSchedules(Callback<List<Schedule>> callback) {
        executorService.execute(() -> {
            List<Schedule> schedules = scheduleDao.getAllSchedules();
            callback.onComplete(schedules);
        });
    }

    public void insert(Schedule schedule, Callback<Long> callback) {
        executorService.execute(() -> {
            long id = scheduleDao.insert(schedule);
            callback.onComplete(id);
        });
    }

    public void update(Schedule schedule, Callback<Void> callback) {
        executorService.execute(() -> {
            scheduleDao.update(schedule);
            callback.onComplete(null);
        });
    }

    public void delete(Schedule schedule, Callback<Void> callback) {
        executorService.execute(() -> {
            scheduleDao.delete(schedule);
            callback.onComplete(null);
        });
    }

    public void getScheduleById(long scheduleId, Callback<Schedule> callback) {
        executorService.execute(() -> {
            Schedule schedule = scheduleDao.getScheduleById(scheduleId);
            callback.onComplete(schedule);
        });
    }
} 