package com.example.dont.hometimelinedemo;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;

/**
 * Created by dont on 12/23/2014.
 */
public class NewVideoPlayer extends VideoPlayer implements TextureView.SurfaceTextureListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnErrorListener {

    TextureView mTextureView;

    NewVideoPlayer(Context context) {
        //Khoi tao mTextureView
        mTextureView = new TextureView(context);
        mParams = new RelativeLayout.LayoutParams(0, 0);
        mTextureView.setLayoutParams(mParams);
        mTextureView.setSurfaceTextureListener(this);

        mContext = context;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
        Surface surface = new Surface(surfaceTexture);

        if(mMediaPlayer != null) {
            try {
                String path = mVideo.getPath();
                if(path != null)
                    mMediaPlayer.setDataSource(path);
                else
                    mMediaPlayer.setDataSource(mVideo.getUrl());

                mMediaPlayer.setSurface(surface);
                mMediaPlayer.setOnBufferingUpdateListener(this);
                mMediaPlayer.setOnCompletionListener(this);
                mMediaPlayer.setOnPreparedListener(this);
                mMediaPlayer.setOnErrorListener(this);
                mMediaPlayer.setOnVideoSizeChangedListener(this);
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(mOnCompletionListener != null)
            mOnCompletionListener.onCompletion(mp);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();

        if(mOnPreparedListener != null)
            mOnPreparedListener.onPrepared(mp);
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

    }

    @Override
    public void bindToParent(ViewGroup parent) {
        parent.addView(mTextureView);
        /*MyProgressImage progressImage = (MyProgressImage) parent.findViewById(R.id.progressImage);
        progressImage.bringToFront();*/
    }

    @Override
    public void unBindFromParent(ViewGroup parent) {
        if(mTextureView != null && mTextureView.getParent() != null && mTextureView.getParent() == parent)
            ((ViewGroup)mTextureView.getParent()).removeView(mTextureView);
    }

    @Override
    public void startConfigHolder() {
        unBindFromParent((ViewGroup)mTextureView.getParent());

        // Thay doi thong so cua params, de mTextureView khop voi mPreviewWindow
        setViewPosition();

        bindToParent((ViewGroup) mPreviewWindow.getParent());
    }

    @Override
    public Object getView() {
        return mTextureView;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if(mOnErrorListener != null)
            mOnErrorListener.onError(mp, what, extra);

        return false;
    }
}
