package com.example.walrus_manager.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.example.walrus_manager.data.model.Event;
import com.example.walrus_manager.data.model.Schedule;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RemoteDatabaseUtil {
    private static final String TAG = "RemoteDatabaseUtil";
    private static final String PREF_DB_URL = "remote_db_url";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public interface RemoteCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    public static void saveCredentials(Context context, String url) {
        Log.d(TAG, "保存数据库URL: " + url);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit()
                .putString(PREF_DB_URL, url)
                .apply();
    }

    public static void clearCredentials(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit()
                .remove(PREF_DB_URL)
                .apply();
    }

    public void testConnection(Context context, RemoteCallback<Void> callback) {
        executor.execute(() -> {
            Log.d(TAG, "开始测试数据库连接...");
            try (Connection conn = getConnection(context)) {
                if (conn != null) {
                    Log.d(TAG, "数据库连接成功");
                    handler.post(() -> callback.onSuccess(null));
                } else {
                    Log.e(TAG, "数据库连接失败: 连接为null");
                    handler.post(() -> callback.onError("无法连接到数据库"));
                }
            } catch (SQLException e) {
                Log.e(TAG, "数据库连接异常: " + e.getMessage(), e);
                handler.post(() -> callback.onError("连接失败：" + e.getMessage()));
            }
        });
    }

    public void getRemoteSchedules(Context context, RemoteCallback<List<Schedule>> callback) {
        executor.execute(() -> {
            Log.d(TAG, "开始获取远程日程...");
            try (Connection conn = getConnection(context)) {
                if (conn == null) {
                    Log.e(TAG, "获取日程失败: 数据库连接为null");
                    handler.post(() -> callback.onError("无法连接到数据库"));
                    return;
                }

                List<Schedule> schedules = new ArrayList<>();
                String sql = "SELECT * FROM remote_schedules ORDER BY createdAt DESC";
                Log.d(TAG, "执行SQL查询: " + sql);
                try (PreparedStatement stmt = conn.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Schedule schedule = new Schedule(
                                rs.getString("title"),
                                rs.getString("description"),
                                rs.getString("createdBy"),
                                rs.getLong("createdAt")
                        );
                        schedule.setId(rs.getLong("id"));
                        schedules.add(schedule);
                    }
                    Log.d(TAG, "成功获取到 " + schedules.size() + " 个日程");
                }
                handler.post(() -> callback.onSuccess(schedules));
            } catch (SQLException e) {
                Log.e(TAG, "获取日程异常: " + e.getMessage(), e);
                handler.post(() -> callback.onError("获取日程失败：" + e.getMessage()));
            }
        });
    }

    public void getRemoteEvents(Context context, long scheduleId, RemoteCallback<List<Event>> callback) {
        executor.execute(() -> {
            Log.d(TAG, "开始获取日程ID " + scheduleId + " 的事件...");
            try (Connection conn = getConnection(context)) {
                if (conn == null) {
                    Log.e(TAG, "获取事件失败: 数据库连接为null");
                    handler.post(() -> callback.onError("无法连接到数据库"));
                    return;
                }

                List<Event> events = new ArrayList<>();
                String sql = "SELECT * FROM remote_events WHERE scheduleId = ? ORDER BY targetDate ASC";
                Log.d(TAG, "执行SQL查询: " + sql + " [scheduleId=" + scheduleId + "]");
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, scheduleId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            Event event = new Event(
                                    rs.getString("title"),
                                    rs.getString("description"),
                                    scheduleId,
                                    rs.getLong("targetDate")
                            );
                            event.setId(rs.getLong("id"));
                            event.setCreatedAt(rs.getLong("createdAt"));
                            events.add(event);
                        }
                        Log.d(TAG, "成功获取到 " + events.size() + " 个事件");
                    }
                }
                handler.post(() -> callback.onSuccess(events));
            } catch (SQLException e) {
                Log.e(TAG, "获取事件异常: " + e.getMessage(), e);
                handler.post(() -> callback.onError("获取事件失败：" + e.getMessage()));
            }
        });
    }

    private Connection getConnection(Context context) throws SQLException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String url = prefs.getString(PREF_DB_URL, "");

        Log.d(TAG, "尝试连接数据库...");
        Log.d(TAG, "数据库URL: " + url);

        if (url.isEmpty()) {
            Log.e(TAG, "数据库URL为空");
            return null;
        }

        try {
            Log.d(TAG, "加载MySQL驱动...");
            Class.forName("com.mysql.jdbc.Driver");
            Log.d(TAG, "MySQL驱动加载成功，开始建立连接...");
            Connection conn = DriverManager.getConnection(url + "&useSSL=false");
            Log.d(TAG, "数据库连接建立成功");
            return conn;
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "MySQL驱动加载失败: " + e.getMessage(), e);
            throw new SQLException("MySQL驱动程序未找到", e);
        } catch (SQLException e) {
            Log.e(TAG, "数据库连接失败: " + e.getMessage(), e);
            throw e;
        }
    }
} 