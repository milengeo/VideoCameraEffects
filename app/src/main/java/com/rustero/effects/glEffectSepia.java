
package com.rustero.effects;


import android.opengl.GLES20;

import com.rustero.App;
import com.rustero.egl.glCore;


public class glEffectSepia extends glEffect {

	private float[] mColorMatrix1;
	private int mColorMatrixLoc1 = -1;
	private float mColorIntensity1;
	private int mColorIntensityLoc1 = -1;


	protected String getFragmentCode() {
//		String result =
//			"precision mediump float;\n" +
//            "varying vec2 vTextureCoord;" +
//            "uniform lowp mat4 uColorMatrix;" +
//            "uniform lowp float uColorIntensity;" +
//            "void main() {" +
//            "    lowp vec4 textureColor = texture2D(sTexture, vTextureCoord);" +
//            "    lowp vec4 outputColor = textureColor * uColorMatrix;" +
//            "    gl_FragColor = (uColorIntensity * outputColor) + ((1.0 - uColorIntensity) * textureColor);" +
//            "}";
		String result = App.readAssetFile("effect_shaders/sepia.glsl");
		return result;
	}



    public glEffectSepia() {
        super();
		name = NAME_Sepia;
        mColorIntensity1 = 1.0f;
        mColorMatrix1 = new float[]{
                0.3588f, 0.7044f, 0.1368f, 0.0f,
                0.2990f, 0.5870f, 0.1140f, 0.0f,
                0.2392f, 0.4696f, 0.0912f, 0.0f,
                0f, 0f, 0f, 1.0f
        };
    }



    //customize in descendents
    protected void compileCustom() {
        mColorMatrixLoc1 = GLES20.glGetUniformLocation(mProgram, "uColorMatrix");
        if (mColorMatrixLoc1 >= 0)
            checkLocation(mColorMatrixLoc1, "uColorMatrix");
        mColorIntensityLoc1 = GLES20.glGetUniformLocation(mProgram, "uColorIntensity");
        if (mColorIntensityLoc1 >= 0)
            checkLocation(mColorIntensityLoc1, "uColorIntensity");
    }



    protected void drawCustom() {
        // Populate the color matrix, if present.
        if (mColorMatrixLoc1 >= 0)
        {
            GLES20.glUniformMatrix4fv(mColorMatrixLoc1, 1, false, mColorMatrix1, 0);
            glCore.checkGlError("glUniformMatrix4fv");
        }
        if (mColorIntensityLoc1 >= 0)
        {
            GLES20.glUniform1f(mColorIntensityLoc1, mColorIntensity1);
            glCore.checkGlError("glUniform1f");
        }
    }


}
