package com.mmandal.catanhelper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;

/**
 * Created by mmandal on 10/3/15.
 */
class Hexagon extends View {
    private static final String TAG = Hexagon.class.getSimpleName();
    private final Resource resource_;
    private Path boundary_ = new Path();
    private Rect iconBounds_ = new Rect();
    private Point textCenter_ = new Point();
    private int numTurns_ = 0;
    private boolean resourceGenerated_ = false;

    Hexagon(Context context, Resource resource) {
        super(context);
        resource_ = resource;
    }

    private static Path createHorizontal(Point leftTop, int width, int height) {
        assert width >= height;
        Path path = new Path();
        path.moveTo(leftTop.x + width / 4,     leftTop.y);
        path.lineTo(leftTop.x + 3 * width / 4, leftTop.y);
        path.lineTo(leftTop.x + width,         leftTop.y + height / 2);
        path.lineTo(leftTop.x + 3 * width / 4, leftTop.y + height);
        path.lineTo(leftTop.x + width / 4, leftTop.y + height);
        path.lineTo(leftTop.x, leftTop.y + height / 2);
        path.close();
        return path;
    }

    private static Path createVertical(Point leftTop, int width, int height) {
        assert width <= height;
        Path path = new Path();
        path.moveTo(leftTop.x + width / 2, leftTop.y);
        path.lineTo(leftTop.x + width,     leftTop.y + height / 4);
        path.lineTo(leftTop.x + width,     leftTop.y + 3 * height / 4);
        path.lineTo(leftTop.x + width / 2, leftTop.y + height);
        path.lineTo(leftTop.x,             leftTop.y + 3 * height / 4);
        path.lineTo(leftTop.x,             leftTop.y + height / 4);
        path.close();
        return path;
    }

    @Override
    public void onLayout(boolean b, int left, int top, int right, int bottom) {
        int width = right - left;
        int height = bottom - top;
        iconBounds_ = new Rect(left, top + height / 4, right, bottom - height / 4);
        boundary_ = createVertical(new Point(left, top), width, height);
        textCenter_ = new Point(left + width / 2, top + height / 2);
    }

    void rollForward(boolean resourceGenrated) {
        resourceGenerated_ = resourceGenrated;
        ++numTurns_;
    }

    void rollBackward(boolean resourceGenrated) {
        resourceGenerated_ = resourceGenrated;
        --numTurns_;
    }

    @Override
    public void onDraw(Canvas canvas) {
        assert numTurns_ >= 0;
        if (numTurns_ == 0) {
            onDrawFirstTime(canvas);
        } else {
            onDrawGamePlay(canvas);
        }
    }


    void onDrawFirstTime(Canvas canvas) {
        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.WHITE);
        canvas.drawPath(boundary_, bgPaint);
        canvas.drawPath(boundary_, resource_.getFillPaint(false));
        canvas.drawPath(boundary_, resource_.getBorderPaint());
        canvas.drawBitmap(resource_.getIcon(), null, iconBounds_, new Paint());

        Paint shadowPaint = resource_.getShadowPaint(true);
        int shadowX = 2 + textCenter_.x;
        int shadowY = 2 + (int)(textCenter_.y - ((shadowPaint.descent() + shadowPaint.ascent()) / 2));
        canvas.drawText(resource_.getTag().label, shadowX, shadowY, shadowPaint);

        Paint textPaint = resource_.getTextPaint(true);
        int textX = textCenter_.x;
        int textY = (int)(textCenter_.y - ((textPaint.descent() + textPaint.ascent()) / 2));
        canvas.drawText(resource_.getTag().label, textX, textY, textPaint);
    }

    void onDrawGamePlay(Canvas canvas) {
        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.WHITE);
        canvas.drawPath(boundary_, bgPaint);
        if (resourceGenerated_) {
            canvas.drawPath(boundary_, resource_.getFillPaint(true));
            canvas.drawPath(boundary_, resource_.getBorderPaint());

            canvas.drawBitmap(resource_.getIcon(), null, iconBounds_, new Paint());

            Paint shadowPaint = resource_.getShadowPaint(true);
            int shadowX = 2 + textCenter_.x;
            int shadowY = 2 + (int)(textCenter_.y - ((shadowPaint.descent() + shadowPaint.ascent()) / 2));
            canvas.drawText(Integer.toString(getDiceFace()), shadowX, shadowY, shadowPaint);

            Paint textPaint = resource_.getTextPaint(true);
            int textX = textCenter_.x;
            int textY = (int)(textCenter_.y - ((textPaint.descent() + textPaint.ascent()) / 2));
            canvas.drawText(Integer.toString(getDiceFace()), textX, textY, textPaint);
        } else {
            canvas.drawPath(boundary_, resource_.getFillPaint(false));
            canvas.drawPath(boundary_, resource_.getBorderPaint());

            Paint shadowPaint = resource_.getShadowPaint(false);
            int shadowX = 1 + textCenter_.x;
            int shadowY = 1 + (int)(textCenter_.y - ((shadowPaint.descent() + shadowPaint.ascent()) / 2));
            canvas.drawText(resource_.getTag().label, shadowX, shadowY, shadowPaint);

            Paint textPaint = resource_.getTextPaint(false);
            int textX = textCenter_.x;
            int textY = (int)(textCenter_.y - ((textPaint.descent() + textPaint.ascent()) / 2));
            canvas.drawText(resource_.getTag().label, textX, textY, textPaint);
        }
    }

    int getDiceFace() {
        return resource_.getTag().diceFace;
    }

    boolean resourceGenerated() {
        return resourceGenerated_;
    }
}
