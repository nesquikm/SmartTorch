package com.greenlog.smarttorch;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ShakeSensitivityCalibrateDialog extends Dialog implements
		SensorEventListener {
	private static final int CALIBRATED = 0xff43;

	private static final int PREPARATION_DELAY_MS = 1000;
	private static final int CALIBRATION_TIME_MS = 5000;

	private static final float CALIBRATION_VALUE_FACTOR = 1.1f;

	private static final String BUNDLE_KEY_CALIBRATION_VALUE = "com.greenlog.smarttorch.BUNDLE_KEY_CALIBRATION_VALUE";

	private long mStartTime;
	private Timer mTimer;
	private TextView mTimeText;
	private ProgressBar mTimeProgress;

	private Handler mListenersHandlerEx;
	private Message mCalibratedMessage;

	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private Utils.AccelerationInterpolator mAccelerationInterpolator;
	private boolean mInCalibration = false;

	private static final float MAX_ACCELERATION_TO_CANCEL = 1.0f;
	private float mMaxAcceleration = 0f;

	public ShakeSensitivityCalibrateDialog(final Context context) {
		super(context);
		init();
	}

	protected ShakeSensitivityCalibrateDialog(final Context context,
			final boolean cancelable, final OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		init();
	}

	public ShakeSensitivityCalibrateDialog(final Context context,
			final int theme) {
		super(context, theme);
		init();
	}

	private void init() {
		mListenersHandlerEx = new ListenersHandlerEx(this);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shake_sensitivity_calibrate_dialog);
		setTitle(R.string.calibration);

		mTimeText = (TextView) findViewById(R.id.time_text);
		mTimeProgress = (ProgressBar) findViewById(R.id.time_progress);
		mStartTime = (new Date()).getTime();
		mTimer = new Timer();
		final Handler handler = new Handler();
		final Resources res = getContext().getResources();
		mTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				handler.post(new Runnable() {
					@Override
					public void run() {
						final long timeFromCreate = (new Date()).getTime()
								- mStartTime;
						if (timeFromCreate < PREPARATION_DELAY_MS) {
						} else if (timeFromCreate < (PREPARATION_DELAY_MS + CALIBRATION_TIME_MS)) {
							mInCalibration = true;

							final long timeFromStart = timeFromCreate
									- PREPARATION_DELAY_MS;
							final int remainSec = (int) (CALIBRATION_TIME_MS - timeFromStart) / 1000 + 1;

							final String timeText = res.getString(
									R.string.calibration_timer, res
											.getQuantityString(R.plurals.sec,
													remainSec, remainSec));

							mTimeText.setText(timeText);

							mTimeProgress
									.setProgress((int) (100 * timeFromStart / CALIBRATION_TIME_MS));
						} else {
							stopTimer();

							calibrated();
							ShakeSensitivityCalibrateDialog.this.dismiss();
						}
					}
				});
			}
		}, 100, 100);

		startSensor();
	}

	@Override
	protected void onStop() {
		stopSensor();
		stopTimer();
		super.onStop();
	}

	private void stopTimer() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer.purge();
			mTimer = null;
		}
	}

	private void startSensor() {
		mSensorManager = (SensorManager) getContext().getSystemService(
				Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		mAccelerationInterpolator = new Utils.AccelerationInterpolator();

		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_UI);
	}

	private void stopSensor() {
		mSensorManager.unregisterListener(this);
	}

	private void calibrated() {
		if (mCalibratedMessage != null) {
			final Bundle bundle = new Bundle();
			bundle.putFloat(BUNDLE_KEY_CALIBRATION_VALUE, mMaxAcceleration
					* CALIBRATION_VALUE_FACTOR);
			mCalibratedMessage.setData(bundle);
			Message.obtain(mCalibratedMessage).sendToTarget();
		}
	}

	public void setOnCalibratedListener(final OnCalibratedListener listener) {
		if (listener != null) {
			mCalibratedMessage = mListenersHandlerEx.obtainMessage(CALIBRATED,
					listener);
		} else {
			mCalibratedMessage = null;
		}
	}

	interface OnCalibratedListener {
		public void onCalibrated(DialogInterface dialog, float calibratedValue);
	}

	private static final class ListenersHandlerEx extends Handler {
		private final WeakReference<DialogInterface> mDialog;

		public ListenersHandlerEx(final Dialog dialog) {
			mDialog = new WeakReference<DialogInterface>(dialog);
		}

		@Override
		public void handleMessage(final Message msg) {
			switch (msg.what) {
			case CALIBRATED: {
				final Bundle bundle = msg.getData();
				((OnCalibratedListener) msg.obj).onCalibrated(mDialog.get(),
						bundle.getFloat(BUNDLE_KEY_CALIBRATION_VALUE));
			}
				break;
			}
		}
	}

	@Override
	public void onSensorChanged(final SensorEvent event) {
		if (!mInCalibration) {
			return;
		}

		final float acceleration = mAccelerationInterpolator
				.getAcceleration(event.values);
		if (acceleration > MAX_ACCELERATION_TO_CANCEL) {
			cancel();
		}

		if (acceleration > mMaxAcceleration) {
			mMaxAcceleration = acceleration;
		}
	}

	@Override
	public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
	}
}
