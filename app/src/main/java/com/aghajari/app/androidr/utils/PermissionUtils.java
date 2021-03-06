package com.aghajari.app.androidr.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.aghajari.app.androidr.adapter.FileInterface;

import java.io.File;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

/**
 * A helper class to check and request read/write/manage storage permissions.
 * Supports all version of androids.
 */
public final class PermissionUtils {

    public static final String ANDROID = "primary:Android";
    public static final String ANDROID_DATA = "primary:Android/data";
    public static final String ANDROID_OBB = "primary:Android/obb";

    /**
     * @return true if you have the permissions.
     */
    public static boolean checkAndRequestPermissions(Activity context, int requestCodeFolder, int requestCodePermission) {
        if (!PermissionUtils.checkPermissions(context)) {
            PermissionUtils.requestPermissions(context, requestCodePermission);
            return false;
        } else if (!PermissionUtils.checkAndroidFolderPermission(context, PermissionUtils.ANDROID)) {
            PermissionUtils.requestAndroidFolderPermission(context, requestCodeFolder, PermissionUtils.ANDROID);
            return false;
        }
        return true;
    }

    /**
     * @return true if you have all of the permissions to manage storage.
     */
    public static boolean hasAllPermissions(Activity context) {
        return PermissionUtils.checkAndroidFolderPermission(context, PermissionUtils.ANDROID)
                && PermissionUtils.checkPermissions(context);
    }

    /**
     * @return true if Android version is higher or equals 30
     */
    public static boolean needToUseDocumentFile() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R;
    }

    /**
     * @return true if you have access to write/read/manage storage.
     */
    public static boolean checkPermissions(Context context) {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = ContextCompat.checkSelfPermission(context, READ_EXTERNAL_STORAGE);
            int result1 = ContextCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    /**
     * request permissions for write/read/manage storage.
     */
    public static void requestPermissions(Activity context, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent i = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            i.setData(Uri.parse("package:" + context.getPackageName()));
            context.startActivityForResult(i, requestCode);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            ActivityCompat.requestPermissions(context, new String[]{
                            READ_EXTERNAL_STORAGE,
                            WRITE_EXTERNAL_STORAGE}
                    , requestCode);
        }
    }

    /**
     * {@link #ANDROID} = /storage/emulated/0/Android
     * {@link #ANDROID_DATA} = /storage/emulated/0/Android/data
     * {@link #ANDROID_OBB} = /storage/emulated/0/Android/obb
     *
     * @return android folder uri.
     */
    public static Uri getAndroidFolderUri(String id, boolean tree) {
        if (tree)
            return DocumentsContract.buildTreeDocumentUri("com.android.externalstorage.documents", id);
        else
            return DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", id);
    }

    /**
     * @return true if you have access to manage Android folders.
     */
    public static boolean checkAndroidFolderPermission(Context context, String id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Uri android_tree_uri = getAndroidFolderUri(id, true);

            for (UriPermission uriPermission : context.getContentResolver().getPersistedUriPermissions()) {
                if (uriPermission.getUri().equals(android_tree_uri) && uriPermission.isReadPermission())
                    return true;
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * request permission to manage Android folders.
     */
    public static void requestAndroidFolderPermission(Activity context, int requestCode, String id) {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                .putExtra("android.provider.extra.SHOW_ADVANCED", true)
                .putExtra("android.content.extra.SHOW_ADVANCED", true)
                .putExtra(DocumentsContract.EXTRA_INITIAL_URI, getAndroidFolderUri(id, false));
        context.startActivityForResult(i, requestCode);
    }

    /**
     * Check if needs to get permission for an Android folder (data/obb)
     */
    public static boolean needToGetPermission(Context context, FileInterface<?> file) {
        return (file.getName().equalsIgnoreCase("obb")
                && !checkAndroidFolderPermission(context, ANDROID_OBB))
                || (file.getName().equalsIgnoreCase("data")
                && !checkAndroidFolderPermission(context, ANDROID_DATA));
    }

    /**
     * Check if needs to get permission for an Android folder (data/obb)
     */
    public static boolean needToGetPermission(Context context, File file) {
        return (file.getName().equalsIgnoreCase("obb")
                && !checkAndroidFolderPermission(context, ANDROID_OBB))
                || (file.getName().equalsIgnoreCase("data")
                && !checkAndroidFolderPermission(context, ANDROID_DATA));
    }

    /**
     * request permission to manage Android/data or Android/obb
     */
    public static void requestAndroidFolderPermission(Activity context, int requestCode, FileInterface<?> file) {
        if (file.getName().equalsIgnoreCase("obb")) {
            requestAndroidFolderPermission(context, requestCode, ANDROID_OBB);
        } else if (file.getName().equalsIgnoreCase("data")) {
            requestAndroidFolderPermission(context, requestCode, ANDROID_DATA);
        }
    }

    public static void grantUriPermission(Context context, Uri uri) {
        int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
        context.getContentResolver().takePersistableUriPermission(uri, flags);
        context.grantUriPermission(context.getPackageName(), uri, flags);
    }

}
