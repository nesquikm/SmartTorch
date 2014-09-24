package com.greenlog.smarttorch;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

// TODO: 00. Cancel when onPause! 

public class ShakeSensitivityCalibrateDialog extends Dialog {
	public ShakeSensitivityCalibrateDialog(final Context context) {
		super(context);
	}

	protected ShakeSensitivityCalibrateDialog(final Context context,
			final boolean cancelable, final OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	public ShakeSensitivityCalibrateDialog(final Context context,
			final int theme) {
		super(context, theme);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shake_sensitivity_calibrate_dialog);
	}

}
