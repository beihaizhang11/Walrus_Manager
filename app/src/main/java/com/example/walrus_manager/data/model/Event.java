package com.example.walrus_manager.data.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "events",
    foreignKeys = @ForeignKey(
        entity = Schedule.class,
        parentColumns = "id",
        childColumns = "scheduleId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("scheduleId")}
)
public class Event {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String title;
    private String description;
    private long scheduleId;
    private long targetDate;
    private long createdAt;

    public Event(String title, String description, long scheduleId, long targetDate) {
        this.title = title;
        this.description = description;
        this.scheduleId = scheduleId;
        this.targetDate = targetDate;
        this.createdAt = System.currentTimeMillis();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public long getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(long targetDate) {
        this.targetDate = targetDate;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
} 