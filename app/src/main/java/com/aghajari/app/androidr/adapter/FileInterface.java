package com.aghajari.app.androidr.adapter;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import com.aghajari.app.androidr.utils.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * a File interface to read File/DocumentFile easier.
 * Use DocumentFile on Android 11 (R) or newer.
 * Use File (normal) for older versions of android.
 */
public class FileInterface<T> {
    @NonNull
    protected final T file;

    private boolean isDirectory;
    private String cachedName;
    private long cachedLastModify;
    private int cacheListFilesCount = -1;
    private Uri cachedUri = null;

    FileInterface(@NonNull T file) {
        this.file = file;

        internalIsDirectory(); // do not comment this one
        // cache data
        internalName();
        getListFilesCount();
        lastModified();
    }

    public static FileInterface<DocumentFile> fromDocumentFile(@NonNull DocumentFile documentFile) {
        return new FileInterface<>(documentFile);
    }

    public static FileInterface<File> fromFile(@NonNull File file) {
        return new FileInterface<>(file);
    }

    DocumentFile asDocumentFile() {
        return (DocumentFile) file;
    }

    File asFile() {
        return (File) file;
    }

    public boolean canRead() {
        if (file instanceof DocumentFile)
            return asDocumentFile().canRead();
        else
            return asFile().canRead();
    }

    public boolean canWrite() {
        if (file instanceof DocumentFile)
            return asDocumentFile().canWrite();
        else
            return asFile().canWrite();
    }

    public boolean isFile() {
        if (file instanceof DocumentFile)
            return asDocumentFile().isFile();
        else
            return asFile().isFile();
    }

    protected void internalUri() {
        if (cachedUri == null)
            return;
        Uri uri;
        if (file instanceof DocumentFile)
            uri = asDocumentFile().getUri();
        else
            uri = Uri.fromFile(asFile());
        if (!uri.equals(cachedUri)) {
            if (CacheManager.contains(cachedUri)) {
                CacheManager.put(uri, CacheManager.remove(cachedUri));
            }
            if (CacheManager.containsState(cachedUri)) {
                CacheManager.saveState(uri, CacheManager.removeState(cachedUri));
            }
        }
        cachedUri = uri;
    }

    public Uri getUri() {
        if (cachedUri != null)
            return cachedUri;

        if (file instanceof DocumentFile)
            return cachedUri = asDocumentFile().getUri();
        else
            return cachedUri = Uri.fromFile(asFile());
    }

    public boolean delete() {
        if (file instanceof DocumentFile)
            return asDocumentFile().delete();
        else
            return asFile().delete();
    }

    public boolean renameTo(String displayName) {
        boolean res;
        if (file instanceof DocumentFile)
            res = asDocumentFile().renameTo(displayName);
        else
            res = asFile().renameTo(new File(asFile().getParent(), displayName));
        cachedName = internalName();
        cachedLastModify = lastModified();
        internalUri(); //update caches
        return res;
    }

    protected void internalIsDirectory() {
        if (file instanceof DocumentFile) {
            isDirectory = asDocumentFile().isDirectory();
        } else {
            isDirectory = asFile().isDirectory();
        }
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public String getAbsolutePath(Context context) {
        if (file instanceof DocumentFile)
            return FileUtil.getPath(context, asDocumentFile().getUri());
        else
            return asFile().getAbsolutePath();
    }

    public boolean exists() {
        if (file instanceof DocumentFile)
            return asDocumentFile().exists();
        else
            return asFile().exists();
    }

    protected String internalName() {
        if (file instanceof DocumentFile)
            return cachedName = asDocumentFile().getName();
        else
            return cachedName = asFile().getName();
    }

    public String getName() {
        if (cachedName == null)
            return internalName();
        return cachedName;
    }

    public long length() {
        if (file instanceof DocumentFile)
            return asDocumentFile().length();
        else
            return asFile().length();
    }

    public long lastModified() {
        if (cachedLastModify >= 0)
            return cachedLastModify;

        if (file instanceof DocumentFile)
            return cachedLastModify = asDocumentFile().lastModified();
        else
            return cachedLastModify = asFile().lastModified();
    }

    protected List<FileInterface<?>> internalListFiles(Context context) {
        Uri uri = getUri();
        if (CacheManager.get(uri) != null) {
            return CacheManager.get(uri);
        }

        List<FileInterface<?>> list = new ArrayList<>();
        if (file instanceof DocumentFile) {
            if (asDocumentFile().listFiles() == null)
                return null;

            for (DocumentFile f : asDocumentFile().listFiles()) {
                if (!Adapter.SHOW_HIDDEN_FILES && f.getName().startsWith("."))
                    continue;
                list.add(FileInterface.fromDocumentFile(f));
            }
        } else {
            if (asFile().listFiles() == null)
                return null;

            for (File f : asFile().listFiles()) {
                if (!Adapter.SHOW_HIDDEN_FILES && f.getName().startsWith("."))
                    continue;
                list.add(FileInterface.fromFile(f));
            }
        }
        CacheManager.put(uri, list);
        return list;
    }

    public FileInterface<?>[] listFiles(Context context) {
        return internalListFiles(context).toArray(new FileInterface[0]);
    }

    public int getListFilesCount() {
        if (cacheListFilesCount >= 0)
            return cacheListFilesCount;

        if (file instanceof DocumentFile) {
            if (asDocumentFile().listFiles() == null)
                return cacheListFilesCount = 0;

            if (Adapter.SHOW_HIDDEN_FILES) {
                return cacheListFilesCount = asDocumentFile().listFiles().length;
            } else {
                cacheListFilesCount = 0;
                for (DocumentFile f : asDocumentFile().listFiles()) {
                    if (!Adapter.SHOW_HIDDEN_FILES && f.getName().startsWith(".")) continue;
                    cacheListFilesCount++;
                }
                return cacheListFilesCount;
            }
        } else {
            if (asFile().listFiles() == null)
                return cacheListFilesCount = 0;

            if (Adapter.SHOW_HIDDEN_FILES) {
                return cacheListFilesCount = asFile().listFiles().length;
            } else {
                cacheListFilesCount = 0;
                for (File f : asFile().listFiles()) {
                    if (!Adapter.SHOW_HIDDEN_FILES && f.getName().startsWith(".")) continue;
                    cacheListFilesCount++;
                }
                return cacheListFilesCount;
            }
        }
    }

    public FileInterface<?> getParentFile() {
        if (file instanceof DocumentFile) {
            if (asDocumentFile().getParentFile() == null) return null;
            return FileInterface.fromDocumentFile(asDocumentFile().getParentFile());
        } else {
            if (asFile().getParentFile() == null) return null;
            return FileInterface.fromFile(asFile().getParentFile());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileInterface<?> that = (FileInterface<?>) o;
        return file.equals(that.file);
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }
}
