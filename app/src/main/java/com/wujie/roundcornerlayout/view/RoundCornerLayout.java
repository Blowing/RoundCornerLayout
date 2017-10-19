package com.wujie.roundcornerlayout.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.wujie.roundcornerlayout.R;

/**
 * Created by Troy on 2017-10-18.
 */

public class RoundCornerLayout extends RelativeLayout {

    private float[] radii = new float[8];   // top-left, top-right, bottom-right, bottom-left
    private Path mClipPath ;                // 剪裁区域路劲
    private Path mStrolePath;               // 描边区域路劲
    private Paint mPaint;                   //画笔
    private boolean mRoundAsCircle = false; //圆形
    private int mStrokeColor;               //描边颜色
    private int mStrokeWidth;               //描边半径
    private Region mAreaRegion;             //内容区域

    public RoundCornerLayout(Context context) {
        this(context, null);
    }

    public RoundCornerLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RoundCornerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RCRelativeLayout);
        mRoundAsCircle = ta.getBoolean(R.styleable.RCRelativeLayout_round_as_circle, false);
        mStrokeColor = ta.getColor(R.styleable.RCRelativeLayout_stroke_color, Color.WHITE);
        mStrokeWidth = ta.getDimensionPixelOffset(R.styleable.RCRelativeLayout_stroke_width, 0);
        int roundCorner = ta.getDimensionPixelOffset(R.styleable.RCRelativeLayout_round_corner, 0);
        int roundCornerTopLeft = ta.getDimensionPixelOffset(R.styleable
                .RCRelativeLayout_round_corner_top_left, roundCorner);
        int roundCornerTopRight = ta.getDimensionPixelOffset(R.styleable
                .RCRelativeLayout_round_corner_top_right, roundCorner);
        int roundCornerBottomLeft = ta.getDimensionPixelOffset(R.styleable
                .RCRelativeLayout_round_corner_bottom_left, roundCorner);
        int roundCornerBottomRight = ta.getDimensionPixelOffset(R.styleable
                .RCRelativeLayout_round_corner_bottom_right, roundCorner);

        radii[0] = roundCornerTopLeft;
        radii[1] = roundCornerTopLeft;

        radii[2] = roundCornerTopRight;
        radii[3] = roundCornerTopRight;

        radii[4] = roundCornerBottomLeft;
        radii[5] = roundCornerBottomLeft;

        radii[6] = roundCornerBottomRight;
        radii[7] = roundCornerBottomRight;

        mClipPath = new Path();
        mStrolePath = new Path();
        mAreaRegion = new Region();
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        RectF aras = new RectF();
        aras.left = getPaddingLeft();
        aras.top = getPaddingTop();
        aras.right = w - getPaddingRight();
        aras.bottom = h - getPaddingBottom();
        mClipPath.reset();
        if (mRoundAsCircle) {
            float d = aras.width() >= aras.height() ? aras.height() : aras.width();
            float r = d / 2;
            PointF center = new PointF(w / 2, h / 2);
            mClipPath.setFillType(Path.FillType.INVERSE_EVEN_ODD);
            mClipPath.addRect(aras, Path.Direction.CW);
            mClipPath.addCircle(center.x, center.y, r, Path.Direction.CW);
            mStrolePath.addCircle(center.x, center.y, r, Path.Direction.CW);
        } else {
            mClipPath.setFillType(Path.FillType.EVEN_ODD);
            mClipPath.addRoundRect(aras, radii, Path.Direction.CW);
            mStrolePath.addRoundRect(aras, radii, Path.Direction.CW);
        }
        Region clip = new Region((int) aras.left, (int) aras.top,
                (int) aras.right, (int) aras.bottom);
        mAreaRegion.setPath(mStrolePath, clip);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.saveLayer(new RectF(0, 0, canvas.getWidth(), canvas.getHeight()),
                null, Canvas.ALL_SAVE_FLAG);
        super.dispatchDraw(canvas);
        if (mStrokeWidth > 0 ) {
            mPaint.setXfermode(null);
            mPaint.setStrokeWidth(mStrokeWidth * 2);
            mPaint.setColor(mStrokeColor);
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(mStrolePath, mPaint);
        }

        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mPaint.setStrokeWidth(0);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(mClipPath, mPaint);
        canvas.restore();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!mAreaRegion.contains((int) ev.getX(), (int) ev.getY())) {
            return false;
        }
        return super.dispatchTouchEvent(ev);
    }
}
