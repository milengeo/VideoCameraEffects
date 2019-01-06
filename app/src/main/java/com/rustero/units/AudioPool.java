package com.rustero.units;


import com.rustero.App;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;


public class AudioPool {


	public class AudioPail {
		public ByteBuffer data;
		public long stamp;
		public void set(int aSize) {
			data = ByteBuffer.allocateDirect(aSize);
		}
	}


	private Object mLock = new Object();
	private Map<Integer, AudioPail> mDepot;
	private ArrayBlockingQueue<AudioPail> mQueue;



	public AudioPool() {
		mQueue = new ArrayBlockingQueue<AudioPail>(99);
		mDepot =new TreeMap<>();
	}



	public AudioPail takeDepot(int aSize) {
		AudioPail result = null;
		synchronized (mLock) {
			try {
				Iterator<Map.Entry<Integer, AudioPail>> iter = mDepot.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<Integer, AudioPail> entry = iter.next();
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
				result = new AudioPail();
				result.data = ByteBuffer.allocateDirect(aSize * 2);
			}
		}
		result.data.clear();
		return result;
	}


	public void feedDepot(AudioPail aPail) {
		synchronized (mLock) {
			Integer key = aPail.data.capacity();
			mDepot.put(key, aPail);
		}
	}



	public boolean pushQueue(AudioPail aPail) {
		return mQueue.offer(aPail);
	}


	public AudioPail pullQueue() {
		AudioPail result = null;
		try {
			result = mQueue.poll(1, TimeUnit.MICROSECONDS);
		} catch (Exception ex) {}
		return result;
	}


	public int getCount() {
		return mQueue.size();
	}


	public void printStats() {
		App.log(" * AudioPool-stats>");
		App.log("length: " + mDepot.size());
		Iterator<Map.Entry<Integer, AudioPail>> iter = mDepot.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<Integer, AudioPail> entry = iter.next();
			App.log("bybu size: " + entry.getKey());
		}

		App.log("</stats>");
	}


}
