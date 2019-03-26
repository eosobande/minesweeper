package com.minesweeper.kuro.minesweeper;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class SquareGridView extends GridView {

    public SquareGridView(Context context) {
        super(context);
    }

    public SquareGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        setColumnWidth((widthMeasureSpec - 9) / getNumColumns());
    }

}
