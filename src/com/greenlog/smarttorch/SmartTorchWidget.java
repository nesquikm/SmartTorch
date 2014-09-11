package com.greenlog.smarttorch;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

// TODO: android:configure="com.example.android.ExampleAppWidgetConfigure"
// TODO: android:previewImage="@drawable/preview"
// TODO: disallow resizing

public class SmartTorchWidget extends AppWidgetProvider {
	public static final String CLICK_ACTION_LED_ON = "com.greenlog.smarttorch.CLICK_ACTION_LED_ON";
	public static final String CLICK_ACTION_LED_OFF = "com.greenlog.smarttorch.CLICK_ACTION_LED_OFF";
	public static final String CLICK_ACTION_ITEM = "com.greenlog.smarttorch.CLICK_ACTION_ITEM";

	@Override
	public void onReceive(final Context context, final Intent intent) {
		// final AppWidgetManager appWidgetManager = AppWidgetManager
		// .getInstance(context);
		switch (intent.getAction()) {
		case CLICK_ACTION_LED_ON:
			sendCommandToService(context,
					SmartTorchService.SERVICE_ACTION_TURN_ON);
			break;
		case CLICK_ACTION_LED_OFF:
			sendCommandToService(context,
					SmartTorchService.SERVICE_ACTION_TURN_OFF);
			break;
		}
		// if (intent.getAction().equals(CLICK_ACTION)) {
		// final int appWidgetId = intent.getIntExtra(
		// AppWidgetManager.EXTRA_APPWIDGET_ID,
		// AppWidgetManager.INVALID_APPWIDGET_ID);
		// final int viewIndex = intent.getIntExtra(CLICK_ACTION_ITEM, 0);
		// // Toast.makeText(context, "View " + viewIndex + mTmp,
		// // Toast.LENGTH_SHORT).show();
		//
		// // final Bundle options = appWidgetManager
		// // .getAppWidgetOptions(appWidgetId);
		// // options.putBoolean("TODO", false);
		// // appWidgetManager.updateAppWidgetOptions(appWidgetId, options);
		// // TODO: here final we can update final only dataset dataset
		// // appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId,
		// // R.id.stack_view);
		//
		// // mTmp = !mTmp;
		// // update(context);
		//
		// sendCommandToService(context,
		// mIsLedOn ? SmartTorchService.SERVICE_ACTION_TURN_OFF
		// : SmartTorchService.SERVICE_ACTION_TURN_ON);
		// }

		super.onReceive(context, intent);
	}

	@Override
	public void onUpdate(final Context context,
			final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
		sendCommandToService(context,
				SmartTorchService.SERVICE_ACTION_GET_STATUS);
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	private void sendCommandToService(final Context context,
			final String command) {
		final Intent serviceIntent = new Intent(context,
				SmartTorchService.class);
		serviceIntent.setAction(command);
		context.startService(serviceIntent);
	}
}
