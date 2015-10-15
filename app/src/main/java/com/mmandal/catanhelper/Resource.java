package com.mmandal.catanhelper;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;

class Tag {
    int outcome;
    String label;
    Tag(int outcome, String label) {
        this.outcome = outcome;
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

    Resource(DisplayMetrics dm, Bitmap icon, int color) {
        displayMetrics_ = dm;
        icon_ = icon;
        color_ = color;
    }

    protected Tag tag_;

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

    Paint getBorderPaint(boolean resourceGenerated) {
        Paint border = new Paint();
        border.setStyle(Paint.Style.STROKE);
        if (resourceGenerated) {
            border.setStrokeWidth(Utility.fromDp(displayMetrics_, 5));
            border.setColor(color_);
        } else {
            border.setStrokeWidth(Utility.fromDp(displayMetrics_, 2));
            border.setColor(Color.BLACK);
        }
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

    public void setTag(Tag outcome) {
        tag_ = outcome;
    }

    Tag getTag() {
        return tag_;
    }

    Bitmap getIcon() {
        return icon_;
    }

    int getColor() {
        return color_;
    }
}