package com.mmandal.catanhelper;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Vector;


@SuppressLint("ValidFragment")
class RobberActivatedDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_robber)
                .setNeutralButton(R.string.dialog_robber_ok, null);
        return builder.create();
    }
}

/**
 * Created by mmandal on 10/3/15.
 */
abstract class Board extends View {
    private static final String TAG = Board.class.getSimpleName();

    protected final Random rand_ = new Random();
    protected final PlayerListView players_;
    protected final int maxNumRolls_;
    private final Expansion expansion_;
    protected List<Hexagon> cells_ = new Vector<Hexagon>();
    private int numTurns_ = 0;
    private LinkedList<Update> pastUpdates_ = new LinkedList<Update>();

    Board(Context ctxt, List<Integer> playerColors, int maxNumRolls, Expansion expansion) {
        super(ctxt);
        players_ = new PlayerListView(getContext(), playerColors);
        expansion_ = expansion;
        maxNumRolls_ = maxNumRolls;
    }

    public List<DialogFragment> getSpecialEvents() {
        List<DialogFragment> dialogs = expansion_.getSpecialEvents();
        for (Hexagon cell : cells_) {
            if (cell.getDiceFace() == 7 && cell.resourceGenerated()) {
                dialogs.add(new RobberActivatedDialog());
                break;
            }
        }
        return dialogs;
    }

    protected float fromDp(float dpSize) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpSize, getResources().getDisplayMetrics());
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        players_.layout(left + (int) fromDp(10) , top + (int) fromDp(10), right - (int) fromDp(10), top + (int) fromDp(30));
        expansion_.layout(left + (int) fromDp(10), top + (int) fromDp(50), right - (int) fromDp(10), top + (int) fromDp(150));
    }

    void initialize() {
        createCells();
    }

    protected abstract void createCells();

    protected Update genNextUpdate() {
        Update update = new Update();
        for (int outcome = 0; outcome < 13; ++outcome) {
            update.numRolls[outcome] = 0;
        }
        for (int i = 0; i < maxNumRolls_; ++i) {
            int roll = rand_.nextInt(6) + rand_.nextInt(6) + 2;
            ++update.numRolls[roll];
        }
        return expansion_.genNextUpdate(update);
    }

    void roll() {
        applyUpdate(genNextUpdate());
    }

    private void applyUpdate(Update update) {
        ++numTurns_;
        players_.forward();
        for (Hexagon cell : cells_) {
            cell.add(update.numRolls[cell.getDiceFace()]);
        }
        expansion_.apply(update);
        pastUpdates_.push(update);
    }

    private Update revertUpdate() {
        Update update = pastUpdates_.pop();
        expansion_.apply(pastUpdates_.peek());
        for (Hexagon cell : cells_) {
            cell.subtract(update.numRolls[cell.getDiceFace()]);
        }
        players_.backward();
        --numTurns_;
        return update;
    }

    boolean revert() {
        if (pastUpdates_.isEmpty()) {
            return false;
        }

        revert(false /* replay */);
        return true;
    }

    private void revert(boolean replay) {
        if (pastUpdates_.isEmpty()) {
            return;
        }

        Update update = revertUpdate();

        if (replay) {
            applyUpdate(update);
        } else {
            revert(true /* replay */);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        setBackgroundColor(Color.BLACK);
        players_.draw(canvas);
        expansion_.draw(canvas);
        for (Hexagon cell : cells_) {
            if (!cell.resourceGenerated()) {
                cell.draw(canvas);
            }
        }
        for (Hexagon cell : cells_) {
            if (cell.resourceGenerated()) {
                cell.draw(canvas);
            }
        }
    }

    int getColor(int resourceId) {
        return getResources().getColor(resourceId);
    }

    Resource createCatanRes(Bitmap icon, int colorId) {
        return new Resource(getResources().getDisplayMetrics(), icon, getColor(colorId));
    }

    protected static class Update {
        int[] numRolls = new int[13];

        public void copy(Update bUpd) {
            numRolls = bUpd.numRolls;
        }
    }
}

class FourPlayerBoard extends Board {
    private static final String TAG = FourPlayerBoard.class.getSimpleName();

    FourPlayerBoard(Context ctxt, List<Integer> playerColors, int maxNumRolls, Expansion expansion) {
        super(ctxt, playerColors, maxNumRolls, expansion);
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        left += (int) fromDp(10);
        top += (int) fromDp(10);
        right -= (int) fromDp(10);
        bottom -= (int) fromDp(10);
        super.onLayout(changed, left, top, right, bottom);

        // height = 8 * edge
        // perp = edge * cos(pi/6)
        // width = 5 * 2 * cos(pi/6) * edge

        int viewHeight = bottom - top;
        int viewWidth = right - left;

        int perp = viewWidth / 10;
        int edge = (int) (perp / Math.cos(Math.PI / 6));
        int cellWidth = 2 * perp;
        int cellHeight = 2 * edge;

        Log.d(TAG, "viewHeight: " + viewHeight + ", viewWidth: " + viewWidth + ", edgeLength: " + edge + ", perpendicular: " + perp);

        Vector<Point> topLefts = new Vector<Point>(19);
        final Point c = new Point(left + viewWidth / 2, top + viewHeight - 4 * edge);
        final int p0 = 0, p1 = perp, p2 = 2 * perp, p3 = 3 * perp, p4 = 4 * perp, p5 = 5 * perp;
        final int e0_5 = edge / 2, e1_0 = edge, e2_0 = 2 * edge, e2_5 = 5 * edge / 2, e4_0 = 4 * edge;

        topLefts.add(new Point(c.x - p3, c.y - e4_0));
        topLefts.add(new Point(c.x - p4, c.y - e2_5));
        topLefts.add(new Point(c.x - p5, c.y - e1_0));
        topLefts.add(new Point(c.x - p4, c.y + e0_5));
        topLefts.add(new Point(c.x - p3, c.y + e2_0));
        topLefts.add(new Point(c.x - p1, c.y + e2_0));
        topLefts.add(new Point(c.x + p1, c.y + e2_0));
        topLefts.add(new Point(c.x + p2, c.y + e0_5));
        topLefts.add(new Point(c.x + p3, c.y - e1_0));
        topLefts.add(new Point(c.x + p2, c.y - e2_5));
        topLefts.add(new Point(c.x + p1, c.y - e4_0));
        topLefts.add(new Point(c.x - p1, c.y - e4_0));

        topLefts.add(new Point(c.x - p2, c.y - e2_5));
        topLefts.add(new Point(c.x - p3, c.y - e1_0));
        topLefts.add(new Point(c.x - p2, c.y + e0_5));
        topLefts.add(new Point(c.x + p0, c.y + e0_5));
        topLefts.add(new Point(c.x + p1, c.y - e1_0));
        topLefts.add(new Point(c.x + p0, c.y - e2_5));

        topLefts.add(new Point(c.x - p1, c.y - e1_0));

        Iterator<Point> topLeftsIter = topLefts.iterator();
        for (Hexagon cell : cells_) {
            Point topLeft = topLeftsIter.next();
            cell.layout(topLeft.x, topLeft.y, topLeft.x + cellWidth, topLeft.y + cellHeight);
        }
    }

    @Override
    protected void createCells() {
        Vector<Resource> resources = new Vector<Resource>(19);

        DisplayMetrics dm = getResources().getDisplayMetrics();

        Bitmap grain  = Utility.getIcon(getResources(), R.drawable.grain);
        Bitmap lumber  = Utility.getIcon(getResources(), R.drawable.lumber);
        Bitmap wool  = Utility.getIcon(getResources(), R.drawable.wool);
        Bitmap bricks  = Utility.getIcon(getResources(), R.drawable.bricks);
        Bitmap ore  = Utility.getIcon(getResources(), R.drawable.ore);
        Bitmap desert  = Utility.getIcon(getResources(), R.drawable.desert);

        for (int i = 0; i < 4; ++i) {
            resources.add(createCatanRes(grain, R.color.grain));
            resources.add(createCatanRes(lumber, R.color.lumber));
            resources.add(createCatanRes(wool, R.color.wool));
        }
        for (int i = 0; i < 3; ++i) {
            resources.add(createCatanRes(bricks, R.color.bricks));
            resources.add(createCatanRes(ore, R.color.ore));
        }
        resources.add(createCatanRes(desert, R.color.desert));
        Collections.shuffle(resources, rand_);

        Vector<Tag> diceFaces = new Vector<Tag>(Arrays.asList(
                new Tag(5, "A"),
                new Tag(2, "B"),
                new Tag(6, "C"),
                new Tag(3, "D"),
                new Tag(8, "E"),
                new Tag(10, "F"),
                new Tag(9, "G"),
                new Tag(12, "H"),
                new Tag(11, "I"),
                new Tag(4, "J"),
                new Tag(8, "K"),
                new Tag(10, "L"),
                new Tag(9, "M"),
                new Tag(4, "N"),
                new Tag(5, "O"),
                new Tag(6, "P"),
                new Tag(3, "Q"),
                new Tag(11, "R")));
        Iterator<Tag> diceFacesIter = diceFaces.iterator();

        for (Resource resource : resources) {
            Tag tag = new Tag(7, "");
            if (!(resource.getColor() == getColor(R.color.desert))) {
                tag = diceFacesIter.next();
            }
            resource.setTag(tag);
            cells_.add(new Hexagon(getContext(), maxNumRolls_, resource));
        }
    }
}

class SixPlayerBoard extends Board {
    private static final String TAG = SixPlayerBoard.class.getSimpleName();

    SixPlayerBoard(Context ctxt, List<Integer> playerColors, int maxNumRolls, Expansion  expansion) {
        super(ctxt, playerColors, maxNumRolls, expansion);
    }


    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        left += (int) fromDp(10);
        top += (int) fromDp(10);
        right -= (int) fromDp(10);
        bottom -= (int) fromDp(10);
        super.onLayout(changed, left, top, right, bottom);
        // perp = edge * cos(pi/6)
        // height = 12 * perp
        // width = 11 * edge

        int viewHeight = bottom - top;
        int viewWidth = right - left;

        int perp = viewWidth / 12;
        int edge = (int) (perp / Math.cos(Math.PI / 6));
        int cellWidth = 2 * perp;
        int cellHeight = 2 * edge;

        Log.d(TAG, "viewHeight: " + viewHeight + ", viewWidth: " + viewWidth + ", edgeLength: " + edge + ", perpendicular: " + perp);

        int p0 = 0, p1 = perp, p2 = 2 * perp, p3 = 3 * perp, p4 = 4 * perp, p5 = 5 * perp, p6 = 6 * perp;
        int e0_5 = edge / 2, e1_0 = edge, e2_5 = 5 * edge / 2, e2_0 = 2 * edge, e4_0 = 4 * edge, e3_5 = 7 * edge / 2, e5_5 = 11 * edge / 2;

        Vector<Point> topLefts = new Vector<Point>(30);
        Point c = new Point(viewWidth / 2, top + viewHeight - (11 * edge / 2));

        // outer ring
        topLefts.add(new Point(c.x - p3, c.y - e5_5));
        topLefts.add(new Point(c.x - p1, c.y - e5_5));
        topLefts.add(new Point(c.x + p1, c.y - e5_5));
        topLefts.add(new Point(c.x + p2, c.y - e4_0));
        topLefts.add(new Point(c.x + p3, c.y - e2_5));
        topLefts.add(new Point(c.x + p4, c.y - e1_0));
        topLefts.add(new Point(c.x + p3, c.y + e0_5));
        topLefts.add(new Point(c.x + p2, c.y + e2_0));
        topLefts.add(new Point(c.x + p1, c.y + e3_5));
        topLefts.add(new Point(c.x - p1, c.y + e3_5));
        topLefts.add(new Point(c.x - p3, c.y + e3_5));
        topLefts.add(new Point(c.x - p4, c.y + e2_0));
        topLefts.add(new Point(c.x - p5, c.y + e0_5));
        topLefts.add(new Point(c.x - p6, c.y - e1_0));
        topLefts.add(new Point(c.x - p5, c.y - e2_5));
        topLefts.add(new Point(c.x - p4, c.y - e4_0));

        // next ring
        topLefts.add(new Point(c.x - p2, c.y - e4_0));
        topLefts.add(new Point(c.x + p0, c.y - e4_0));
        topLefts.add(new Point(c.x + p1, c.y - e2_5));
        topLefts.add(new Point(c.x + p2, c.y - e1_0));
        topLefts.add(new Point(c.x + p1, c.y + e0_5));
        topLefts.add(new Point(c.x + p0, c.y + e2_0));
        topLefts.add(new Point(c.x - p2, c.y + e2_0));
        topLefts.add(new Point(c.x - p3, c.y + e0_5));
        topLefts.add(new Point(c.x - p4, c.y - e1_0));
        topLefts.add(new Point(c.x - p3, c.y - e2_5));

        //inner most ring
        topLefts.add(new Point(c.x - p1, c.y - e2_5));
        topLefts.add(new Point(c.x + p0, c.y - e1_0));
        topLefts.add(new Point(c.x - p1, c.y + e0_5));
        topLefts.add(new Point(c.x - p2, c.y - e1_0));

        Iterator<Point> topLeftsIter = topLefts.iterator();
        for (Hexagon cell : cells_) {
            Point topLeft = topLeftsIter.next();
            cell.layout(topLeft.x, topLeft.y, topLeft.x + cellWidth, topLeft.y + cellHeight);
        }
    }

    @Override
    protected void createCells() {
        Vector<Resource> resources = new Vector<Resource>(19);

        DisplayMetrics dm = getResources().getDisplayMetrics();

        Bitmap grain  = Utility.getIcon(getResources(), R.drawable.grain);
        Bitmap lumber  = Utility.getIcon(getResources(), R.drawable.lumber);
        Bitmap wool  = Utility.getIcon(getResources(), R.drawable.wool);
        Bitmap bricks  = Utility.getIcon(getResources(), R.drawable.bricks);
        Bitmap ore  = Utility.getIcon(getResources(), R.drawable.ore);
        Bitmap desert  = Utility.getIcon(getResources(), R.drawable.desert);

        for (int i = 0; i < 6; ++i) {
            resources.add(createCatanRes(grain, R.color.grain));
            resources.add(createCatanRes(lumber, R.color.lumber));
            resources.add(createCatanRes(wool, R.color.wool));
        }
        for (int i = 0; i < 5; ++i) {
            resources.add(createCatanRes(bricks, R.color.bricks));
            resources.add(createCatanRes(ore, R.color.ore));
        }
        for (int i = 0; i < 2; ++i) {
            resources.add(createCatanRes(desert, R.color.desert));
        }
        Collections.shuffle(resources, rand_);

        Vector<Tag> diceFaces = new Vector<Tag>(Arrays.asList(
                new Tag(2, "A"),
                new Tag(5, "B"),
                new Tag(4, "C"),
                new Tag(6, "D"),
                new Tag(3, "E"),
                new Tag(9, "F"),
                new Tag(8, "G"),
                new Tag(11, "H"),
                new Tag(11, "I"),
                new Tag(10, "J"),
                new Tag(6, "K"),
                new Tag(3, "L"),
                new Tag(8, "M"),
                new Tag(4, "N"),
                new Tag(8, "O"),
                new Tag(10, "P"),
                new Tag(11, "Q"),
                new Tag(12, "R"),
                new Tag(10, "S"),
                new Tag(5, "T"),
                new Tag(4, "U"),
                new Tag(9, "V"),
                new Tag(5, "W"),
                new Tag(9, "X"),
                new Tag(12, "Y"),
                new Tag(3, "Za"),
                new Tag(2, "Zb"),
                new Tag(6, "Zc")));
        Iterator<Tag> diceFacesIter = diceFaces.iterator();

        for (Resource resource : resources) {
            Tag tag = new Tag(7, "");
            if (!(resource.getColor() == getColor(R.color.desert))) {
                tag = diceFacesIter.next();
            }
            resource.setTag(tag);
            cells_.add(new Hexagon(getContext(), maxNumRolls_, resource));
        }
    }
}
