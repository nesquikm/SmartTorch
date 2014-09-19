package com.greenlog.smarttorch;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

// TODO: 03. android:previewImage="@drawable/preview"
// TODO: 03. set correct icon sizes
// TODO: 04. set as large as possible large icon for lockscreen
// TODO: 10. automatically lock screen after turn on (configurable)
// TODO: 03. icon in settings->running app (service icon
// TODO: 02. update widget when shaking/timer
// TODO: 05. remove/change Log.v (sss)

public class SmartTorchWidget extends AppWidgetProvider {
	public static final String CLICK_ACTION_LED_ON = "com.greenlog.smarttorch.CLICK_ACTION_LED_ON";
	public static final String CLICK_ACTION_LED_OFF = "com.greenlog.smarttorch.CLICK_ACTION_LED_OFF";
	private static final long DOUBLE_CLICK_DELAY_MILLIS = 500;

	private static ClickCatchRunnable mClickCatchRunnable = null;

	@Override
	public void onReceive(final Context context, final Intent intent) {
		switch (intent.getAction()) {
		case CLICK_ACTION_LED_ON:
		case CLICK_ACTION_LED_OFF: {
			// If command from keyguard widget, do not try to check doubleclick
			if (isKeyguard(context, intent)) {
				processCommand(context, intent.getAction(), intent.getExtras());
			} else {
				// is it double click?
				if (mClickCatchRunnable == null) {
					mClickCatchRunnable = new ClickCatchRunnable(context,
							intent.getAction(), intent.getExtras());
					(new Handler()).postDelayed(mClickCatchRunnable,
							DOUBLE_CLICK_DELAY_MILLIS);
				} else {
					mClickCatchRunnable.cancel();
					mClickCatchRunnable = null;
					SmartTorchService.sendCommandToService(context,
							SmartTorchService.SERVICE_ACTION_TURN_OFF, null);

					// start configure activity
					final Intent configureActivityIntent = new Intent(context,
							SmartTorchWidgetConfigure.class);
					configureActivityIntent
							.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					configureActivityIntent.putExtras(intent.getExtras());
					context.startActivity(configureActivityIntent);
				}
			}
			break;
		}
		}

		super.onReceive(context, intent);
	}

	@Override
	public void onUpdate(final Context context,
			final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
		SmartTorchService.sendCommandToService(context,
				SmartTorchService.SERVICE_ACTION_UPDATE_WIDGETS, null);
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	@SuppressLint("NewApi")
	private boolean isKeyguard(final Context context, final Intent intent) {
		if (Build.VERSION.SDK_INT >= 17) {
			final int appWidgetId = intent.getExtras().getInt(
					AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
			final AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(context);
			// Get the value of OPTION_APPWIDGET_HOST_CATEGORY
			final Bundle myOptions = appWidgetManager
					.getAppWidgetOptions(appWidgetId);

			final int category = myOptions.getInt(
					AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY, -1);

			// If the value is WIDGET_CATEGORY_KEYGUARD, it's a lockscreen
			// widget
			return (category == AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD);
		}
		return false;
	}

	private void processCommand(final Context context, final String action,
			final Bundle extras) {
		switch (action) {
		case CLICK_ACTION_LED_ON:
			SmartTorchService.sendCommandToService(context,
					SmartTorchService.SERVICE_ACTION_TURN_ON, extras);
			break;
		case CLICK_ACTION_LED_OFF:
			SmartTorchService.sendCommandToService(context,
					SmartTorchService.SERVICE_ACTION_TURN_OFF, null);
			break;
		}
	}

	private class ClickCatchRunnable implements Runnable {
		private final Context mContext;
		private final String mAction;
		private final Bundle mExtras;
		private boolean mIsCancelled = false;

		public ClickCatchRunnable(final Context context, final String action,
				final Bundle extras) {
			mContext = context;
			mAction = action;
			mExtras = extras;
		}

		public void cancel() {
			mIsCancelled = true;
		}

		@Override
		public void run() {
			if (mIsCancelled) {
				return;
			}
			processCommand(mContext, mAction, mExtras);
			mClickCatchRunnable = null;
		}

	}
}
