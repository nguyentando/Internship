package com.example.dont.hometimelinedemo;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Created by dont on 1/6/2015.
 */
public class MyProgressImage extends ImageView {

    public MyProgressImage(Context context) {
        super(context);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("MyProgressImage", "progress image clicked");
            }
        });
    }

    public MyProgressImage(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("MyProgressImage", "progress image clicked");
            }
        });
    }

    public MyProgressImage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("MyProgressImage", "progress image clicked");
            }
        });
    }

    public MyProgressImage(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("MyProgressImage", "progress image clicked");
            }
        });
    }
}
