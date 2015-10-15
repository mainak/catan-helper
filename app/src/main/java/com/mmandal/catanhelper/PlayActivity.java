package com.mmandal.catanhelper;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import java.util.List;

/**
 * Created by mmandal on 10/3/15.
 */
public class PlayActivity extends Activity implements View.OnClickListener {

    Board board_;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int numRolls = getIntent().getIntExtra("num_rolls", 100);
        List<Integer> playerColors = getIntent().getIntegerArrayListExtra("player_colors");
        Expansion expansion =
                getIntent().getIntExtra("game_type", R.id.cf_classic) == R.id.cf_classic
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
        board_.invalidate();
    }
}
