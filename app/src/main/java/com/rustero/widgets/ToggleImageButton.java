package com.rustero.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.ImageButton;

import com.rustero.R;





public class ToggleImageButton extends ImageButton implements Checkable {
	private OnCheckedChangeListener onCheckedChangeListener;


	private boolean mChecked;


	public ToggleImageButton(Context context) {
		super(context);
	}

	public ToggleImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setChecked(attrs);
	}

	public ToggleImageButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setChecked(attrs);
	}

	private void setChecked(AttributeSet attrs) {
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ToggleImageButton);
		setChecked(a.getBoolean(R.styleable.ToggleImageButton_android_checked, false));
		a.recycle();
	}


	@Override
	public boolean isChecked() {
		return mChecked;
	}

	@Override
	public void setChecked(boolean checked) {
		mChecked = checked;
		if (onCheckedChangeListener != null) {
			onCheckedChangeListener.onCheckedChanged(this, checked);
		}
		invalidate();
	}


	@Override
	public void toggle() {
		setChecked(!isChecked());
	}

	@Override
	public boolean performClick() {
		toggle();
		return super.performClick();
	}

	public OnCheckedChangeListener getOnCheckedChangeListener() {
		return onCheckedChangeListener;
	}

	public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
		this.onCheckedChangeListener = onCheckedChangeListener;
	}

	public interface OnCheckedChangeListener {
		void onCheckedChanged(ToggleImageButton buttonView, boolean isChecked);
	}





	@Override
	protected void onDraw(Canvas canvas) {

		final int EDGE = 4;
		int width = getWidth();
		int height = getHeight();
		int cora = width / 5;

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

		if (isChecked()) {
			paint.setStyle(Paint.Style.FILL);

			RectF rect = new RectF(0, 0, width, height);
			paint.setColor(0xff888888);
			canvas.drawRoundRect(rect, cora, cora, paint);

			rect = new RectF(EDGE, EDGE, width - EDGE, height - EDGE);
			paint.setColor(0xffcccccc);
			canvas.drawRoundRect(rect, cora, cora, paint);
		} else {
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(4);

			RectF rect = new RectF(0, 0, width, height);
			paint.setColor(0xffffffff);
			canvas.drawRoundRect(rect, cora, cora, paint);
		}

		super.onDraw(canvas);
	}

}