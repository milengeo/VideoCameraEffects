package com.rustero.tools;


import android.util.Log;

public class Clerk {

    public long period = 999;
    private long mTack;
    public float rate = 16f;
    private float mBusy = 0;
    private String mTag;
    long mTick;


    public Clerk(String aTag) {
        mTag = aTag;
        mTack = System.currentTimeMillis();
    }



    public void start() {
        mTick = System.currentTimeMillis();
    }



    public void stop() {
        long span = System.currentTimeMillis() - mTick;
        mBusy =  (rate-1)/rate * mBusy;
        mBusy +=  1/rate * span;
        if (System.currentTimeMillis() - mTack < 1000) return;
        mTack = System.currentTimeMillis();
        Log.d(mTag, "busy: " + Math.round(mBusy));
    }


}
