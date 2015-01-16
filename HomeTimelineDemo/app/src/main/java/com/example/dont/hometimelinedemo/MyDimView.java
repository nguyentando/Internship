package com.example.dont.hometimelinedemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

public class MyDimView extends RelativeLayout {
    private Paint innerPaint;
    Context mContext;

    public MyDimView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyDimView(Context context) {
        super(context);
        init(context);
    }

    private void init(final Context context) {
        mContext = context;

        innerPaint = new Paint();
        innerPaint.setARGB(180, 75, 75, 75);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("MyDimView", "clicked");

                // Stop video player
                MainActivity activity = (MainActivity)context;
                activity.stopVideoPlayer(MyDimView.this);

                // hide the holder
                MyDimView.this.setVisibility(INVISIBLE);
            }
        });
    }

    public void setInnerPaint(Paint innerPaint) {
        this.innerPaint = innerPaint;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {

        RectF drawRect = new RectF();
        drawRect.set(0, 0, getMeasuredWidth(), getMeasuredHeight());

        canvas.drawRect(drawRect, innerPaint);

        super.dispatchDraw(canvas);
    }
}