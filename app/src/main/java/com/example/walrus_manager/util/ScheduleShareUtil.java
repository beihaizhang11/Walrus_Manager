package com.example.walrus_manager.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.example.walrus_manager.data.AppDatabase;
import com.example.walrus_manager.data.model.Event;
import com.example.walrus_manager.data.model.Schedule;
import com.example.walrus_manager.repository.EventRepository;
import com.example.walrus_manager.repository.ScheduleRepository;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ScheduleShareUtil {
    private static final String SHARE_FILE_PREFIX = "schedule_";
    private static final String SHARE_FILE_SUFFIX = ".json";

    public static class ShareData {
        public Schedule schedule;
        public List<Event> events;

        public ShareData(Schedule schedule, List<Event> events) {
            this.schedule = schedule;
            this.events = events;
        }
    }

    public interface ImportCallback {
        void onSuccess();
        void onError(String message);
    }

    public static void shareSchedule(Context context, ShareData shareData) {
        try {
            // 创建临时文件
            File cacheDir = context.getCacheDir();
            File shareFile = new File(cacheDir, SHARE_FILE_PREFIX + 
                    shareData.schedule.getId() + SHARE_FILE_SUFFIX);

            // 将数据写入文件
            Gson gson = new Gson();
            String jsonData = gson.toJson(shareData);
            FileWriter writer = new FileWriter(shareFile);
            writer.write(jsonData);
            writer.close();

            // 创建分享意图
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/json");
            Uri contentUri = Uri.parse("content://" + context.getPackageName() + 
                    ".provider/cache/" + shareFile.getName());
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "分享日程：" + shareData.schedule.getTitle());
            shareIntent.putExtra(Intent.EXTRA_TEXT, "这是我的日程分享，点击导入即可使用！");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // 启动分享
            context.startActivity(Intent.createChooser(shareIntent, "分享日程"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void importSchedule(Context context, Uri uri, ImportCallback callback) {
        try {
            // 读取JSON数据
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            inputStream.close();

            // 解析数据
            ShareData shareData = parseShareData(stringBuilder.toString());
            if (shareData == null || shareData.schedule == null) {
                callback.onError("无效的日程数据");
                return;
            }

            importScheduleData(context, shareData, callback);
        } catch (IOException e) {
            callback.onError("读取文件失败：" + e.getMessage());
        }
    }

    public static void importSchedule(Context context, ShareData shareData, ImportCallback callback) {
        if (shareData == null || shareData.schedule == null) {
            callback.onError("无效的日程数据");
            return;
        }
        importScheduleData(context, shareData, callback);
    }

    private static void importScheduleData(Context context, ShareData shareData, ImportCallback callback) {
        // 获取数据库和仓库实例
        AppDatabase database = AppDatabase.getInstance(context);
        ScheduleRepository scheduleRepository = new ScheduleRepository(database.scheduleDao());
        EventRepository eventRepository = new EventRepository(database.eventDao());

        // 重置ID以避免冲突
        shareData.schedule.setId(0);
        for (Event event : shareData.events) {
            event.setId(0);
        }

        // 使用CountDownLatch等待数据库操作完成
        CountDownLatch latch = new CountDownLatch(1);
        final long[] scheduleId = new long[1];

        // 插入日程
        scheduleRepository.insert(shareData.schedule, id -> {
            scheduleId[0] = id;
            // 更新事件的scheduleId
            for (Event event : shareData.events) {
                event.setScheduleId(id);
            }

            // 插入所有事件
            insertEvents(eventRepository, shareData.events, 0, () -> {
                latch.countDown();
            });
        });

        // 等待所有操作完成
        try {
            latch.await();
            callback.onSuccess();
        } catch (InterruptedException e) {
            callback.onError("导入过程被中断");
        }
    }

    private static void insertEvents(EventRepository repository, List<Event> events, int index, Runnable onComplete) {
        if (index >= events.size()) {
            onComplete.run();
            return;
        }

        repository.insert(events.get(index), id -> 
            insertEvents(repository, events, index + 1, onComplete)
        );
    }

    private static ShareData parseShareData(String jsonData) {
        Gson gson = new Gson();
        return gson.fromJson(jsonData, ShareData.class);
    }
} 