package com.rustero.widgets;


import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.MotionEvent;
import android.view.View;


public class PanelManager {

	public enum TYPE{NONE, TOP, BOTTOM, LEFT, RIGHT};

	public static TYPE type = TYPE.NONE;
	public boolean quick;
	private View mBack, mOpener;
	private View mView;
	private int mDuration, mX1, mX2, mY1, mY2;



	public PanelManager(int aDuration, View aBack, View aOpener) {
		mDuration = aDuration;
		mBack = aBack;
		mOpener = aOpener;
		mBack.setOnTouchListener(mBackToucher);
	}




	public boolean clear() {
		if (type == TYPE.NONE) return false;
		close();
		type = TYPE.NONE;
		return true;
	}



	View.OnTouchListener mBackToucher = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			clear();
			return true;
		}
	};



	private View.OnTouchListener mViewToucher = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			return true;
		}
	};




	private boolean close() {
		if (mView == null) return false;
		if (type== TYPE.TOP  ||  type== TYPE.BOTTOM)
			animateY(mY2, mY1);
		else
			animateX(mX2, mX1);
		if (null != mBack)
			mBack.setVisibility(View.GONE);

		if (null != mOpener) {
			ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mOpener, "rotation", 90f, 0f);
			objectAnimator.setDuration(mDuration);
			objectAnimator.start();
		}
		return true;
	}





	private void onOpen(View aView) {
		clear();
		mView = aView;
		mView.setOnTouchListener(mViewToucher);
		mBack.setVisibility(View.VISIBLE);

		if (null == mOpener) return;
		ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mOpener , "rotation", 0f, 90f);
		objectAnimator.setDuration(mDuration);
		objectAnimator.start();
	}






	public void openTop(View aView) {
		onOpen(aView);
		type = TYPE.TOP;
		mY1 = - mView.getHeight();
		mY2 = 0;
		animateY(mY1, mY2);
		quick = false;
	}



	public void openBottom(View aParent, View aView) {
		onOpen(aView);
		type = TYPE.BOTTOM;
		mY1 = aParent.getHeight();
		mY2 = mY1 - mView.getHeight();
		animateY(mY1, mY2);
		quick = false;
	}



	public void openLeft(View aView) {
		onOpen(aView);
		type = TYPE.LEFT;
		mX1 = - mView.getWidth();
		mX2 = 0;
		animateX(mX1, mX2);
		quick = false;
	}



	public void openRight(View aParent, View aView) {
		onOpen(aView);
		type = TYPE.RIGHT;
		mX1 = aParent.getWidth();
		mX2 = mX1 - mView.getWidth();
		animateX(mX1, mX2);
		quick = false;
	}







	private void animateY(int aY1, int aY2) {
		ValueAnimator anim = ValueAnimator.ofInt(aY1, aY2);
		anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
			int value = (Integer) valueAnimator.getAnimatedValue();
			mView.setTranslationY(value);
			}
		});
		if (quick)
			anim.setDuration(0);
		else
			anim.setDuration(mDuration);
		anim.start();
	}



	private void animateX(int aX1, int aX2) {
		ValueAnimator anim = ValueAnimator.ofInt(aX1, aX2);
		anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
			int value = (Integer) valueAnimator.getAnimatedValue();
			mView.setTranslationX(value);
			}
		});
		if (quick)
			anim.setDuration(0);
		else
			anim.setDuration(mDuration);
		anim.start();
	}

}
