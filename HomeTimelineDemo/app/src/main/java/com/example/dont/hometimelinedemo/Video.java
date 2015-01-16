package com.example.dont.hometimelinedemo;

import android.graphics.Bitmap;

/**
 * Created by dont on 12/26/2014.
 */
public class Video {
    private String mUrl;
    private Bitmap mThumbnail;
    private String mPath;
    private String mKey;

    public Video(){};
    public Video(String url, String mPath, Bitmap thumbnail, String key) {
        setUrl(url);
        setPath(mPath);
        setThumbnail(thumbnail);
        setKey(key);
    }

    private void setKey(String key) {
        mKey = key;
    }

    private void setPath(String path) {
        this.mPath = path;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setThumbnail(Bitmap bitmap) {
        mThumbnail = bitmap;
    }

    public Bitmap getmThumbnail() {
        return mThumbnail;
    }

    public String getPath() {
        return mPath;
    }

    public String getKey() {
        return mKey;
    }
}
