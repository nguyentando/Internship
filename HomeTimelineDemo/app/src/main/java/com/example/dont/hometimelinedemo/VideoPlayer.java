package com.example.dont.hometimelinedemo;

import android.content.Context;
import android.drm.DrmManagerClient;
import android.media.MediaPlayer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Created by dont on 12/23/2014.
 */
public abstract class VideoPlayer {
    protected MediaPlayer mMediaPlayer = new MediaPlayer();
    protected RelativeLayout.LayoutParams mParams;
    protected View mPreviewWindow;
    protected Context mContext;
    protected Video mVideo;
    protected DiskLruVideoCache mDiskLruVideoCache;

    // listeners
    MediaPlayer.OnPreparedListener mOnPreparedListener;
    MediaPlayer.OnErrorListener mOnErrorListener;
    MediaPlayer.OnCompletionListener mOnCompletionListener;

    public void setOnPreparedListener(MediaPlayer.OnPreparedListener listener) {
        mOnPreparedListener = listener;
    }

    public void setOnErrorListener(MediaPlayer.OnErrorListener listener) {
        mOnErrorListener = listener;
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
        mOnCompletionListener = listener;
    }

    public void playVideo(Video video, View previewWindow) {
        Reset();

        setVideo(video);
        setPreviewWindow(previewWindow);
        startConfigHolder();
    }

    public abstract void startConfigHolder();

    public void setVideo(Video video) {
        mVideo = video;
    }

    public void setPreviewWindow(View view) {
        mPreviewWindow = view;
    }

    public void setViewPosition() {
        mParams.leftMargin = mPreviewWindow.getLeft();
        mParams.topMargin = mPreviewWindow.getTop();
        mParams.width = mPreviewWindow.getWidth();
        mParams.height = mPreviewWindow.getHeight();
    }

    public abstract void unBindFromParent(ViewGroup view);
    public abstract void bindToParent(ViewGroup parent);

    public void Reset() {
        Stop();
        if(mMediaPlayer != null)
            mMediaPlayer.reset();
    }

    public void Stop() {
        if(mMediaPlayer != null) {
            if(mMediaPlayer.isPlaying())
                mMediaPlayer.stop();
        }
    }

    public void PauseOrResume() {
        if(mMediaPlayer != null) {
            if(mMediaPlayer.isPlaying())
                mMediaPlayer.pause();
            else
                mMediaPlayer.start();
        }
    }

    public boolean isPlaying() {
        if(mMediaPlayer != null)
            return mMediaPlayer.isPlaying();

        return false;
    }

    public abstract Object getView();

    public void seekTo(int i) {
        mMediaPlayer.seekTo(i);
    }

    public void start() {
        mMediaPlayer.start();
    }

    public void prepareAsync() {
        mMediaPlayer.prepareAsync();
    }

    public void stop() {
        mMediaPlayer.stop();
    }
}
