package com.dami.fileexplorer.view;

import com.dami.fileexplorer.R;
import com.dami.fileexplorer.util.StyleAble;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.View;



public class RoundProgressBar extends View {
    public static final int FILL = 1;
    public static final int STROKE = 0;
    private static final int ZERO = 0;
    private long Card;
    private int DIF;
    private int dyrRoundWidth;
    private int isCard;
    private boolean isInfo;
    private long max;
    private Paint paint;
    private int percent;
    private long progress;
    private int roundColor;
    private int roundProgressColor;
    private float roundWidth;
    private long sdCardFrees;
    private String sdCardUsed;
    private int startPosition;
    private int style;
    private int textColor;
    private boolean textIsDisplayable;
    private float textSize;
    private float textWidth;

    public RoundProgressBar(Context context) {
        this(context, null);
    }

    public RoundProgressBar(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public RoundProgressBar(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        DIF = 11;
        startPosition = 122;
        paint = new Paint();
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, StyleAble.RoundProgressBar);
        roundColor = obtainStyledAttributes.getColor(0, SupportMenu.CATEGORY_MASK);
        roundProgressColor = obtainStyledAttributes.getColor(1, -16711936);
        textColor = obtainStyledAttributes.getColor(3, -16711936);
        textSize = obtainStyledAttributes.getDimension(4, 15.0f);
        roundWidth = obtainStyledAttributes.getDimension(2, 15.0f);
        textIsDisplayable = obtainStyledAttributes.getBoolean(6, true);
        style = obtainStyledAttributes.getInt(7, 0);
        obtainStyledAttributes.recycle();
    }

    public int getCricleColor() {
        return roundColor;
    }

    public int getCricleProgressColor() {
        return roundProgressColor;
    }

    public long getMax() {
        long j;
        synchronized (this) {
            j = max;
        }
        return j;
    }

    public long getProgress() {
        long j;
        synchronized (this) {
            j = progress;
        }
        return j;
    }

    public float getRoundWidth() {
        return roundWidth;
    }

    public int getTextColor() {
        return textColor;
    }

    public float getTextSize() {
        return textSize;
    }

    @Override
	protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth() / 2;
        int i = (int) ((width) - (roundWidth / 2.0f));
        paint.setColor(getResources().getColor(R.color.roundProgressColorb));
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(roundWidth);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(0.0f);
        paint.setColor(getResources().getColor(R.color.textColor));
        paint.setTypeface(Typeface.DEFAULT);
        if (max != 0) {
            percent = (int) ((((float) progress) / ((float) max)) * 100.0f);
            textWidth = paint.measureText(percent + "%");
        } else {
            textWidth = paint.measureText("0%");
        }
        if (this.style == 0) {
            if (isInfo) {
                dyrRoundWidth = getResources().getDimensionPixelSize(R.dimen.round_Large_Width);
                DIF = getResources().getDimensionPixelSize(R.dimen.round_Large_dif);
                startPosition = getResources().getDimensionPixelSize(R.dimen.roundprogressbar_startPosition);
                paint.setTextSize(getResources().getDimension(R.dimen.memory_percent_textsize));
                canvas.drawText(percent + getResources().getString(R.string.percent), ((width) - (textWidth / 2.0f)) + getResources().getDimension(R.dimen.roundprogressbar_percent_left2), ((width) + (this.textSize / 2.0f)) + getResources().getDimension(R.dimen.roundprogressbar_Large_percent_top), this.paint);
            } else {
                dyrRoundWidth = getResources().getDimensionPixelSize(R.dimen.round_Small_Width);
                DIF = getResources().getDimensionPixelSize(R.dimen.round_Small_dif);
                startPosition = getResources().getDimensionPixelSize(R.dimen.roundprogressbar_startPosition);
                paint.setTextSize(getResources().getDimension(R.dimen.memory_percent_textsize));
                if (max != 0) {
                    canvas.drawText(percent + getResources().getString(R.string.percent), ((width) - (textWidth / 2.0f)) - getResources().getDimension(R.dimen.roundprogressbar_percent_left), ((width) + (this.textSize / 2.0f)) + getResources().getDimension(R.dimen.roundprogressbar_percent_top), this.paint);
                } else {
                    paint.setColor(getResources().getColor(R.color.textColor2));
                    canvas.drawText(0 + getResources().getString(R.string.percent), ((width) - (textWidth / 2.0f)) - getResources().getDimension(R.dimen.roundprogressbar_percent__zero_left), ((width) + (this.textSize / 2.0f)) + getResources().getDimension(R.dimen.roundprogressbar_percent_top), this.paint);
                }
                paint.setTextSize(24.0f);
            }
        }
        paint.setStrokeWidth(dyrRoundWidth);
        paint.setColor(getResources().getColor(R.color.roundProgressColor));
        RectF rectF = new RectF((width - i) + DIF, (width - i) + DIF, (width + i) - DIF, (width + i) - DIF);
        Log.i("oval", "centre = " + width + "------" + "DIF = " + DIF + "------" + "radius =" + i);
        switch (style) {
            case 0:
                paint.setStyle(Style.STROKE);
                if (max != 0 && (360 * (progress * 5)) / (max * 6) != 0) {
                    canvas.drawArc(rectF, startPosition, (360 * (progress * 5)) / (max * 6), false, paint);
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void setCricleColor(int i) {
        roundColor = i;
    }

    public void setCricleProgressColor(int i) {
        roundProgressColor = i;
    }

    public void setMax(long j, long j2, long j3, int i, boolean z) {
        synchronized (this) {
            if (j < 0) {
                throw new IllegalArgumentException("max not less than 0");
            }
            max = j;
            Card = j2;
            sdCardFrees = j3;
            isInfo = z;
            isCard = i;
        }
    }

    public void setProgress(long j) {
        synchronized (this) {
            if (j < 0) {
                throw new IllegalArgumentException("progress not less than 0");
            }
            if (j > max) {
                j = max;
            }
            if (j <= max) {
                progress = j;
                postInvalidate();
            }
        }
    }

    public void setRoundWidth(float f) {
        roundWidth = f;
    }

    public void setTextColor(int i) {
        textColor = i;
    }

    public void setTextSize(float f) {
        textSize = f;
    }
    
    public interface SupportMenu extends Menu {
        public static final int CATEGORY_MASK = -65536;
        public static final int CATEGORY_SHIFT = 16;
        public static final int USER_MASK = 65535;
        public static final int USER_SHIFT = 0;
    }

}
