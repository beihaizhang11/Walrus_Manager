package com.example.walrus_manager.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.walrus_manager.data.dao.EventDao;
import com.example.walrus_manager.data.dao.ScheduleDao;
import com.example.walrus_manager.data.model.Event;
import com.example.walrus_manager.data.model.Schedule;

@Database(entities = {Schedule.class, Event.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract ScheduleDao scheduleDao();
    public abstract EventDao eventDao();

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 创建临时表
            database.execSQL(
                "CREATE TABLE schedules_new (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "title TEXT, " +
                "description TEXT, " +
                "userId INTEGER NOT NULL, " +
                "createdAt INTEGER NOT NULL, " +
                "isRemote INTEGER NOT NULL DEFAULT 0, " +
                "createdBy TEXT NOT NULL DEFAULT '')"
            );

            // 复制现有数据
            database.execSQL(
                "INSERT INTO schedules_new (id, title, description, userId, createdAt) " +
                "SELECT id, title, description, userId, createdAt FROM schedules"
            );

            // 删除旧表
            database.execSQL("DROP TABLE schedules");

            // 重命名新表
            database.execSQL("ALTER TABLE schedules_new RENAME TO schedules");
        }
    };

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "walrus_manager.db"
                    )
                    .addMigrations(MIGRATION_1_2)
                    .build();
                }
            }
        }
        return INSTANCE;
    }
} 