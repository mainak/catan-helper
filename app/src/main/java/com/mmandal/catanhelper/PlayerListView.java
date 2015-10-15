package com.mmandal.catanhelper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;

import java.util.List;

/**
 * Created by mmandal on 10/8/15.
 */
class PlayerListView extends View {

    private final List<Integer> playerColors_;

    int nextPlayer_ = -1;

    Rect box_ = null;

    public PlayerListView(Context context, List<Integer> playerColors) {
        super(context);
        playerColors_ = playerColors;
    }

    void forward() {
        ++nextPlayer_;
    }

    void backward() {
        --nextPlayer_;
        if (nextPlayer_ < -1) {
            nextPlayer_ = -1;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        box_ = new Rect(left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int cellWidth = box_.width() / playerColors_.size();
        int current = box_.left;
        int selectedId = -1;
        Rect selected = null;
        for (int id = 0; id < playerColors_.size(); ++id, current += cellWidth) {
            Rect cell = new Rect(current, box_.top, current + cellWidth, box_.bottom);
            if (nextPlayer_ >= 0 && nextPlayer_ % playerColors_.size() == id) {
                selectedId = id;
                selected = cell;
            }
            Paint fillPaint = new Paint();
            fillPaint.setColor(playerColors_.get(id));
            fillPaint.setStyle(Paint.Style.FILL);
            fillPaint.setAlpha(150);
            canvas.drawRect(cell, fillPaint);

            Paint borderPaint = new Paint();
            borderPaint.setColor(id == nextPlayer_ ? Color.WHITE : Color.BLACK);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(5);
            canvas.drawRect(cell, borderPaint);
        }

        if (selected != null) {
            Paint fillPaint = new Paint();
            fillPaint.setColor(playerColors_.get(selectedId));
            fillPaint.setStyle(Paint.Style.FILL);
            canvas.drawRect(selected, fillPaint);

            Paint borderPaint = new Paint();
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setColor(Color.WHITE);
            borderPaint.setStrokeWidth(7);
            canvas.drawRect(selected, borderPaint);
            borderPaint.setColor(Color.BLACK);
            borderPaint.setStrokeWidth(3);
            canvas.drawRect(selected, borderPaint);
        }
    }
}
