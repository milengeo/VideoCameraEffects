
package com.rustero.effects;


import android.opengl.GLES20;


public class glEffectEmbossing extends glEffect {

	private static final int KERNEL_3 = 9;
	private float[] mKernel31;
	private int mKernel3Loc1 = -1;
	private float[] mGrid31;
	private int mGrid3Loc1;
	private float mColorAdjust1;
	private int muColorAdjustLoc1;


	protected String getFragmentCode() {
		String result =
				"precision mediump float;\n" +
				"#define KERNEL_SIZE " + KERNEL_3 + "\n" +
				"varying vec2 vTextureCoord;\n" +
				"uniform float uKernel3[KERNEL_SIZE];\n" +
				"uniform vec2 uGrid3[KERNEL_SIZE];\n" +
				"uniform float uColorAdjust;\n" +
				"void main() {\n" +
				"    int i = 0;\n" +
				"    vec4 sum = vec4(0.0);\n" +
				"    sum += texture2D(sTexture, vTextureCoord + uGrid3[0]) * uKernel3[0];\n" +
				"    sum += texture2D(sTexture, vTextureCoord + uGrid3[4]) * uKernel3[4];\n" +
				"    sum += texture2D(sTexture, vTextureCoord + uGrid3[8]) * uKernel3[8];\n" +
				"    sum += uColorAdjust;\n" +
				"    gl_FragColor = vec4(sum.r, sum.g, sum.b, 1.0);\n" +
				"}\n";
		return result;
	}



    public glEffectEmbossing() {
        super();
		name = NAME_Embossing;
		mKernel31 = new float[] {
            2f, 0f, 0f,
            0f, -1f, 0f,
            0f, 0f, -1f };
        mColorAdjust1 = 0.5f;
    }




    //customize in descendents
    protected void compileCustom() {
		mKernel3Loc1 = GLES20.glGetUniformLocation(mProgram, "uKernel3");
		if (mKernel3Loc1 >= 0)
			checkLocation(mKernel3Loc1, "uKernel3");

		mGrid3Loc1 = GLES20.glGetUniformLocation(mProgram, "uGrid3");
		if (mGrid3Loc1 >= 0)
			checkLocation(mGrid3Loc1, "uGrid3");

        muColorAdjustLoc1 = GLES20.glGetUniformLocation(mProgram, "uColorAdjust");
        if (muColorAdjustLoc1 >= 0)
            checkLocation(muColorAdjustLoc1, "uColorAdjust");
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
        if (muColorAdjustLoc1 >= 0)
            GLES20.glUniform1f(muColorAdjustLoc1, mColorAdjust1);
    }



}
