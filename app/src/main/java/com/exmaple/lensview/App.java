package com.exmaple.lensview;

import android.graphics.Bitmap;
import android.support.annotation.ColorInt;
import android.view.View;

public class App {

    private int mId;
    private Bitmap mIcon;
    private @ColorInt
    int mPaletteColor;
    private View view;

    public App() {
    }

    public int getId() {
        return mId;
    }

    public void setId(int mId) {
        this.mId = mId;
    }

    @Override
    public String toString() {
        return "App{" +
                "mId=" + mId +
                ", mIcon=" + mIcon +
                ", mPaletteColor=" + mPaletteColor +
                '}';
    }

    public Bitmap getIcon() {
        return mIcon;
    }

    public void setIcon(Bitmap icon) {
        mIcon = icon;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }
}