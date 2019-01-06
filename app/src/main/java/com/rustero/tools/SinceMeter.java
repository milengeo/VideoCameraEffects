package com.rustero.tools;



public class SinceMeter {
	private long mKick;


	public void reset() {
		mKick = 0;
	}


	public long since() {
		if (0 == mKick) {
			mKick = System.currentTimeMillis();
		}
		long result = System.currentTimeMillis() - mKick;
		return result;
	}


	public void click() {
		mKick = System.currentTimeMillis();
	}

}
