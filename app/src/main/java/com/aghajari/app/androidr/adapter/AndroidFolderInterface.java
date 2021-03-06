package com.aghajari.app.androidr.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import com.aghajari.app.androidr.utils.PermissionUtils;

import java.io.File;
import java.util.List;

/**
 * a FileInterface for root of Android folder.
 * Will force to add Android/data and Android/obb folders on Android 11 (R) or newer.
 */
public class AndroidFolderInterface<T> extends FileInterface<T> {

    AndroidFolderInterface(@NonNull T file) {
        super(file);
    }

    public static AndroidFolderInterface<DocumentFile> fromDocumentFile(@NonNull DocumentFile documentFile) {
        return new AndroidFolderInterface<>(documentFile);
    }

    public static AndroidFolderInterface<File> fromFile(@NonNull File file) {
        return new AndroidFolderInterface<>(file);
    }

    public static AndroidFolderInterface<?> fromFileInterface(@NonNull FileInterface file) {
        if (file instanceof AndroidFolderInterface) return (AndroidFolderInterface<?>) file;
        return new AndroidFolderInterface<>(file.file);
    }

    @Override
    protected List<FileInterface<?>> internalListFiles(Context context) {
        List<FileInterface<?>> list = super.internalListFiles(context);
        checkDataAndObb(context, list);
        return list;
    }

    public void checkDataAndObb(Context context, List<FileInterface<?>> list) {
        if (PermissionUtils.needToUseDocumentFile()) {
            boolean obb = false, data = false;
            for (FileInterface<?> f : list) {
                if (f.getName().equalsIgnoreCase("obb"))
                    obb = true;
                if (f.getName().equalsIgnoreCase("data"))
                    data = true;

                if (obb && data) break;
            }

            if (!obb)
                checkDir(context, PermissionUtils.ANDROID_OBB, "obb", list);
            if (!data)
                checkDir(context, PermissionUtils.ANDROID_DATA, "data", list);
        }
    }

    private void checkDir(Context context, String id, String name, List<FileInterface<?>> list) {
        File file = new File(getAbsolutePath(context), name) {
            @Override
            public boolean isDirectory() {
                return true;
            }

            @Override
            public boolean exists() {
                return false;
            }
        };

        if (PermissionUtils.needToGetPermission(context, file)) {
            list.add(0, FileInterface.fromFile(file));
        } else {
            list.add(0, FileInterface.fromDocumentFile(DocumentFile.fromTreeUri(context, PermissionUtils.getAndroidFolderUri(id, true))));
        }
    }

}
