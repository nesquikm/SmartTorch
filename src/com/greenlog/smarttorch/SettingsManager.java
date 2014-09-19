package com.greenlog.smarttorch;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

// TODO: 09. Test with clean configuration

public class SettingsManager {
	private static final String TAG = SettingsManager.class.getSimpleName();

	private static final String PREFERENCE_FILE_KEY = "com.greenlog.smarttorch.PREFERENCE_FILE_KEY";
	private static final String TORCH_MODE_COUNT = "torch_mode_count";
	private static final String TORCH_MODE_TIMEOUT = "torch_mode_timeout_";
	private static final String TORCH_MODE_SHAKE_SENSOR_ENABLED = "shake_sensor_enabled_";

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
				final String[] substrings = modes[i].split(",");
				if (substrings.length != 2) {
					Log.w(TAG, "checkTorchModes: wrong field count in mode: "
							+ modes[i]);
					continue;
				}
				final TorchMode torchMode = new TorchMode();
				try {
					torchMode.setTimeoutSec(Integer.parseInt(substrings[0]));
					torchMode.setShakeSensorEnabled(Boolean
							.parseBoolean(substrings[1]));
					torchModes.add(torchMode);
				} catch (final NumberFormatException e) {
					Log.w(TAG, "checkTorchModes: can't parse mode fields: "
							+ modes[i]);
				}
			}
		}
	}
}