package com.rustero.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageButton;


public class MyImageButton extends ImageButton {


	private int mFrameColor = -1;


	public MyImageButton(Context context) {
		super(context);
	}


	public MyImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}


	public MyImageButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}


	public void setFrameColor(int aColor) {
		mFrameColor = aColor;
	}


	@Override
	protected void onDraw(Canvas canvas) {

		int width = getWidth();
		int height = getHeight();

		int frwi = width / 20;
		int cora = width / 5;

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

		if (mFrameColor != 0) {
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(frwi);

			RectF rect = new RectF(0, 0, width, height);
			paint.setColor(mFrameColor);
			canvas.drawRoundRect(rect, cora, cora, paint);
		}

		super.onDraw(canvas);
	}

}