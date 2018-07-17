package com.jm.messageview;


import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tujiong on 2018/4/17.
 */

public class MessageView extends View {

    private static final int MAX_LINE = 2;
    private static final int INTERVAL = 2000;

    private List<Entry> list;
    private TextPaint paint;
    private Paint.FontMetrics fontMetrics;
    private float dividerHeight;
    private int color;
    private int index;

    private ValueAnimator animator;

    private Runnable action = new Runnable() {
        @Override
        public void run() {
            if (animator == null) {
                animator = ValueAnimator.ofInt(0, (int) (getHeight() + dividerHeight));
                animator.setDuration(300);
                animator.setInterpolator(new LinearInterpolator());
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        invalidate();
                    }
                });
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        post(new Runnable() {
                            @Override
                            public void run() {
                                index += 2;
                            }
                        });
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
            }
            animator.start();
            postDelayed(this, INTERVAL);
        }
    };

    public MessageView(Context context) {
        this(context, null);
    }

    public MessageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MessageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        loadMessage();
    }

    private void init() {
        dividerHeight = getResources().getDimension(R.dimen.dp_8);
        color = Color.parseColor("#4a4a4a");
        paint = new TextPaint();
        paint.setColor(color);
        paint.setAntiAlias(true);
        paint.setTextSize(getResources().getDimensionPixelSize(R.dimen.sp_12));
        paint.setTextAlign(Paint.Align.LEFT);
        fontMetrics = paint.getFontMetrics();
    }

    public void loadMessage() {
        if (list != null) {
            list.clear();
        } else {
            list = new ArrayList<>();
        }

        list.add(new Entry("没货了,快来补货吧1,没货了快来补货吧1没货了快来补货吧1没货了快来补货吧1", "  1分钟以前"));
        list.add(new Entry("没货了,快来补货吧2,没货了,快来补货吧2,没货了,快来补货吧2,没货了,快来补货吧2,", "  2分钟以前"));
        list.add(new Entry("没货了,快来补货吧3,没货了,快来补货吧3,没货了,快来补货吧3,没货了,快来补货吧3,", "  3分钟以前"));
        list.add(new Entry("没货了,快来补货吧4,没货了,快来补货吧4,没货了,快来补货吧4,没货了,快来补货吧4,", "  4分钟以前"));
        list.add(new Entry("没货了,快来补货吧5,没货了,快来补货吧5,没货了,快来补货吧5,没货了,快来补货吧5,", "  5分钟以前"));

        onMessageLoaded(list);
        postDelayed(action, INTERVAL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = 0;
        if (hasMessage()) {
            for (int i = 0; i < list.size(); i++) {
                Entry entry = list.get(i);
                entry.makeLayout(paint, width - getPaddingLeft() - getPaddingRight());
                if (i < MAX_LINE) {
                    height += entry.getHeight();
                }
            }
            height += (MAX_LINE - 1) * dividerHeight;
        }
        int offset = (int) (fontMetrics.ascent - fontMetrics.top);
        height += getPaddingTop() + getPaddingBottom() + offset;
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(getPaddingLeft(), getPaddingTop());
        float y;
        if (hasMessage()) {
            for (int i = 0; i < MAX_LINE * 2; i++) {
                Entry entry = list.get((index + i) % list.size());
                y = entry.getHeight() * i + dividerHeight * i;
                float offset = 0;
                if (animator != null) {
                    offset = (int) animator.getAnimatedValue();
                }
                drawText(canvas, entry.getStaticLayout(), y - offset);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState savedState = (SavedState) state;
            super.onRestoreInstanceState(savedState.getSuperState());
            this.index = savedState.index;
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        SavedState savedState = new SavedState(parcelable);
        savedState.index = this.index;
        return savedState;
    }

    private void drawText(Canvas canvas, StaticLayout staticLayout, float y) {
        canvas.save();
        canvas.translate(0, y);
        staticLayout.draw(canvas);
        canvas.restore();
    }

    private boolean hasMessage() {
        return list != null && !list.isEmpty();
    }

    private void onMessageLoaded(List<Entry> entryList) {
        if (entryList != null && !entryList.isEmpty()) {
            requestLayout();
        }
    }

    class Entry {

        private SpannableString text;
        private StaticLayout staticLayout;
        private String prefix;
        private String suffix;

        Entry(String prefix, String suffix) {
            this.prefix = prefix;
            this.suffix = suffix;
        }

        void makeLayout(TextPaint paint, int width) {
            StringBuilder builder = new StringBuilder();
            String tmp = builder.append(prefix).append(suffix).toString();
            StaticLayout tmpStaticLayout = new StaticLayout(tmp, paint, width, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false);
            int end = tmpStaticLayout.getLineEnd(0);
            if (tmp.length() >= end) {
                int index = tmp.length() - end;
                builder.delete(0, builder.length());
                tmp = builder.append(prefix.substring(0, prefix.length() - index)).append("...").toString();
            }
            text = new SpannableString(builder.append(suffix));
            text.setSpan(new ForegroundColorSpan(Color.parseColor("#a0a0a0")), tmp.length(), text.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            staticLayout = new StaticLayout(text, paint, width, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false);
        }

        StaticLayout getStaticLayout() {
            return staticLayout;
        }

        int getHeight() {
            if (staticLayout == null) {
                return 0;
            }
            return staticLayout.getHeight();
        }
    }

    public static class SavedState extends BaseSavedState {

        int index;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.index = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.index);
        }

        //required field that makes Parcelables from a Parcel
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }


    public static abstract class Adapter {
        public void draw(Canvas canvas) {

        }
    }
}
