
package com.rustero.effects;


import android.opengl.GLES20;

import com.rustero.egl.glCore;

public class glEffectToon extends glEffect {


    private float mResoX;
    private int mResoXLoc = -1;
    private float mResoY;
    private int mResoYLoc = -1;


	protected String getFragmentCode() {
		String result =
				"precision mediump float;\n" +
				"varying vec2 vTextureCoord;" +
				"void main() {" +
				"   vec4 color1 = texture2D(sTexture, vTextureCoord); " +
				"   vec4 color2 = color1; " +
				"	float intensity = dot(color1.rgb, vec3(0.299, 0.587, 0.114)); "+
				"	if (intensity > 0.75) " +
				"		color2 = vec4(1.0, 0.5, 0.5, 1.0); " +
				"	else if (intensity > 0.5) " +
				"		color2 = vec4(0.6, 0.3, 0.3, 1.0); " +
				"	else if (intensity > 0.25) " +
				"		color2 = vec4(0.4, 0.2, 0.2, 1.0); " +
				"	else " +
				"		color2 = vec4(0.2, 0.1, 0.1, 1.0); " +
//				"	gl_FragColor = color2 * color1; " +
				"	gl_FragColor = color2; " +

				"}";
		return result;
	}


    public glEffectToon() {
        super();
        name = NAME_Toon;
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
