package com.greenlog.smarttorch;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

// TODO: android:configure="com.example.android.ExampleAppWidgetConfigure"
// TODO: android:previewImage="@drawable/preview"
// TODO: set correct icon sizes
// TODO: set as large as possible large icon for lockscreen
// TODO: automatically lock screen after turn on (configurable)

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
				SmartTorchService.SERVICE_ACTION_GET_STATUS, null);
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
