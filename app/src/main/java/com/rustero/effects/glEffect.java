package com.rustero.effects;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.rustero.App;
import com.rustero.egl.glCore;
import com.rustero.egl.glScene;
import com.rustero.tools.Size2D;

import java.util.ArrayList;
import java.util.List;


public class


glEffect extends glScene {

	public static final String NAME_HorizontalMirror = "Horizontal Mirror";
	public static final String NAME_VerticalMirror = "Vertical Mirror";
	public static final String NAME_Swirl = "Swirl";
	public static final String NAME_Bulge = "Bulge";
	public static final String NAME_Pinch = "Pinch";

	public static final String NAME_Toon = "Toon";
	public static final String NAME_Posterize = "Posterize";
	public static final String NAME_Pixelation = "Pixelation";
	public static final String NAME_InvertColors = "Invert colors";
	public static final String NAME_GlowingEdges = "Glowing edges";
	public static final String NAME_BlackAndWhite = "Black&White";
    public static final String NAME_Sepia = "Sepia";
    public static final String NAME_Sharpen = "Sharpen";
    public static final String NAME_EdgeDetection = "Edge detection";
    public static final String NAME_Embossing = "Embossing";


	public String name = "";
	public Size2D mSize = new Size2D(1, 1);
	protected float mTurn90 = 0.0f;
	protected int mTurn90Loc=-1;



	public static List<String> getEffects() {
		List<String> result = new ArrayList<>();
		result.add(NAME_HorizontalMirror);
		result.add(NAME_VerticalMirror);
		result.add(NAME_Swirl);
		result.add(NAME_Bulge);
		result.add(NAME_Pinch);

		result.add(NAME_Toon);
		result.add(NAME_Posterize);
		result.add(NAME_Pixelation);
		result.add(NAME_InvertColors);
		result.add(NAME_GlowingEdges);
		result.add(NAME_BlackAndWhite);
		result.add(NAME_Sepia);
		result.add(NAME_Sharpen);
		result.add(NAME_EdgeDetection);
		result.add(NAME_Embossing);
		return  result;
	}



	public static glEffect createEffect(String aName) {
		glEffect effect = null;
		switch (aName) {

			case glEffect.NAME_HorizontalMirror: effect = new glEffectHorMirror(); break;
			case glEffect.NAME_VerticalMirror: effect = new glEffectVerMirror(); break;
			case glEffect.NAME_Swirl: effect = new glEffectSwirl();	break;
			case glEffect.NAME_Bulge: effect = new glEffectBulge(); break;
			case glEffect.NAME_Pinch: effect = new glEffectPinch();	break;

			case glEffect.NAME_Toon: effect = new glEffectToon(); break;
			case glEffect.NAME_Posterize: effect = new glEffectPosterize(); break;
			case glEffect.NAME_Pixelation: effect = new glEffectPixelation(); break;
			case glEffect.NAME_InvertColors: effect = new glEffectInvertColors(); break;
			case glEffect.NAME_GlowingEdges: effect = new glEffectGlowingEdges(); break;
			case glEffect.NAME_BlackAndWhite: effect = new glEffectBlackAndWhite();	break;
			case glEffect.NAME_Sepia: effect = new glEffectSepia();	break;
			case glEffect.NAME_Sharpen:
				effect = new glEffectSharpen();
				break;
			case glEffect.NAME_EdgeDetection:
				effect = new glEffectEdgeDetection();
				break;
			case glEffect.NAME_Embossing:
				effect = new glEffectEmbossing();
				break;

			default:
				App.log("no filter");
		}
		return effect;
	}




    public glEffect() {
        super(false);
        mVertexBuffer = FULL_RECTANGLE_BUF;
        mVertexCount = 4;
        mCoordsPerVertex = 2;
        mVertexStride = 8;
        mTexBuffer = FULL_RECTANGLE_TEX_BUF;
        mTexStride = 8;

        texMatrix = new float[16];
        Matrix.setIdentityM(texMatrix, 0);

        mvpMatrix = new float[16];
        Matrix.setIdentityM(mvpMatrix, 0);
    }



    public void release() {
        App.log("release");
        if (mProgram > 0) {
            GLES20.glDeleteProgram(mProgram);
            mProgram = 0;
        }
    }



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
        if (program == 0) {
            App.log("Could not create program");
        }
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
        if (mProgram == 0) {
            throw new RuntimeException("Unable to create program");
        }
        App.log("Created program " + mProgram + " (" + mProgram + ")");

        // get locations of attributes and uniforms
        mPositionLoc = GLES20.glGetAttribLocation(mProgram, "aPosition");
        checkLocation(mPositionLoc, "aPosition");

        mTextureCoordLoc = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        checkLocation(mTextureCoordLoc, "aTextureCoord");

        mMVPMatrixLoc = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        checkLocation(mMVPMatrixLoc, "uMVPMatrix");

        mTexMatrixLoc = GLES20.glGetUniformLocation(mProgram, "uTexMatrix");
        checkLocation(mTexMatrixLoc, "uTexMatrix");

		compileCustom();
    }



	//customize in descendents
	protected void compileCustom() {
	}



    // Sets the size of the texture.  This is used to find adjacent texels when filtering.
    public void resize(Size2D aSize) {
		mSize = new Size2D(aSize);
		resizeCustom();
    }


	//customize in descendents
	protected void resizeCustom() {
	}




    // Draws a viewport-filling rect, texturing it with the specified texture object.
    public void draw() {
        //set the target
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, outputStage.getFrambufId());

        // set the source
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, sourceStage.getTextureId());

        // Select the program.
        GLES20.glUseProgram(mProgram);
        glCore.checkGlError("glUseProgram");

        // Copy the model / view / projection matrix over.
        GLES20.glUniformMatrix4fv(mMVPMatrixLoc, 1, false, mvpMatrix, 0);
        glCore.checkGlError("glUniformMatrix4fv");

        // Copy the texture transformation matrix over.
        GLES20.glUniformMatrix4fv(mTexMatrixLoc, 1, false, texMatrix, 0);
        glCore.checkGlError("glUniformMatrix4fv");

        // Enable the "aPosition" vertex attribute.
        GLES20.glEnableVertexAttribArray(mPositionLoc);
        glCore.checkGlError("glEnableVertexAttribArray");

        // Connect VertexBuffer to "aPosition".
        GLES20.glVertexAttribPointer(mPositionLoc, mCoordsPerVertex, GLES20.GL_FLOAT, false, mVertexStride, mVertexBuffer);
        glCore.checkGlError("glVertexAttribPointer");

        // Enable the "aTextureCoord" vertex attribute.
        GLES20.glEnableVertexAttribArray(mTextureCoordLoc);
        glCore.checkGlError("glEnableVertexAttribArray");

        // Connect mTexBuffer to "aTextureCoord".
        GLES20.glVertexAttribPointer(mTextureCoordLoc, 2, GLES20.GL_FLOAT, false, mTexStride, mTexBuffer);
        glCore.checkGlError("glVertexAttribPointer");

		drawCustom();

        // Draw the rect.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertexCount);
        glCore.checkGlError("glDrawArrays");

        // Done -- disable vertex array, texture, and program.
        GLES20.glDisableVertexAttribArray(mPositionLoc);
        GLES20.glDisableVertexAttribArray(mTextureCoordLoc);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        GLES20.glUseProgram(0);
        glCore.checkGlError("glEffect-draw-finish");
    }



	//customize in descendents
	protected void drawCustom() {
	}


	public void setRotation(int aRotation) {
		if (Math.abs(aRotation) == 90)
			mTurn90 = 1.0f;
		else
			mTurn90 = 0f;
	}


}
