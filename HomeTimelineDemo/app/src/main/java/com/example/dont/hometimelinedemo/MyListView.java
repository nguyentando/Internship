package com.example.dont.hometimelinedemo;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.example.dont.hometimelinedemo.MainActivity;

class MyListView extends ListView {

    Context mContext;

    public MyListView(Context context) {
        super(context);
        init(context);
    }

    public MyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public MyListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public void init(Context context) {
        mContext = context;
    }

    @Override
    public ListAdapter getAdapter() {
        return super.getAdapter();
        //return ((MainActivity)mContext).getListAdapter();
    }

    @Override
    public void setSelection(int position) {
        super.setSelection(position);
    }

    public int computeVerticalScrollOffset() {
        return super.computeVerticalScrollOffset();
    }
}