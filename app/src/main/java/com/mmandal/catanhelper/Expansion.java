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

    abstract Dice getExtraDice();
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

    @Override
    public Dice getExtraDice() { return null; }
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

    Update currState_ = null;

    private List<RectF> barbarians_ = new Vector<RectF>();

    private final MediaPlayer barbarianSoundPlayer_;

    private final Bitmap ship_;

    private final Bitmap fire_;

    private final Dice featureDice = new FeatureDice(getContext());

    CitiesAndKnights(Context context) {
        super(context);
        barbarianSoundPlayer_ = MediaPlayer.create(getContext(), R.raw.barbarians);
        ship_ = Utility.getIcon(getResources(), R.drawable.ship);
        fire_ = Utility.getIcon(getResources(), R.drawable.fire);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        float width = right - left;
        float height = bottom - top;
        float edge = Math.min(height, Utility.fromDp(getResources().getDisplayMetrics(), 30f));
        float barbRadius = Math.min(edge / 2, width / 23);
        PointF barbCenter = new PointF(left + (width - 23 * barbRadius) / 2 + barbRadius, top + height /2);
        for (int i = 0; i < 8; ++i) {
            barbarians_.add(new RectF(barbCenter.x - barbRadius, barbCenter.y - barbRadius,
                    barbCenter.x + barbRadius, barbCenter.y + barbRadius));
            barbCenter.x += 3 * barbRadius;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBarbarians(canvas);

        if (currState_ == null) {
            return;
        }
    }
    
    private void drawBarbarianPos(Canvas canvas, RectF rect, Bitmap icon) {
        Paint fill = new Paint();
        fill.setColor(Utility.getColor(getResources(), R.color.barbarian));
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
    
    private void drawBarbarians(Canvas canvas) {
        int shipPos = 0;
        if (currState_ != null) {
            shipPos = currState_.shipPos;
        }
        
        for (int i = 0; i < barbarians_.size(); ++i) {
            if (i < shipPos) {
                drawBarbarianPos(canvas, barbarians_.get(i), fire_);
            } else if (i == shipPos) {
                drawBarbarianPos(canvas, barbarians_.get(i), ship_);
            } else {
                drawBarbarianPos(canvas, barbarians_.get(i), null);
            }
        }
    }

    @Override
    void apply(Board.Update update) {
        currState_ = (Update) update;
        if (currState_ != null) {
            featureDice.setNumber(currState_.featureDiceFace);
        } else {
            featureDice.disable();
        }
    }

    @Override
    public Board.Update genNextUpdate(Board.Update bUpd) {
        Update update = new Update();
        update.copy(bUpd);
        update.featureDiceFace = rand.nextInt(6) + 1;
        update.shipPos = 0;
        if (currState_ != null) {
            if (currState_.shipPos < barbarians_.size() - 1) {
                update.shipPos = currState_.shipPos;
            }
        }

        if (4 <= update.featureDiceFace) {
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

    @Override
    public Dice getExtraDice() { return featureDice; }

    private static class Update extends Board.Update {
        int featureDiceFace;
        int shipPos;
    }
}