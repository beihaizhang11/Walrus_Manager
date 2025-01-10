package com.example.walrus_manager.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.walrus_manager.data.model.Schedule;

import java.util.List;

@Dao
public interface ScheduleDao {
    @Query("SELECT * FROM schedules ORDER BY createdAt DESC")
    List<Schedule> getAllSchedules();

    @Insert
    long insert(Schedule schedule);

    @Update
    void update(Schedule schedule);

    @Delete
    void delete(Schedule schedule);

    @Query("SELECT * FROM schedules WHERE id = :scheduleId")
    Schedule getScheduleById(long scheduleId);
} 