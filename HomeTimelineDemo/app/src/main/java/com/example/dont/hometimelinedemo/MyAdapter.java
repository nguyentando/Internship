package com.example.dont.hometimelinedemo;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

/**
 * Created by dont on 12/24/2014.
 */
public class MyAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<Video> mVideos;
    private VideoPlayer mVideoPlayer;
    private MainActivity mMainActivity;

    public MyAdapter(){}

    public MyAdapter(Context context, List<Video> mVideos, VideoPlayer videoPlayer) {
        mInflater = LayoutInflater.from(context);
        this.mVideos = mVideos;
        this.mVideoPlayer = videoPlayer;
        mMainActivity = (MainActivity) context;
    }

    public void setVideoPlayer(VideoPlayer videoPlayer) {
        this.mVideoPlayer = videoPlayer;
    }

    @Override
    public int getCount() {
        return mVideos.size();
    }

    @Override
    public Object getItem(int position) {
        return mVideos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;

        if(convertView == null) {
            view = mInflater.inflate(R.layout.row_layout, parent, false);

            // Create holder
            holder = new ViewHolder();
            holder.textView = (TextView) view.findViewById(R.id.textView);
            holder.imageView = (ImageView) view.findViewById(R.id.imageView);
            holder.controlBtn = (View) view.findViewById(R.id.controlBtn);
            //if(!holder.controlBtn.hasOnClickListeners())
                holder.controlBtn.setOnClickListener(mMainActivity.newControlBtnListener());
            holder.progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

            // save holder
            view.setTag(holder);
        }else {
            //Stop download video
            ViewHolder convertViewHolder = (ViewHolder) convertView.getTag();
            if(convertViewHolder.playVideoRunnable != null)
                convertViewHolder.playVideoRunnable.isStop = true;

            // unbind convertView from its parent
            mVideoPlayer.unBindFromParent((ViewGroup)convertView);

            // Hide control button
            ((ViewHolder)convertView.getTag()).controlBtn.setVisibility(View.INVISIBLE);

            // Hide progress bar
            ((ViewHolder)convertView.getTag()).progressBar.setVisibility(View.INVISIBLE);

            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        /*String url = mVideos.get(position).getUrl();
        holder.textView.setText(url);*/

        return view;
    }

    public class ViewHolder {
        public TextView textView;
        public ImageView imageView;
        public View controlBtn;
        public ProgressBar progressBar;
        public MainActivity.PlayVideoRunnable playVideoRunnable;
    }
}
