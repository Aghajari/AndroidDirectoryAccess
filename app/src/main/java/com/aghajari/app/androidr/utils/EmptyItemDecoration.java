package com.aghajari.app.androidr.utils;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import androidx.recyclerview.widget.RecyclerView;

import com.aghajari.app.androidr.adapter.Adapter;

public class EmptyItemDecoration extends RecyclerView.ItemDecoration {
    Paint paint;

    public EmptyItemDecoration(int size) {
        paint = new Paint();
        //paint.setColor(Color.LTGRAY);
        //paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(size);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (parent.getLayoutManager() == null)
            return;

        if (parent.getAdapter().getItemCount() == 0
                && !PermissionUtils.hasAllPermissions((Activity) parent.getContext()))
            drawCenter(c,paint,"Permissions Needed!");

        else if (((Adapter) parent.getAdapter()).isEmpty())
            drawCenter(c, paint, "No files");
    }

    private final Rect r = new Rect();

    private void drawCenter(Canvas canvas, Paint paint, String text) {
        canvas.getClipBounds(r);
        int cHeight = r.height();
        int cWidth = r.width();
        paint.setTextAlign(Paint.Align.LEFT);
        paint.getTextBounds(text, 0, text.length(), r);
        float x = cWidth / 2f - r.width() / 2f - r.left;
        float y = cHeight / 2f + r.height() / 2f - r.bottom;
        canvas.drawText(text, x, y, paint);
    }

}