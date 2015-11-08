package com.mmandal.catanhelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;

import java.util.List;
import java.util.Vector;

/**
 * Created by mmandal on 11/7/15.
 */
abstract class Dice extends View {

    private static final String TAG = Dice.class.getName();

    protected RectF dice_;

    protected int number_ = 1;

    protected abstract int getColor();

    protected boolean disabled_ = true;

    Dice(Context context) {
        super(context);
    }

    void setNumber(int number) {
        number_ = number;
        disabled_ = false;
    }

    void disable() {
        disabled_ = true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        dice_ = new RectF(left, top, right, bottom);
        Log.d(TAG, dice_.toString());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint fill = new Paint();
        fill.setColor(getColor());
        fill.setStyle(Paint.Style.FILL);
        if (disabled_) {
            fill.setAlpha(80);
        }
        canvas.drawRoundRect(dice_,
                Utility.fromDp(getResources().getDisplayMetrics(), 2),
                Utility.fromDp(getResources().getDisplayMetrics(), 2),
                fill);
    }
}

/**
 * Created by mmandal on 11/7/15.
 */
class NumberDice extends Dice {

    private final List<List<PointF>> diceFaces_ = new Vector<List<PointF>>();

    private final int color_;

    NumberDice(Context context, int color) {
        super(context);
        color_ = Utility.getColor(getResources(), color);
    }

    @Override
    protected int getColor() {
        return color_;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        float width = dice_.width();
        float height = dice_.height();
        float dEdge = Math.min(width, height) / 2;

        diceFaces_.clear();

        Vector<PointF> face = new Vector<PointF>();
        face.add(new PointF(dice_.centerX(), dice_.centerY()));
        diceFaces_.add(face);

        face = new Vector<PointF>();
        face.add(new PointF(dice_.centerX() - dEdge / 2, dice_.centerY() - dEdge / 2));
        face.add(new PointF(dice_.centerX() + dEdge / 2, dice_.centerY() + dEdge / 2));
        diceFaces_.add(face);

        face = new Vector<PointF>();
        face.add(new PointF(dice_.centerX() - dEdge / 2, dice_.centerY() - dEdge / 2));
        face.add(new PointF(dice_.centerX(), dice_.centerY()));
        face.add(new PointF(dice_.centerX() + dEdge / 2, dice_.centerY() + dEdge / 2));
        diceFaces_.add(face);

        face = new Vector<PointF>();
        face.add(new PointF(dice_.centerX() - dEdge / 2, dice_.centerY() - dEdge / 2));
        face.add(new PointF(dice_.centerX() + dEdge / 2, dice_.centerY() + dEdge / 2));
        face.add(new PointF(dice_.centerX() - dEdge / 2, dice_.centerY() + dEdge / 2));
        face.add(new PointF(dice_.centerX() + dEdge / 2, dice_.centerY() - dEdge / 2));
        diceFaces_.add(face);

        face = new Vector<PointF>();
        face.add(new PointF(dice_.centerX() - dEdge / 2, dice_.centerY() - dEdge / 2));
        face.add(new PointF(dice_.centerX() + dEdge / 2, dice_.centerY() + dEdge / 2));
        face.add(new PointF(dice_.centerX(), dice_.centerY()));
        face.add(new PointF(dice_.centerX() - dEdge / 2, dice_.centerY() + dEdge / 2));
        face.add(new PointF(dice_.centerX() + dEdge / 2, dice_.centerY() - dEdge / 2));
        diceFaces_.add(face);

        face = new Vector<PointF>();
        face.add(new PointF(dice_.centerX() - dEdge / 2, dice_.centerY() - dEdge / 2));
        face.add(new PointF(dice_.centerX(), dice_.centerY() - dEdge / 2));
        face.add(new PointF(dice_.centerX() + dEdge / 2, dice_.centerY() - dEdge / 2));
        face.add(new PointF(dice_.centerX() - dEdge / 2, dice_.centerY() + dEdge / 2));
        face.add(new PointF(dice_.centerX(), dice_.centerY() + dEdge / 2));
        face.add(new PointF(dice_.centerX() + dEdge / 2, dice_.centerY() + dEdge / 2));
        diceFaces_.add(face);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // draw the points
        if (disabled_) {
            return;
        }
        Paint pt = new Paint();
        pt.setColor(Color.BLACK);
        pt.setStyle(Paint.Style.FILL);
        pt.setStrokeWidth(Utility.fromDp(getResources().getDisplayMetrics(), 7));
        Paint spt = new Paint();
        spt.setColor(Color.WHITE);
        spt.setStyle(Paint.Style.FILL);
        spt.setStrokeWidth(Utility.fromDp(getResources().getDisplayMetrics(), 4));
        for (PointF p : diceFaces_.get(number_ - 1)) {
            canvas.drawPoint(p.x, p.y, pt);
            canvas.drawPoint(p.x, p.y, spt);
        }
    }
}

/**
 * Created by mmandal on 11/7/15.
 */
class FeatureDice extends Dice {

    enum RollType {
        MARKET,
        TOWNHALL,
        ABBEY,
        BARBARIAN
    };

    private class FeatureResources {
        RollType roll;
        int color;
        Bitmap icon;
    }

    private final List<FeatureResources> diceFaces_ = new Vector<FeatureResources>();

    private RectF iconBounds_;

    @Override
    protected int getColor() {
        return diceFaces_.get(number_ - 1).color;
    }

    FeatureDice(Context context) {
        super(context);
        Bitmap castle = Utility.getIcon(getResources(), R.drawable.castle);
        Bitmap ship = Utility.getIcon(getResources(), R.drawable.ship);

        FeatureResources abbey = new FeatureResources();
        abbey.roll = RollType.ABBEY;
        abbey.color = Utility.getColor(getResources(), R.color.abbey);
        abbey.icon = castle;

        FeatureResources market = new FeatureResources();
        market.roll = RollType.MARKET;
        market.color = Utility.getColor(getResources(), R.color.market);
        market.icon = castle;

        FeatureResources townhall = new FeatureResources();
        townhall.roll = RollType.TOWNHALL;
        townhall.color = Utility.getColor(getResources(), R.color.townhall);
        townhall.icon = castle;

        FeatureResources barbarian = new FeatureResources();
        barbarian.roll = RollType.BARBARIAN;
        barbarian.color = Utility.getColor(getResources(), R.color.barbarian);
        barbarian.icon = ship;

        diceFaces_.add(abbey);
        diceFaces_.add(market);
        diceFaces_.add(townhall);
        diceFaces_.add(barbarian);
        diceFaces_.add(barbarian);
        diceFaces_.add(barbarian);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        iconBounds_ = new RectF(dice_);
        iconBounds_.inset(dice_.width() / 5, dice_.height() / 5);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (disabled_) {
            return;
        }
        FeatureResources feature = diceFaces_.get(number_ - 1);
        canvas.drawBitmap(feature.icon, null, iconBounds_, new Paint());
    }
}
