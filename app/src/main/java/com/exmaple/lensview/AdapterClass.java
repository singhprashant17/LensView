package com.exmaple.lensview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by webonise on 19/1/18.
 */

public class AdapterClass {
    private final LayoutInflater inflater;

    private final Context context;
    private final List<App> apps;

    public AdapterClass(Context context, List<App> apps) {
        this.context = context;
        this.apps = apps;
        inflater = LayoutInflater.from(context);
    }

    public static Bitmap loadBitmapFromView(View v) {
        v.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Bitmap b = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap
                .Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        v.draw(c);
        return b;
    }

    public int getCount() {
        return apps.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position) {
        return inflater.inflate(R.layout.dummmy, null);
    }

    public Bitmap getBitmap(int position) {
        return loadBitmapFromView(getView(position));
    }
}
