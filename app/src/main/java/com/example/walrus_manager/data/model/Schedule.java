package com.example.walrus_manager.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

@Entity(tableName = "schedules")
public class Schedule {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String title;
    private String description;
    private long userId;
    private long createdAt;
    private boolean isRemote;
    private String createdBy;

    public Schedule(String title, String description, long userId) {
        this.title = title;
        this.description = description;
        this.userId = userId;
        this.createdAt = System.currentTimeMillis();
        this.isRemote = false;
        this.createdBy = "";
    }

    @Ignore
    public Schedule(String title, String description, String createdBy, long createdAt) {
        this.title = title;
        this.description = description;
        this.userId = 0;
        this.createdAt = createdAt;
        this.isRemote = true;
        this.createdBy = createdBy;
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

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRemote() {
        return isRemote;
    }

    public void setRemote(boolean remote) {
        isRemote = remote;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
} 