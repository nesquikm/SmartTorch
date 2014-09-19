package com.greenlog.smarttorch;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
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
	public static final String CLICK_ACTION_TORCH_MODE = "com.greenlog.smarttorch.CLICK_ACTION_TORCH_MODE";

	private static ClickCatchRunnable mClickCatchRunnable = null;

	@Override
	public void onReceive(final Context context, final Intent intent) {
		// is it double click?
		if (mClickCatchRunnable == null) {
			mClickCatchRunnable = new ClickCatchRunnable(context,
					intent.getAction(), intent.getExtras());
			(new Handler()).postDelayed(mClickCatchRunnable, 500);
		} else {
			mClickCatchRunnable.cancel();
			mClickCatchRunnable = null;
			SmartTorchService.sendCommandToService(context,
					SmartTorchService.SERVICE_ACTION_TURN_OFF, null);

			// start configure activity
			final Intent configureActivityIntent = new Intent(context,
					SmartTorchWidgetConfigure.class);
			configureActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			configureActivityIntent.putExtras(intent.getExtras());
			context.startActivity(configureActivityIntent);
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
			switch (mAction) {
			case CLICK_ACTION_LED_ON:
				SmartTorchService.sendCommandToService(mContext,
						SmartTorchService.SERVICE_ACTION_TURN_ON, mExtras);
				break;
			case CLICK_ACTION_LED_OFF:
				SmartTorchService.sendCommandToService(mContext,
						SmartTorchService.SERVICE_ACTION_TURN_OFF, null);
				break;
			}
			mClickCatchRunnable = null;
		}

	}
}
