package com.greenlog.smarttorch;

import android.os.Bundle;
import android.util.Log;

public class TorchMode {
	private final static String TAG = TorchMode.class.getSimpleName();
	private final static String BUNDLE_KEY_TORCH_MODE_PRESENTS = "com.greenlog.smarttorch.TORCH_MODE_PRESENTS";
	private final static String BUNDLE_KEY_IS_SHAKE_SENSOR_ENABLED = "com.greenlog.smarttorch.IS_SHAKE_SENSOR_ENABLED";
	private final static String BUNDLE_KEY_TIMEOUT_SEC = "com.greenlog.smarttorch.TIMEOUT_SEC";
	private final static String BUNDLE_KEY_KNOCK_COUNT = "com.greenlog.smarttorch.KNOCK_COUNT";

	private boolean mIsShakeSensorEnabled = false;
	private int mTimeoutSec = 0;
	private int mKnockCount = 0;

	public TorchMode() {

	}

	public TorchMode(final TorchMode copyFrom) {
		setShakeSensorEnabled(copyFrom.isShakeSensorEnabled());
		setTimeoutSec(copyFrom.getTimeoutSec());
		setKnockCount(copyFrom.getKnockCount());
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

	public TorchMode setKnockCount(final int knockCount) {
		mKnockCount = knockCount;
		return this;
	}

	public int getKnockCount() {
		return mKnockCount;
	}

	public boolean isKnockEnabled() {
		return mKnockCount > 0;
	}

	public Bundle getBundle() {
		final Bundle bundle = new Bundle();
		bundle.putInt(BUNDLE_KEY_TIMEOUT_SEC, mTimeoutSec);
		bundle.putBoolean(BUNDLE_KEY_IS_SHAKE_SENSOR_ENABLED,
				mIsShakeSensorEnabled);
		bundle.putInt(BUNDLE_KEY_KNOCK_COUNT, mKnockCount);
		bundle.putBoolean(BUNDLE_KEY_TORCH_MODE_PRESENTS, true);
		return bundle;
	}

	public TorchMode(final Bundle bundle) {
		mTimeoutSec = bundle.getInt(BUNDLE_KEY_TIMEOUT_SEC, -1);
		mIsShakeSensorEnabled = bundle.getBoolean(
				BUNDLE_KEY_IS_SHAKE_SENSOR_ENABLED, false);
		mKnockCount = bundle.getInt(BUNDLE_KEY_KNOCK_COUNT, 0);
	}

	public static boolean isTorchModePresents(final Bundle bundle) {
		return bundle.getBoolean(BUNDLE_KEY_TORCH_MODE_PRESENTS, false);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (mIsShakeSensorEnabled ? 1231 : 1237);
		result = prime * result + mKnockCount;
		result = prime * result + mTimeoutSec;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final TorchMode other = (TorchMode) obj;
		if (mIsShakeSensorEnabled != other.mIsShakeSensorEnabled)
			return false;
		if (mKnockCount != other.mKnockCount)
			return false;
		if (mTimeoutSec != other.mTimeoutSec)
			return false;
		return true;
	}

	public static TorchMode fromString(final String string) {
		final String[] substrings = string.split(",");
		if (substrings.length != 3) {
			Log.w(TAG, "fromString: wrong field count in mode: " + string);
			return null;
		}
		final TorchMode torchMode = new TorchMode();
		try {
			torchMode.setTimeoutSec(Integer.parseInt(substrings[0]));
			torchMode
					.setShakeSensorEnabled(Boolean.parseBoolean(substrings[1]));
			torchMode.setKnockCount(Integer.parseInt(substrings[2]));
			return torchMode;
		} catch (final NumberFormatException e) {
			Log.w(TAG, "checkTorchModes: can't parse mode fields: " + string);
		}
		return null;
	}
}
