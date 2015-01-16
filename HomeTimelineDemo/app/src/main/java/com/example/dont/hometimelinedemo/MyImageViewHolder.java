package com.example.dont.hometimelinedemo;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by dont on 1/6/2015.
 */
public class MyImageViewHolder extends ImageView {
    Context mContext;
    Video mVideo;

    public MyImageViewHolder(Context context) {
        super(context);
        init(context);
    }

    public MyImageViewHolder(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyImageViewHolder(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public MyImageViewHolder(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public void init(Context context) {
        mContext = context;

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) mContext;
                activity.onVideoViewClicked(MyImageViewHolder.this, mVideo);
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    public void setVideo(Video video) {
        mVideo = video;
    }
}
