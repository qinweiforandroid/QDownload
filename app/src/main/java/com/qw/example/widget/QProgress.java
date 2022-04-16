package com.qw.example.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.qw.example.R;


/**
 * Created by qinwei on 2019-07-11 21:55
 * email: qinwei_it@163.com
 */
public class QProgress extends View {
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF mRectF;

    private long max = 100;
    private long progress = 0;

    private int startX;
    private int endX;
    private int startY;
    private int endY;
    private float radius;
    private int progressBg = Color.parseColor("#03A9F4");
    private int progressColor = Color.parseColor("#FF9D2F");

    public QProgress(Context context) {
        super(context);
        init(context, null);
    }


    public QProgress(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public QProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mRectF = new RectF();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.QProgress);
        if (a.hasValue(R.styleable.QProgress_max)) {
            max = a.getInteger(R.styleable.QProgress_max, 100);
        }
        if (a.hasValue(R.styleable.QProgress_progress)) {
            progress = a.getInteger(R.styleable.QProgress_progress, 0);
        }
        if (a.hasValue(R.styleable.QProgress_progressBg)) {
            progressBg = a.getColor(R.styleable.QProgress_progressBg, progressBg);
        }

        if (a.hasValue(R.styleable.QProgress_progressColor)) {
            progressColor = a.getColor(R.styleable.QProgress_progressColor, progressColor);
        }
        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mRectF.top = startY;
        mRectF.bottom = endY;
        mRectF.left = startX;
        mRectF.right = endX;
        mPaint.setColor(progressBg);
        //画背景
        canvas.drawRoundRect(mRectF, (float) (pH() / 2.0), radius, mPaint);
        //画圆弧
        mRectF.right = startX + 2 * radius;
        mPaint.setColor(progressColor);
        if (pl() <= radius) {
            float percent = pl() / radius;
            canvas.drawArc(mRectF, 180 - 90 * percent, 180 * percent, false, mPaint);
        } else {
            canvas.drawArc(mRectF, 90, 180, false, mPaint);
        }
        if (pl() > radius && pl() < endX - radius) {
            mRectF.left = startX + radius;
            mRectF.top = startY;
            mRectF.bottom = endY;
            mRectF.right = pl();
            canvas.drawRect(mRectF, mPaint);
        } else if (pl() >= endX - radius) {
            mRectF.left = startX + radius;
            mRectF.right = endX - radius;
            canvas.drawRect(mRectF, mPaint);

            //背景改成蓝色
            mRectF.top = startY;
            mRectF.bottom = endY;
            mRectF.left = startX;
            mRectF.right = endX;
            mPaint.setColor(progressColor);
            canvas.drawRoundRect(mRectF, radius, radius, mPaint);

            mRectF.left = endX - pH();
            mRectF.top = startY;
            mRectF.bottom = endY;
            mRectF.right = endX;
            mPaint.setColor(progressBg);
            float percent = 1 - ((pW() - pl()) / radius);
            canvas.drawArc(mRectF, -90 + 90 * percent, 180 - 180 * percent, false, mPaint);
        }
    }


    private float pl() {
        return (float) (progress / (max * 1.0) * (getWidth() - getPaddingLeft() - getPaddingRight()));
    }

    public void notifyDataChanged(int progress) {
        this.progress = progress;
        invalidate();
    }

    public void setMax(int max) {
        this.max = max;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    @Override
    public void layout(int l, int t, int r, int b) {
        super.layout(l, t, r, b);
        startX = getPaddingLeft();
        endX = getWidth() - getPaddingRight();
        startY = getPaddingTop();
        endY = getHeight() - getPaddingBottom();
        radius = (float) (pH() / 2.0);
    }

    private float pH() {
        return endY - startY;
    }

    private float pW() {
        return endX - startX;
    }

    private int measureHeight(int heightMeasureSpec) {
        return MeasureSpec.getSize(heightMeasureSpec);
    }

    private int measureWidth(int widthMeasureSpec) {
        return MeasureSpec.getSize(widthMeasureSpec);
    }
}