package com.greenlog.smarttorch;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

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

	@Override
	public void onReceive(final Context context, final Intent intent) {
		switch (intent.getAction()) {
		case CLICK_ACTION_LED_ON:
			sendCommandToService(context,
					SmartTorchService.SERVICE_ACTION_TURN_ON,
					intent.getExtras());
			break;
		case CLICK_ACTION_LED_OFF:
			sendCommandToService(context,
					SmartTorchService.SERVICE_ACTION_TURN_OFF, null);
			break;
		}

		super.onReceive(context, intent);
	}

	@Override
	public void onUpdate(final Context context,
			final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
		sendCommandToService(context,
				SmartTorchService.SERVICE_ACTION_UPDATE_WIDGETS, null);
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	private void sendCommandToService(final Context context,
			final String command, final Bundle extras) {
		final Intent serviceIntent = new Intent(context,
				SmartTorchService.class);
		serviceIntent.setAction(command);
		if (extras != null)
			serviceIntent.putExtras(extras);
		context.startService(serviceIntent);
	}
}
