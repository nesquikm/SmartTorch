package com.greenlog.smarttorch;

import java.util.ArrayList;

import android.os.Bundle;
import android.util.Log;

public class TorchModes extends ArrayList<TorchMode> {
	private static final String TAG = TorchModes.class.getSimpleName();
	private static final long serialVersionUID = 2168019416098900380L;
	private final static String BUNDLE_KEY_TORCH_MODES_COUNT = "com.greenlog.smarttorch.TORCH_MODES_COUNT";
	private final static String BUNDLE_KEY_PREFIX_TORCH_MODES_MODE = "com.greenlog.smarttorch.TORCH_MODES_MODE_";

	public TorchModes() {
	}

	public TorchModes(final Bundle bundle) {
		final int count = bundle.getInt(BUNDLE_KEY_TORCH_MODES_COUNT, 0);
		for (int i = 0; i < count; i++) {
			final Bundle torchModeBundle = bundle
					.getBundle(BUNDLE_KEY_PREFIX_TORCH_MODES_MODE + i);
			if (torchModeBundle == null) {
				Log.w(TAG, "TorchModes(bundle): null TorchMode bundle");
				continue;
			}
			add(new TorchMode(torchModeBundle));
		}
	}

	public Bundle getBundle() {
		final Bundle bundle = new Bundle();
		bundle.putInt(BUNDLE_KEY_TORCH_MODES_COUNT, size());

		for (int i = 0; i < size(); i++) {
			bundle.putBundle(BUNDLE_KEY_PREFIX_TORCH_MODES_MODE + i, get(i)
					.getBundle());
		}

		return bundle;
	}
}
