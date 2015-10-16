package com.mmandal.catanhelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
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
public class PlayActivity extends Activity implements View.OnClickListener {

    Board board_;

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
        board_.setOnClickListener(this);
        setContentView(board_);

        int dialogId = gameType == R.id.cf_classic ?
                R.string.dialog_board_classic : R.string.dialog_board_cities;
        DialogFragment boardDialog = new BoardLayoutDialog(dialogId);
        boardDialog.show(getFragmentManager(), "board_layout");
    }


    @Override
    public void onBackPressed() {
        if (board_ != null) {
            if (board_.revert()) {
                board_.invalidate();
                return;
            }
        }
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        board_.roll();
        for (DialogFragment dialog : board_.getSpecialEvents()) {
            dialog.show(getFragmentManager(), "special");
        }
        board_.invalidate();
    }
}
