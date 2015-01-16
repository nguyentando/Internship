package com.example.dont.hometimelinedemo;

import android.content.Context;
import android.drm.DrmManagerClient;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Map;

/**
 * Created by dont on 12/23/2014.
 */
public abstract class VideoPlayer {
    protected MediaPlayer mMediaPlayer = new MediaPlayer();
    protected ViewGroup.MarginLayoutParams mParams;
    protected View mPreviewWindow;
    protected Context mContext;
    protected Video mVideo;
    protected DiskLruVideoCache mDiskLruVideoCache;

    public enum VideoPlayerState {
        IDLE,
        INITIALIZED,
        PREPARING,
        PREPARED,
        STARTED,
        PAUSED,
        STOPPED,
        PLAYBACK_COMPLETED,
        END,
        ERROR
    }

    protected VideoPlayerState state = VideoPlayerState.IDLE;

    public VideoPlayerState getState() {
        return state;
    }

    protected void setState(VideoPlayerState state) {
        this.state = state;
    }

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

    public void start() throws IllegalStateException {
        mMediaPlayer.start();
        setState(VideoPlayerState.STARTED);
    }

    public void stop() throws IllegalStateException {
        mMediaPlayer.stop();
        setState(VideoPlayerState.STOPPED);
    }

    public void reset() {
        mMediaPlayer.reset();
        setState(VideoPlayerState.IDLE);
    }

    public void release() {
        mMediaPlayer.release();
        setState(VideoPlayerState.END);
    }

    public void setDataSource(String path)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        mMediaPlayer.setDataSource(path);
        setState(VideoPlayerState.INITIALIZED);
    }

    public void setDataSource(Context context, Uri uri, Map<String, String> headers)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        mMediaPlayer.setDataSource(context, uri, headers);
        setState(VideoPlayerState.INITIALIZED);
    }

    public void setDataSource(Context context, Uri uri)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        mMediaPlayer.setDataSource(context, uri);
        setState(VideoPlayerState.INITIALIZED);
    }

    public void setDataSource(FileDescriptor fd, long offset, long length)
            throws IOException, IllegalArgumentException, IllegalStateException {
        mMediaPlayer.setDataSource(fd, offset, length);
        setState(VideoPlayerState.INITIALIZED);
    }

    public void setDataSource(FileDescriptor fd)
            throws IOException, IllegalArgumentException, IllegalStateException {
        mMediaPlayer.setDataSource(fd);
        setState(VideoPlayerState.INITIALIZED);
    }

    public void prepareAsync () throws IllegalStateException {
        mMediaPlayer.prepareAsync();
        setState(VideoPlayerState.PREPARING);
    }

    public void prepare () throws IOException, IllegalStateException {
        mMediaPlayer.prepare();
        setState(VideoPlayerState.PREPARED);
    }

    public void seekTo(int msec) throws IllegalStateException {
        mMediaPlayer.seekTo(msec);
    }

    public void pause() throws IllegalStateException {
        mMediaPlayer.pause();
        setState(VideoPlayerState.PAUSED);
    }

}
