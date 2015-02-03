package com.example.dont.hometimelinedemo;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.io.IOException;

/**
 * Created by dont on 12/23/2014.
 */
public class OldVideoPlayer extends VideoPlayer implements MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnVideoSizeChangedListener, SurfaceHolder.Callback, MediaPlayer.OnErrorListener {

    SurfaceView mSurfaceView;
    SurfaceHolder mSurfaceHolder;

    OldVideoPlayer(Context context) {
        // Khoi tao Surface View
        mSurfaceView = new SurfaceView(context);
        mParams = new ViewGroup.MarginLayoutParams(0, 0);
        mSurfaceView.setLayoutParams(mParams);
        mSurfaceView.getHolder().addCallback(this);
        mSurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // Set listener
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnVideoSizeChangedListener(this);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mContext = context;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        Log.e("Media Player", "onBufferingUpdate", null);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        setState(VideoPlayerState.PLAYBACK_COMPLETED);
        if(mOnCompletionListener != null)
            mOnCompletionListener.onCompletion(mp);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        start();

        if(mOnPreparedListener != null)
            mOnPreparedListener.onPrepared(mp);
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        Log.e("Media Player", "onVideoSizeChanged", null);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        if(mMediaPlayer != null) {
            try {
                String path = mVideo.getPath();
                if(path != null)
                    setDataSource(path);
                else
                    setDataSource(mVideo.getUrl());

                mMediaPlayer.setDisplay(holder);
                prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void unBindFromParent(ViewGroup parent) {
        if(mSurfaceView != null && mSurfaceView.getParent() != null  && mSurfaceView.getParent() == parent)
            ((ViewGroup)mSurfaceView.getParent()).removeView(mSurfaceView);
    }

    @Override
    public void bindToParent(ViewGroup parent) {
        parent.addView(mSurfaceView);
    }


    @Override
    public void startConfigHolder() {
        unBindFromParent((ViewGroup)mSurfaceView.getParent());

        // adjust some params's values (left, top, width, height) to fit mTextureView in mPreviewWindow
        setViewPosition();

        bindToParent((ViewGroup) mPreviewWindow.getParent());
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {

        setState(VideoPlayerState.ERROR);

        if(mOnErrorListener != null)
            mOnErrorListener.onError(mp, what, extra);

        return false;
    }
}
