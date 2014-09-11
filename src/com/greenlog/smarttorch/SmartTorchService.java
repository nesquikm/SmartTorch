package com.greenlog.smarttorch;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class SmartTorchService extends Service {
	public static final String SERVICE_ACTION_TURN_ON = "com.greenlog.smarttorch.SERVICE_ACTION_TURN_ON";
	public static final String SERVICE_ACTION_TURN_OFF = "com.greenlog.smarttorch.SERVICE_ACTION_TURN_OFF";
	public static final String SERVICE_ACTION_GET_STATUS = "com.greenlog.smarttorch.SERVICE_ACTION_GET_STATUS";

	private static final int NOTIFY_ID = 1;

	private boolean mIsLedOn;

	@Override
	public void onCreate() {
		Log.v("sss", "SmartTorchService onCreate");
		mIsLedOn = false;
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		Log.v("sss", "SmartTorchService onDestroy");
		turnLed(false);
		super.onDestroy();
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	public int onStartCommand(final Intent intent, final int flags,
			final int startId) {
		Log.v("sss", "SmartTorchService onHandleIntent " + intent.getAction());
		if (intent.getAction() == null) {
			stopSelf();
			return START_NOT_STICKY;
		}
		switch (intent.getAction()) {
		case SERVICE_ACTION_TURN_ON:
			final Intent newIntent = new Intent(this, SmartTorchService.class);
			newIntent.setAction(SERVICE_ACTION_TURN_OFF);
			final PendingIntent newPendingIntent = PendingIntent.getService(
					this, 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			// TODO: strings
			final Notification.Builder notificationBuilder = new Notification.Builder(
					this).setContentTitle("TODO title")
					.setContentText("TODO content")
					.setSmallIcon(R.drawable.ic_stat_notify)
					.setContentIntent(newPendingIntent);
			Notification notification;

			if (Build.VERSION.SDK_INT >= 16) {
				notification = notificationBuilder.setPriority(
						Notification.PRIORITY_LOW).build();
			} else {
				notification = notificationBuilder.getNotification();
			}
			startForeground(NOTIFY_ID, notification);

			turnLed(true);
			break;
		case SERVICE_ACTION_TURN_OFF:
			turnLed(false);
			stopSelf();
			break;
		case SERVICE_ACTION_GET_STATUS:
			sendStatusToWidgets();
			if (!mIsLedOn)
				stopSelf();
			break;
		}
		// TODO Auto-generated method stub
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(final Intent intent) {
		// We don't provide binding, so return null
		return null;
	}

	private void turnLed(final boolean isOn) {
		if (isOn == mIsLedOn)
			return;
		mIsLedOn = isOn;
		Log.v("sss", "SmartTorchService turnLed " + mIsLedOn);
		sendStatusToWidgets();
	}

	@SuppressWarnings("deprecation")
	private void sendStatusToWidgets() {
		Log.v("sss", "SmartTorchService sendStatusToWidgets " + mIsLedOn);

		final AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(this);

		final int[] appWidgetIds = appWidgetManager
				.getAppWidgetIds(new ComponentName(this, SmartTorchWidget.class));

		for (int i = 0; i < appWidgetIds.length; ++i) {
			final Intent intent = new Intent(this, StackViewService.class);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					appWidgetIds[i]);
			intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

			final RemoteViews rv = new RemoteViews(getPackageName(),
					R.layout.appwidget);
			if (Build.VERSION.SDK_INT >= 14) {
				rv.setRemoteAdapter(R.id.stack_view, intent);
			} else {
				rv.setRemoteAdapter(appWidgetIds[i], R.id.stack_view, intent);
			}

			rv.setViewVisibility(R.id.stack_view, !mIsLedOn ? View.VISIBLE
					: View.GONE);
			rv.setViewVisibility(R.id.led_on, mIsLedOn ? View.VISIBLE
					: View.GONE);

			final Intent clickIntent = new Intent(this, SmartTorchWidget.class);
			clickIntent
					.setAction(mIsLedOn ? SmartTorchWidget.CLICK_ACTION_LED_OFF
							: SmartTorchWidget.CLICK_ACTION_LED_ON);
			clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					appWidgetIds[i]);
			clickIntent.setData(Uri.parse(intent
					.toUri(Intent.URI_INTENT_SCHEME)));

			final PendingIntent clickPendingIntent = PendingIntent
					.getBroadcast(this, 0, clickIntent,
							PendingIntent.FLAG_UPDATE_CURRENT);

			if (!mIsLedOn) {
				rv.setPendingIntentTemplate(R.id.stack_view, clickPendingIntent);
			} else {
				rv.setOnClickPendingIntent(R.id.led_on, clickPendingIntent);
			}
			appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
		}
	}
}
