package com.rustero.egl;


import android.graphics.Bitmap;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.rustero.App;
import com.rustero.tools.Size2D;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


/**
 * Base class for stuff we like to draw.
 * This class essentially represents a viewport-sized sprite that will be rendered with
 * a texture, usually from an external source like the camera or video decoder.
 */


public class glScene {

    public String tag;
    private static final String LOG_TAG = "glScene";
    protected static final int SIZEOF_FLOAT = 4;


    // Simple vertex shader
    protected String getVertexCode() {
        String result =
                "uniform mat4 uMVPMatrix;\n" +
                        "uniform mat4 uTexMatrix;\n" +
                        "attribute vec4 aPosition;\n" +
                        "attribute vec4 aTextureCoord;\n" +
                        "varying mediump vec2 vTextureCoord;\n" +
                        "void main() {\n" +
                        "    gl_Position = uMVPMatrix * aPosition;\n" +
                        "    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n" +
                        "}\n";
        return result;
    }


    // Simple fragment shader
    protected String getFragmentCode() {
        String result =
                "precision mediump float;\n" +
                "varying mediump vec2 vTextureCoord;\n" +
                "void main() {\n" +
                "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                "}\n";
        return result;
    }



    // A "full" square, extending from -1 to +1 in both dimensions.  When the model/surfView/projection mEncoderMatrix is identity, this will exactly cover the viewport.
    // The texture coordinates are Y-inverted relative to RECTANGLE.  (This seems to work out right with external textures from SurfaceTexture.)
    protected static final float FULL_RECTANGLE_COORDS[] = {
       -1.0f,-1.0f,   // 0 bottom left
        1.0f,-1.0f,   // 1 bottom right
       -1.0f, 1.0f,   // 2 top left
        1.0f, 1.0f,   // 3 top right
    };
    protected static final FloatBuffer FULL_RECTANGLE_BUF = glCore.createFloatBuffer(FULL_RECTANGLE_COORDS);
    protected static final float FULL_RECTANGLE_TEX_COORDS[] = {
        0.0f, 0.0f,     // 0 bottom left
        1.0f, 0.0f,     // 1 bottom right
        0.0f, 1.0f,     // 2 top left
        1.0f, 1.0f      // 3 top right
    };
    protected static final FloatBuffer FULL_RECTANGLE_TEX_BUF = glCore.createFloatBuffer(FULL_RECTANGLE_TEX_COORDS);

    protected String mVertexCode, mFragmentCode;
    protected FloatBuffer mVertexBuffer;
    protected int mVertexCount;
    protected int mCoordsPerVertex;
    protected int mVertexStride;
    protected FloatBuffer mTexBuffer;
    protected int mTexStride;
    protected boolean mSaved;

    // Handles to the GL program and various components of it.
    protected boolean mExternal;
    protected int mProgram;
    protected int mMVPMatrixLoc;
    protected int mTexMatrixLoc;
    protected int mPositionLoc;
    protected int mTextureCoordLoc;

    public int sourceTexid = 0;
    public glStage sourceStage, outputStage;
    public float[] texMatrix;
    public float[] mvpMatrix;

	private Size2D mSourceSize = new Size2D();
	private Size2D mTargetSize = new Size2D();



	public static glScene create() {
		glScene scene = new glScene(false);
		scene.compile();
		return scene;
	}


    public static glScene create(boolean aExternal) {
        glScene scene = new glScene(aExternal);
        scene.compile();
        return scene;
    }


    public static void delete(glScene aScene) {
        if (null == aScene) return;
        aScene.release();
    }



    public glScene(boolean aExternal) {
        mExternal = aExternal;

        mVertexBuffer = FULL_RECTANGLE_BUF;
        mVertexCount = 4;
        mCoordsPerVertex = 2;
        mVertexStride = 8;
        mTexBuffer = FULL_RECTANGLE_TEX_BUF;
        mTexStride = 2 * SIZEOF_FLOAT;

        texMatrix = new float[16];
        Matrix.setIdentityM(texMatrix, 0);

        mvpMatrix = new float[16];
        Matrix.setIdentityM(mvpMatrix, 0);
    }




    /**
     * Releases resources.
     * This must be called with the appropriate EGL context current (i.e. the one that was
     * current when the constructor was called).  If we're about to destroy the EGL context,
     * there's no value in having the caller make it current just to do this cleanup, so you
     * can pass a flag that will tell this function to skip any EGL-context-specific cleanup.
     */
    public void release() {
        if (mProgram >= 0) {
            App.log( "deleting program " + mProgram);
            GLES20.glDeleteProgram(mProgram);
            mProgram = -1;
        }
    }


	public String toString() {
		return "glScene: " + tag;
	}



	// Creates a new program from the supplied vertex and fragment shaders.
    public int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        glCore.checkGlError("glCreateProgram");
        if (program == 0) App.log("Could not create program");
        GLES20.glAttachShader(program, vertexShader);
        glCore.checkGlError("glAttachShader");
        GLES20.glAttachShader(program, pixelShader);
        glCore.checkGlError("glAttachShader");
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            App.log("Could not link program: ");
            App.log(GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            program = 0;
        }
        return program;
    }


    // Compiles the provided shader source.
    protected int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        glCore.checkGlError("glCreateShader name=" + shaderType);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            App.log("Could not compile shader " + shaderType + ":");
            App.log(" " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }



    public void compile() {
        mVertexCode = getVertexCode();
        mFragmentCode = getFragmentCode();
        compileShaders();
    }



    protected void compileShaders() {
        if (mExternal) {
            mFragmentCode = "#extension GL_OES_EGL_image_external : require\n" +
                "uniform samplerExternalOES sTexture;\n" +
                mFragmentCode;
        } else {
            mFragmentCode = "uniform sampler2D sTexture;\n" +
                mFragmentCode;
        }

        mProgram = createProgram(mVertexCode, mFragmentCode);
        if (mProgram == 0) throw new RuntimeException("Unable to create program");

        // get locations of attributes and uniforms
        mPositionLoc = GLES20.glGetAttribLocation(mProgram, "aPosition");
        checkLocation(mPositionLoc, "aPosition");
        mTextureCoordLoc = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        checkLocation(mTextureCoordLoc, "aTextureCoord");
        mMVPMatrixLoc = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        checkLocation(mMVPMatrixLoc, "uMVPMatrix");
        mTexMatrixLoc = GLES20.glGetUniformLocation(mProgram, "uTexMatrix");
        checkLocation(mTexMatrixLoc, "uTexMatrix");
    }



    /**
     * Checks to see if the location we obtained is valid.  GLES returns -1 if a label
     * could not be found, but does not set the GL error.
     * Throws a RuntimeException if the location is invalid.
     */
    public static void checkLocation(int location, String label) {
        if (location < 0) {
            throw new RuntimeException("Unable to locate '" + label + "' in program");
        }
    }



    // Draws a viewport-filling rect, texturing it with the specified texture object.
    public void draw() {
        // set the target
        if (null == outputStage)
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        else
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, outputStage.getFrambufId());

        // set the source
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        if (mExternal)
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, sourceTexid);
        else
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, sourceTexid);

        // select the program
        GLES20.glUseProgram(mProgram);
        glCore.checkGlError("glUseProgram");

        // copy the texture transformation mEncoderMatrix over
        GLES20.glUniformMatrix4fv(mTexMatrixLoc, 1, false, texMatrix, 0);
        glCore.checkGlError("mTexMatrixLoc");

        // copy the model / surfView / projection mEncoderMatrix over
        GLES20.glUniformMatrix4fv(mMVPMatrixLoc, 1, false, mvpMatrix, 0);
        glCore.checkGlError("mMVPMatrixLoc");

        // enable the "aPosition" vertex attribute
        GLES20.glEnableVertexAttribArray(mPositionLoc);
        // Connect mVertexBuffer to "aPosition".
        GLES20.glVertexAttribPointer(mPositionLoc, mCoordsPerVertex, GLES20.GL_FLOAT, false, mVertexStride, mVertexBuffer);
        glCore.checkGlError("glVertexAttribPointer");

        // enable the "aTextureCoord" vertex attribute
        GLES20.glEnableVertexAttribArray(mTextureCoordLoc);
        // Connect mTexBuffer to "aTextureCoord".
        GLES20.glVertexAttribPointer(mTextureCoordLoc, 2, GLES20.GL_FLOAT, false, mTexStride, mTexBuffer);
        glCore.checkGlError("glVertexAttribPointer");

        // draw the rect
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertexCount);
        glCore.checkGlError("glDrawArrays");

        // done, disable vertex array, texture, and program
        GLES20.glDisableVertexAttribArray(mPositionLoc);
        GLES20.glDisableVertexAttribArray(mTextureCoordLoc);

        if (mExternal)
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        else
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        GLES20.glUseProgram(0);
        glCore.checkGlError("glScene-draw");
    }







    public void saveRgb(Size2D aSize, String aFileName) throws IOException {
        if (mSaved) return;
        mSaved = true;

        if (null == outputStage)
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        else
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, outputStage.getFrambufId());

        ByteBuffer buf = ByteBuffer.allocateDirect(aSize.x * aSize.y * 4);
        buf.order(ByteOrder.LITTLE_ENDIAN);

        GLES20.glFinish();
        GLES20.glReadPixels(0, 0, aSize.x, aSize.y, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf);
        glCore.get().checkGlError("glReadPixels");
        buf.rewind();

        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(aFileName));
            Bitmap bmp = Bitmap.createBitmap(aSize.x, aSize.y, Bitmap.Config.ARGB_8888);
            bmp.copyPixelsFromBuffer(buf);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, bos);
            bmp.recycle();
        } finally {
            if (bos != null) bos.close();
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        //App.log( "Saved " + x + "x" + y + " frame as '" + aFileName + "'");
    }



    public void saveYuv(Size2D aSize, String aFileName) throws IOException {
        if (mSaved) return;
        mSaved = true;

        if (null == outputStage)
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        else
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, outputStage.getFrambufId());

        ByteBuffer buf = ByteBuffer.allocateDirect(aSize.x * aSize.y * 4);
        buf.order(ByteOrder.LITTLE_ENDIAN);

        GLES20.glFinish();
        GLES20.glReadPixels(0, 0, aSize.x, aSize.y, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf);
        glCore.get().checkGlError("glReadPixels");
        buf.rewind();


        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(aFileName));
            Bitmap bmp = Bitmap.createBitmap(aSize.x, aSize.y, Bitmap.Config.ARGB_8888);
            bmp.copyPixelsFromBuffer(buf);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, bos);
            bmp.recycle();

        } finally {
            if (bos != null) bos.close();
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        //App.log( "Saved " + x + "x" + y + " frame as '" + aFileName + "'");
    }



	public void fillAspect(Size2D aSource, Size2D aTarget) {
        if (aSource.isZero()) return;
		if (aTarget.isZero()) return;

		mSourceSize = new Size2D(aSource);
		mTargetSize = new Size2D(aTarget);

        float wico = 1f;
        float heco = 1f;
        float wiRatio = (float) mTargetSize.x / mSourceSize.x;
        float heRatio = (float) mTargetSize.y / mSourceSize.y;

        if (heRatio < wiRatio) {
            // stretch by y
            heco = wiRatio / heRatio;
        } else {
            // stretch by x
            wico = heRatio / wiRatio;
        }

        Matrix.setIdentityM(mvpMatrix, 0);
        Matrix.scaleM(mvpMatrix, 0, wico, heco, 1f);
    }



	public void cropAspect(Size2D aSource, Size2D aTarget) {
		if (aSource.isZero()) return;
		if (aTarget.isZero()) return;

		mSourceSize = new Size2D(aSource);
		mTargetSize = new Size2D(aTarget);

		int drawWidth, drawHeight;
		float aspectRatio = (float) aSource.y / aSource.x;

		if (aTarget.y > (int) (aTarget.x * aspectRatio)) {
			// limited by narrow x; restrict y
			drawWidth = aTarget.x;
			drawHeight = (int) (aTarget.x * aspectRatio);
		} else {
			// limited by short y; restrict x
			drawWidth = (int) (aTarget.y / aspectRatio);
			drawHeight = aTarget.y;
		}

		float xs = (float) drawWidth / aTarget.x;
		float ys = (float) drawHeight / aTarget.y;

		Matrix.setIdentityM(mvpMatrix, 0);
		Matrix.scaleM(mvpMatrix, 0, xs, ys, 1f);
	}



    public void rotateUpscale(Size2D aSize, int aAngle) {
        if (0 == aAngle) return;
		float aspect = 1f * aSize.x / aSize.y;
		Matrix.scaleM(mvpMatrix, 0, 1f, aspect, 1f);
        Matrix.rotateM(mvpMatrix, 0, 1f*aAngle, 0f, 0f, 1f);
		Matrix.scaleM(mvpMatrix, 0, 1f, 1/aspect, 1f);
    }


    public void rotateDownscale(Size2D aSize, int aAngle) {
        if (0 == aAngle) return;
        float aspect = 1f * aSize.y / aSize.x;
        Matrix.scaleM(mvpMatrix, 0, aspect, 1f, 1f);
        Matrix.rotateM(mvpMatrix, 0, 1f*aAngle, 0f, 0f, 1f);
        Matrix.scaleM(mvpMatrix, 0, 1/aspect, 1f, 1f);
    }
}



