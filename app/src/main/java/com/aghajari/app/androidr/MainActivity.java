package com.aghajari.app.androidr;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.aghajari.app.androidr.adapter.Adapter;
import com.aghajari.app.androidr.utils.DividerItemDecoration;
import com.aghajari.app.androidr.utils.EmptyItemDecoration;
import com.aghajari.app.androidr.utils.PermissionUtils;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    public final static int REQUEST_CODE = 100;

    Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adapter = new Adapter(this, findViewById(R.id.loading_parent));

        RecyclerView rv = findViewById(R.id.rv);
        rv.setAdapter(adapter);
        rv.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL,
                new int[]{dp(84), dp(24)}));
        rv.addItemDecoration(new EmptyItemDecoration(dp(20)));

        if (PermissionUtils.checkAndRequestPermissions(this, REQUEST_CODE, 2))
            load();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && data != null && data.getData() != null) {
            PermissionUtils.grantUriPermission(this, data.getData());
            load();
        }
        PermissionUtils.checkAndRequestPermissions(this, REQUEST_CODE, 2);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);

        if (PermissionUtils.checkAndRequestPermissions(this, REQUEST_CODE, 2))
            load();
    }

    private void load() {
        adapter.load(PermissionUtils.getAndroidFolderUri(PermissionUtils.ANDROID, true));
    }

    @Override
    public void onBackPressed() {
        if (!adapter.backFolder())
            super.onBackPressed();
    }

    private int dp(int value) {
        return (int) (getResources().getDisplayMetrics().density * value);
    }
}