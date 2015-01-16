package com.example.dont.hometimelinedemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DiskLruVideoCache {

    private DiskLruCache mDiskCache;
    public String  mExtension = "mp4";
    private static final int APP_VERSION = 1;
    private static final int VALUE_COUNT = 1;
    private static final String TAG = "DiskLruVideoCache";
    private boolean isStop = false;
    private boolean isCaching = false;

    public DiskLruVideoCache(Context context, String subDirectory, int diskCacheSize,
                             String extension) {
        try {
            final File diskCacheDir = getDiskCacheDir(context, subDirectory);
            mDiskCache = DiskLruCache.open(diskCacheDir, APP_VERSION, VALUE_COUNT, diskCacheSize);

            mExtension = extension;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean copyStream(InputStream input, OutputStream output, String key)
    {
        byte[] buffer = new byte[1024]; // Adjust if you want
        int bytesRead;
        try {
            while ((bytesRead = input.read(buffer)) != -1)
            {
                if(isStop) {
                    // Remove this cache
                    if(containsKey(key))
                        mDiskCache.remove(key);
                    isStop = false;
                    return false;
                }
                output.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean writeVideoToFile(InputStream in, DiskLruCache.Editor editor, String key) {
        OutputStream out = null;
        OutputStream editorStream = null;
        try {
            editorStream = editor.newOutputStream(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        out = new BufferedOutputStream(editorStream, Utils.IO_BUFFER_SIZE);

        return copyStream(in, out, key);
    }

    private File getDiskCacheDir(Context context, String uniqueName) {

        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !Utils.isExternalStorageRemovable() ?
                        Utils.getExternalCacheDir(context).getPath() :
                        context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }

    public void put( String key, InputStream data ) {

        isCaching = true;
        DiskLruCache.Editor editor = null;
        try {
            editor = mDiskCache.edit( key );
            if ( editor == null ) {
                return;
            }

            if( writeVideoToFile(data, editor, key) ) {
                mDiskCache.flush();
                editor.commit();
                if ( BuildConfig.DEBUG ) {
                    Log.d( "cache_test_DISK_", "video put on disk cache " + key );
                }
            } else {
                editor.abort();
                if ( BuildConfig.DEBUG ) {
                    Log.d("cache_test_DISK_", "ERROR on: video put on disk cache " + key);
                }
            }
        } catch (IOException e) {
            if ( BuildConfig.DEBUG ) {
                Log.d( "cache_test_DISK_", "ERROR on: video put on disk cache " + key );
            }
            try {
                if ( editor != null ) {
                    editor.abort();
                }
            } catch (IOException ignored) {
            }
        }

        isCaching = false;
    }

    public boolean isCaching() {
        return isCaching;
    }

    public boolean containsKey( String key ) {

        boolean contained = false;
        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = mDiskCache.get( key );
            contained = snapshot != null;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if ( snapshot != null ) {
                snapshot.close();
            }
        }

        return contained;

    }

    public void clearCache() {
        if ( BuildConfig.DEBUG ) {
            Log.d( "cache_test_DISK_", "disk cache CLEARED");
        }
        try {
            mDiskCache.delete();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    public File getCacheFolder() {
        return mDiskCache.getDirectory();
    }

    public void stopCaching() {
        isStop = true;
    }

    public void continueCaching() {
        isStop = false;
    }
}