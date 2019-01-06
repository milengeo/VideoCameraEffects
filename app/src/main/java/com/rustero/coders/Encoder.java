package com.rustero.coders;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.rustero.R;
import com.rustero.App;
import com.rustero.Errors;
import com.rustero.effects.Effector;
import com.rustero.egl.glWiper;
import com.rustero.egl.glStill;
import com.rustero.effects.glEffect;
import com.rustero.egl.glCore;
import com.rustero.egl.glScene;
import com.rustero.egl.glStage;
import com.rustero.egl.glSurface;
import com.rustero.egl.glTexture;
import com.rustero.themes.ThemeC;
import com.rustero.themes.Themer;
import com.rustero.tools.Size2D;
import com.rustero.units.AudioPool;
import com.rustero.tools.Caminf;
import com.rustero.tools.Tools;
import com.rustero.units.FlashLight;
import com.rustero.units.TrackPool;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.microedition.khronos.opengles.GL10;


@SuppressWarnings("deprecation")




public class Encoder {

	public static final int ACTOR_ID 			= 0;
//	public static final int ACTOR_ID 			= R.raw.actor4;

//	//public static final int ASKED_HOR_THEME			= 0;
//	public static final int ASKED_HOR_THEME			= R.raw.birthday9h_1;
//
//	//public static final int ASKED_VER_THEME			= 0;
//	public static final int ASKED_VER_THEME			= R.raw.birthday9v_1;


	public static final int STATE_IDLE 			= 0;
	public static final int STATE_STARTING 		= 1;
	public static final int STATE_RUNNING 		= 2;
	public static final int STATE_RECORDING 	= 3;


	public interface Events {
		void onStateChanged();
		void onProgress();
		void onFault(final String aMessage);
	}


	public class StatusC {
		public void reset() {
			name = "";
			secs = 0; size = 0;	rate = 0;
		}
		public String name;
		public long secs, size;
		public int rate;
	}




	protected static Encoder self;
	protected volatile int mState = STATE_IDLE;
	protected volatile StatusC mStatus = new StatusC();
	private volatile int mRotation=0;




	public static Encoder get() {
		create();
		return self;
	}



	public static void create() {
		if (null != self) return;
		self = new Encoder();
	}



	public static void delete() {
		if (null == self) return;
		self = null;
	}




    public String lastFault;
    private static boolean RECORD_AUDIO = true;
	private volatile boolean mVideoReady, mAudioReady;
	private long mTrackKick, mTrackMils;

	private final int LOGO_SIZE = 36;
	private final int LOGO_COLOR = 0x66ffffff;

	private OrderFifo mOrderFifo = new OrderFifo();
	private Size2D mCameraSize = new Size2D();
    private int mMaxiZoom, mThisZoom, mZoomStep;
    private List<Integer> mZooms = null;

    private Events mEventer;
	private volatile boolean mAskGrid;

    private SurfaceHolder mSurfHolder;
	private Size2D mScreenSize = new Size2D();

    private Engine mEngine;
    private Enaudio mEnaudio;
    private Entrack mEntrack;

    private String mOutputName;
    private File mOutputFile;
    private int mBitrate = 5000000;
    private MediaCodec mVideoCodec;
	private List<String> mAskedEffects;
	private ThemeC mAskedTheme, mCurrentTheme;




	private Encoder()	{
//		if (ASKED_HOR_THEME > 0)
//			mAskedHorTheme = ASKED_HOR_THEME;
//		if (ASKED_VER_THEME > 0)
//			mAskedVerTheme = ASKED_VER_THEME;
	}



	public void attachEngine(Events aEventer) {
		if (mEventer != null) return;
		mEventer = aEventer;
		createEngine();
		setEffects(App.sMyEffects);
	}


	public void detachEngine() {
		cease();
		mEventer = null;
		deleteEngine();
	}



	public void attachScreen(SurfaceHolder aHolder) {
		mSurfHolder = aHolder;
	}


	public void detachScreen() {
		mSurfHolder = null;
	}



	public void changeScreen(int aWidth, int aHeight) {
		mScreenSize = new Size2D(aWidth, aHeight);
	}







	public void begin() {
		App.log( "begin");
		mStatus.reset();
		mState = STATE_STARTING;
		for (int i=0; i<9999; i++) {
			mOutputName = App.FILM_PREFIX + "_" + App.takeNameCount() + ".mp4";
			mOutputFile = new File(App.getOutputFolder(), mOutputName);
			if (!mOutputFile.exists())
				break;
		}
		mOrderFifo.push(Order.ORDER_BEGIN_RECORDING);
	}


	public void cease() {
		if (mState == STATE_IDLE) return;
		App.log("cease");
		mOrderFifo.push(Order.ORDER_CEASE_RECORDING);

		for (int i=0; i<11; i++) {
			Tools.delay(55);
			if (!Encoder.get().isRecording()) {
				break;
			}
		}
	}




	private void createEngine() {
		try {
			mOrderFifo.clear();
			mEngine = new Engine();
			mEngine.start();
		} catch (Exception ex) {
			App.log(" *** ex_Enfilm_attachEngine " + ex.getMessage());
		};
	}


	private void deleteEngine() {
		try {
			if (mEngine != null) {
				mEngine.finish();
				mEngine = null;
			}
		} catch (Exception ex) {
			App.log(" *** ex_Enfilm_attachEngine " + ex.getMessage());
		};
	}



	private boolean isCamera90() {
		if (90 == mRotation)
			return true;
		else
			return false;
	}






	public String getOutputName() {
		return mOutputName;
	}



	public boolean isRecording() {
		return (mState == STATE_RECORDING);
	}



	public float getZoom() {
		if (null == mZooms) return 1.0f;
		float result = 1.0f;
		if (mThisZoom < mZooms.size())
			result = mZooms.get(mThisZoom) / 100.0f;
		return result;
	}



	public void applyZoom() {
		if (null == mEngine) return;
		try {
			mEngine.mCampars.setZoom(mThisZoom);
			mEngine.mCamera.setParameters(mEngine.mCampars);
			App.log( "applyZoom: " + mThisZoom);
		} catch (Exception ex) {
			App.log( " *** ex_applyZoom error");
		}
	}



	public void incZoom() {
		mThisZoom += mZoomStep;
		if (mThisZoom > mMaxiZoom)
			mThisZoom = mMaxiZoom;
		applyZoom();
	}



	public void decZoom() {
		mThisZoom -= mZoomStep;
		if (mThisZoom < 0)
			mThisZoom = 0;
		applyZoom();
	}



	public boolean hasFlash() {
		if (null == mEngine) return false;
		return mEngine.mFlashModes.isAvailable();
	}



	public void turnFlash(boolean aOn) {
		if (null == mEngine) return;
		try {
			if (aOn)
				mEngine.mCampars.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
			else
				mEngine.mCampars.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			mEngine.mCamera.setParameters(mEngine.mCampars);
			App.log( "setFlashMode: " + aOn);
		} catch (Exception ex) {
			App.log( " *** ex_setFlashMode");
		}
	}



	public void setEffects(List<String> aList) {
		mAskedEffects = aList;
	}



	public void setTheme(ThemeC aTheme) {
		mAskedTheme = aTheme;
	}



	public void setGrid(boolean aEnabled) {
		mAskGrid = aEnabled;
	}



	public Size2D getCameraSize() {
		return mCameraSize;
	}



	public int getState() {
		return mState;
	}




	public void resetStatus() {
		mStatus.reset();
	}


	public StatusC getStatus() {
		return mStatus;
	}






    private void setLastFault(String aError) {
        lastFault = aError;
		cease();
        if (null != mEventer)
	        mEventer.onFault(lastFault);
    }



	private boolean isFaulted() {
		return ( (null != lastFault) && (!lastFault.isEmpty()) );
	}



	private int bitrateFromHeight(int aHeight) {
		int result;
		if (aHeight <= 240)
			result = 750;
		else if (aHeight <= 480)
			result = 1500;
		else if (aHeight <= 720)
			result = 3000;
		else if (aHeight <= 1080)
			result = 6000;
		else
			result = 9000;
		return result * 1000;
	}







	private class Engine extends Thread {
        private volatile boolean mQuit = false;
        private volatile boolean mImageReady = false;

		private glSurface mCameraGurface, mDisplayGurface, mCodecGurface;

        private long mStateMils;
        private glTexture mCameraTexture;
        private SurfaceTexture mCameraSurtex;  // receives the output from the camera preview

		private Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();
		private int mCameraId;
        private Camera mCamera;
        private Camera.Parameters mCampars;

		private FlashLight mFlashModes = new FlashLight();
        private MediaFormat mAskedVideoFormat, mCodecVideoFormat;
        private MediaCodec.BufferInfo bufferInfo;
        private ByteBuffer bufferData;

		private Size2D mLogoSpot = new Size2D();
		private Size2D mLogoSize = new Size2D();
		private Size2D mStageSize = new Size2D();
//		private glWiper mWiper;
        private glScene mCameraScene, mScreenScene, mCodecScene, mLogoScene, mStillScene;
        private glStage mStage0, mStage1, mSourceStage, mOutputStage;
		private glStill mLogoStill, mGridStill, mHorThemeStill, mVerThemeStill, mActorStill;







        public Engine() {
            bufferInfo = new MediaCodec.BufferInfo();
        }




		public void updateRotation() {
			int deviceRotation = App.getDeviceRotation();
			if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				mRotation = (mCameraInfo.orientation + deviceRotation) % 360;
				mRotation = (360 - mRotation) % 360;
			} else {
				mRotation = (mCameraInfo.orientation - deviceRotation + 360) % 360;
			}
			App.log( "updateRotation: " + mRotation);
		}





        private void attachEgl() {
			try {
				glCore.create();

				mCameraGurface = new glSurface(glCore.get(), 128, 72);
				mCameraGurface.makeCurrent();
				GLES20.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
				GLES20.glEnable(GL10.GL_BLEND);

//				mWiper = glWiper.create();
				mCameraScene = glScene.create(true);
				mScreenScene = glScene.create();
				mCodecScene = glScene.create();
				mLogoScene = glScene.create();
				mStillScene = glScene.create();

				mCameraTexture = glTexture.create(true);
				mCameraSurtex = new SurfaceTexture(mCameraTexture.id);
				mCameraSurtex.setOnFrameAvailableListener(new SurfaceTextureListener());

			} catch (Exception ex) {
				App.log(" ***_ex attachEgl: " + ex.getMessage());
			}
		}


		private void detachEgl() {
            if (mCameraSurtex != null) {
                mCameraSurtex.release();
                mCameraSurtex = null;
            }

			if (mCameraGurface != null) {
				mCameraGurface.release();
				mCameraGurface = null;
			}

//			glWiper.delete(mWiper);
			glScene.delete(mCameraScene);
			glScene.delete(mScreenScene);
			glScene.delete(mCodecScene);
			glScene.delete(mLogoScene);
			glScene.delete(mStillScene);

			glStage.delete(mStage0);
			glStage.delete(mStage1);

			glCore.delete();
        }




		private void attachCamera() {
			if (mCamera != null) {
				App.log( " * camera already initialized");
				throw new RuntimeException("camera already initialized");
			}

			Size2D askedSize = new Size2D(640, 480);
			String resstr = "";

			if (App.getPrefBln("now_front")) {
				resstr = App.getPrefStr("front_resolution");
				mCameraId = Caminf.findFrontCamera();
				if (mCameraId < 0)
					mCameraId = Caminf.findBackCamera();
			} else {
				resstr = App.getPrefStr("back_resolution");
				mCameraId = Caminf.findBackCamera();
				if (mCameraId < 0)
					mCameraId = Caminf.findFrontCamera();
			}

			if (mCameraId > -1)
				mCamera = Camera.open(mCameraId);
			if (mCamera == null) {
				App.log( " * Unable to open camera");
				setLastFault("Unable to open camera");
				return;
			}

			if (!resstr.isEmpty())
				askedSize = Size2D.parseText(resstr);
			Camera.getCameraInfo(mCameraId, mCameraInfo);
			mCampars = mCamera.getParameters();
			mFlashModes.setModes(mCampars.getSupportedFlashModes());
			mCampars.setFlashMode(mCampars.FLASH_MODE_OFF);
			mCampars.setRecordingHint(true);  // Give the camera a hint that we're recording video.  This can have a big impact on frame rate.

			mMaxiZoom = 1;
			if (mCampars.isZoomSupported()) {
				mMaxiZoom = mCampars.getMaxZoom();
				mZooms = mCampars.getZoomRatios();
			}
			mZoomStep = mZooms.size()/30;
			if (mZoomStep < 1) mZoomStep = 1;
			App.log( "MaxiZoom: " + mMaxiZoom + "  zoom step: " + mZoomStep);

			mCampars.setPreviewSize(askedSize.x, askedSize.y);
			boolean previewFailed = false;
			try {
				mCamera.setParameters(mCampars);
			} catch (Exception ex) {
				App.log( " * Error configuring desired preview size");
				previewFailed = true;
			}

			if (previewFailed) {
				previewFailed = false;
				mCampars.setPreviewSize(640, 480);
				try {
					mCamera.setParameters(mCampars);
				} catch (Exception ex) {
					App.log( " *** ex_Error configuring preview VGA size");
					previewFailed = true;
				}
			}

			if (previewFailed) {
				App.log( " * Error configuring camera");
				setLastFault("Error configuring camera");
				return;
			}

			Camera.Size cameraPreviewSize = mCampars.getPreviewSize();
			int fps = mCampars.getPreviewFrameRate();
			String previewFacts = cameraPreviewSize.width + "x" + cameraPreviewSize.height + " @" + (fps / 1.0f) + "fps";
			App.log("Camera config: " + previewFacts);

			mCameraSize = new Size2D(cameraPreviewSize.width, cameraPreviewSize.height);
			mBitrate = bitrateFromHeight(mCameraSize.y);
			try {
				mCamera.setPreviewTexture(mCameraSurtex);
			} catch (IOException ioe) {
				App.log(" ***_ex setPreviewTexture Error!");
				throw new RuntimeException(ioe);
			}

			mCampars.setFlashMode(mCampars.FLASH_MODE_AUTO);

			mCamera.startPreview();

			App.log("startPreview_99");
		}


		private void detachCamera() {
			if (mCamera != null) {
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
				App.log( "releaseCamera -- done");
			}
		}




		private void attachStills() {
			if (App.wantWatermark()) {
				Bitmap bitmap = createTextBitmap(App.getLote(), LOGO_SIZE, LOGO_COLOR, Color.TRANSPARENT);
				mLogoStill = new glStill(bitmap);
				bitmap.recycle();
			}

			if (ACTOR_ID > 0) {
				mActorStill = new glStill(App.resbmp(ACTOR_ID));
			}
		}



		private void detachStills() {
			glStill.delete(mGridStill);
			glStill.delete(mLogoStill);
		}





		private void attachCodec() {
            try {
                mAskedVideoFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, mCameraSize.x, mCameraSize.y);
                mAskedVideoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
                mAskedVideoFormat.setInteger(MediaFormat.KEY_BIT_RATE, mBitrate);
                mAskedVideoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
                mAskedVideoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

				if (!isFaulted()) {
					mVideoCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
					mVideoCodec.configure(mAskedVideoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
					Surface surface = mVideoCodec.createInputSurface();
					mCodecGurface = new glSurface(glCore.get(), surface);
					mVideoCodec.start();
				}

				if (!isFaulted()) {
					mEntrack = new Entrack();
					mEntrack.attach(mOutputFile);
				}

				if (!isFaulted()) {
					if (RECORD_AUDIO) {
						mEnaudio = new Enaudio();
						mEnaudio.mTracker = mEntrack;
						mEnaudio.start();
					} else {
						mEntrack.addAudioTrack(null);
					}
				}

				if (isFaulted())
					cease();

            } catch (Exception ex) {
                App.log(" ***_ex attachCodec: " + ex.getMessage());
            }
        }


        private void detachCodec() {
            if (mEnaudio != null) {
                mEnaudio.finish();
                mEnaudio = null;
            }

            try {
                if (mVideoCodec != null) {
                    mVideoCodec.stop();
                    mVideoCodec.release();
                    mVideoCodec = null;
                }
            } catch (Exception ex) {
                App.log("release-mVideoCodec: " + ex.getMessage());
            }

            try {
                if (mEntrack != null) {
                    mEntrack.detach();
                    mEntrack = null;
                }
            } catch (Exception ex) {
                App.log("release-mVideoCodec: " + ex.getMessage());
            }
        }




		private void attachDisplay() {
			if (null != mDisplayGurface) return;
			if (null == mSurfHolder) return;
			if (null == mSurfHolder.getSurfaceFrame()) return;
			if (mScreenSize.isZero()) return;
			try {
				Surface surface = mSurfHolder.getSurface();
				if (null == surface) return;

				updateRotation();
				mDisplayGurface = new glSurface(glCore.get(), surface);
				mDisplayGurface.makeCurrent();
				GLES20.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
				GLES20.glEnable(GL10.GL_BLEND);
			} catch (Exception ex) {
				App.log(" ***_ex attachCodec: " + ex.getMessage());
			}
		}


		private void detachDisplay() {
			if (mDisplayGurface != null) {
				mDisplayGurface.release();
				mDisplayGurface = null;
			}
		}




		private void createTheme() {
			mCurrentTheme = Themer.get().getTheme(mAskedTheme.name);
			if (mCurrentTheme == null) return;
			mHorThemeStill = new glStill(App.resbmp(mCurrentTheme.horizontalId));
			mVerThemeStill = new glStill(App.resbmp(mCurrentTheme.verticalId));
		}


		private void deleteTheme() {
			glStill.delete(mHorThemeStill);
			glStill.delete(mVerThemeStill);
			mCurrentTheme = null;
		}




		@Override
        public void run() {
            App.log("Engine starting");

			attachEgl();
			attachCamera();
			attachStills();
			attachDisplay();

			if (null != mEventer)
				mEventer.onStateChanged();

			mState = STATE_RUNNING;
            try {
                while (!mQuit) {
                    Thread.sleep(1);

					if (null == mSurfHolder) {
						if (null != mDisplayGurface)
							detachDisplay();
					} else {
						if (null == mDisplayGurface) {
							attachDisplay();
							Effector.get().updateRotation(mRotation);
						}
					}

					checkOrders();

                    if (mImageReady) {
                        mImageReady = false;
						mCameraGurface.makeCurrent();
						mCameraSurtex.updateTexImage();    // latch the next frame from the camera
						mCameraSurtex.getTransformMatrix(mCameraScene.texMatrix);
						resizeStages(mCameraSize);
						drawCamera();

						updateEffects();
						drawFilter();

						loadTheme();
						drawTheme();

						if (isRecording()) {
                            drawCodec();
                        }

						drawDisplay();
						checkGrid();
					}

                    if (isRecording()) {
                        pullCodec();

						long mils = mTrackMils - mTrackKick;
						if (mils - mStateMils > 999) {
							mStateMils = mils;
							mStatus.secs = (mils+500)/1000;
							mStatus.size = getTotalBytes();
							mStatus.rate = (int) (mStatus.size / (mStatus.secs+1));
							if (null != mEventer)
								mEventer.onProgress();
							//App.log("progress: " + mStatus.secs + ", A: " + mFoneAngle);
						}
                    }
                }
            } catch (Exception ex) {
                App.log( " ***_ex Engine_run: " + ex.getMessage());
            }

			Effector.get().detach();
			deleteTheme();
			detachDisplay();
			detachStills();
			detachCamera();
			detachEgl();
			mState = STATE_IDLE;
            App.log( "Engine quiting");
        }



		private void drawCamera() {
			try {
				mCameraGurface.makeCurrent();

				Matrix.setIdentityM(mCameraScene.mvpMatrix, 0);
				glCore.setViewport(mCameraSize);
				mCameraScene.sourceTexid = mCameraTexture.id;
				mCameraScene.outputStage = mStage0;
				mCameraScene.draw();

				mOutputStage = mStage0;
				mSourceStage = mStage1;

				if (mActorStill != null) {
					Matrix.setIdentityM(mStillScene.mvpMatrix, 0);
					mStillScene.sourceTexid = mActorStill.getTextureId();
					mStillScene.outputStage = mStage0;
					mStillScene.draw();
				}

				mCameraGurface.swapBuffers();
			} catch (Exception ex) {
				App.log( " ***_ex drawDisplay: " + ex.getMessage());
			}
		}



		private void drawFilter() {
			Effector.get().goFirst();
			while (true) {
				glEffect effect = Effector.get().getCurrent();
				if (null == effect) break;
				try {
					Matrix.setIdentityM(effect.mvpMatrix, 0);
					glCore.setViewport(mCameraSize);

					effect.sourceStage = mOutputStage;
					effect.outputStage = mSourceStage;
					effect.draw();
					mOutputStage = effect.outputStage;
					mSourceStage = effect.sourceStage;

				} catch (Exception ex) {
					App.log(" ***_ex drawDisplay: " + ex.getMessage());
				}
				Effector.get().goNext();
			}
		}



		private void loadTheme() {
			if (mAskedTheme == mCurrentTheme) return;
			try {
				if (null == mAskedTheme) {
					deleteTheme();
				} else if (null != mCurrentTheme  &&  mCurrentTheme.name.equals(mAskedTheme.name) ) {
					return;
				} else {
					// need to create a new theme
					deleteTheme();
					createTheme();
				}
			} catch (Exception ex) {
				App.log( " ***_ex drawTheme: " + ex.getMessage());
			}
		}


		private void drawTheme() {
			if (null == mCurrentTheme) return;
			try {
				Matrix.setIdentityM(mStillScene.mvpMatrix, 0);
				glCore.setViewport(mCameraSize);
				if (isCamera90() && mVerThemeStill != null) {
					mStillScene.sourceTexid = mVerThemeStill.getTextureId();
				} else if (!isCamera90() && mHorThemeStill != null) {
					mStillScene.sourceTexid = mHorThemeStill.getTextureId();
				} else {
					return;
				}

				mStillScene.outputStage = mOutputStage;
				mStillScene.draw();
			} catch (Exception ex) {
				App.log( " ***_ex drawTheme: " + ex.getMessage());
			}
		}





		private void drawCodec() {
			try {
				mCodecGurface.makeCurrent();
				mCodecScene.sourceTexid = mOutputStage.getTextureId();
				glCore.setViewport(mCameraSize);

				mCodecScene.draw();

				if (null != mLogoStill) {
					Matrix.setIdentityM(mLogoScene.mvpMatrix, 0);
					if (isCamera90()) {
						Size2D size = new Size2D(mLogoSize.y, mLogoSize.x);
						Size2D spot = new Size2D((mCameraSize.x-mLogoSize.y-1), (mCameraSize.y-mLogoSize.x)/2);
						glCore.setViewport(spot, size);
						Matrix.rotateM(mLogoScene.mvpMatrix, 0, 1f*mRotation, 0f, 0f, 1f);
					} else
						glCore.setViewport(mLogoSpot, mLogoSize);

					mLogoScene.sourceTexid = mLogoStill.getTextureId();
					mLogoScene.draw();
				}

				mCodecGurface.setPresentationTime(mCameraSurtex.getTimestamp());
				mCodecGurface.swapBuffers();
			} catch (Exception ex) {
				App.log( " ***_ex drawCodec: " + ex.getMessage());
			}
		}



		private void drawDisplay() {
			if (null == mDisplayGurface) return;
			try {
				mDisplayGurface.makeCurrent();
				Matrix.setIdentityM(mScreenScene.mvpMatrix, 0);

				if (isCamera90()) {
					mScreenScene.cropAspect(mCameraSize, mScreenSize);
					mScreenScene.rotateUpscale(mCameraSize, -mRotation);
					float screenAspect = 1f * mScreenSize.y / mScreenSize.x;
					Matrix.scaleM(mScreenScene.mvpMatrix, 0, screenAspect, screenAspect, 1f);
				} else {
					///mScreenScene.fillAspect(mCameraSize, mScreenSize);
					mScreenScene.cropAspect(mCameraSize, mScreenSize);
				}

				glCore.setViewport(mScreenSize);
				glCore.clearScreen(0,0,0);
				mScreenScene.sourceTexid = mOutputStage.getTextureId();
				mScreenScene.draw();

				if (mGridStill != null) {
					glCore.setViewport(mScreenSize);
					Matrix.setIdentityM(mStillScene.mvpMatrix, 0);
					mStillScene.sourceTexid = mGridStill.getTextureId();
					mStillScene.outputStage = null;
					mStillScene.draw();
				}

				mDisplayGurface.swapBuffers();
			} catch (Exception ex) {
				App.log( " ***_ex drawDisplay: " + ex.getMessage());
				//detachDispay();
			}
		}




		private void updateEffects() {
			if (mAskedEffects == null) return;

//			mAskedEffects.add(glEffect.NAME_BlackAndWhite);
//			mAskedEffects.add(glEffect.NAME_Bulge);

			Effector.get().updateEffects(mAskedEffects, mCameraSize);
			mAskedEffects = null;
			Effector.get().updateRotation(mRotation);
		}




		private void resizeStages(Size2D aSize) {
			if (mStageSize.equals(aSize)) return;
			if (aSize.isZero()) return;
			mStageSize = new Size2D(aSize);

			glStage.delete(mStage0);
			mStage0 = new glStage(mStageSize);
			mStage0.tag = "0";

			glStage.delete(mStage1);
			mStage1 = new glStage(mStageSize);
			mStage1.tag = "1";

			if (null != mLogoStill) {
				float scale = mCameraSize.y / 480f;
				int width = (int) (mLogoStill.size.x * scale);
				int height = (int) (mLogoStill.size.y * scale);
				mLogoSize = new Size2D(width, height);
				mLogoSpot = new Size2D((mCameraSize.x - width) / 2, 1);
			}
		}






		private void checkOrders() {
			Order order = mOrderFifo.pull();
			if (null == order) return;

			if (order.code == Order.ORDER_BEGIN_RECORDING) {
				if (!isRecording()) {
					// turn on
					if (null != mEventer)
						mEventer.onProgress();
					lastFault = "";
					mTrackKick = 0;
					mTrackMils = 0;
					mStateMils = 0;
					mVideoReady = false;
					mAudioReady = false;

					attachCodec();
					mState = STATE_RECORDING;
					if (null != mEventer)
						mEventer.onStateChanged();
				}

			} else if (order.code == Order.ORDER_CEASE_RECORDING) {
				if (isRecording()) {
					// turn off
					detachCodec();
					mState = STATE_RUNNING;
					if (null != mEventer)
						mEventer.onStateChanged();
				}
			}
		}



		private void checkGrid() {
			if (mAskGrid) {
				if (null == mGridStill  &&  null != mDisplayGurface) {
					Bitmap bitmap = createGridBitmap(mScreenSize);
					mGridStill = new glStill(bitmap);
					bitmap.recycle();
				}
			} else {
				if (null != mGridStill) {
					mGridStill.release();
					mGridStill = null;
				}
			}
		}



        private void finish() {
            App.log( "Engine finish 11");
            mQuit = true;
            try {
                join();
            } catch (InterruptedException ie) {}
            App.log( "Engine finish 99");
        }



        // Drains all pending output from the encoder
        private void pullCodec() {
            try {
                ByteBuffer[] outputBuffers = mVideoCodec.getOutputBuffers();
                int bufIdx = mVideoCodec.dequeueOutputBuffer(bufferInfo, 1000);
                if (bufIdx >= 0) {
                    bufferData = outputBuffers[bufIdx];
                    if (bufferData != null) {
                        bufferData.position(bufferInfo.offset);
                        bufferData.limit(bufferInfo.offset + bufferInfo.size);
                        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                            App.log( "MediaCodec.BUFFER_FLAG_CODEC_CONFIG");
                        } else {
                            mEntrack.writeVideoSample(bufferData, bufferInfo);
                        }
                        releaseBuffer(bufIdx);
                        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            App.log( "video encoder finished");
                        }
                    }

                } else if (bufIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
					mCodecVideoFormat = mVideoCodec.getOutputFormat();
					mEntrack.addVideoTrack(mCodecVideoFormat);
                    App.log( "mCodecVideoFormat: " + mCodecVideoFormat);
                }

            } catch (Exception ex) {
                App.log( " ***** Enfilm_pullCodec: " + ex.getMessage());
            }
        }



        public void releaseBuffer(int aBufferIndex) {
            mVideoCodec.releaseOutputBuffer(aBufferIndex, false);
            bufferInfo.size = 0;
        }





        public long getTotalBytes() {
            long result = mEntrack.mVideoBytes + mEntrack.mAudioBytes;
            return result;
        }



        private class SurfaceTextureListener implements SurfaceTexture.OnFrameAvailableListener {
            @Override   // SurfaceTexture.OnFrameAvailableListener; runs on arbitrary thread
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                mImageReady = true;
            }
        }


    }










    private class Enaudio extends Thread {

        private volatile boolean mQuit = false;
        private volatile boolean mDone = false;
		private static final String LOG_TAG = "AudioRecorder";


        // audio format settings
		private static final String MIME_TYPE_AUDIO = "audio/mp4a-latm";
		private static final int SAMPLE_RATE = 44100;
		private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
		private static final int BYTES_PER_FRAME = 1024; // AAC
		private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
		private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.DEFAULT;

		private static final int SLICE_BYTES = BYTES_PER_FRAME * 1;
		private static final long MICS_PER_FRAME = 1000000 * BYTES_PER_FRAME / (SAMPLE_RATE*2);

		private ByteBuffer mBuffer = ByteBuffer.allocateDirect(SLICE_BYTES);
        private MediaCodec mCodec;
        private MediaFormat mAudioFormat, mCodecAudioFormat;
        private AudioRecord mAudioRecorder;
        private MediaCodec.BufferInfo mBufInfo;
        private ByteBuffer bufData;
        private Entrack mTracker;
		private long mMikeEpoch, mMikeCount;
		private AudioPool mMikePool = new AudioPool();





        Enaudio() {
            mBufInfo = new MediaCodec.BufferInfo();
        }



        private void attach() {
            // prepare encoder
            try {
                mAudioFormat = new MediaFormat();
                mAudioFormat.setString(MediaFormat.KEY_MIME, MIME_TYPE_AUDIO);
                mAudioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
                mAudioFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, 44100);
                mAudioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
                mAudioFormat.setInteger(MediaFormat.KEY_BIT_RATE, 128 * 1024);
                mAudioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384);

                mCodec = MediaCodec.createEncoderByType(MIME_TYPE_AUDIO);
                mCodec.configure(mAudioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                mCodec.start();
            } catch (Exception ex) {
                Log.e(LOG_TAG, "ex: " + ex.getMessage());
            }

            // prepare recorder
            try {
                int iMinBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
                int bufferSize = iMinBufferSize * 16;

                // Ensure buffer is adequately sized for the AudioRecord object to initialize
                if (bufferSize < iMinBufferSize)
                    bufferSize = ((iMinBufferSize / BYTES_PER_FRAME) + 1) * BYTES_PER_FRAME * 2;

                mAudioRecorder = new AudioRecord(
                    AUDIO_SOURCE,   // source
                    SAMPLE_RATE,    // sample rate, hz
                    CHANNEL_CONFIG, // channels
                    AUDIO_FORMAT,   // audio format
                    bufferSize);   // buffer size (bytes)

                mAudioRecorder.startRecording();

				mMikeEpoch = 0;
				mMikeCount = 0;
            } catch (Exception ex) {
                Log.e(LOG_TAG, "ex: " + ex.getMessage());
            }
        }



        private void detach() {
            if (mCodec != null) {
                mCodec.stop();
                mCodec.release();
                mCodec = null;
            }

            if (mAudioRecorder != null) {
                mAudioRecorder.stop();
                mAudioRecorder.release();
                mAudioRecorder = null;
            }

			if (null != mMikePool)
				mMikePool.printStats();
        }



        @Override
        public void run() {
            App.log( "Enaudio_run_11");
			try {
				attach();
				while (true) {
                    if (mQuit) {
                        break;
                    }
                    Thread.sleep(1);
					takeSlice();
					pushFrame();
					pullChunk();
				}

				detach();
            } catch (Exception ex) {
                App.log( " *** Enaudio_run ex: " + ex.getMessage());
            }

            mDone = true;
            App.log( "Enaudio_run_99");
        }



        private void finish() {
            App.log( "autor_finish_11");
            mQuit = true;
            while (true) {
                if (mDone) {
                    App.log( "autor_quit_done");
                    break;
                }
                try {
                    Thread.sleep(22);
                } catch (InterruptedException ie) {
                }
            }
            App.log( "autor_finish_99");
        }




		public void takeSlice() {
			try {
				int sliceTotal = mAudioRecorder.read(mBuffer, SLICE_BYTES);
				if (sliceTotal != SLICE_BYTES) {
					App.log(String.format(Locale.getDefault(), "weird_audio_frame: %d", sliceTotal));
				}

				int sliceCount = 0;
				while (sliceCount < sliceTotal) {
					AudioPool.AudioPail pail = mMikePool.takeDepot(BYTES_PER_FRAME);
					mBuffer.position(sliceCount);
					mBuffer.limit(sliceCount + BYTES_PER_FRAME);
					deepCopy(mBuffer, pail.data);
					pail.data.limit(BYTES_PER_FRAME);
					sliceCount += BYTES_PER_FRAME;

					if (0 == mMikeCount) {
						mMikeEpoch = System.nanoTime() / 1000;
					}
					mMikeCount++;
					long mics = mMikeCount * 1000000 * BYTES_PER_FRAME / (SAMPLE_RATE * 2);
					pail.stamp = mMikeEpoch + mics;
					mMikePool.pushQueue(pail);
					//App.log(String.format(Locale.getDefault(), "takeSlice_frame: %d, %d", BYTES_PER_FRAME, pail.stamp / 1000));
				}
			} catch (Exception ex) {
				Log.e(LOG_TAG, " ***** pushFrame: " + ex.getMessage());
			}
		}




        public void pushFrame() {
            try {
				while (mMikePool.getCount() > 0) {
					ByteBuffer[] buffers = mCodec.getInputBuffers();
					int inputBufferIndex = mCodec.dequeueInputBuffer(1000);
					if (inputBufferIndex >= 0) {
						ByteBuffer inputBuffer = buffers[inputBufferIndex];
						inputBuffer.clear();

						AudioPool.AudioPail pail = mMikePool.pullQueue();
						deepCopy(pail.data, inputBuffer);
						mCodec.queueInputBuffer(inputBufferIndex, 0, pail.data.limit(), pail.stamp, 0);
						mMikePool.feedDepot(pail);
						//App.log(String.format(Locale.getDefault(), "pushFrame_frame: %d, %d", pail.bufData.limit(), pail.stamp / 1000));
					}
				}
            } catch (Exception ex) {
                Log.e(LOG_TAG, " ***_ex pushFrame: " + ex.getMessage());
            }
        }



        private void pullChunk() {
            if (null==mCodec) return;
            //App.log( "AudioMotor_pullChunk");
            ByteBuffer[] outputBuffers = mCodec.getOutputBuffers();
            try {
                int bufIndex = mCodec.dequeueOutputBuffer(mBufInfo, 0);
                if (bufIndex >= 0) {
                    bufData = outputBuffers[bufIndex];
                    if (bufData != null) {
                        bufData.position(mBufInfo.offset);
                        bufData.limit(mBufInfo.offset + mBufInfo.size);
                        if ((mBufInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                            App.log( "AudioCodec.BUFFER_FLAG_CODEC_CONFIG");
                        } else {
                            mTracker.writeAudioSample(bufData, mBufInfo);
							//App.log( "writeAudioSample");
                        }
                        mCodec.releaseOutputBuffer(bufIndex, false);
                        if ((mBufInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0)
                            App.log( "audio encoder finished");
                    }

                } else if (bufIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
					mCodecAudioFormat = mCodec.getOutputFormat();
					mTracker.addAudioTrack(mCodecAudioFormat);
					App.log( "mCodecAudioFormat: " + mCodecAudioFormat);
                }

            } catch (Exception ex) {
                Log.e(LOG_TAG, " ***_ex pullChunk: " + ex.getMessage());
            }
        }


    }








    public class Entrack extends Thread {

		private Object mWriteLock = new Object();
		private Enmuxer mEnmuxer;

        private volatile boolean mQuit = false;
        private volatile boolean mDone = false;
        private File mMp4File, mTmpFile;

		private int mVideoCount;
        private long mVideoBytes, mAudioBytes;
        private int mAudioTrack=-1, mVideoTrack=-1;
        private long mLastPull;
		private TrackPool mTrackPool = new TrackPool();



        public Entrack() {  }



        @Override
        public void run() {
            App.log( " * Tracker_run_11");
            try {
                while (true) {
                    Thread.sleep(1);
                    if (mQuit) {
                        App.log( "Tracker_need_quit");
                        break;
                    }

                    long span = System.currentTimeMillis() - mLastPull;
                    if (span > 5) {
                        mLastPull = System.currentTimeMillis();
                        pullQueue();
                    }
                }

            } catch (Exception ex) {
                App.log( " *** ex_Entrack_run: " + ex.getMessage());
            }
            mDone = true;
            App.log( "Tracker_run_99");
        }



        private void pullQueue() {
			while (mTrackPool.getCount() > 0) {
                long took = System.currentTimeMillis();
				TrackPool.TrackPail pail = mTrackPool.pullQueue();
				if (null != pail) {
					boolean result = false;

					synchronized (mWriteLock) {
						if (pail.trackIndex == mVideoTrack) {
							if (pail.bufInfo.flags > 0)
								App.log("pullQueue: sync frame - " + pail.bufInfo.presentationTimeUs/1000);
							result = mEnmuxer.writeVideoSample(pail.bufData, pail.bufInfo);
						} else if (pail.trackIndex == mAudioTrack) {
							result = mEnmuxer.writeAudioSample(pail.bufData, pail.bufInfo);
						}

						if (!result) {
							setLastFault(Errors.getText(Errors.WRITE_FILE));
						}
					}
				}
				mTrackPool.feedDepot(pail);
                took = System.currentTimeMillis() - took;
                if (took > 55) App.log( "### long writeSampleData " + took);
            }
            //App.log( "pullQueue " + "  audioCount:" + audioCount + "  videoCount: " + videoCount);
            //App.log( "  posi0: "+mPool.get(0).size() + "  posi1: "+mPool.get(1).size() + "  posi2: "+mPool.get(2).size() + "  posi3: " + mPool.get(3).size());
        }



		private void makeTempFile() {
			String path = mMp4File.getPath();
			int p = path.lastIndexOf('.');
			if (p < 1)
			    p = path.length();
			path = path.substring(0, p) + ".temp";
			mTmpFile = new File(path);
		}



        public void attach(File aFile) {
			mTrackPool = new TrackPool();
			mMp4File = aFile;
			makeTempFile();
            try {
				mEnmuxer = new Enmuxer();
				mEnmuxer.attach(mTmpFile.getPath());
				mEnmuxer.setRotation(mRotation);
            }
            catch (Exception ex) {
				App.log(" *** ex_Entrack_attach " + ex.getMessage());
            }
        }



        public void detach() {
			mQuit = true;
            try {
				sleep(99);
				pullQueue();

				if (mEnmuxer != null) {
					mEnmuxer.detach();

					if (isFaulted() || mVideoCount < 22)
						mTmpFile.delete();
					else
						mTmpFile.renameTo(mMp4File);

					if (null != mTrackPool)
						mTrackPool.printStats();
				}

            } catch (Exception ex) {
                App.log(" *** ex_Entrack_detach " + ex.getMessage());
            }
            App.log( "Tracker_detach_99");
        }



		private void tryToStart() {
			if (!mEnmuxer.isStarted()) return;
			start();  // thread
			App.log("tryToStart");
		}



        public void addVideoTrack(MediaFormat aFormat) {
			if (mEnmuxer == null) return;
			try {
				App.log("addVideoTrack_11");
				if (!isFaulted()) {
					synchronized (mWriteLock) {
						mVideoTrack = mEnmuxer.addVideoTrack(aFormat);
						if (mVideoTrack < 0) {
							setLastFault(Errors.getText(mVideoTrack));
						}
					}
				}

				if (!isFaulted()) {
					App.log("mVideoReady = true");
					mVideoReady = true;
					tryToStart();
				}

				//App.log( "addVideoTrack_99 " + aFormat);
			} catch (Exception ex) {
				App.log("addVideoTrack " + ex.getMessage());
			}
        }



		public void addAudioTrack(MediaFormat aFormat) {
			if (mEnmuxer == null) return;
			if (aFormat == null) return;
			try {
				App.log( " * addAudioTrack_11");

				if (!isFaulted()) {
					synchronized (mWriteLock) {
						mAudioTrack = mEnmuxer.addAudioTrack(aFormat);
						if (mAudioTrack < 0) {
							setLastFault(Errors.getText(mAudioTrack));
						}
					}
				}

				if (!isFaulted()) {
					App.log("mAudioReady = true");
					mAudioReady = true;
					tryToStart();
				}

				//App.log( "addAudioTrack_99 " + mAudioTrack);
			} catch (Exception ex) {
				App.log(" ***_ex Tracker addAudioTrack " + ex.getMessage());
			}
		}




        public void writeVideoSample(ByteBuffer aBufData, MediaCodec.BufferInfo aBufInfo) {
			if (!mVideoReady || !mAudioReady) return;

			if (0 == mTrackKick) {
				if ( (aBufInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) == 0) {
					return;  // wait for a sync frame
				}
			}

			queueForTrack(mVideoTrack, aBufData, aBufInfo);
			mVideoCount++;
			mVideoBytes += aBufInfo.size;

			mTrackMils = aBufInfo.presentationTimeUs / 1000;
			if (0 == mTrackKick) {
				mTrackKick = mTrackMils;
				App.log("  * Entrack_writeVideoSample: mTarkReady = true");
			}
        }



        public void writeAudioSample(ByteBuffer aBufData, MediaCodec.BufferInfo aBufInfo) {
			if (0 == mTrackKick) return;
			queueForTrack(mAudioTrack, aBufData, aBufInfo);
            mAudioBytes += aBufInfo.size;
        }



        private void queueForTrack(int aTrackIndex, ByteBuffer aBufData, MediaCodec.BufferInfo aBufInfo) {
            long took = System.currentTimeMillis();
			try {
				TrackPool.TrackPail pail = mTrackPool.takeDepot(aBufInfo.size);
				pail.trackIndex = aTrackIndex;
				pail.bufInfo.set(0, aBufInfo.size, aBufInfo.presentationTimeUs, aBufInfo.flags);
				aBufData.rewind();
				pail.bufData.clear();
				pail.bufData.put(aBufData);
				pail.bufData.flip();
				mTrackPool.pushQueue(pail);
            }
            catch (Exception ex) {
				App.log(" *** ex_queueForTrack " + ex.getMessage());
            }
            took = System.currentTimeMillis() - took;
            if (took > 11) App.log( "###*** long queueForTrack " + took);
        }

    }








	public Bitmap createTextBitmap(String aText, int aSize, int aColor, int aBackgr) {
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setTextSize(aSize);
		paint.setColor(aColor);
		paint.setTypeface(Typeface.create("Arial", Typeface.BOLD_ITALIC));

		Rect rect = new Rect();
		paint.getTextBounds(aText, 0, aText.length(), rect);
		int width = rect.width() + aSize;
		int height = rect.height() + aSize;
		int x = rect.left + aSize/2;
		int y = rect.height() + aSize/2;
		//App.log("  * " + String.format("x: %d,  y: %d,  x: %d, y: %d, left: %d, top: %d, right: %d, bottom: %d",
		//		x, y, x, y, rect.left, rect.top, rect.right, rect.bottom));

		width = Tools.nextPowerOf2(width);
		height = Tools.nextPowerOf2(height);
		final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bitmap.setDensity(Bitmap.DENSITY_NONE);
		bitmap.eraseColor(aBackgr);
		Canvas canvas = new Canvas(bitmap);
		canvas.setDensity(Bitmap.DENSITY_NONE);
		canvas.drawText(aText, x, y, paint);

		return bitmap;
	}



	public Bitmap createGridBitmap(Size2D aSize) {
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(Color.WHITE);
		paint.setStrokeWidth(1);
		paint.setAntiAlias(true);

		int width = Tools.nextPowerOf2(aSize.x);
		int height = Tools.nextPowerOf2(aSize.y);

		final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bitmap.setDensity(Bitmap.DENSITY_NONE);
		bitmap.eraseColor(Color.TRANSPARENT);
		Canvas canvas = new Canvas(bitmap);
		canvas.setDensity(Bitmap.DENSITY_NONE);

		float y0 = height/3;
		canvas.drawLine(0, y0, width, y0, paint);
		canvas.drawLine(0, 2*height/3, width, 2*height/3, paint);
		canvas.drawLine(width/3, 0, width/3, height, paint);
		canvas.drawLine(2*width/3, 0, 2*width/3, height, paint);

		return bitmap;
	}



	public class Order {
		private static final int ORDER_BEGIN_RECORDING 		= 1;
		private static final int ORDER_CEASE_RECORDING 		= 2;
		private int code;
	}



	public class OrderFifo {

		ArrayBlockingQueue<Order> mQueue;

		public OrderFifo() {
			mQueue = new ArrayBlockingQueue<Order>(9);
		}



		public boolean push(int aCode) {
			Order order = new Order();
			order.code = aCode;
			return mQueue.offer(order);
		}


		public Order pull() {
			Order result = null;
			try {
				result = mQueue.poll(1, TimeUnit.MICROSECONDS);
			} catch (Exception ex) {};
			return result;
		}


		public void clear() {
			mQueue.clear();
		}

	}



	private void deepCopy(ByteBuffer aSource, ByteBuffer aTarget) {
		aTarget.clear();
		aTarget.put(aSource);
		aTarget.flip();
		aSource.flip();
	}




}

