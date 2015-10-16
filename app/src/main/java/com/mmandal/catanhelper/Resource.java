package com.mmandal.catanhelper;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;

class Tag {
    int diceFace;
    String label;

    Tag(int diceFace, String label) {
        this.diceFace = diceFace;
        this.label = label;
    }
}

/**
 * Created by mmandal on 10/3/15.
 */
class Resource {

    private final DisplayMetrics displayMetrics_;

    private final Bitmap icon_;

    private final int color_;
    protected Tag tag_;

    Resource(DisplayMetrics dm, Bitmap icon, int color) {
        displayMetrics_ = dm;
        icon_ = icon;
        color_ = color;
    }

    Paint getFillPaint(boolean resourceGenerated) {
        Paint fill = new Paint();
        fill.setAntiAlias(true);
        fill.setColor(color_);
        fill.setStyle(Paint.Style.FILL);
        if (resourceGenerated) {
            fill.setAlpha(100);
        }
        return fill;
    }

    Paint getBorderPaint() {
        Paint border = new Paint();
        border.setStyle(Paint.Style.STROKE);
        border.setStrokeWidth(Utility.fromDp(displayMetrics_, 2));
        border.setColor(Color.BLACK);
        border.setAntiAlias(true);
        return border;
    }

    Paint getOvalPaint() {
        Paint border = new Paint();
        border.setStyle(Paint.Style.STROKE);
        border.setStrokeWidth(Utility.fromDp(displayMetrics_, 2));
        border.setColor(color_);
        border.setAntiAlias(true);
        return border;
    }

    Paint getOvalPaint2() {
        Paint border = new Paint();
        border.setStyle(Paint.Style.STROKE);
        border.setStrokeWidth(Utility.fromDp(displayMetrics_, 4));
        border.setColor(Color.BLACK);
        border.setAntiAlias(true);
        return border;
    }

    Paint getTextPaint(boolean resourceGenerated) {
        Paint text = new Paint();
        text.setColor(Color.WHITE);
        text.setAntiAlias(true);
        text.setTextSize(resourceGenerated ? Utility.fromDp(displayMetrics_, 30) : Utility.fromDp(displayMetrics_, 15));
        text.setTextAlign(Paint.Align.CENTER);
        return text;
    }

    Paint getShadowPaint(boolean resourceGenerated) {
        Paint text = new Paint();
        text.setColor(Color.BLACK);
        text.setAntiAlias(true);
        text.setTextSize(resourceGenerated ? Utility.fromDp(displayMetrics_, 30) : Utility.fromDp(displayMetrics_, 15));
        text.setTextAlign(Paint.Align.CENTER);
        return text;
    }

    Tag getTag() {
        return tag_;
    }

    public void setTag(Tag outcome) {
        tag_ = outcome;
    }

    Bitmap getIcon() {
        return icon_;
    }

    int getColor() {
        return color_;
    }
}