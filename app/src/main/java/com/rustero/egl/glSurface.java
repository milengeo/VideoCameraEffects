package com.rustero.egl;


import android.graphics.Bitmap;
import android.opengl.EGL14;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.view.Surface;

import com.rustero.App;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


/**
 * Wrapper class for EGL surfaces.
 * There can be multiple surfaces associated with a single context.
 */
public class glSurface {

    // EglCore object we're associated with.  It may be associated with multiple surfaces.
    protected glCore mCore;
    private EGLSurface mEglSurface = EGL14.EGL_NO_SURFACE;
    private int mWidth = -1;
    private int mHeight = -1;



    public glSurface(glCore eglCore, int width, int height) {
        mCore = eglCore;
        if (mEglSurface != EGL14.EGL_NO_SURFACE) {
            throw new IllegalStateException("surface already created");
        }
        mEglSurface = mCore.createOffscreenSurface(width, height);
        mWidth = width;
        mHeight = height;
    }



    public glSurface(glCore eglCore, Surface surface) {
        mCore = eglCore;
        if (mEglSurface != EGL14.EGL_NO_SURFACE) {
            throw new IllegalStateException("surface already created");
        }
        mEglSurface = mCore.createWindowSurface(surface);
    }



    // Releases any resources associated with the EGL surface (and, if configured to do so, with the Surface as well). Does not require that the surface's EGL context be current.
    public void release() {
        mCore.releaseSurface(mEglSurface);
        mEglSurface = EGL14.EGL_NO_SURFACE;
        mWidth = mHeight = -1;
    }



    public boolean isVisual() {
        return (mWidth == -1);
    }



    /**
     * Returns the surface x, in pixels.
     */
    public int getWidth() {
        if (mWidth < 0) {
            return mCore.querySurface(mEglSurface, EGL14.EGL_WIDTH);
        } else {
            return mWidth;
        }
    }



    /**
     * Returns the surface y, in pixels.
     */
    public int getHeight() {
        if (mHeight < 0) {
            return mCore.querySurface(mEglSurface, EGL14.EGL_HEIGHT);
        } else {
            return mHeight;
        }
    }



    /**
     * Makes our EGL context and surface current.
     */
    public void makeCurrent() {
        mCore.makeCurrent(mEglSurface);
    }



    /**
     * Makes our EGL context and surface current for drawing, using the supplied surface
     * for reading.
     */
    public void makeCurrentReadFrom(glSurface readSurface) {
        mCore.makeCurrent(mEglSurface, readSurface.mEglSurface);
    }



    /**
     * Calls eglSwapBuffers.  Use this to "publish" the current frame.
     *
     * @return false on failure
     */
    public boolean swapBuffers() {
        boolean result = mCore.swapBuffers(mEglSurface);
        if (!result) {
            App.log("WARNING: swapBuffers() failed");
        }
        return result;
    }



    /**
     * Sends the presentation time stamp to EGL.
     *
     * @param nsecs Timestamp, in nanoseconds.
     */
    public void setPresentationTime(long nsecs) {
        mCore.setPresentationTime(mEglSurface, nsecs);
    }



    /**
     * Saves the EGL surface to a file.
     * Expects that this object's EGL surface is current.
     */
    public void saveFrame(File file) throws IOException {
        if (!mCore.isCurrent(mEglSurface)) {
            throw new RuntimeException("Expected EGL context/surface is not current");
        }

        // glReadPixels fills in a "direct" ByteBuffer with what is essentially big-endian RGBA
        // bufData (i.e. a byte of red, followed by a byte of green...).  While the Bitmap
        // constructor that takes an int[] wants little-endian ARGB (blue/red swapped), the
        // Bitmap "copy pixels" method wants the same format GL provides.
        //
        // Ideally we'd have some way to re-use the ByteBuffer, especially if we're calling
        // here often.
        //
        // Making this even more interesting is the upside-down nature of GL, which means
        // our output will look upside down relative to what appears on screen if the
        // typical GL conventions are used.

        String filename = file.toString();

        int width = getWidth();
        int height = getHeight();
        ByteBuffer buf = ByteBuffer.allocateDirect(width * height * 4);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf);
        glCore.checkGlError("glReadPixels");
        buf.rewind();

        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(filename));
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bmp.copyPixelsFromBuffer(buf);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, bos);
            bmp.recycle();
        } finally {
            if (bos != null) bos.close();
        }
        App.log("Saved " + width + "x" + height + " frame as '" + filename + "'");
    }
}




