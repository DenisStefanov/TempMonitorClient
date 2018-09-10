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

import static android.graphics.Color.RED;

public class DrawView extends View {
    private ShapeDrawable mStillBar;
    private ShapeDrawable mTowerBar;
    private ShapeDrawable mStillBarS;
    private ShapeDrawable mTowerBarS;
    private ShapeDrawable mStillPath;
    private ShapeDrawable mTowerPath;
    private ShapeDrawable mStillBarC;
    private ShapeDrawable mTowerBarC;
    private ShapeDrawable mLiqLevel;

    public DrawView(Context context, String stillTempText, String stillTempThresholdText,
                    String towerTempText, String towerTempThresholdText, Boolean liqLevel) {
        super(context);

        float stillTemp = Float.parseFloat(stillTempText);
        float stillTempThreshold = Float.parseFloat(stillTempThresholdText);
        float towerTemp = Float.parseFloat(towerTempText);
        float towerTempThreshold = Float.parseFloat(towerTempThresholdText);

        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        int dispW = metrics.widthPixels;
        int dispH = metrics.heightPixels;

        int ChartBottom = dispH / 10 * 3;
        int ChartLeft = dispW / 10;
        int ChartWidth = dispW / 10 * 2;
        int ChartGap = dispW / 10;
        int biggestTemp = 100;
        int BarHeight = dispH / 10 * 5;
        int DegreePixRatio = BarHeight / biggestTemp;

        //empty bars
        mStillBarC = new ShapeDrawable(new RectShape());
        mStillBarC.getPaint().setColor(Color.LTGRAY);
        mStillBarC.getPaint().setStyle(Paint.Style.FILL);
        mStillBarC.setBounds(ChartLeft, dispH - (ChartBottom + BarHeight), ChartLeft+ChartWidth, dispH - ChartBottom);

        mTowerBarC = new ShapeDrawable(new RectShape());
        mTowerBarC.getPaint().setColor(Color.LTGRAY);
        mTowerBarC.getPaint().setStyle(Paint.Style.FILL);
        mTowerBarC.setBounds(ChartLeft+ChartWidth+ChartGap, dispH - (ChartBottom + BarHeight), ChartLeft+ChartWidth+ChartGap+ChartWidth, dispH - ChartBottom);

        mStillBar = new ShapeDrawable(new RectShape());
        mStillBar.getPaint().setColor(Color.GRAY);
        mStillBar.getPaint().setStyle(Paint.Style.STROKE);
        mStillBar.setBounds(ChartLeft, dispH - (ChartBottom + BarHeight), ChartLeft+ChartWidth, dispH - ChartBottom);

        mTowerBar = new ShapeDrawable(new RectShape());
        mTowerBar.getPaint().setColor(Color.GREEN);
        mTowerBar.getPaint().setStyle(Paint.Style.STROKE);
        mTowerBar.setBounds(ChartLeft+ChartWidth+ChartGap, dispH - (ChartBottom + BarHeight), ChartLeft+ChartWidth+ChartGap+ChartWidth, dispH - ChartBottom);

        //solid bars
        mStillBarS = new ShapeDrawable(new RectShape());
        mStillBarS.getPaint().setColor(Color.GREEN);
        mStillBarS.getPaint().setStyle(Paint.Style.FILL);
        mStillBarS.setBounds(ChartLeft, dispH - (ChartBottom + (int)stillTemp*DegreePixRatio), ChartLeft+ChartWidth, dispH - ChartBottom);

        mTowerBarS = new ShapeDrawable(new RectShape());
        mTowerBarS.getPaint().setColor(Color.GREEN);
        mTowerBarS.getPaint().setStyle(Paint.Style.FILL);
        mTowerBarS.setBounds(ChartLeft+ChartWidth+ChartGap, dispH - (ChartBottom + (int)towerTemp*DegreePixRatio), ChartLeft+ChartWidth+ChartGap+ChartWidth, dispH - ChartBottom);

        //Liquid level circle
        mLiqLevel = new ShapeDrawable(new OvalShape());
        mLiqLevel.getPaint().setColor(liqLevel?Color.RED:Color.GREEN);
        mLiqLevel.getPaint().setStyle(Paint.Style.FILL);
        mLiqLevel.setBounds(ChartLeft+ChartWidth*2+ChartGap*2, dispH - (ChartBottom + BarHeight + ChartGap*3), ChartLeft+ChartWidth*2+ChartGap*4, dispH - (ChartBottom + BarHeight + ChartGap));

        //limit lines
        Path stillPath = new Path();
        stillPath.moveTo(ChartLeft, dispH - (ChartBottom + (int)stillTempThreshold *DegreePixRatio));
        stillPath.lineTo(ChartLeft+ChartWidth, dispH - (ChartBottom + (int)stillTempThreshold *DegreePixRatio));
        mStillPath = new ShapeDrawable(new PathShape(stillPath, dispW, dispH));
        mStillPath.getPaint().setColor(RED);
        mStillPath.getPaint().setStrokeWidth(2);
        mStillPath.getPaint().setStyle(Paint.Style.STROKE);
        mStillPath.getPaint().setPathEffect(new DashPathEffect(new float[] {10,20}, 0));
        mStillPath.setBounds(0, 0, dispW, dispH);

        Path towerPath = new Path();
        towerPath.moveTo(ChartLeft+ChartWidth+ChartGap, dispH - (ChartBottom + (int)towerTempThreshold *DegreePixRatio));
        towerPath.lineTo(ChartLeft+ChartWidth+ChartGap+ChartWidth, dispH - (ChartBottom + (int)towerTempThreshold *DegreePixRatio));
        mTowerPath = new ShapeDrawable(new PathShape(towerPath, dispW, dispH));
        mTowerPath.getPaint().setColor(RED);
        mTowerPath.getPaint().setStrokeWidth(2);
        mTowerPath.getPaint().setStyle(Paint.Style.STROKE);
        mTowerPath.getPaint().setPathEffect(new DashPathEffect(new float[] {10,20}, 0));
        mTowerPath.setBounds(0, 0, dispW, dispH);

    }
    @Override
    public void onDraw(Canvas canvas) {
        mStillBarC.draw(canvas);
        mTowerBarC.draw(canvas);
        mStillBar.draw(canvas);
        mTowerBar.draw(canvas);
        mStillBarS.draw(canvas);
        mTowerBarS.draw(canvas);
        mStillPath.draw(canvas);
        mTowerPath.draw(canvas);
        mLiqLevel.draw(canvas);
    }

}
