package com.greenlog.smarttorch;

import android.os.Bundle;

public class TorchMode {
	private final static String BUNDLE_KEY_TORCH_MODE_PRESENTS = "com.greenlog.smarttorch.TORCH_MODE_PRESENTS";
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
		bundle.putBoolean(BUNDLE_KEY_TORCH_MODE_PRESENTS, true);
		return bundle;
	}

	public TorchMode(final Bundle bundle) {
		mTimeoutSec = bundle.getInt(BUNDLE_KEY_TIMEOUT_SEC, -1);
		mIsShakeSensorEnabled = bundle.getBoolean(
				BUNDLE_KEY_IS_SHAKE_SENSOR_ENABLED, false);
	}

	public static boolean isTorchModePresents(final Bundle bundle) {
		return bundle.getBoolean(BUNDLE_KEY_TORCH_MODE_PRESENTS, false);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (mIsShakeSensorEnabled ? 1231 : 1237);
		result = prime * result + mTimeoutSec;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TorchMode))
			return false;
		final TorchMode other = (TorchMode) obj;
		if (mIsShakeSensorEnabled != other.mIsShakeSensorEnabled)
			return false;
		if (mTimeoutSec != other.mTimeoutSec)
			return false;
		return true;
	}
}
