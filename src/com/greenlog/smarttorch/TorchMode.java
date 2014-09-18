package com.greenlog.smarttorch;

import android.os.Bundle;

public class TorchMode {
	private final static String BUNDLE_KEY_IS_SHAKE_SENSOR_ENABLED = "com.greenlog.smarttorch.IS_SHAKE_SENSOR_ENABLED";
	private final static String BUNDLE_KEY_TIMEOUT_SEC = "com.greenlog.smarttorch.TIMEOUT_SEC";
	private boolean mIsShakeSensorEnabled = false;
	private int mTimeoutSec = 0;

	public TorchMode() {

	}

	public TorchMode(final TorchMode copyFrom) {
		setShakeSensorEnabled(copyFrom.isShakeSensorEnabled());
		setTimeoutSec(copyFrom.getTimeoutSec());
	}

	public boolean isShakeSensorEnabled() {
		if (mTimeoutSec <= 0)
			return false;
		return mIsShakeSensorEnabled;
	}

	public TorchMode setShakeSensorEnabled(final boolean isShakeSensorEnabled) {
		mIsShakeSensorEnabled = isShakeSensorEnabled;
		return this;
	}

	public int getTimeoutSec() {
		if (mTimeoutSec <= 0)
			return 0;
		return mTimeoutSec;
	}

	public TorchMode setTimeoutSec(final int timeoutSec) {
		mTimeoutSec = timeoutSec;
		return this;
	}

	public boolean isInfinitely() {
		return (getTimeoutSec() == 0);
	}

	public Bundle getBundle() {
		final Bundle bundle = new Bundle();
		bundle.putInt(BUNDLE_KEY_TIMEOUT_SEC, mTimeoutSec);
		bundle.putBoolean(BUNDLE_KEY_IS_SHAKE_SENSOR_ENABLED,
				mIsShakeSensorEnabled);
		return bundle;
	}

	public TorchMode(final Bundle bundle) {
		mTimeoutSec = bundle.getInt(BUNDLE_KEY_TIMEOUT_SEC, -1);
		mIsShakeSensorEnabled = bundle.getBoolean(
				BUNDLE_KEY_IS_SHAKE_SENSOR_ENABLED, false);
	}
}
