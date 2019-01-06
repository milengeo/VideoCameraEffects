package com.rustero.egl;


import android.opengl.GLES20;
import android.opengl.Matrix;

import com.rustero.App;

import java.nio.FloatBuffer;


/**
 * Erase the output texture wit asked color.
 */


public class glWiper {

    public String tag;


    // Simple vertex shader
    protected String getVertexCode() {
        String result =
                "uniform mat4 uMVPMatrix;\n" +
                "attribute vec4 aPosition;\n" +
                "void main() {\n" +
				"    gl_Position = uMVPMatrix * aPosition;\n" +
				"}\n";
        return result;
    }


    // Simple fragment shader
    protected String getFragmentCode() {
        String result =
                        "precision mediump float;\n" +
                        "void main() {\n" +
                        "    gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);\n" +
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

    protected String mVertexCode, mFragmentCode;
    protected FloatBuffer mVertexBuffer;
    protected int mVertexCount;
    protected int mCoordsPerVertex;
    protected int mVertexStride;

    // Handles to the GL program and various components of it.
    protected int mProgram;
    protected int mMVPMatrixLoc;
    protected int mPositionLoc;

    public glStage outputStage;
    public float[] mvpMatrix;


	public static glWiper create() {
		glWiper scene = new glWiper();
		scene.compile();
		return scene;
	}


    public static void delete(glWiper aScene) {
        if (null == aScene) return;
        aScene.release();
    }




    public glWiper() {
        mVertexBuffer = FULL_RECTANGLE_BUF;
        mVertexCount = 4;
        mCoordsPerVertex = 2;
        mVertexStride = 8;
        mvpMatrix = new float[16];
        Matrix.setIdentityM(mvpMatrix, 0);
    }




    public void release() {
        if (mProgram >= 0) {
            App.log( "deleting program " + mProgram);
            GLES20.glDeleteProgram(mProgram);
            mProgram = -1;
        }
    }


	public String toString() {
		return "glWiper: " + tag;
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
        mFragmentCode = "uniform sampler2D sTexture;\n" + mFragmentCode;

        mProgram = createProgram(mVertexCode, mFragmentCode);
        if (mProgram == 0) throw new RuntimeException("Unable to create program");

        // get locations of attributes and uniforms
        mPositionLoc = GLES20.glGetAttribLocation(mProgram, "aPosition");
        checkLocation(mPositionLoc, "aPosition");
        mMVPMatrixLoc = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        checkLocation(mMVPMatrixLoc, "uMVPMatrix");
    }



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

        // select the program
        GLES20.glUseProgram(mProgram);
        glCore.checkGlError("glUseProgram");

        // copy the model / surfView / projection mEncoderMatrix over
        GLES20.glUniformMatrix4fv(mMVPMatrixLoc, 1, false, mvpMatrix, 0);
        glCore.checkGlError("mMVPMatrixLoc");

        // enable the "aPosition" vertex attribute
        GLES20.glEnableVertexAttribArray(mPositionLoc);
        // Connect mVertexBuffer to "aPosition".
        GLES20.glVertexAttribPointer(mPositionLoc, mCoordsPerVertex, GLES20.GL_FLOAT, false, mVertexStride, mVertexBuffer);
        glCore.checkGlError("glVertexAttribPointer");

        // draw the rect
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertexCount);
        glCore.checkGlError("glDrawArrays");

        // done, disable vertex array, texture, and program
        GLES20.glDisableVertexAttribArray(mPositionLoc);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glUseProgram(0);
        glCore.checkGlError("glWiper-draw");
    }


}



