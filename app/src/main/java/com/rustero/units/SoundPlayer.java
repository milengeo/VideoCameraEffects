package com.rustero.units;


import android.content.Context;
import android.annotation.TargetApi;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import java.util.Map;
import java.util.TreeMap;

public class SoundPlayer {

	private static SoundPlayer self;
	private SoundPool mPool;
	private Map<Integer, Integer> mSounds;



	public static SoundPlayer get() {
		return self;
	}


	public static void create() {
		if (null != self) return;
		self = new SoundPlayer();
	}


	public static void delete() {
		if (null == self) return;
		self = null;
	}


	private SoundPlayer() {
		mSounds = new TreeMap<>();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			createNewSoundPool();
		} else {
			createOldSoundPool();
		}
	}



	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	protected void createNewSoundPool(){
		AudioAttributes attributes = new AudioAttributes.Builder()
				.setUsage(AudioAttributes.USAGE_GAME)
				.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
				.build();
		mPool = new SoundPool.Builder()
				.setAudioAttributes(attributes)
				.build();
	}

	@SuppressWarnings("deprecation")
	protected void createOldSoundPool(){
		mPool = new SoundPool(5, AudioManager.STREAM_MUSIC,0);
	}



	public void addSound(Context aContext, int aResId) {
		int soundId = mPool.load(aContext, aResId, 1);
		mSounds.put(aResId, soundId);
	}


	public void playSound(int aResId) {
		Integer si = mSounds.get(aResId);
		if (null == si) return;
		mPool.play(si, 1, 1, 0, 0, 1);
	}
}
