package com.mmandal.catanhelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.gesture.Gesture;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;

@SuppressLint("ValidFragment")
class BoardLayoutDialog extends DialogFragment {

    private int dialogId_;

    BoardLayoutDialog(int dialogId) {
        dialogId_ = dialogId;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialoag_board_title)
                .setMessage(dialogId_)
                .setNeutralButton(R.string.dialog_board_ok, null);
        // Create the AlertDialog object and return it
        return builder.create();
    }
}

/**
 * Created by mmandal on 10/3/15.
 */
public class PlayActivity extends Activity {

    private Board board_;

    private MediaPlayer rollSoundPlayer_;

    private GestureDetectorCompat detector_;

    private boolean exitConfirmed_ = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final int numRolls = getIntent().getIntExtra("num_rolls", 100);
        final List<Integer> playerColors = getIntent().getIntegerArrayListExtra("player_colors");
        final int gameType = getIntent().getIntExtra("game_type", R.id.cf_classic);
        Expansion expansion =
                gameType == R.id.cf_classic
                ? new DefaultExpansion(this)
                : new CitiesAndKnights(this);

        if (playerColors.size() <= 4) {
            board_ = new FourPlayerBoard(this, playerColors, numRolls, expansion);
        } else {
            board_ = new SixPlayerBoard(this, playerColors, numRolls, expansion);
        }
        board_.initialize();
        setContentView(board_);

        int dialogId = gameType == R.id.cf_classic ?
                R.string.dialog_board_classic : R.string.dialog_board_cities;
        DialogFragment boardDialog = new BoardLayoutDialog(dialogId);
        boardDialog.show(getFragmentManager(), "board_layout");

        rollSoundPlayer_ = MediaPlayer.create(this, R.raw.roll);
        detector_ = new GestureDetectorCompat(this, new PlayGestureListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        detector_.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        if (exitConfirmed_) {
            super.onBackPressed();
            return;
        }

        final PlayActivity thisPlayActivity = this;
        new AlertDialog.Builder(this)
                .setMessage("This will end the game. Are you sure?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        thisPlayActivity.exitConfirmed_ = true;
                        thisPlayActivity.onBackPressed();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    class PlayGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String DEBUG_TAG = "Gestures";

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            rollSoundPlayer_.start();
            board_.roll();
            for (DialogFragment dialog : board_.getSpecialEvents()) {
                dialog.show(getFragmentManager(), "special");
            }
            board_.invalidate();
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float distanceX = e2.getX() - e1.getX();
            if (distanceX < 50) {
                return false;
            }

            if (board_ != null && board_.revert()) {
                board_.invalidate();
            }
            return true;
        }
    }
}
