package com.aghajari.app.androidr.adapter;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Task to load a folder files with progress.
 */
final class OpenFolderTask extends AsyncTask<FileInterface<?>, Integer, List<FileInterface<?>>> {
    final Adapter adapter;
    boolean loading = true;

    OpenFolderTask(Adapter adapter) {
        super();
        this.adapter = adapter;
    }

    @Override
    protected List<FileInterface<?>> doInBackground(FileInterface<?>... file) {
        Uri uri = file[0].getUri();
        if (CacheManager.get(uri) != null) {
            return CacheManager.get(uri);
        }

        List<FileInterface<?>> list = new ArrayList<>();
        int max;

        if (file[0].file instanceof DocumentFile) {
            if (file[0].asDocumentFile().listFiles() == null)
                return null;

            DocumentFile[] files = file[0].asDocumentFile().listFiles();
            max = files.length;
            for (int i = 1; i <= max; i++) {
                if (isCancelled())
                    return null;
                if (!Adapter.SHOW_HIDDEN_FILES && files[i - 1].getName().startsWith("."))
                    continue;

                publishProgress(i, max);
                list.add(FileInterface.fromDocumentFile(files[i - 1]));
            }
        } else {
            if (file[0].asFile().listFiles() == null)
                return null;

            File[] files = file[0].asFile().listFiles();
            max = files.length;
            for (int i = 1; i <= max; i++) {
                if (isCancelled())
                    return null;
                if (!Adapter.SHOW_HIDDEN_FILES && files[i - 1].getName().startsWith("."))
                    continue;

                publishProgress(i, max);
                list.add(FileInterface.fromFile(files[i - 1]));
            }
        }
        CacheManager.put(uri, list);

        if (file[0] instanceof AndroidFolderInterface) {
            ((AndroidFolderInterface<?>) file[0]).checkDataAndObb(adapter.context, list);
        }
        return list;
    }

    private final static Handler handler = new Handler();

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (loading)
                    adapter.loading.setVisibility(View.VISIBLE);
            }
        }, 100);
    }

    @Override
    protected void onPostExecute(List<FileInterface<?>> files) {
        super.onPostExecute(files);
        loading = false;
        if (!isCancelled() && files != null) {
            adapter.list.addAll(files);
            adapter.loading.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
            adapter.loaded();
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (!isCancelled()) {
            adapter.loading_progress.setText(values[0] + "/" + values[1]);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        loading = false;
        adapter.loading.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();
    }
}