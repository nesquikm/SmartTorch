package com.greenlog.smarttorch;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class SmartTorchService extends Service {
	public static final String SERVICE_ACTION_TURN_ON = "com.greenlog.smarttorch.SERVICE_ACTION_TURN_ON";
	public static final String SERVICE_ACTION_TURN_OFF = "com.greenlog.smarttorch.SERVICE_ACTION_TURN_OFF";
	// TODO: rename it to "...update_widgets"
	public static final String SERVICE_ACTION_GET_STATUS = "com.greenlog.smarttorch.SERVICE_ACTION_GET_STATUS";

	private static final int NOTIFY_ID = 1;

	private NotificationManager mNotificationManager;

	private boolean mIsLedOn = false;
	private TorchMode mTorchMode = null;
	private final boolean mIsShaking = false;
	private long mRemainSeconds = 0;
	private long mWhenTurnOff;

	private Timer mTimer = null;

	@Override
	public void onCreate() {
		Log.v("sss", "SmartTorchService onCreate");
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		Log.v("sss", "SmartTorchService onDestroy");
		stopTimer();
		turnLed(false);
		super.onDestroy();
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags,
			final int startId) {
		if (intent.getAction() == null) {
			stopSelf();
			return START_NOT_STICKY;
		}
		switch (intent.getAction()) {
		case SERVICE_ACTION_TURN_ON:
			mTorchMode = new TorchMode(intent.getExtras());
			Log.w("sss", "@@@????????TS inf" + mTorchMode.isInfinitely()
					+ " timeout " + mTorchMode.getTimeoutSec() + " shake "
					+ mTorchMode.isShakeSensorEnabled());

			if (updateTimer(true)) {
				final Notification notification = updateNotification();
				startForeground(NOTIFY_ID, notification);
				turnLed(true);
			} else {
				stopSelf();
			}
			break;
		case SERVICE_ACTION_TURN_OFF:
			stopTimer();
			turnLed(false);
			stopSelf();
			break;
		case SERVICE_ACTION_GET_STATUS:
			updateWidgets();
			if (!mIsLedOn) {
				stopTimer();
				stopSelf();
			}
			break;
		}
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
		updateWidgets();
	}

	private boolean updateTimer(final boolean forceRestart) {
		if (mTorchMode.isInfinitely()) {
			return true;
		}

		final long now = (new Date()).getTime();

		if (forceRestart || (mTorchMode.isShakeSensorEnabled() && mIsShaking)) {
			mWhenTurnOff = now + mTorchMode.getTimeoutSec() * 1000;
		}

		mRemainSeconds = (mWhenTurnOff - now) / 1000;

		if (mRemainSeconds > 0) {
			startTimer();
			return true;
		} else {
			stopTimer();
			return false;
		}
	}

	private void startTimer() {
		if (mTimer == null) {
			mTimer = new Timer();
			mTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					if (!updateTimer(false)) {
						stopTimer();
						turnLed(false);
						stopSelf();
					} else {
						final Notification notification = updateNotification();
						mNotificationManager.notify(NOTIFY_ID, notification);
					}
				}
			}, 500, 500);
		}
	}

	private void stopTimer() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer.purge();
			mTimer = null;
		}
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private Notification updateNotification() {
		final Intent newIntent = new Intent(this, SmartTorchService.class);
		newIntent.setAction(SERVICE_ACTION_TURN_OFF);
		final PendingIntent newPendingIntent = PendingIntent.getService(this,
				0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		String content = "";
		if (mTorchMode.isInfinitely()) {
			content = getString(R.string.notify_turn_off);
		} else if (mTorchMode.isShakeSensorEnabled() && mIsShaking) {
			content = getString(R.string.notify_until_stop_shaking);
		} else {
			final Resources res = getResources();
			final int remainMin = (int) (mRemainSeconds / 60);
			final int remainSec = (int) (mRemainSeconds % 60);
			final String remainMinString = remainMin > 0 ? res
					.getQuantityString(R.plurals.min, remainMin, remainMin)
					+ " " : "";
			content = getString(R.string.notify_timer, remainMinString,
					res.getQuantityString(R.plurals.sec, remainSec, remainSec));
		}

		final Notification.Builder notificationBuilder = new Notification.Builder(
				this).setContentTitle(getString(R.string.notify_title))
				.setContentText(content)
				.setSmallIcon(R.drawable.ic_stat_notify)
				.setContentIntent(newPendingIntent);
		Notification notification;

		if (Build.VERSION.SDK_INT >= 16) {
			notification = notificationBuilder.setPriority(
					Notification.PRIORITY_LOW).build();
		} else {
			notification = notificationBuilder.getNotification();
		}

		return notification;
	}

	@SuppressWarnings("deprecation")
	private void updateWidgets() {
		Log.v("sss", "SmartTorchService updateWidgets " + mIsLedOn);

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
			clickIntent.setData(Uri.parse(clickIntent
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
