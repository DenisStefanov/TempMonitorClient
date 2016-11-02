package org.duckdns.denis_st.tempmonitorclient;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
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
    private ShapeDrawable mTowerPath;

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
        stillPath.moveTo(ChartLeft, dispH - (ChartBottom + (int)stillTempThreshold *DegreePixRatio));
        stillPath.lineTo(ChartLeft+ChartWidth, dispH - (ChartBottom + (int)stillTempThreshold *DegreePixRatio));
        mStillPath = new ShapeDrawable(new PathShape(stillPath, dispW, dispH));
        mStillPath.getPaint().setColor(Color.RED);
        mStillPath.getPaint().setStrokeWidth(2);
        mStillPath.getPaint().setStyle(Paint.Style.STROKE);
        mStillPath.getPaint().setPathEffect(new DashPathEffect(new float[] {10,20}, 0));
        mStillPath.setBounds(0, 0, dispW, dispH);

        Path towerPath = new Path();
        towerPath.moveTo(ChartLeft+ChartWidth+ChartGap, dispH - (ChartBottom + (int)towerTempThreshold *DegreePixRatio));
        towerPath.lineTo(ChartLeft+ChartWidth+ChartGap+ChartWidth, dispH - (ChartBottom + (int)towerTempThreshold *DegreePixRatio));
        mTowerPath = new ShapeDrawable(new PathShape(towerPath, dispW, dispH));
        mTowerPath.getPaint().setColor(Color.RED);
        mTowerPath.getPaint().setStrokeWidth(2);
        mTowerPath.getPaint().setStyle(Paint.Style.STROKE);
        mTowerPath.getPaint().setPathEffect(new DashPathEffect(new float[] {10,20}, 0));
        mTowerPath.setBounds(0, 0, dispW, dispH);

    }
    @Override
    public void onDraw(Canvas canvas) {
        mStillBarS.draw(canvas);
        mTowerBarS.draw(canvas);
        mStillBar.draw(canvas);
        mTowerBar.draw(canvas);
        mStillPath.draw(canvas);
        mTowerPath.draw(canvas);
    }

}
