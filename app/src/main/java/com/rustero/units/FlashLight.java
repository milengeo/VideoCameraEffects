package com.rustero.units;


import android.hardware.Camera;

import java.util.List;

public class FlashLight {

	private List<String> mModes;

	public void setModes(List<String> aModes) {
		mModes = aModes;
	}

	public boolean isAvailable() {
		if (null == mModes) return false;
		boolean result = mModes.contains(Camera.Parameters.FLASH_MODE_TORCH);
		return result;
	}

}
