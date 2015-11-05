package com.mmandal.catanhelper;

import java.util.Random;

/**
 * Created by mmandal on 11/4/15.
 */
class DiceRoller {

    final private Random random_ = new Random();

    final private int numRolls_;

    final private int[] buckets_ = new int[13];

    DiceRoller(int numRolls) {
        numRolls_ = numRolls;
        for (int face = 2; face <= 12; ++face) {
            buckets_[face] = 0;
        }
    }

    private int rollOnce() {
        return random_.nextInt(6) + random_.nextInt(6) + 2;
    }

    int getNextRoll() {
        while (true) {
            int face = rollOnce();
            buckets_[face] += 1;
            if (buckets_[face] == numRolls_) {
                buckets_[face] = 0;
                return face;
            }
        }
    }
}