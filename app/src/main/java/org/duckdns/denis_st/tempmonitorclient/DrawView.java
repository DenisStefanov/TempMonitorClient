package org.duckdns.denis_st.tempmonitorclient;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.View;

public class DrawView extends View {
    private ShapeDrawable mStillBar;
    private ShapeDrawable mTowerBar;

    public DrawView(Context context, ShapeDrawable stillBar, ShapeDrawable towerBar) {
        super(context);
        mStillBar = stillBar;
        mTowerBar = towerBar;

    }

    @Override
    public void onDraw(Canvas canvas) {
        mStillBar.draw(canvas);
        mTowerBar.draw(canvas);
    }

}
