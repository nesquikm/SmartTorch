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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

public class SmartTorchService extends Service implements SensorEventListener {
	private static final String TAG = SmartTorchService.class.getSimpleName();

	public static final String SERVICE_ACTION_TURN_ON = "com.greenlog.smarttorch.SERVICE_ACTION_TURN_ON";
	public static final String SERVICE_ACTION_TURN_OFF = "com.greenlog.smarttorch.SERVICE_ACTION_TURN_OFF";
	public static final String SERVICE_ACTION_UPDATE_WIDGETS = "com.greenlog.smarttorch.SERVICE_ACTION_UPDATE_WIDGETS";
	public static final String SERVICE_ACTION_UPDATE_WIDGETS_CONFIG = "com.greenlog.smarttorch.SERVICE_ACTION_UPDATE_WIDGETS_CONFIG";

	private static final int NOTIFY_ID = 1;

	private NotificationManager mNotificationManager;
	private Notification.Builder mNotificationBuilder = null;

	private boolean mIsLedOn = false;
	private TorchMode mTorchMode = null;
	private boolean mIsShaking = false;
	private long mRemainSeconds = 0;
	private long mWhenTurnOff;

	private Timer mTimer = null;

	private SensorManager mSensorManager;
	private Sensor mAccelerometer;

	private TorchCamera mTorchCamera;

	private PowerManager.WakeLock mWakeLock = null;

	private Utils.AccelerationInterpolator mAccelerationInterpolator;

	private SettingsManager mSettingsManager;

	@Override
	public void onCreate() {
		Log.v(TAG, "onCreate");
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		mAccelerationInterpolator = new Utils.AccelerationInterpolator();

		mTorchCamera = new TorchCamera(this);

		mSettingsManager = new SettingsManager(this);

		super.onCreate();
	}

	@Override
	public void onDestroy() {
		Log.v(TAG, "onDestroy");
		mNotificationManager.cancelAll();

		stopSensor();
		stopTimer();
		turnLed(false);
		stopWakeLock();
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
			startSensor();
			updateTimer(true);
			final Notification notification = updateNotification();
			startForeground(NOTIFY_ID, notification);
			startWakeLock();
			turnLed(true);
			break;
		case SERVICE_ACTION_TURN_OFF:
			stopSelf();
			break;
		case SERVICE_ACTION_UPDATE_WIDGETS:
			updateWidgets();
			if (!mIsLedOn) {
				stopSelf();
			}
			break;
		case SERVICE_ACTION_UPDATE_WIDGETS_CONFIG:
			updateWidgetsConfig();
			break;
		}
		return START_NOT_STICKY;
	}

	public static void sendCommandToService(final Context context,
			final String command, final Bundle extras) {
		final Intent serviceIntent = new Intent(context,
				SmartTorchService.class);

		serviceIntent.setAction(command);
		if (extras != null)
			serviceIntent.putExtras(extras);
		context.startService(serviceIntent);
	}

	@Override
	public IBinder onBind(final Intent intent) {
		// We don't provide binding, so return null
		return null;
	}

	private void turnLed(final boolean isOn) {
		if (isOn == mIsLedOn)
			return;

		final boolean oldIsLedOn = mIsLedOn;
		mIsLedOn = isOn;
		updateWidgets();
		if (!mTorchCamera.turn(isOn)) {
			mIsLedOn = oldIsLedOn;
			updateWidgets();
			if (isOn) {
				Toast.makeText(this, R.string.cant_turn_on, Toast.LENGTH_SHORT)
						.show();
				stopSelf();
			}
		}
	}

	private void startWakeLock() {
		if (mTorchMode.isInfinitely()) {
			return;
		}

		if (mWakeLock != null) {
			return;
		}

		final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				TAG);

		mWakeLock.acquire();
	}

	private void stopWakeLock() {
		if (mWakeLock != null)
			mWakeLock.release();
	}

	private void updateTimer(final boolean forceRestart) {
		if (mTorchMode.isInfinitely()) {
			return;
		}

		final long now = (new Date()).getTime();

		if (forceRestart || (mTorchMode.isShakeSensorEnabled() && mIsShaking)) {
			mWhenTurnOff = now + mTorchMode.getTimeoutSec() * 1000;
		}

		mRemainSeconds = (mWhenTurnOff - now) / 1000;

		if (mTimer == null) {
			mTimer = new Timer();
			mTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					updateTimer(false);
					if (mRemainSeconds <= 0) {
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
			content = getString(R.string.notify_timer,
					Utils.formatTimerTime(this, mRemainSeconds, false));
		}

		// Only for first time
		if (mNotificationBuilder == null) {
			mNotificationBuilder = new Notification.Builder(this)
					.setContentTitle(getString(R.string.notify_title))
					.setSmallIcon(R.drawable.ic_stat_notify)
					.setContentIntent(newPendingIntent);
		}

		mNotificationBuilder.setContentText(content);

		Notification notification;

		if (Build.VERSION.SDK_INT >= 16) {
			notification = mNotificationBuilder.setPriority(
					Notification.PRIORITY_LOW).build();
		} else {
			notification = mNotificationBuilder.getNotification();
		}

		return notification;
	}

	@SuppressWarnings("deprecation")
	private void updateWidgets() {
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

	private void updateWidgetsConfig() {
		final AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(this);

		final int[] appWidgetIds = appWidgetManager
				.getAppWidgetIds(new ComponentName(this, SmartTorchWidget.class));

		appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds,
				R.id.stack_view);
	}

	private void startSensor() {
		if (mTorchMode.isInfinitely() || !mTorchMode.isShakeSensorEnabled()) {
			return;
		}
		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_UI);
	}

	private void stopSensor() {
		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onSensorChanged(final SensorEvent event) {
		mIsShaking = mAccelerationInterpolator.getAcceleration(event.values) > mSettingsManager
				.getSensitivityValue();
	}

	@Override
	public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
	}
}
