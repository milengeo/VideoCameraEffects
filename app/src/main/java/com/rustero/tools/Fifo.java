package com.rustero.tools;


import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Fifo<E> {

	ArrayBlockingQueue<E> mQueue;


	public Fifo(int aSize) {
		mQueue = new ArrayBlockingQueue<E>(aSize);
	}


	public boolean push(E item) {
		return mQueue.offer(item);
	}


	public E pull() {
		E result = null;
		try {
			result = mQueue.poll(1, TimeUnit.MICROSECONDS);
		} catch (Exception ex) {};
		return result;
	}


	public E peek() {
		return mQueue.peek();
	}


	public int getCount() {
		return mQueue.size();
	}

}
