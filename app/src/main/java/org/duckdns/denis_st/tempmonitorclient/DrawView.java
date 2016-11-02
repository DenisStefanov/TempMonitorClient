package org.duckdns.denis_st.tempmonitorclient;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.PathShape;
import android.graphics.drawable.shapes.RectShape;
import android.util.DisplayMetrics;
import android.view.View;

public class DrawView extends View {
    private ShapeDrawable mStillBar;
    private ShapeDrawable mTowerBar;
    private ShapeDrawable mStillBarS;
    private ShapeDrawable mTowerBarS;
    private ShapeDrawable mStillPath;
    private ShapeDrawable shape;

    public DrawView(Context context, String stillTempText, String stillTempThresholdText,
                    String towerTempText, String towerTempThresholdText) {
        super(context);

        float stillTemp = Float.parseFloat(stillTempText);
        float stillTempThreshold = Float.parseFloat(stillTempThresholdText);
        float towerTemp = Float.parseFloat(towerTempText);
        float towerTempThreshold = Float.parseFloat(towerTempThresholdText);

        int ChartBottom = 500;
        int ChartLeft = 100;
        int ChartWidth = 200;
        int ChartGap = 100;

        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        int dispW = metrics.widthPixels;
        int dispH = metrics.heightPixels;

        int DegreePixRatio = 10;
        int stillHeight = 100*DegreePixRatio;
        int towerHeight = 100*DegreePixRatio;

        //empty bars
        mStillBar = new ShapeDrawable(new RectShape());
        mStillBar.getPaint().setColor(0xff74AC23);
        mStillBar.getPaint().setStyle(Paint.Style.STROKE);
        mStillBar.setBounds(ChartLeft, dispH - (ChartBottom + stillHeight), ChartLeft+ChartWidth, dispH - ChartBottom);

        mTowerBar = new ShapeDrawable(new RectShape());
        mTowerBar.getPaint().setColor(0xff74AC23);
        mTowerBar.getPaint().setStyle(Paint.Style.STROKE);
        mTowerBar.setBounds(ChartLeft+ChartWidth+ChartGap, dispH - (ChartBottom + towerHeight), ChartLeft+ChartWidth+ChartGap+ChartWidth, dispH - ChartBottom);

        //solid bars
        mStillBarS = new ShapeDrawable(new RectShape());
        mStillBarS.getPaint().setColor(Color.GREEN);
        mStillBarS.getPaint().setStyle(Paint.Style.FILL);
        mStillBarS.setBounds(ChartLeft, dispH - (ChartBottom + (int)stillTemp*DegreePixRatio), ChartLeft+ChartWidth, dispH - ChartBottom);

        mTowerBarS = new ShapeDrawable(new RectShape());
        mTowerBarS.getPaint().setColor(Color.GREEN);
        mTowerBarS.getPaint().setStyle(Paint.Style.FILL);
        mTowerBarS.setBounds(ChartLeft+ChartWidth+ChartGap, dispH - (ChartBottom + (int)towerTemp*DegreePixRatio), ChartLeft+ChartWidth+ChartGap+ChartWidth, dispH - ChartBottom);

        //limit lines
        Path stillPath = new Path();
//            stillPath.moveTo(ChartLeft, dispH - (ChartBottom + (int)towerTempThreshold *DegreePixRatio));
//            stillPath.lineTo(ChartLeft+ChartWidth, dispH - (ChartBottom + (int)towerTempThreshold *DegreePixRatio));
        stillPath.moveTo(0, 0);
        stillPath.lineTo(500, 500);
        mStillPath = new ShapeDrawable(new PathShape(stillPath, 1, 1));
        mStillPath.setIntrinsicHeight(600);
        mStillPath.setIntrinsicWidth(600);
        mStillPath.getPaint().setColor(Color.BLACK);
        mStillPath.getPaint().setStyle(Paint.Style.STROKE);


        Path p = new Path();
        p.moveTo(50, 0);
        p.lineTo(25, 100);
        p.lineTo(100, 50);
        p.lineTo(0, 50);
        p.lineTo(75, 100);
        p.lineTo(50, 0);

        shape = new ShapeDrawable(new PathShape(p, this.getWidth(), this.getHeight()));
        shape.setIntrinsicHeight(100);
        shape.setIntrinsicWidth(100);
        shape.getPaint().setColor(Color.RED);
        shape.getPaint().setStyle(Paint.Style.STROKE);
        shape.setBounds(200,200, this.getWidth(), this.getHeight());

    }
    @Override
    public void onDraw(Canvas canvas) {
//        mStillBarS.draw(canvas);
//        mTowerBarS.draw(canvas);
        mStillBar.draw(canvas);
//        mTowerBar.draw(canvas);
        mStillPath.draw(canvas);
        shape.draw(canvas);
    }

}
