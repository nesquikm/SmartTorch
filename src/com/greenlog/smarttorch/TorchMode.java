package com.greenlog.smarttorch;

import android.os.Bundle;

public class TorchMode {
	private final static String BUNDLE_PREFIX = "com.greenlog.smarttorch.";
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
		bundle.putBoolean(BUNDLE_PREFIX + "mIsShakeSensorEnabled",
				mIsShakeSensorEnabled);
		bundle.putInt(BUNDLE_PREFIX + "mTimeoutSec", mTimeoutSec);
		return bundle;
	}

	public TorchMode(final Bundle bundle) {
		mIsShakeSensorEnabled = bundle.getBoolean(BUNDLE_PREFIX
				+ "mIsShakeSensorEnabled", false);
		mTimeoutSec = bundle.getInt(BUNDLE_PREFIX + "mTimeoutSec", -1);
	}
}
