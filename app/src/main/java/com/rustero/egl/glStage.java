package com.rustero.egl;


import android.opengl.GLES20;

import com.rustero.tools.Size2D;

import static android.opengl.GLES20.GL_COLOR_ATTACHMENT0;


/**
 * Framebuffer object with a texture
 */


public class glStage {

    public String tag;
    private int[] fraarr = new int[1];
    private int[] texarr = new int[1];
    public Size2D mSize = new Size2D();



    public static void delete(glStage aStage) {
        if (null == aStage) return;
        aStage.release();
    }



    public int getFrambufId() {
        return fraarr[0];
    }



    public int getTextureId() {
        return texarr[0];
    }



    public glStage(Size2D aSize) {
        mSize = new Size2D(aSize);

        GLES20.glGenFramebuffers(1, fraarr, 0);
        GLES20.glGenTextures(1, texarr, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texarr[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mSize.x, mSize.y, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fraarr[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, texarr[0], 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }



    public void release() {
        GLES20.glDeleteFramebuffers(1, fraarr, 0);
        GLES20.glDeleteTextures(1, texarr, 0);
    }



//	public void clear() {
//		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, getFrambufId());
//		GLES20.glDepthMask(false);
//		GLES20.glClearColor(1, 0, 1, 0);
//		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
//		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
//	}

}

