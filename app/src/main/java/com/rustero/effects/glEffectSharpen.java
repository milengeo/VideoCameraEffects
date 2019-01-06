
package com.rustero.effects;


import android.opengl.GLES20;


public class glEffectSharpen extends glEffect {

    private static final int KERNEL_3 = 9;
    private float[] mKernel31;
    private int mKernel3Loc1 = -1;
    private float[] mGrid31;
    private int mGrid3Loc1;



	protected String getFragmentCode() {
		String result =
			"precision mediump float;\n" +
            "#define KERNEL_SIZE " + KERNEL_3 + "\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform float uKernel3[KERNEL_SIZE];\n" +
            "uniform lowp vec2 uGrid3[KERNEL_SIZE];\n" +
            "void main() {\n" +
            "    vec4 sum = vec4(0.0);\n" +

            "        sum += texture2D(sTexture, vTextureCoord + uGrid3[1]) * uKernel3[1];\n" +
            "        sum += texture2D(sTexture, vTextureCoord + uGrid3[3]) * uKernel3[3];\n" +
            "        sum += texture2D(sTexture, vTextureCoord + uGrid3[4]) * uKernel3[4];\n" +
            "        sum += texture2D(sTexture, vTextureCoord + uGrid3[5]) * uKernel3[5];\n" +
            "        sum += texture2D(sTexture, vTextureCoord + uGrid3[7]) * uKernel3[7];\n" +

            "    gl_FragColor = sum;\n" +
            "}\n";
		return result;
	}



    public glEffectSharpen() {
        super();
		name = NAME_Sharpen;
		mKernel31 = new float[] {
                0f, -1f, 0f,
                -1f, 5f, -1f,
                0f, -1f, 0f
        };
    }



	protected void compileCustom() {
		mKernel3Loc1 = GLES20.glGetUniformLocation(mProgram, "uKernel3");
		if (mKernel3Loc1 >= 0)
			checkLocation(mKernel3Loc1, "uKernel3");

		mGrid3Loc1 = GLES20.glGetUniformLocation(mProgram, "uGrid3");
		if (mGrid3Loc1 >= 0)
			checkLocation(mGrid3Loc1, "uGrid3");
	}



	public void resizeCustom() {
		float rw = 1.0f / mSize.x;
		float rh = 1.0f / mSize.y;
		mGrid31 = new float[]{
				-rw, -rh, 0f, -rh, rw, -rh,
				-rw, 0f, 0f, 0f, rw, 0f,
				-rw, rh, 0f, rh, rw, rh
		};
	}



	//customize in descendents
	protected void drawCustom() {
		// Populate the convolution kernel, if present.
		if (mKernel3Loc1 >= 0)
			GLES20.glUniform1fv(mKernel3Loc1, KERNEL_3, mKernel31, 0);
		if (mGrid3Loc1 >= 0)
			GLES20.glUniform2fv(mGrid3Loc1, KERNEL_3, mGrid31, 0);
	}



}
