package com.rustero.egl;


import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.rustero.tools.Size2D;


public class glStill {

    private int[] texarr = new int[1];
	public Size2D size = new Size2D();



	public static void delete(glStill aStill) {
		if (null == aStill) return;
		aStill.release();
	}



	public int getTextureId() {
        return texarr[0];
    }



    public glStill(Bitmap aBitmap) {
		Bitmap bitmap = flipBitmap(aBitmap);
		size = new Size2D(bitmap.getWidth(), bitmap.getHeight());

        GLES20.glGenTextures(1, texarr, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texarr[0]);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);

		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

//		int x = bitmap.getWidth();
//		int y = bitmap.getHeight();
//		int size = x*y*4;
//		ByteBuffer buffer = ByteBuffer.allocateDirect(size);
//		bitmap.copyPixelsToBuffer(buffer); //Move the byte bufData to the buffer
//		buffer.position(0);
//		GLES20.glTexImage2D ( GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, x, y, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }



    public void release() {
        GLES20.glDeleteTextures(1, texarr, 0);
    }



	public Bitmap flipBitmap(Bitmap src) {
		android.graphics.Matrix matrix = new android.graphics.Matrix();
		matrix.preScale(1.0f, -1.0f);
		return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
	}



}

