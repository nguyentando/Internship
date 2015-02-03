package com.example.dont.hometimelinedemo;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ListActivity {

    String[] mUrls = new String[]{"http://videos-c-19.ak.instagram.com/hphotos-ak-xaf1/t50.2886-16/10820230_808770289181179_1288684802_n.mp4",
            "http://videos-g-10.ak.instagram.com/hphotos-ak-xaf1/t50.2886-16/10858011_319067034948450_881331007_n.mp4",
            "http://videos-h-18.ak.instagram.com/hphotos-ak-xaf1/t50.2886-16/10871302_521005848034718_1368788454_n.mp4",
            "http://videos-a-4.ak.instagram.com/hphotos-ak-xaf1/t50.2886-16/10858332_399664680199364_446208248_n.mp4",
            "http://videos-a-0.ak.instagram.com/hphotos-ak-xaf1/t50.2886-16/10871773_1577481855814880_2107348315_n.mp4",
            "http://videos-e-16.ak.instagram.com/hphotos-ak-xaf1/t50.2886-16/10871479_1606315942930016_1940105576_n.mp4",
            "http://videos-e-6.ak.instagram.com/hphotos-ak-xaf1/t50.2886-16/10842475_524799390990486_1283420740_n.mp4",
            "http://videos-c-3.ak.instagram.com/hphotos-ak-xaf1/t50.2886-16/10831563_328710820647883_2011577670_n.mp4",
            "http://videos-h-14.ak.instagram.com/hphotos-ak-xfa1/t50.2886-16/10826750_561611827303514_1913340305_n.mp4",
            "http://videos-h-16.ak.instagram.com/hphotos-ak-xaf1/t50.2886-16/10813363_725537580866236_1953292738_n.mp4"};

    List<Video> mVideos;

    MyAdapter mAdapter;

    VideoPlayer mVideoPlayer;

    // Video cache
    DiskLruVideoCache mDiskLruVideoCache;
    public final String mCacheFolder = "MyVideo";
    public final int mCacheSize = 1024 * 1024 * 100; // = 100 MB
    public final String mExtension = "mp4";

    // Download thread
    Downloader mDownloader;

    // The previous view which is used for playing video
    View previousView;

    // save state and visible items to use in onScroll
    private static final int INIT = 5;
    private int mFirstVisible = 0;
    private int mVisibleItemCount = 0;

    // use for playing video with devices which are under 2.3 (SDK <= 10)
    RelativeLayout rootVideoViewHolder;
    MyImageViewHolder videoViewHolder;
    View controlBtnHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Save video view holder instances
        if(Build.VERSION.SDK_INT <= 10) {
            rootVideoViewHolder = (RelativeLayout) findViewById(R.id.rootVideoViewHolder);
            videoViewHolder = (MyImageViewHolder) rootVideoViewHolder.findViewById(R.id.imageViewHolder);
            controlBtnHolder = (View) rootVideoViewHolder.findViewById(R.id.controlBtnHolder);
            controlBtnHolder.setOnClickListener(newControlBtnListener());
        }

        // Init DiskLruVideoCache
        mDiskLruVideoCache = new DiskLruVideoCache(this, mCacheFolder, mCacheSize, mExtension);

        // Init mVideos
        mVideos = new ArrayList<Video>();

        for (int i=0; i<mUrls.length; i++) {
            int hashKey = mUrls[i].hashCode();
            String keyStr = String.valueOf(hashKey) + "." + mExtension;
            Video video = new Video(mUrls[i], getVideoPath(keyStr), null, keyStr);
            mVideos.add(video);
        }

        // Init mVideoPlayer
        if (Build.VERSION.SDK_INT >= 14)
            mVideoPlayer = new NewVideoPlayer(MainActivity.this);
        else
            mVideoPlayer = new OldVideoPlayer(MainActivity.this);

        // set state listener for mVideoPlayer
        mVideoPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.e("VideoPlayer", "OnPrepared");
            }
        });

        mVideoPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.e("VideoPlayer", "OnCompletion");
            }
        });

        mVideoPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.e("VideoPlayer", "OnError");
                return false;
            }
        });

        // Init adapter
        mAdapter = new MyAdapter(this, mVideos, mVideoPlayer);
        setListAdapter(mAdapter);

        /*getListView().setRecyclerListener(new AbsListView.RecyclerListener() {
            @Override
            public void onMovedToScrapHeap(View view) {
                //runOnUiThread(new MyRunnable(view));
                //if(((ViewGroup)view.getParent()).ish)
                mVideoPlayer.unBindFromParent((ViewGroup)view);
            }
        });*/

        // Start downloader thread
        mDownloader = new Downloader();
        mDownloader.start();

        while (mDownloader.mHandler == null) {}

        if(Build.VERSION.SDK_INT > 10) {
            // Add onScrollListener
            getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
                int curState = INIT;
                int mCurVideo = -1;
                boolean shouldPlay = true;

                private boolean mIsScrollingUp = false;
                private int mInitialScroll = 0;

                @Override
                public void onScrollStateChanged(AbsListView listview, int scrollState) {

                    // Detecting whether the list is scrolling up or down
                    int scrolledOffset = ((MyListView)listview).computeVerticalScrollOffset();
                    mIsScrollingUp = scrolledOffset > mInitialScroll;
                    mInitialScroll = scrolledOffset;

                    if(mIsScrollingUp)
                        Log.e("scroll direction", "up");
                    else
                        Log.e("scroll direction", "down");

                    if(curState != SCROLL_STATE_IDLE && scrollState == SCROLL_STATE_IDLE) {
                        shouldPlay = true;
                        onScroll(listview, mFirstVisible, mVisibleItemCount, getListAdapter().getCount());
                    }
                    else
                        shouldPlay = false;

                    curState = scrollState;
                }

                @Override
                public void onScroll(AbsListView listview, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    // save 2 values to use in onScrollStateChanged
                    mFirstVisible = firstVisibleItem;
                    mVisibleItemCount = visibleItemCount;

                    // Check if we should to play video or not
                    if(!shouldPlay)
                        return;

                    new OnScrollTask().execute(listview, firstVisibleItem, visibleItemCount);
                }

                class OnScrollTask extends AsyncTask<Object, Void, Void> {

                    @Override
                    protected Void doInBackground(Object... params) {
                        AbsListView listview = (AbsListView) params[0];
                        int firstVisibleItem = (int) params[1];
                        int visibleItemCount = (int) params[2];

                        // Find the first view (based on the scroll direction), which is showed more than 80% of its view
                        if(visibleItemCount <= 0)
                            return null;

                        int idMax = 0;
                        float percentageMax = 0;
                        if(mIsScrollingUp) {
                            for(int i=visibleItemCount-1; i>=0; i--) {
                                View rowView = listview.getChildAt(i);

                                // Calculate the screen percentage of rowView
                                float percentageTemp = CalculatePercentage(rowView);

                                if(percentageTemp >= 0.8){
                                    idMax = i;
                                    break;
                                }
                            }
                        }else {
                            for(int i=0; i<visibleItemCount; i++) {
                                View rowView = listview.getChildAt(i);

                                // Calculate the screen percentage of rowView
                                float percentageTemp = CalculatePercentage(rowView);

                                if(percentageTemp >= 0.8){
                                    idMax = i;
                                    break;
                                }
                            }
                        }

                        /*// Find the view which has the most screen percentage
                        for(int i=0; i<visibleItemCount; i++) {
                            View rowView = listview.getChildAt(i);

                            // Calculate the screen percentage of rowView
                            float percentageTemp = CalculatePercentage(rowView);

                            // Compare with percentageMax and alternate the value of idMax
                            if(i == 0 || percentageTemp > percentageMax) {
                                percentageMax = percentageTemp;
                                idMax = i;
                            }
                        }*/

                        // Check whether previous video and current video are the same or not
                        int playVideoIdx = idMax + firstVisibleItem;
                        if(mCurVideo == playVideoIdx)
                            return null;

                        mCurVideo = playVideoIdx;

                        // Play video of that view
                        if(previousView != null) {
                            // Stop downloading and caching previous video
                            mDiskLruVideoCache.stopCaching();

                            // Stop play previous video
                            stopPlayPreviousVideo(previousView);
                        }

                        View v = listview.getChildAt(idMax);
                        previousView = v;

                        // Cancel all ealier messages
                        mDownloader.removeAllMessagesAndCallbacks();

                        // Download, cache and play video
                        Video video = mVideos.get(mCurVideo);
                        View view = ((MyAdapter.ViewHolder) v.getTag()).imageView;

                        // Show progress bar
                        ProgressBar progressBar = getProgressBar((View) view.getParent());
                        showView(progressBar, true, true);

                        PlayVideoRunnable run = new PlayVideoRunnable(video, view);

                        // Save play video runnable into row view
                        ((MyAdapter.ViewHolder) v.getTag()).playVideoRunnable = run;

                        // post play video runnable
                        while(mDiskLruVideoCache.isCaching()) {}
                        mDiskLruVideoCache.continueCaching();
                        mDownloader.post(run);

                        shouldPlay = false;

                        return null;
                    }
                }
            });
        }
    }

    private float CalculatePercentage(View view) {
        Rect rect = new Rect();
        view.getLocalVisibleRect(rect);

        int width = view.getWidth();
        int height = view.getHeight();

        return (float)(rect.width()*rect.height())/(width*height);
    }

    private String getVideoPath(String fileName) {
        return mDiskLruVideoCache.getCacheFolder().getAbsolutePath() + File.separator + fileName;
    }

    public void showView(View v, boolean isShow, boolean isRunOnUIThread) {
        ShowViewRunnable run = new ShowViewRunnable(v, isShow);
        if(isRunOnUIThread) {
            runOnUiThread(run);
        }else
            run.run();
    }

    public void stopVideoPlayer(ViewGroup view) {
        // Reset media player
        mVideoPlayer.Reset();

        // unbind preview windows of its parent
        mVideoPlayer.unBindFromParent(view);

        // Reset icon of control btn
        controlBtnHolder.setBackgroundResource(R.drawable.ic_action_stop);
    }

    public void onVideoViewClicked(MyImageView myImageView) {
        View v = (View) myImageView.getParent();
        int position = getListView().getPositionForView(v);

        if(previousView != null) {
            // Stop downloading and caching previous video
            mDiskLruVideoCache.stopCaching();

            // Stop play previous video
            stopPlayPreviousVideo(previousView);
        }

        previousView = v;

        // Cancel all ealier messages
        mDownloader.removeAllMessagesAndCallbacks();

        // Download, cache and play video
        Video video = mVideos.get(position);
        View view = ((MyAdapter.ViewHolder) v.getTag()).imageView;
        PlayVideoRunnable run = new PlayVideoRunnable(video, view);

        // Save play video runnable into row view
        ((MyAdapter.ViewHolder) v.getTag()).playVideoRunnable = run;

        // post play video runnable
        while(mDiskLruVideoCache.isCaching()) {}
        mDiskLruVideoCache.continueCaching();
        mDownloader.post(run);
    }


    public void onVideoViewClicked(MyImageViewHolder view, Video video) {

        if(mVideoPlayer.isPlaying()) {
            mVideoPlayer.seekTo(0);
            mVideoPlayer.start();
        }else{
            mVideoPlayer.Reset();
            mVideoPlayer.playVideo(video, view);
        }
        controlBtnHolder.setBackgroundResource(R.drawable.ic_action_stop);
        controlBtnHolder.bringToFront();
    }

    private class ShowViewRunnable implements Runnable {

        View view;
        boolean isShow;

        ShowViewRunnable(View view, boolean isShow) {
            this.view = view;
            this.isShow = isShow;
        }

        @Override
        public void run() {
            if(isShow)
                view.setVisibility(View.VISIBLE);
            else
                view.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        /*if(previousView != null) {
            // Stop downloading and caching previous video
            mDiskLruVideoCache.stopCaching();

            // Stop play previous video
            stopPlayPreviousVideo(previousView);
        }

        previousView = v;

        // Cancel all ealier messages
        mDownloader.removeAllMessagesAndCallbacks();

        // Download, cache and play video
        Video video = mVideos.get(position);
        View view = ((MyAdapter.ViewHolder) v.getTag()).imageView;
        PlayVideoRunnable run = new PlayVideoRunnable(video, view);

        // Save play video runnable into row view
        ((MyAdapter.ViewHolder) v.getTag()).playVideoRunnable = run;

        // post play video runnable
        while(mDiskLruVideoCache.isCaching()) {}
        mDiskLruVideoCache.continueCaching();
        mDownloader.post(run);*/
    }

    public void stopPlayPreviousVideo(View v) {
        MyAdapter.ViewHolder holder = (MyAdapter.ViewHolder) v.getTag();
        if(holder.playVideoRunnable != null)
            holder.playVideoRunnable.isStop = true;
    }

    public class PlayVideoTask extends AsyncTask<Object, Void, Void> {
        Video mVideo;
        View mView;

        protected Void doInBackground(Object... params) {

            if(!isCancelled()) {
                mVideo = (Video)params[0];
                mView = (View)params[1];

                // Hide control button
                if(mVideoPlayer.mPreviewWindow != null) {
                    View controlBtn = getControlButton((View) mVideoPlayer.mPreviewWindow.getParent());
                    showView(controlBtn, false, true);
                }

                if(!mDiskLruVideoCache.containsKey(mVideo.getKey())) {
                    // Show progress bar in mView
                    ProgressBar progressBar = getProgressBar((View) mView.getParent());
                    showView(progressBar, true, true);

                    // Download mVideo ve
                    URL url = null;
                    try {
                        url = new URL(mVideo.getUrl());
                        HttpURLConnection c = (HttpURLConnection) url.openConnection();
                        mDiskLruVideoCache.put(mVideo.getKey(), c.getInputStream());
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            // hide progress bar
            ((MyAdapter.ViewHolder)((ViewGroup) mView.getParent()).getTag()).progressBar.setVisibility(View.INVISIBLE);

            // Play video
            mVideoPlayer.playVideo(mVideo, mView);

            // Show control button
            MyAdapter.ViewHolder holder = (MyAdapter.ViewHolder) ((ViewGroup) mView.getParent()).getTag();
            holder.controlBtn.setVisibility(View.VISIBLE);

            // Bring Media Control Buttons to the top
            View controlBtn = (View) ((ViewGroup)mView.getParent()).findViewById(R.id.controlBtn);
            controlBtn.bringToFront();
        }
    }

    public ProgressBar getProgressBar(View v) {
        MyAdapter.ViewHolder holder = (MyAdapter.ViewHolder) v.getTag();
        return holder.progressBar;
    }

    public View getControlButton(View v) {
        if(Build.VERSION.SDK_INT > 10) {
            MyAdapter.ViewHolder holder = (MyAdapter.ViewHolder) v.getTag();
            return holder.controlBtn;
        }else {
            // for dialog
            //return v.findViewById(R.id.controlBtnOldSdk);

            // for holder
            return v.findViewById(R.id.controlBtnHolder);
        }
    }

    public class PlayVideoRunnable implements Runnable {
        Video mVideo;
        View mView;
        boolean isStop = false;

        public PlayVideoRunnable(Video video, View view) {
            mVideo = video;
            mView = view;
        }

        @Override
        public void run() {
            if(isStop) return;
            if(mVideoPlayer.mPreviewWindow != null) {
                // Hide control button
                View controlBtn = getControlButton((View) mVideoPlayer.mPreviewWindow.getParent());
                showView(controlBtn, false, true);
            }

            // Reset mVideoPlayer
            mVideoPlayer.Reset();

            // Download and cache video
            if(isStop)
                return;

            // Show progress bar
            ProgressBar progressBar = getProgressBar((View) mView.getParent());
            showView(progressBar, true, true);

            if(!mDiskLruVideoCache.containsKey(mVideo.getKey())) {
                URL url = null;
                try {
                    url = new URL(mVideo.getUrl());
                } catch (MalformedURLException e) {
                    return;
                }

                HttpURLConnection c = null;
                try {
                    c = (HttpURLConnection) url.openConnection();
                } catch (IOException e) {
                    handleNoConnection((View) mView.getParent());
                    return;
                }

                InputStream in = null;
                try {
                    in = c.getInputStream();
                } catch (IOException e) {
                    handleNoConnection((View) mView.getParent());
                    return;
                }
                mDiskLruVideoCache.put(mVideo.getKey(), in);
            }

            // hide progress bar
            ProgressBar progressBarTemp = getProgressBar((View) mView.getParent());
            showView(progressBarTemp, false, true);

            // Play video
            if(isStop)
                return;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Play video
                    if (Build.VERSION.SDK_INT > 10) {
                        mVideoPlayer.playVideo(mVideo, mView);

                        // Show control button
                        View controlBtnTemp = getControlButton((View) mView.getParent());
                        showView(controlBtnTemp, true, false);

                        // Bring Media Control Buttons to the top
                        controlBtnTemp.bringToFront();
                    }else {
                        /*// Use dialog
                        Dialog dialog = new Dialog(MainActivity.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.play_video);
                        dialog.setOnShowListener(new MyOnShowListener(dialog, mVideo));
                        SurfaceView view = (SurfaceView) mVideoPlayer.getView();
                        view.setZOrderOnTop(true);
                        dialog.show();*/

                        // Use view holder on the root view
                        rootVideoViewHolder.setVisibility(View.VISIBLE);
                        videoViewHolder.setVideo(mVideo);
                        mVideoPlayer.playVideo(mVideo, videoViewHolder);

                        // show and bring control btn to front
                        showView(controlBtnHolder, true, false);
                        controlBtnHolder.bringToFront();
                    }
                }
            });
        }
    }

    public void handleNoConnection(View view) {

         class HandleNoConnectionRunnable implements Runnable {
            View view;
            public HandleNoConnectionRunnable(View view) {
                this.view = view;
            }
            @Override
            public void run() {
                // Hide progress bar
                ProgressBar progressBar = getProgressBar(view);
                showView(progressBar, false, false);

                // Show Toast
                Toast.makeText(MainActivity.this, "No connection!", Toast.LENGTH_LONG).show();
            }
        }

        HandleNoConnectionRunnable run = new HandleNoConnectionRunnable(view);
        runOnUiThread(run);
    }

    public class MyControlButton extends ImageButton {

        public MyControlButton(Context context) {
            super(context);
            setClickEvent();
        }

        public MyControlButton(Context context, AttributeSet attrs) {
            super(context, attrs);
            setClickEvent();
        }

        public MyControlButton(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            setClickEvent();
        }

        public MyControlButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
            setClickEvent();
        }

        public void setClickEvent() {
            setOnClickListener(new ControlBtnClickListener());
        }
    }

    public class ControlBtnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if(mVideoPlayer != null) {
                if(mVideoPlayer.isPlaying()) {
                    v.setBackgroundResource(R.drawable.ic_action_play);
                }else
                    v.setBackgroundResource(R.drawable.ic_action_stop);

                mVideoPlayer.PauseOrResume();
            }
        }
    }

    public ControlBtnClickListener newControlBtnListener() {
        return new ControlBtnClickListener();
    }

    class MyOnShowListener implements DialogInterface.OnShowListener {

        Dialog mDialog;
        Video mVideo;

        public MyOnShowListener(Dialog dialog, Video video) {
            mDialog = dialog;
            mVideo = video;
        }

        @Override
        public void onShow(DialogInterface dialog) {
            ImageView view = (ImageView) mDialog.findViewById(R.id.imageViewOldSdk);
            mVideoPlayer.playVideo(mVideo, view);

            /*VideoView videoView = (VideoView) mDialog.findViewById(R.id.videoViewOldSDK);
            videoView.setVideoURI(Uri.parse(mVideo.getPath()));
            videoView.setZOrderOnTop(true);
            try {
                videoView.start();
            }catch (Exception e) {
                return;
            }*/
        }
    }
}
