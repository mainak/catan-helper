package com.mmandal.catanhelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

class OrderedColor implements Comparable<OrderedColor> {

    int color;

    private int order_;

    public OrderedColor(int color, int order) {
        this.color = color;
        order_ = order;
    }

    int getOrder() {
        return order_;
    }

    @Override
    public int compareTo(OrderedColor another) {
        return Integer.valueOf(order_).compareTo(another.order_);
    }
}

class ColorListAdapter extends BaseAdapter {

    private final Activity activity_;

    private final List<OrderedColor> colors_ = new Vector<OrderedColor>();

    ColorListAdapter(Activity activity, Set<OrderedColor> colors) {
        activity_ = activity;
        colors_.addAll(colors);
        colors_.add(new OrderedColor(R.color.unselected, 0));
    }

    @Override
    public int getCount() {
        return colors_.size();
    }

    @Override
    public Object getItem(int position) {
        return colors_.get(position);
    }

    @Override
    public long getItemId(int position) {
        return colors_.get(position).hashCode();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = activity_.getLayoutInflater();
        View row = inflater.inflate(R.layout.color_picker, parent, false);
        View colorView = row.findViewById(R.id.cp_row_item);
        colorView.setBackgroundColor(Utility.getColor(activity_.getResources(), ((OrderedColor) getItem(position)).color));
        return row;
    }
}

class PlayerOption implements Spinner.OnItemSelectedListener {

    private static final String TAG = PlayerOption.class.getSimpleName();

    private final MainActivity activity_;
    private final int playerPos_;
    private final Spinner spinner_;

    private Set<OrderedColor> colors_ = null;

    private PlayerOption previous_ = null;
    private PlayerOption next_ = null;

    PlayerOption(MainActivity activity, int playerPos, Spinner spinner) {
        activity_ = activity;
        playerPos_ = playerPos + 1;
        spinner_ = spinner;
        spinner_.setOnItemSelectedListener(this);
    }

    void setChain(PlayerOption previous, PlayerOption next) {
        previous_ = previous;
        next_ = next;
    }

    void setColors(final Set<OrderedColor> colors) {
        colors_ = colors;
        Log.d(TAG, "For player " + playerPos_ + ", setting colors " + colors.size());
        spinner_.setAdapter(new ColorListAdapter(activity_, colors_));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        ColorListAdapter adapter = (ColorListAdapter) parent.getAdapter();
        OrderedColor color = (OrderedColor) adapter.getItem(position);
        Log.d(TAG, "Selected color " + color.getOrder() + " for player " + playerPos_);
        //       spinner_.setBackgroundColor(color.color);
        if (view != null) {
            TextView tv = (TextView) view.findViewById(R.id.cp_row_item);
            tv.setText("Player " + playerPos_);
        }
        if (next_ != null) {
            if (color.color == R.color.unselected) {
                next_.setColors(new TreeSet<OrderedColor>());
            } else {
                Set<OrderedColor> remainingColors = new TreeSet<OrderedColor>(colors_);
                remainingColors.remove(color);
                boolean notFirstSetting = null != next_.colors_;
                next_.setColors(remainingColors);
                if (next_.playerPos_ > 4 || notFirstSetting) {
                    next_.spinner_.setSelection(remainingColors.size());
                }
            }
        }

        if (playerPos_ == 2) {
            if (R.color.unselected == color.color) {
                activity_.disableStartGameButton();
            } else {
                activity_.enableStartGameButton();
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    Integer getColor() {
        return ((OrderedColor) spinner_.getAdapter().getItem(spinner_.getSelectedItemPosition())).color;
    }
}

@SuppressLint("ValidFragment")
class ExplainGameDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialoag_explain_title)
                .setMessage(R.string.dialog_explain_steps)
                .setNeutralButton(R.string.dialog_explain_ok, null);
        // Create the AlertDialog object and return it
        return builder.create();
    }
}

public class MainActivity extends Activity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    int numRolls_ = 1;

    private PlayerOption[] players_ = new PlayerOption[6];

    private Button classicGame_;

    private Button citiesAndKnights_;

    void enableStartGameButton() {
        classicGame_.setEnabled(true);
        citiesAndKnights_.setEnabled(true);
    }

    void disableStartGameButton() {
        classicGame_.setEnabled(false);
        citiesAndKnights_.setEnabled(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((SeekBar) findViewById(R.id.cf_num_rolls)).setOnSeekBarChangeListener(this);

        classicGame_ = (Button) findViewById(R.id.cf_classic);
        classicGame_.setOnClickListener(this);
        citiesAndKnights_ = (Button) findViewById(R.id.cf_cities_and_knights);
        citiesAndKnights_.setOnClickListener(this);

        disableStartGameButton();

        // create the players
        players_[0] = new PlayerOption(
                this,
                0,
                (Spinner) findViewById(R.id.cf_spinner_p1));
        players_[1] = new PlayerOption(
                this,
                1,
                (Spinner) findViewById(R.id.cf_spinner_p2));
        players_[2] = new PlayerOption(
                this,
                2,
                (Spinner) findViewById(R.id.cf_spinner_p3));
        players_[3] = new PlayerOption(
                this,
                3,
                (Spinner) findViewById(R.id.cf_spinner_p4));
        players_[4] = new PlayerOption(
                this,
                4,
                (Spinner) findViewById(R.id.cf_spinner_p5));
        players_[5] = new PlayerOption(
                this,
                5,
                (Spinner) findViewById(R.id.cf_spinner_p6));

        // connect the players
        players_[0].setChain(null, players_[1]);
        players_[1].setChain(players_[0], players_[2]);
        players_[2].setChain(players_[1], players_[3]);
        players_[3].setChain(players_[2], players_[4]);
        players_[4].setChain(players_[3], players_[5]);
        players_[5].setChain(players_[4], null);


        players_[0].setColors(new TreeSet<OrderedColor>(Arrays.asList(
                new OrderedColor(R.color.red, 1),
                new OrderedColor(R.color.white, 2),
                new OrderedColor(R.color.orange, 3),
                new OrderedColor(R.color.blue, 4),
                new OrderedColor(R.color.brown, 5),
                new OrderedColor(R.color.green, 6))));

        ExplainGameDialog explainDiag = new ExplainGameDialog();
        explainDiag.show(getFragmentManager(), "explanation");
    }

    @Override
    public void onClick(View v) {
        Intent playIntent = new Intent(this, PlayActivity.class);
        playIntent.putExtra("game_type", v.getId());
        playIntent.putExtra("num_rolls", numRolls_);
        ArrayList<Integer> player_colors = new ArrayList<Integer>();
        for (PlayerOption player : players_) {
            if (player.getColor() == R.color.unselected) {
                break;
            }
            player_colors.add(Utility.getColor(getResources(), player.getColor()));
        }
        playIntent.putIntegerArrayListExtra("player_colors", player_colors);
        startActivity(playIntent);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int numRolls, boolean fromUser) {
        numRolls_ = (int) Math.pow(2, numRolls);
        TextView disp = (TextView) findViewById(R.id.cf_num_rolls_disp);
        disp.setText(String.format("%2d", numRolls_));
        disp.invalidate();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
}
