package com.rustero.egl;


import android.opengl.Matrix;

public class glAnimator {

    public static final int NONE        = 1;
    public static final int SCALE_0_1   = 2;
    public static final int ROTATE_X    = 3;
    public static final int ROTATE_Y    = 4;
    public static final int ROTATE_Z    = 5;
    public static final int ROTATE_XYZ  = 6;

    public boolean active;
    private int mType;
    private float[] mMatrix = new float[16];
    private long mKick, mSpan;

    private final long LIVE_MILS = 400;
    private float mScale, mAngle;
    private boolean mReverse;


    private static glAnimator mAnimator = new glAnimator();


    private glAnimator() {}


    public static glAnimator get() {
        return mAnimator;
    }



    public void begin(int aType) {
        mType = aType;
        mKick = System.currentTimeMillis();
        mReverse = !mReverse;
        active = true;
    }



    public float[] take() {
        if (!active) return glCore.IDENTITY_MATRIX;
        mSpan = System.currentTimeMillis() - mKick;
        if (mSpan >= LIVE_MILS) active = false;
        if (!active) return glCore.IDENTITY_MATRIX;

        Matrix.setIdentityM(mMatrix, 0);
        mScale = (float) mSpan/LIVE_MILS;
        mAngle = 360 * mScale;
        if (mReverse) mAngle = 360 - mAngle;

        switch (mType) {
            case NONE:
                doNone();
                break;
            case SCALE_0_1:
                doScale_0_1();
                break;
            case ROTATE_X:
                doRotate_x();
                break;
            case ROTATE_Y:
                doRotate_y();
                break;
            case ROTATE_Z:
                doRotate_z();
                break;
            case ROTATE_XYZ:
                doRotate_xyz();
                break;
            default:
        }
        return mMatrix;
    }


    private void doNone() {   }



    private void doScale_0_1() {
        Matrix.scaleM(mMatrix, 0, mScale, mScale, 1.0f);
    }



    private void doRotate_x() {
        Matrix.setRotateM(mMatrix, 0, mAngle, 1, 0, 0);
        Matrix.scaleM(mMatrix, 0, mScale, mScale, 1.0f);
    }



    private void doRotate_y() {
        Matrix.setRotateM(mMatrix, 0, mAngle, 0, 1, 0);
        Matrix.scaleM(mMatrix, 0, mScale, mScale, 1.0f);
    }



    private void doRotate_z() {
        Matrix.setRotateM(mMatrix, 0, mAngle, 0, 0, 1);
        Matrix.scaleM(mMatrix, 0, mScale, mScale, 1.0f);
    }

    private void doRotate_xyz() {
        Matrix.setRotateM(mMatrix, 0, mAngle, 1, 1, 1);
        Matrix.scaleM(mMatrix, 0, mScale, mScale, 1.0f);
    }

}
