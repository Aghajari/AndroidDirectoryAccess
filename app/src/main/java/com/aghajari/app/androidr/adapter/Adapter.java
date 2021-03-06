package com.aghajari.app.androidr.adapter;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.RecyclerView;

import com.aghajari.app.androidr.utils.FileUtil;
import com.aghajari.app.androidr.MainActivity;
import com.aghajari.app.androidr.utils.PermissionUtils;
import com.aghajari.app.androidr.R;
import com.bumptech.glide.Glide;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.VH> {

    public static boolean SHOW_HIDDEN_FILES = true;

    private final SimpleDateFormat format = new SimpleDateFormat("dd MMM hh:mm a");
    final Context context;
    private RecyclerView.LayoutManager layoutManager = null;

    private FileInterface<?> parentDir = null;
    private FileInterface<?> selectedDir = null;
    final List<FileInterface<?>> list = new ArrayList<>();

    //loading
    final LinearLayout loading;
    final TextView loading_progress;

    private OpenFolderTask task = null;

    public Adapter(Context context, LinearLayout loading) {
        this.context = context;
        this.loading = loading;
        loading_progress = loading.findViewById(R.id.loading_progress);
    }

    public boolean isEmpty() {
        return getItemCount() == 0 && loading.getVisibility() == View.GONE;
    }

    public void load(Uri treeUri) {
        if (PermissionUtils.needToUseDocumentFile()) {
            parentDir = AndroidFolderInterface.fromDocumentFile(DocumentFile.fromTreeUri(context, treeUri));
        } else {
            parentDir = AndroidFolderInterface.fromFile(new File(FileUtil.getFullPathFromTreeUri(treeUri, context)));
        }
        openFolder(parentDir, false);
    }

    public void load(File file) {
        parentDir = FileInterface.fromFile(file);
        openFolder(parentDir, false);
    }

    public void openFolder(FileInterface<?> file, boolean save) {
        if (task != null)
            task.cancel(true);

        if (save && selectedDir != null)
            CacheManager.saveState(selectedDir.getUri(), layoutManager.onSaveInstanceState());

        selectedDir = file;
        list.clear();
        notifyDataSetChanged();
        loading.setVisibility(View.INVISIBLE);

        task = new OpenFolderTask(this);
        task.execute(file);
    }

    public boolean backFolder() {
        if (selectedDir != null)
            CacheManager.removeState(selectedDir.getUri());

        if (task != null)
            task.cancel(true);

        if (parentDir == null || selectedDir.equals(parentDir))
            return false;

        FileInterface<?> select = selectedDir.getParentFile();
        if (select == null ||
                select.getAbsolutePath(context).equalsIgnoreCase(parentDir.getAbsolutePath(context)))
            openFolder(parentDir, false);
        else
            openFolder(select, false);
        return true;
    }

    void loaded() {
        Parcelable state = CacheManager.getState(selectedDir.getUri());
        if (state != null)
            layoutManager.onRestoreInstanceState(state);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            return new VH(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_item_dir, parent, false));
        } else {
            return new VH(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_item_file, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).isDirectory() ? 0 : 1;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.layoutManager = recyclerView.getLayoutManager();
    }

    public class VH extends RecyclerView.ViewHolder {

        AppCompatImageView img;
        TextView name, date, count;

        public VH(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            date = itemView.findViewById(R.id.date);
            count = itemView.findViewById(R.id.count);

            img = itemView.findViewById(R.id.img);
        }

        public void bind(final FileInterface<?> file) {
            if (file == null) return;

            // hidden file
            if (file.getName().startsWith("."))
                img.setAlpha(0.4f);
            else
                img.setAlpha(1f);

            name.setText(file.getName());
            date.setText(format.format(new Date(file.lastModified())));

            if (file.isDirectory()) {
                Glide.with(img).load(R.drawable.folder).into(img);

                if (PermissionUtils.needToGetPermission(context, file))
                    count.setText("Permission Needed! (Click)");
                else
                    count.setText(file.getListFilesCount() + " items");

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (PermissionUtils.needToGetPermission(context, file)) {
                            PermissionUtils.requestAndroidFolderPermission((Activity) context, MainActivity.REQUEST_CODE, file);
                        } else if (file.exists()) {
                            openFolder(file, true);
                        }
                    }
                });
            } else {
                Glide.with(img).load(R.drawable.file).into(img);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String text = "Path : " + file.getAbsolutePath(context)
                                + "\nSize : " + FileUtil.humanReadableByteCountSI(file.length());
                        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    String text = file.getAbsolutePath(context);
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }
    }
}
