package com.aghajari.app.androidr.adapter;

import android.net.Uri;
import android.os.Parcelable;

import java.util.List;
import java.util.WeakHashMap;

/**
 * 1. Cache loaded folder files
 * for a better performance on second load.
 * <p>
 * 2. Cache RecyclerView's LayoutManager state
 * for showing the previous position when {@link Adapter#backFolder()} called.
 */
final class CacheManager {

    private static final WeakHashMap<Uri, List<FileInterface<?>>> cached = new WeakHashMap<>();

    public static List<FileInterface<?>> get(Uri uri) {
        return cached.get(uri);
    }

    public static void put(Uri uri, List<FileInterface<?>> files) {
        cached.put(uri, files);
    }

    public static List<FileInterface<?>> remove(Uri uri) {
        return cached.remove(uri);
    }

    public static boolean contains(Uri uri) {
        return cached.containsKey(uri);
    }

    // ****

    private static final WeakHashMap<Uri, Parcelable> cachedStates = new WeakHashMap<>();

    public static Parcelable getState(Uri uri) {
        return cachedStates.get(uri);
    }

    public static void saveState(Uri uri, Parcelable state) {
        cachedStates.put(uri, state);
    }

    public static Parcelable removeState(Uri uri) {
        return cachedStates.remove(uri);
    }

    public static boolean containsState(Uri uri) {
        return cachedStates.containsKey(uri);
    }
}
