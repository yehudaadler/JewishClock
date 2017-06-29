package com.yehudaadler.jewishclock;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Yehuda on 19-Jun-15.
 */
public class ClockView extends View {
    
    private static final float BASE_RADIUS = 250;

    private Canvas mCanvas;
    private Paint mPaint;
    private float mRadius;
    private float mCenterX;
    private float mCenterY;
    private float mScale;
    private boolean mIsHideSeconds;

    private AnglesComputer.Angles mAngles;

    public ClockView(Context context) {
        super(context);
        init();
    }

    public ClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ClockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void hideSeconds() {
        mIsHideSeconds = true;
    }

    private void init() {
        mAngles = AnglesComputer.getInitAngles();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mCenterX = w/2f;
        mCenterY = h/2f;
        mRadius = Math.min(w,h)/2f;
        mScale = mRadius / BASE_RADIUS;
    }

    public void setAngles(AnglesComputer.Angles angles) {
        mAngles = angles;
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mCanvas = canvas;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        mPaint.setTextSize(36*mScale);

        drawClockBody();

        drawHoursDial(mAngles.getHoursDialAngle());
        drawMinutesDial(mAngles.getMinutesDialAngle());

        if (!mIsHideSeconds) {
            drawSecondsDial(mAngles.getSecondsDialAngle());
        }

        drawSmallCircle();
    }

    private void drawHoursDial(float degrees) {
        mPaint.setColor(getResources().getColor(R.color.clock_black));

        float dialWidthBottom = 6*mScale;
        float dialWidthTop = 24*mScale;
        float dialHeight = 200*mScale;
        float dialCenterProportion = 0.18f;

        drawDial(degrees, dialWidthBottom, dialWidthTop, dialHeight, dialCenterProportion);

    }

    private void drawMinutesDial(float degrees) {
        mPaint.setColor(getResources().getColor(R.color.clock_black));

        float dialWidthBottom = 6*mScale;
        float dialWidthTop = 24*mScale;
        float dialHeight = 250*mScale;
        float dialCenterProportion = 0.2f;

        drawDial(degrees, dialWidthBottom, dialWidthTop, dialHeight, dialCenterProportion);

    }

    private void drawSecondsDial(float degrees) {
        mPaint.setColor(getResources().getColor(R.color.clock_red));

        float dialWidthBottom = 2*mScale;
        float dialWidthTop = 8*mScale;
        float dialHeight = 260*mScale;
        float dialCenterProportion = 0.22f;

        drawDial(degrees, dialWidthBottom, dialWidthTop, dialHeight, dialCenterProportion);

    }
    private void drawDial(float degrees, float dialWidthBottom, float dialWidthTop,
                          float dialHeight, float dialCenterProportion
                          ) {

        mCanvas.rotate(degrees, mCenterX, mCenterY);

        Path dial = new Path();
        dial.moveTo(mCenterX - dialWidthTop/2, mCenterY + dialHeight*dialCenterProportion);
        dial.lineTo(mCenterX + dialWidthTop/2, mCenterY + dialHeight*dialCenterProportion);
        dial.lineTo(mCenterX + dialWidthBottom/2, mCenterY - dialHeight*(1-dialCenterProportion));
        dial.lineTo(mCenterX - dialWidthBottom/2, mCenterY - dialHeight*(1-dialCenterProportion));
        dial.lineTo(mCenterX - dialWidthTop/2, mCenterY + dialHeight*dialCenterProportion);

        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeWidth(0);
        mCanvas.drawPath(dial, mPaint);

        mCanvas.rotate(-degrees, mCenterX, mCenterY);
    }

    private void drawTick(float degrees) {
        mPaint.setStyle(Paint.Style.FILL);
        mCanvas.rotate(degrees, mCenterX, mCenterY);

        float width = 6*mScale;
        float height = 20*mScale;
        float space = 16*mScale;

        mCanvas.drawRect(mCenterX - width/2, mCenterY - mRadius + space, mCenterX + width/2, mCenterY - mRadius + space + height, mPaint);

        mCanvas.rotate(-degrees, mCenterX, mCenterY);
    }

    private RectF getOval(float radius) {
        return new RectF(mCenterX-radius, mCenterY-radius, mCenterX+radius, mCenterY+radius);
    }

    private void drawClockBody() {
        drawDayAndNight(mRadius);

        // draw black ring
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(12*mScale);
        mCanvas.drawOval(getOval(mRadius - 6*mScale), mPaint);

        float nightStart = -90;
        float dayStart = nightStart + mAngles.getNightAngle();

        //night ticks and numbers
        mPaint.setColor(getResources().getColor(R.color.clock_12));

        for (int i = 0; i < 12; i++) {
            float angle = nightStart + i*(mAngles.getNightAngle() / 12f);
            drawTick(angle - nightStart);
            drawNumberAtAngle(i > 0 ? i : 12, angle);
            mPaint.setColor(getResources().getColor(R.color.clock_white)); //all but 12
        }

        //day ticks and numbers
        mPaint.setColor(getResources().getColor(R.color.clock_12)); //orange

        for (int i = 0; i < 12; i++) {
            float angle = dayStart + i*(mAngles.getDayAngle() / 12f);
            drawTick(angle - nightStart);
            drawNumberAtAngle(i > 0 ? i : 12, angle);
            mPaint.setColor(getResources().getColor(R.color.clock_black)); //all but 12
        }
    }

    // position clock numbers. Nice to have math at our disposal, sin() and cos() do the trick
    private void drawNumberAtAngle(int number, float degrees) {
        double radians = degrees * (Math.PI/180);
        double x = -(number < 10 ? 10 : 20)*mScale + mCenterX + (mRadius - mScale*60)*Math.cos(radians);
        double y = 10*mScale + mCenterY + (mRadius - mScale*60)*Math.sin(radians);
        mCanvas.drawText("" + number, (float)x, (float)y, mPaint);
    }

    private void drawDayAndNight(float radius) {
        RectF oval = getOval(radius);

        // draw night part
        float nightStart = -90;

        mPaint.setColor(getResources().getColor(R.color.clock_dark_gray));
        mPaint.setStyle(Paint.Style.FILL);
        mCanvas.drawArc(oval, nightStart, mAngles.getNightAngle(), true, mPaint);

        // draw day part
        float dayStart = nightStart + mAngles.getNightAngle();

        mPaint.setColor(getResources().getColor(R.color.clock_white));
        mPaint.setStyle(Paint.Style.FILL);
        mCanvas.drawArc(oval, dayStart, mAngles.getDayAngle(), true, mPaint);
    }

    private void drawSmallCircle() {
        float radius = 6*mScale;
        mPaint.setColor(getResources().getColor(R.color.clock_red));
        mCanvas.drawCircle(mCenterX, mCenterY, radius, mPaint);
    }
}
