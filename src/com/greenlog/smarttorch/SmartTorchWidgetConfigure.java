package com.greenlog.smarttorch;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.StackView;

// TODO: 00. Remove <action android:name="android.intent.action.MAIN" /> and <category android:name="android.intent.category.LAUNCHER" from manifest 
// TODO: 01. Orientation change tests!
// TODO: 01. Trash can with animation
// TODO: 01. Create new mode button with animation
// TODO: 02. Don't show this configuration window if one or more widgets are already placed
// TODO: 02. Show toast with "you always can double click to configure SmartTorch widget" 

public class SmartTorchWidgetConfigure extends Activity {
	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setResult(RESULT_CANCELED);
		// if (savedInstanceState == null) {
		setContentView(R.layout.configure_activity);
		// }

		// Find the widget id from the intent.
		final Intent intent = getIntent();
		final Bundle extras = intent.getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		// If they gave us an intent without the widget id, just bail.
		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			// TODO: 0. uncomment this!
			// finish();
		}

		final Button saveButton = (Button) findViewById(R.id.save_button);
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				final Intent resultValue = new Intent();
				resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
						mAppWidgetId);
				setResult(RESULT_OK, resultValue);
				finish();
			}
		});

		final StackView stackView = (StackView) findViewById(R.id.stack_view);
		stackView.setAdapter(new TorchModeAdapter(this));
	}

}
