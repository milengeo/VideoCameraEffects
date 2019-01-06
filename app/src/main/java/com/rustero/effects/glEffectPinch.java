
package com.rustero.effects;


import android.opengl.GLES20;

import com.rustero.egl.glCore;


public class glEffectPinch extends glEffect {


	private float mResoX;
	private int mResoXLoc = -1;
	private float mResoY;
	private int mResoYLoc = -1;


	protected String getFragmentCode() {
		String result =
			"precision mediump float;\n" +
			"varying vec2 vTextureCoord;" +
			"uniform float uResoX;" +
			"uniform float uResoY;" +
			"void main() {" +
			"   vec2 position = vTextureCoord;" +
			"	float radius = 0.35;" +
			"	float scale = 0.5;" +
			"	vec2 center = vec2(0.5, 0.5);" +
			"   vec2 resolution = vec2(uResoX, uResoY);" +
			"	float aspectRatio = resolution.x / resolution.y;" +
			"	highp vec2 texturePos = vec2(position.x, (position.y * aspectRatio + 0.5 - 0.5 * aspectRatio));" +

			"	highp float dist = distance(center, texturePos);" +
			"	texturePos = position;" +

			"	lowp float isInBulge = step(dist, radius); // radius >= dist\n" +
			"	texturePos -= center * isInBulge;" +
			"	highp float percent = 1.0 + ((radius - dist) / radius) * scale;" +

			"	percent = percent * percent;" +
			"	texturePos = (texturePos * (1.0 - isInBulge))   +   (texturePos * percent * isInBulge);" +
			"   texturePos += center * isInBulge;" +

			"   gl_FragColor = texture2D(sTexture, texturePos);" +
       	"}";
		return result;
	}



    public glEffectPinch() {
        super();
		name = NAME_Pinch;
	}



	protected void resizeCustom() {
		mResoX = 1.0f * mSize.x;
		mResoY = 1.0f * mSize.y;
	}



	protected void compileCustom() {
		mResoXLoc = GLES20.glGetUniformLocation(mProgram, "uResoX");
		if (mResoXLoc >= 0)
			checkLocation(mResoXLoc, "uResoX");
		mResoYLoc = GLES20.glGetUniformLocation(mProgram, "uResoY");
		if (mResoYLoc >= 0)
			checkLocation(mResoYLoc, "uResoY");
	}



	protected void drawCustom() {
		if (mResoXLoc >= 0) {
			GLES20.glUniform1f(mResoXLoc, mResoX);
			glCore.checkGlError("glUniform1f");
		}
		if (mResoYLoc >= 0) {
			GLES20.glUniform1f(mResoYLoc, mResoY);
			glCore.checkGlError("glUniform1f");
		}
	}
}
