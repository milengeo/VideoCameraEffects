
package com.rustero.coders;


import android.hardware.Camera;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import com.rustero.App;

import java.io.File;
import java.nio.ByteBuffer;


public class Enmuxer {


	private final int TRACK_NUMBER = 2;
	private File mFile;
	private volatile int mTrackCount;
	private int mAudioTrack, mVideoTrack;
    private MediaMuxer mMuxer;



    synchronized public void attach(String aPath) {
        mTrackCount = 0;
        try {
            mMuxer = new MediaMuxer(aPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        }
        catch (Exception ex) {
            App.log(" * Tracker attach " + ex.getMessage());
        }
    }



    synchronized public void detach() {
            try {
                if (mMuxer != null) {
                    mTrackCount = 0;
                    mMuxer.stop();
                    mMuxer.release();
                    App.log("mMuxer.cease");
                    mMuxer = null;
                }
            } catch (Exception ex) {
                App.log(" *** Tracker detach " + ex.getMessage());
            }
    }



	public void setRotation(int aRotation) {
		mMuxer.setOrientationHint(aRotation);
	}



    synchronized public int addAudioTrack(MediaFormat aFormat) {
            try {
                if (mMuxer != null) {
                    if (aFormat != null)
                        mAudioTrack = mMuxer.addTrack(aFormat);
                    App.log(" * addAudioTrack " + mAudioTrack);
                    mTrackCount++;
                    if (mTrackCount == TRACK_NUMBER) {
                        mMuxer.start();
                    }

                }
            }
            catch (Exception ex) {
                App.log(" *** Tracker addAudioTrack " + ex.getMessage());
            }
		return mAudioTrack;
    }



    synchronized public int addVideoTrack(MediaFormat aFormat) {
            try {
                if (mMuxer != null) {
                    if (aFormat != null)
                        mVideoTrack = mMuxer.addTrack(aFormat);
                    App.log(" * addVideoTrack " + mVideoTrack);
                    mTrackCount++;
                    if (mTrackCount == TRACK_NUMBER) {
                        mMuxer.start();
                    }
                }
            }
            catch (Exception ex) {
                App.log("addVideoTrack " + ex.getMessage());
            }
		return mVideoTrack;
    }



    synchronized public boolean isStarted() {
		if (mMuxer == null) return false;
        return (mTrackCount == TRACK_NUMBER);
    }



    synchronized public boolean writeAudioSample(ByteBuffer aData, MediaCodec.BufferInfo aInfo) {
        if (!isStarted()) return true;
		boolean result = true;
            try {
                mMuxer.writeSampleData(mAudioTrack, aData, aInfo);
				//App.log("writeAudio " + aMics/1000);
            }
            catch (Exception ex) {
				result = false;
                App.log(" * EX writeAudio " + ex.getMessage());
            }
		return result;
    }



    synchronized public boolean writeVideoSample(ByteBuffer aData, MediaCodec.BufferInfo aInfo) {
		if (!isStarted()) return true;
		boolean result = true;
            try {
                mMuxer.writeSampleData(mVideoTrack, aData, aInfo);
				//App.log("writeVideo " + aMics/1000);
            }
            catch (Exception ex) {
				result = false;
                App.log("writeVideo " + ex.getMessage());
            }
		return result;
    }



}




