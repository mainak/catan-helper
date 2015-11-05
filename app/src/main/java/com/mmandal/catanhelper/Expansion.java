package com.mmandal.catanhelper;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

/**
 * Created by mmandal on 10/8/15.
 */
abstract class Expansion extends View {
    public Expansion(Context context) {
        super(context);
    }

    abstract void apply(Board.Update update);

    abstract Board.Update genNextUpdate(Board.Update update);

    public abstract List<DialogFragment> getSpecialEvents();
}

class DefaultExpansion extends Expansion {

    public DefaultExpansion(Context context) {
        super(context);
    }

    @Override
    void apply(Board.Update update) {
    }

    @Override
    public Board.Update genNextUpdate(Board.Update update) {
        return update;
    }

    @Override
    public List<DialogFragment> getSpecialEvents() {
        return new LinkedList<DialogFragment>();
    }
}


@SuppressLint("ValidFragment")
class BarbarianAttackDialog extends DialogFragment {
    private static final String TAG = BarbarianAttackDialog.class.getSimpleName();

    private final Context ctxt_;

    private final MediaPlayer barbarianSoundPlayer_;

    BarbarianAttackDialog(Context ctxt, MediaPlayer barbarianSoundPlayer) {
        ctxt_ = ctxt;
        barbarianSoundPlayer_ = barbarianSoundPlayer;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        TextView msg = new TextView(ctxt_);
        msg.setText(R.string.dialog_barbarian);
        msg.setPadding(10, 10, 10, 10);
        msg.setGravity(Gravity.CENTER);
        msg.setTextSize(18);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(msg)
                .setNeutralButton(R.string.dialog_barbarian_ok, null);
        return builder.create();
    }

    @Override
    public void onStart()  {
        barbarianSoundPlayer_.seekTo(0);
        barbarianSoundPlayer_.start();
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            barbarianSoundPlayer_.stop();
            barbarianSoundPlayer_.prepare();
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
    }
}

class CitiesAndKnights extends Expansion {

    private final Random rand = new Random();

    private final Bitmap castle_;

    private final Bitmap fire_;

    private final Bitmap ship_;
    List<List<PointF>> diceFaces_ = new Vector<List<PointF>>();
    Update currState_ = null;
    private RectF dice_;

    private RectF market_;

    private RectF townhall_;

    private RectF abbey_;

    private List<RectF> barbarians_ = new Vector<RectF>();

    private final MediaPlayer barbarianSoundPlayer_;

    CitiesAndKnights(Context context) {
        super(context);
        castle_ = Utility.getIcon(getResources(), R.drawable.castle);
        fire_ = Utility.getIcon(getResources(), R.drawable.fire);
        ship_ = Utility.getIcon(getResources(), R.drawable.ship);
        barbarianSoundPlayer_ = MediaPlayer.create(getContext(), R.raw.barbarians);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        float width = right - left;
        float height = bottom - top;
        float edge = Math.min(height / 2, Utility.fromDp(getResources().getDisplayMetrics(), 30f));
        PointF center = new PointF(left + width / 2, top + height / 2);

        PointF bottomLineCenter = new PointF(center.x, bottom - edge / 2);
        float barbRadius = Math.min(edge / 2, width / 23);
        PointF barbCenter = new PointF(left + (width - 23 * barbRadius) / 2 + barbRadius, bottomLineCenter.y);
        for (int i = 0; i < 8; ++i) {
            barbarians_.add(new RectF(barbCenter.x - barbRadius, barbCenter.y - barbRadius,
                    barbCenter.x + barbRadius, barbCenter.y + barbRadius));
            barbCenter.x += 3 * barbRadius;
        }

        float fRadius = Math.min((height - edge) / 2, width / 14);
        PointF topLineCenter = new PointF(center.x, top + fRadius);
        PointF fCenter = new PointF(left + (width - 14 * fRadius) / 2 + fRadius, topLineCenter.y);
        abbey_ = new RectF(fCenter.x - fRadius, fCenter.y - fRadius,
                fCenter.x + fRadius, fCenter.y + fRadius);
        fCenter.x += 4 * fRadius;
        market_ = new RectF(fCenter.x - fRadius, fCenter.y - fRadius,
                fCenter.x + fRadius, fCenter.y + fRadius);
        fCenter.x += 4 * fRadius;
        townhall_ = new RectF(fCenter.x - fRadius, fCenter.y - fRadius,
                fCenter.x + fRadius, fCenter.y + fRadius);
        fCenter.x += 4 * fRadius;
        dice_ = new RectF(fCenter.x - fRadius, fCenter.y - fRadius,
                fCenter.x + fRadius, fCenter.y + fRadius);

        float dEdge = fRadius;

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

        drawFeature(canvas, abbey_, R.color.abbey, null);
        drawFeature(canvas, market_, R.color.market, null);
        drawFeature(canvas, townhall_, R.color.townhall, null);
        drawBarbarians(canvas);

        if (currState_ == null) {
            return;
        }

        if (currState_.roll != RollType.BARBARIAN) {
            drawDice(canvas, currState_.number, true /* visible */);
        } else {
            drawDice(canvas, currState_.number, false /* visible */);
        }

        switch (currState_.roll) {
            case ABBEY:
                drawFeature(canvas, abbey_, R.color.abbey, castle_);
                break;
            case MARKET:
                drawFeature(canvas, market_, R.color.market, castle_);
                break;
            case TOWNHALL:
                drawFeature(canvas, townhall_, R.color.townhall, castle_);
                break;
            case BARBARIAN:
                break;
        }
    }

    private void drawBarbarians(Canvas canvas) {
        int shipPos = 0;
        if (currState_ != null) {
            shipPos = currState_.shipPos;
        }
        for (int i = 0; i < barbarians_.size(); ++i) {
            if (i < shipPos) {
                drawFeature(canvas, barbarians_.get(i), R.color.barbarian, fire_);
            } else if (i == shipPos) {
                drawFeature(canvas, barbarians_.get(i), R.color.barbarian, ship_);
            } else {
                drawFeature(canvas, barbarians_.get(i), R.color.barbarian, null);
            }
        }
    }

    private void drawDice(Canvas canvas, int number, boolean visible) {
        Paint fill = new Paint();
        fill.setColor(visible
                ? Utility.getColor(getResources(), R.color.dice_red)
                : Color.BLACK);
        fill.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(dice_,
                Utility.fromDp(getResources().getDisplayMetrics(), 2),
                Utility.fromDp(getResources().getDisplayMetrics(), 2),
                fill);

        if (!visible) {
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
        for (PointF p : diceFaces_.get(number)) {
            canvas.drawPoint(p.x, p.y, pt);
            canvas.drawPoint(p.x, p.y, spt);
        }
    }

    private void drawFeature(Canvas canvas, RectF rect, int color, Bitmap icon) {
        Paint fill = new Paint();
        fill.setColor(Utility.getColor(getResources(), color));
        fill.setStyle(Paint.Style.FILL);
        if (icon == null) {
            fill.setAlpha(80);
        }
        canvas.drawOval(rect, fill);

        if (icon != null) {
            Paint border = new Paint();
            border.setColor(Color.WHITE);
            border.setStyle(Paint.Style.STROKE);
            border.setStrokeWidth(Utility.fromDp(getResources().getDisplayMetrics(), 3));
            canvas.drawOval(rect, border);

            RectF smaller = new RectF(rect);
            float edge = rect.height();
            float dx = edge * (float) (Math.pow(2, 0.5) - 1f) / 2;
            smaller.inset(dx, dx);
            canvas.drawBitmap(icon, null, smaller, new Paint());
        }
    }

    @Override
    void apply(Board.Update update) {
        currState_ = (Update) update;
    }

    @Override
    public Board.Update genNextUpdate(Board.Update bUpd) {
        int number = rand.nextInt(6);
        RollType roll = RollType.BARBARIAN;
        if (number == 0) {
            roll = RollType.ABBEY;
        } else if (number == 1) {
            roll = RollType.MARKET;
        } else if (number == 2) {
            roll = RollType.TOWNHALL;
        }

        Update update = new Update();
        update.copy(bUpd);
        update.number = rand.nextInt(6);
        update.roll = roll;
        update.shipPos = 0;
        if (currState_ != null) {
            if (currState_.shipPos < barbarians_.size() - 1) {
                update.shipPos = currState_.shipPos;
            }
        }

        if (RollType.BARBARIAN == roll) {
            update.shipPos += 1;
        }
        return update;
    }

    @Override
    public List<DialogFragment> getSpecialEvents() {
        List<DialogFragment> dialogs = new LinkedList<DialogFragment>();
        if (barbarians_.size() - 1 == currState_.shipPos) {
            dialogs.add(new BarbarianAttackDialog(getContext(),
                    barbarianSoundPlayer_));
        }
        return dialogs;
    }

    private enum RollType {
        MARKET,
        TOWNHALL,
        ABBEY,
        BARBARIAN
    }

    private static class Update extends Board.Update {
        RollType roll;
        int number;
        int shipPos;
    }
}