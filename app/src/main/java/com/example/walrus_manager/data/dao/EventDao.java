package com.example.walrus_manager.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.walrus_manager.data.model.Event;

import java.util.List;

@Dao
public interface EventDao {
    @Query("SELECT * FROM events WHERE scheduleId = :scheduleId ORDER BY targetDate ASC")
    List<Event> getEventsBySchedule(long scheduleId);

    @Insert
    long insert(Event event);

    @Update
    void update(Event event);

    @Delete
    void delete(Event event);
} 