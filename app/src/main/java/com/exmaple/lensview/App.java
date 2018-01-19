package com.exmaple.lensview;

import android.graphics.Bitmap;
import android.support.annotation.ColorInt;

public class App {

    private int mId;
    private Bitmap mIcon;
    private @ColorInt
    int mPaletteColor;

    public App() {
    }

    public int getId() {
        return mId;
    }

    public void setId(int mId) {
        this.mId = mId;
    }

    public Bitmap getIcon() {
        return mIcon;
    }

    public void setIcon(Bitmap icon) {
        mIcon = icon;
    }

    @Override
    public String toString() {
        return "App{" +
                "mId=" + mId +
                ", mIcon=" + mIcon +
                ", mPaletteColor=" + mPaletteColor +
                '}';
    }
}