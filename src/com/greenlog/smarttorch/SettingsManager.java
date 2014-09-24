package com.greenlog.smarttorch;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

// TODO: 09. Test with clean configuration

public class SettingsManager {
	private static final String TAG = SettingsManager.class.getSimpleName();

	private static final String PREFERENCE_FILE_KEY = "com.greenlog.smarttorch.PREFERENCE_FILE_KEY";

	private static final String TORCH_MODE_COUNT = "torch_mode_count";
	private static final String TORCH_MODE_TIMEOUT = "torch_mode_timeout_";
	private static final String TORCH_MODE_SHAKE_SENSOR_ENABLED = "shake_sensor_enabled_";

	private static final String SHAKE_SENSITIVITY_MODE = "shake_sensitivity_mode";
	private static final String SHAKE_SENSITIVITY_CALIBRATED_VALUE = "shake_sensitivity_calibrated_value";

	public final static int MAX_MODE_COUNT = 5;

	public final static int SHAKE_SENSITIVITY_LOW = 0;
	public final static int SHAKE_SENSITIVITY_MEDIUM = 1;
	public final static int SHAKE_SENSITIVITY_HIGH = 2;
	public final static int SHAKE_SENSITIVITY_CALIBRATED = 3;
	public final static int SHAKE_SENSITIVITY_DEFAULT = 1;

	public final static float SHAKE_SENSITIVITY_LOW_VALUE = 0.5f;
	public final static float SHAKE_SENSITIVITY_MEDIUM_VALUE = 0.3f;
	public final static float SHAKE_SENSITIVITY_HIGH_VALUE = 0.15f;

	private final Context mContext;
	private final SharedPreferences mSharedPreferences;

	public SettingsManager(final Context context) {
		mContext = context;
		mSharedPreferences = mContext.getSharedPreferences(PREFERENCE_FILE_KEY,
				Context.MODE_PRIVATE);
	}

	public TorchModes readTorchModes() {
		final TorchModes torchModes = new TorchModes();

		final int count = mSharedPreferences.getInt(TORCH_MODE_COUNT, 0);

		for (int i = 0; i < count; i++) {
			final TorchMode torchMode = new TorchMode();
			torchMode.setTimeoutSec(mSharedPreferences.getInt(
					TORCH_MODE_TIMEOUT + i, 0));
			torchMode.setShakeSensorEnabled(mSharedPreferences.getBoolean(
					TORCH_MODE_SHAKE_SENSOR_ENABLED + i, true));
			torchModes.add(torchMode);
		}

		checkTorchModes(torchModes);
		return torchModes;
	}

	public void writeTorchModes(final TorchModes torchModes) {
		checkTorchModes(torchModes);
		final SharedPreferences.Editor editor = mSharedPreferences.edit();

		final int count = torchModes.size();
		editor.putInt(TORCH_MODE_COUNT, count);

		for (int i = 0; i < count; i++) {
			editor.putInt(TORCH_MODE_TIMEOUT + i, torchModes.get(i)
					.getTimeoutSec());
			editor.putBoolean(TORCH_MODE_SHAKE_SENSOR_ENABLED + i, torchModes
					.get(i).isShakeSensorEnabled());
		}

		editor.commit();
	}

	private void checkTorchModes(final TorchModes torchModes) {
		if (torchModes.size() == 0) {
			final Resources resources = mContext.getResources();
			final String modes[] = resources
					.getStringArray(R.array.default_modes);
			for (int i = 0; i < modes.length; i++) {
				final TorchMode torchMode = TorchMode.fromString(modes[i]);
				if (torchMode != null) {
					torchModes.add(torchMode);
				}
			}
		} else {
			while (torchModes.size() > MAX_MODE_COUNT) {
				torchModes.remove(torchModes.size() - 1);
			}
		}
	}

	public void writeShakeSensorSensitivity(final int mode, final float value) {
		final SharedPreferences.Editor editor = mSharedPreferences.edit();
		editor.putInt(SHAKE_SENSITIVITY_MODE, mode);
		editor.putFloat(SHAKE_SENSITIVITY_CALIBRATED_VALUE, value);
		editor.commit();
	}

	public int readShakeSensorSensitivityMode() {
		final int mode = mSharedPreferences.getInt(SHAKE_SENSITIVITY_MODE,
				SHAKE_SENSITIVITY_DEFAULT);
		if (readShakeSensorSensitivityCalibratedValue() < 0
				&& mode == SHAKE_SENSITIVITY_CALIBRATED) {
			return SHAKE_SENSITIVITY_DEFAULT;
		}
		return mode;
	}

	public float readShakeSensorSensitivityCalibratedValue() {
		return mSharedPreferences.getFloat(SHAKE_SENSITIVITY_CALIBRATED_VALUE,
				-1f);
	}

	public float getSensitivityValue() {
		switch (readShakeSensorSensitivityMode()) {
		case SHAKE_SENSITIVITY_LOW:
			return SHAKE_SENSITIVITY_LOW_VALUE;
		default:
		case SHAKE_SENSITIVITY_MEDIUM:
			return SHAKE_SENSITIVITY_MEDIUM_VALUE;
		case SHAKE_SENSITIVITY_HIGH:
			return SHAKE_SENSITIVITY_HIGH_VALUE;
		case SHAKE_SENSITIVITY_CALIBRATED:
			return readShakeSensorSensitivityCalibratedValue();
		}
	}
}
