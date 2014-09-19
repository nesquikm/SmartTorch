package com.greenlog.smarttorch;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.StackView;
import android.widget.Toast;

// TODO: 00. Double-tap on the widget call configure activity
// TODO: 03. Remove <action android:name="android.intent.action.MAIN" /> and <category android:name="android.intent.category.LAUNCHER" from manifest 
// TODO: 01. Orientation change tests!
// TODO: 01. Trash can with animation
// TODO: 01. Create new mode button with animation
// TODO: 02. Don't show this configuration window if one or more widgets are already placed
// TODO: 02. Show toast with "you always can double click to configure SmartTorch widget" 

public class SmartTorchWidgetConfigure extends Activity {
	private final static String BUNDLE_KEY_TORCH_MODES = "com.greenlog.smarttorch.TORCH_MODES";

	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	private TorchModeAdapter mTorchModeAdapter;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setResult(RESULT_CANCELED);
		setContentView(R.layout.configure_activity);

		// Find the widget id from the intent.
		final Intent intent = getIntent();
		final Bundle extras = intent.getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		final Button saveButton = (Button) findViewById(R.id.save_button);
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				mTorchModeAdapter.saveTorchModes();
				// Update ALL widgets config!
				SmartTorchService.sendCommandToService(
						SmartTorchWidgetConfigure.this,
						SmartTorchService.SERVICE_ACTION_UPDATE_WIDGETS_CONFIG,
						null);

				if (mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
					final Intent resultValue = new Intent();
					resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
							mAppWidgetId);
					setResult(RESULT_OK, resultValue);

					Toast.makeText(SmartTorchWidgetConfigure.this,
							R.string.double_click_to_configure,
							Toast.LENGTH_SHORT).show();
				}
				finish();
			}
		});

		final StackView stackView = (StackView) findViewById(R.id.stack_view);
		if (savedInstanceState != null) {
			mTorchModeAdapter = new TorchModeAdapter(this,
					savedInstanceState.getBundle(BUNDLE_KEY_TORCH_MODES));
		} else {
			mTorchModeAdapter = new TorchModeAdapter(this);
		}
		stackView.setAdapter(mTorchModeAdapter);
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBundle(BUNDLE_KEY_TORCH_MODES,
				mTorchModeAdapter.getTorchModesBundle());
	}
}
