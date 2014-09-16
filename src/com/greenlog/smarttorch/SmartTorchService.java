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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

public class SmartTorchService extends Service implements SensorEventListener {
	public static final String SERVICE_ACTION_TURN_ON = "com.greenlog.smarttorch.SERVICE_ACTION_TURN_ON";
	public static final String SERVICE_ACTION_TURN_OFF = "com.greenlog.smarttorch.SERVICE_ACTION_TURN_OFF";
	// TODO: rename it to "...update_widgets"
	public static final String SERVICE_ACTION_GET_STATUS = "com.greenlog.smarttorch.SERVICE_ACTION_GET_STATUS";

	private static final int NOTIFY_ID = 1;

	private NotificationManager mNotificationManager;

	private boolean mIsLedOn = false;
	private TorchMode mTorchMode = null;
	private boolean mIsShaking = false;
	private long mRemainSeconds = 0;
	private long mWhenTurnOff;

	private Timer mTimer = null;

	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private final float mAccelerationSlow[] = new float[3];
	private final float mAccelerationFast[] = new float[3];
	private final static float ACCELERATION_FILTER_ALPHA_SLOW = 0.2f;
	private final static float ACCELERATION_FILTER_ALPHA_FAST = 1f;
	// TODO: configurable sensitivity!!!!!
	private final static float ACCELERATION_THRESHOLD = 0.1f;

	private TorchCamera mTorchCamera;

	@Override
	public void onCreate() {
		Log.v("sss", "SmartTorchService onCreate");
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		mTorchCamera = new TorchCamera(this);

		super.onCreate();
	}

	@Override
	public void onDestroy() {
		Log.v("sss", "SmartTorchService onDestroy");
		stopSensor();
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
			if (updateTimer(true)) {
				if (!mTorchMode.isInfinitely()
						&& mTorchMode.isShakeSensorEnabled()) {
					startSensor();
				}
				final Notification notification = updateNotification();
				startForeground(NOTIFY_ID, notification);
				turnLed(true);
			} else {
				stopSelf();
			}
			break;
		case SERVICE_ACTION_TURN_OFF:
			stopSensor();
			stopTimer();
			turnLed(false);
			stopSelf();
			break;
		case SERVICE_ACTION_GET_STATUS:
			updateWidgets();
			if (!mIsLedOn) {
				stopSensor();
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

		Log.v("sss", "SmartTorchService turnLed " + mIsLedOn);
		final boolean oldIsLedOn = mIsLedOn;
		mIsLedOn = isOn;
		updateWidgets();
		if (!mTorchCamera.turn(isOn)) {
			if (isOn) {
				Toast.makeText(this, R.string.cant_turn_on, Toast.LENGTH_SHORT)
						.show();
			}
			mIsLedOn = oldIsLedOn;
			updateWidgets();
		}
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
							mNotificationManager
									.notify(NOTIFY_ID, notification);
						}
					}
				}, 500, 500);
			}
			return true;
		} else {
			stopTimer();
			return false;
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

	private void startSensor() {
		Log.v("sss", "@@@ startSensor");
		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_UI);
	}

	private void stopSensor() {
		Log.v("sss", "@@@ stopSensor");
		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onSensorChanged(final SensorEvent event) {

		for (int i = 0; i < 3; i++) {
			mAccelerationSlow[i] = ACCELERATION_FILTER_ALPHA_SLOW
					* event.values[i] + (1 - ACCELERATION_FILTER_ALPHA_SLOW)
					* mAccelerationSlow[i];
			mAccelerationFast[i] = ACCELERATION_FILTER_ALPHA_FAST
					* event.values[i] + (1 - ACCELERATION_FILTER_ALPHA_FAST)
					* mAccelerationFast[i];
		}

		for (int i = 0; i < 3; i++) {
			mIsShaking = (Math.abs(mAccelerationSlow[i] - mAccelerationFast[i]) > ACCELERATION_THRESHOLD);
			if (mIsShaking)
				break;
		}
		// Log.v("sss",
		// "@@@ dx "
		// + Math.round(10000 * Math.abs(mAccelerationSlow[0]
		// - mAccelerationFast[0]))
		// + " dy "
		// + +Math.round(10000 * Math.abs(mAccelerationSlow[1]
		// - mAccelerationFast[1]))
		// + " dz "
		// + +Math.round(10000 * Math.abs(mAccelerationSlow[2]
		// - mAccelerationFast[2])));
	}

	@Override
	public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
		String accuracyString = "unknown";
		switch (accuracy) {
		case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
			accuracyString = "high";
			break;
		case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
			accuracyString = "medium";
			break;
		case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
			accuracyString = "low";
			break;
		case SensorManager.SENSOR_STATUS_NO_CONTACT:
			accuracyString = "no_contact";
			break;
		case SensorManager.SENSOR_STATUS_UNRELIABLE:
			accuracyString = "unreliable";
			break;
		}
		Log.v("sss", "onAccuracyChanged " + sensor.getName() + " accuracy "
				+ accuracyString);
	}
}