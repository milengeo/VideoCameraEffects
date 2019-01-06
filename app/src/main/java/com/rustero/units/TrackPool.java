package com.rustero.units;


import android.media.MediaCodec;

import com.rustero.App;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;


public class TrackPool {


	public class TrackPail {
		public ByteBuffer bufData;
		public int trackIndex;
		public MediaCodec.BufferInfo bufInfo;
	}


	private Object mLock = new Object();
	private Map<Integer, TrackPail> mDepot;
	private ArrayBlockingQueue<TrackPail> mQueue;



	public TrackPool() {
		mQueue = new ArrayBlockingQueue<TrackPail>(99);
		mDepot =new TreeMap<>();
	}



	public TrackPail takeDepot(int aSize) {
		TrackPail result = null;
		synchronized (mLock) {
			try {
				Iterator<Map.Entry<Integer, TrackPail>> iter = mDepot.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<Integer, TrackPail> entry = iter.next();
					int size = entry.getKey();
					if (size >= aSize) {
						result = entry.getValue();
						iter.remove();
						break;
					}
				}
			} catch (Exception ex) {}

			if (result == null) {
				//create new
				result = new TrackPail();
				result.bufInfo = new MediaCodec.BufferInfo();
				result.bufData = ByteBuffer.allocateDirect(aSize * 2);
			}
		}
		result.bufData.clear();
		return result;
	}


	public void feedDepot(TrackPail aPail) {
		synchronized (mLock) {
			Integer key = aPail.bufData.capacity();
			mDepot.put(key, aPail);
		}
	}



	public boolean pushQueue(TrackPail aPail) {
		return mQueue.offer(aPail);
	}


	public TrackPail pullQueue() {
		TrackPail result = null;
		try {
			result = mQueue.poll(1, TimeUnit.MICROSECONDS);
		} catch (Exception ex) {}
		return result;
	}


	public int getCount() {
		return mQueue.size();
	}


	public void printStats() {
		App.log(" * TrackPool-stats>");
		App.log("length: " + mDepot.size());
		Iterator<Map.Entry<Integer, TrackPail>> iter = mDepot.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<Integer, TrackPail> entry = iter.next();
			App.log("bybu size: " + entry.getKey());
		}

		App.log("</stats>");
	}


}
