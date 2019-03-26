package com.minesweeper.kuro.minesweeper;

import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

public class MineField implements AdapterView.OnItemClickListener {

    private static final String MINE = "M";
    int MARKED_MINES = 0;
    final int NO_OF_MINES = 20;
    private final int NO_OF_CELLS = 100;
    private int[] surroundingIndex = {1,9,10,11};
    private int red, blue, green, yellow, black, darker_gray;
    boolean OVER = false, isUncover = true;

    private MainActivity mMainActivity;
    private ArrayList<Integer> mMines = new ArrayList<>(),
            mUncovered = new ArrayList<>(),
            mMarked = new ArrayList<>(),
            mSurround = new ArrayList<>();

    String[] mSurroundings = new String[NO_OF_CELLS];

    MineField(MainActivity main) {
        mMainActivity = main;
        resolveAllColors();
        setMines();
        getAllSurroundingMinesCounts();
    }

    private void resolveAllColors() {
        black = getResolvedColor(android.R.color.black);
        darker_gray = getResolvedColor(android.R.color.darker_gray);
        red = getResolvedColor(android.R.color.holo_red_light);
        blue = getResolvedColor(android.R.color.holo_blue_light);
        green = getResolvedColor(android.R.color.holo_green_light);
        yellow = getResolvedColor(R.color.yellow);
    }

    private boolean checkRange(int cell, int position, int surroundingIndex) {
        for (int i = 0; i < NO_OF_CELLS; i += 10) {
            if (position >= i && position <= i + 9) {
                if (surroundingIndex == 1) {
                    return cell >= i && cell <= i + 9;
                } else if (surroundingIndex == 9) {
                    return !(cell >= i && cell <= i + 9);
                } else if (surroundingIndex == 11) {
                    return !(cell < position ? cell < (i - 10) : cell > (i + 19));
                }
                break;
            }
        }
        return true;
    }

    void reset() {
        // uncovered
        for (int cell : mUncovered) {
            cover(mMainActivity.mField.getChildAt(cell));
        }
        mUncovered.clear();
        // marked
        View v;
        for (int cell : mMarked) {
            v = mMainActivity.mField.getChildAt(cell);
            v.setBackgroundColor(black);
            ((TextView) v.findViewById(R.id.cell)).setTextColor(black);
        }
        mMarked.clear();
        // mines
        for (int cell : mMines) {
            mMainActivity.mField.getChildAt(cell).setBackgroundColor(black);
        }
        mMines.clear();
        mSurround.clear();
        setMines();
        getAllSurroundingMinesCounts();
        isUncover = true;
        OVER = false;
    }

    private void setMines() {

        Random rand = new Random();
        int mine;

        for (int i = 0; i < NO_OF_MINES; i++) {
            mine = rand.nextInt(NO_OF_CELLS);
            while(mMines.contains(mine)) {
                mine = rand.nextInt(NO_OF_CELLS);
            }
            mMines.add(mine);
        }

    }

    private void getAllSurroundingMinesCounts() {
        int count;
        for (int i = 0; i < NO_OF_CELLS; i++) {
            if (mMines.contains(i)) {
                mSurroundings[i] = MineField.MINE;
            } else {
                count = getSurroundingMinesCount(i);
                mSurroundings[i] = count > 0 ? String.valueOf(count) : "";
            }
        }
    }

    private int getSurroundingMinesCount(int position) {

        int surroundingMines = 0, cell;

        for (int index : surroundingIndex) {
            cell = position - index;
            if (cell >= 0 && checkRange(cell, position, index) && mMines.contains(cell)) {
                surroundingMines++;
            }
            cell = position + index;
            if (cell < 100 && checkRange(cell, position, index) && mMines.contains(cell)) {
                surroundingMines++;
            }
        }

        return surroundingMines;

    }

    private void uncoverSurroundingCells(int position) {

        int cell;
        for (int index : surroundingIndex) {
            cell = position - index;
            shouldUncoverSurrounding(position, cell, index, cell >= 0);
            cell = position + index;
            shouldUncoverSurrounding(position, cell, index, cell < 100);
        }

    }

    private void shouldUncoverSurrounding(int position, int cell, int index, boolean uncover) {
        if (uncover && !mSurround.contains(cell) && !mMarked.contains(cell) && !mUncovered.contains(cell)
                && !mMines.contains(cell) && checkRange(cell, position, index)) {
            mSurround.add(cell);
            uncover(mMainActivity.mField.getChildAt(cell), cell);
        }
    }

    private void explode(View view) {
        view.setBackgroundColor(red);
        ((TextView) view.findViewById(R.id.cell)).setTextColor(black);
    }

    private void uncover(View view, int position) {
        mUncovered.add(position);
        view.setBackgroundColor(darker_gray);

        TextView cell = view.findViewById(R.id.cell);
        if (!mSurroundings[position].isEmpty()) {
            cell.setTextColor(getColor(Integer.valueOf(mSurroundings[position])));
            cell.setText(mSurroundings[position]);
        } else {
            uncoverSurroundingCells(position);
        }

        if (mUncovered.size() == NO_OF_CELLS - NO_OF_MINES) {
            gameOver(green, R.string.you_win);
        }

    }

    private void gameOver(int color, int text) {
        OVER = true;
        mMainActivity.endText.setText(text);
        mMainActivity.endText.setTextColor(color);
    }

    private void cover(View view) {
        view.setBackgroundColor(black);
        TextView cell = view.findViewById(R.id.cell);
        cell.setTextColor(black);
        cell.setText("");
    }

    private void mark(View view, int cell) {
        if (mMarked.contains(cell)) {
            mMarked.remove(mMarked.indexOf(cell));
            view.setBackgroundColor(black);
            ((TextView) view.findViewById(R.id.cell)).setTextColor(black);
        } else {
            mMarked.add(cell);
            view.setBackgroundColor(yellow);
            ((TextView) view.findViewById(R.id.cell)).setTextColor(yellow);
        }
        mMainActivity.marked.setText(mMainActivity.getString(R.string.total_marked, mMarked.size()));
    }

    private int getResolvedColor(@ColorRes int color) {
        return ContextCompat.getColor(mMainActivity, color);
    }

    private int getColor(int count) {
        switch (count) {
            case 1: return blue;
            case 2: return green;
            case 3: return yellow;
            default: return red;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        if (!OVER) {
            if (isUncover) {
                if (!mMarked.contains(i)) {
                    if (mMines.contains(i)) {
                        for (int mine : mMines) {
                            explode(mMainActivity.mField.getChildAt(mine));
                        }
                        gameOver(red, R.string.game_over);
                    } else if (!mUncovered.contains(i)) {
                        uncover(view, i);
                    }
                }
            } else if (!mUncovered.contains(i)) {
                mark(view, i);
            }
        }

    }

}
