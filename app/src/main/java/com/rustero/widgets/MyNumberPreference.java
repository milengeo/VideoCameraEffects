package com.rustero.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.NumberPicker;




/**
 * A {@link android.preference.Preference} that displays a number mPicker as a dialog.
 */
public class MyNumberPreference extends DialogPreference {

	// enable or disable the 'circular behavior'
	public static final boolean WRAP_SELECTOR_WHEEL = false;

	private NumberPicker mPicker;
	private int mValue;
	private int mMinValue = 1;
	public int mMaxValue = 30;


	public MyNumberPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		for (int i=0;i<attrs.getAttributeCount();i++) {
			String attr = attrs.getAttributeName(i);
			if (attr.equalsIgnoreCase("min_value"))
				mMinValue = attrs.getAttributeIntValue(i, 1);
			if (attr.equalsIgnoreCase("max_value"))
				mMaxValue = attrs.getAttributeIntValue(i, 1);
		}
	}


	public MyNumberPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}


	@Override
	protected View onCreateDialogView() {
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		layoutParams.gravity = Gravity.CENTER;

		mPicker = new NumberPicker(getContext());
		mPicker.setLayoutParams(layoutParams);

		FrameLayout dialogView = new FrameLayout(getContext());
		dialogView.addView(mPicker);

		return dialogView;
	}


	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		mPicker.setMinValue(mMinValue);
		mPicker.setMaxValue(mMaxValue);
		mPicker.setWrapSelectorWheel(WRAP_SELECTOR_WHEEL);
		mPicker.setValue(getValue());
	}


	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			mPicker.clearFocus();
			int newValue = mPicker.getValue();
			if (callChangeListener(newValue)) {
				setValue(newValue);
			}
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInt(index, mMinValue);
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		setValue(restorePersistedValue ? getPersistedInt(mMinValue) : (Integer) defaultValue);
	}

	public void setValue(int aValue) {
		this.mValue = aValue;
		persistInt(this.mValue);
	}

	public int getValue() {
		return this.mValue;
	}
}
