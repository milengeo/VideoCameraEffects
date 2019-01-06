
package com.rustero.tools;


import android.hardware.Camera;
import android.util.Log;

import com.rustero.App;

import java.util.List;

@SuppressWarnings("deprecation")




public class Caminf {

	private static final String LOG_TAG = "CamC";
	public SizeList resolutions = new SizeList();


	public Caminf() {
	}


	public static int findFrontCamera() {
		int result = -1;
		Camera.CameraInfo info = new Camera.CameraInfo();
		try {
			int numCameras = Camera.getNumberOfCameras();
			for (int i = 0; i < numCameras; i++) {
				Camera.getCameraInfo(i, info);
				if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
					result = i;
					break;
				}
			}
		} catch (Exception ex) {
			App.log(" *** ex Caminf_openFrontCamera" + ex.getMessage());
		}

		return result;
	}



	public static int findBackCamera() {
		int result = -1;
		Camera.CameraInfo info = new Camera.CameraInfo();
		try {
			int numCameras = Camera.getNumberOfCameras();
			for (int i = 0; i < numCameras; i++) {
				Camera.getCameraInfo(i, info);
				if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
					result = i;
					break;
				}
			}
		} catch (Exception ex) {
			App.log(" *** ex Caminf_openFrontCamera" + ex.getMessage());
		}

		return result;
	}



    public static SizeList getCameraSizes(Camera aCamera) {
        SizeList result = new SizeList();

		Size2D maxiSize = new Size2D(1920, 1080);
		Camera.Size prefSize = aCamera.getParameters().getPreferredPreviewSizeForVideo();
		if (prefSize != null) {
            App.log( "Camera preferred preview size for video is " + prefSize.width + "x" + prefSize.height);
			//maxiSize = new Size2D(prefSize.x, prefSize.y);
		}

        List<Camera.Size> casis = getSupportedSizes(aCamera.getParameters());
        for (Camera.Size casi : casis) {
            Log.i("VideoSize", "Supported Size: " + casi.width + "x" + casi.height);
			Size2D size2 = new Size2D(casi.width, casi.height);
			if (size2.isAbove(maxiSize.x, maxiSize.y)) {
				if (!result.hasSize(maxiSize))
					result.addSize(maxiSize);
				continue;
			}
            result.addSize(size2);
        }

        return result;
    }



    public static List<Camera.Size> getSupportedSizes(Camera.Parameters aPars) {
        List<Camera.Size> casis = aPars.getSupportedVideoSizes();
        if (null == casis)
            casis = aPars.getSupportedPreviewSizes();
        return casis;
    }



    /**
     * Attempts to find a preview size that matches the provided x and outHeight (which
     * specify the dimensions of the encoded video).  If it fails to find a match it just
     * uses the default preview size for video.
     */


//	public static Camera.Size findPreviewSize(Camera aCamera, int aWidth, int aHeight) {
//    public static Camera.Size findPreviewSize(Camera.Parameters aPars, int aWidth, int aHeight) {
//        List<Camera.Size> casis = getSupportedSizes(aPars);
//        for (Camera.Size size : casis) {
//            if (size.x <= aWidth && size.y <= aHeight) {
//                return size;
//            }
//        }
//        //Log.w(LOG_TAG, "Unable to set preview size to " + aWidth + "x" + aHeight);
//		Camera.Size preferredPreviewSize = aPars.getPreferredPreviewSizeForVideo();
//		return preferredPreviewSize;
//    }



//    public static void selectPreferredSize(Camera.Parameters aPars) {
//        Camera.Size size = aPars.getPreferredPreviewSizeForVideo();
//        if (size != null) {
//            App.log( "Camera preferred preview size for video is " + size.x + "x" + size.y);
//            aPars.setPreviewSize(size.x, size.y);
//        } else {
//            aPars.setPreviewSize(640, 480);
//        }
//
//    }



}
