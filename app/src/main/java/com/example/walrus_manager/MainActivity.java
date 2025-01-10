package com.example.walrus_manager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.walrus_manager.util.ScheduleShareUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MainActivity extends AppCompatActivity {
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 设置Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 设置Navigation
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.scheduleListFragment)
                    .build();
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        }

        // 处理分享文件的打开
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.getData() != null) {
            Uri uri = intent.getData();
            if (uri != null) {
                showImportDialog(uri);
            }
        }
    }

    private void showImportDialog(Uri uri) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("导入日程")
                .setMessage("是否导入此日程？导入后可以在日程列表中查看。")
                .setPositiveButton("导入", (dialog, which) -> {
                    importSchedule(uri);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void importSchedule(Uri uri) {
        ScheduleShareUtil.importSchedule(this, uri, new ScheduleShareUtil.ImportCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "导入成功", Toast.LENGTH_SHORT).show();
                    // 导航到日程列表页面
                    navController.navigate(R.id.scheduleListFragment);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "导入失败：" + message, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}