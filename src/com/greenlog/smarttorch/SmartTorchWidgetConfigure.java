package com.greenlog.smarttorch;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.StackView;
import android.widget.Toast;

import com.greenlog.smarttorch.ShakeSensitivityCalibrateDialog.OnCalibratedListener;

// TODO: 03. Remove <action android:name="android.intent.action.MAIN" /> and <category android:name="android.intent.category.LAUNCHER" from manifest 
// TODO: 01. Orientation change tests!

public class SmartTorchWidgetConfigure extends Activity {
	private final static String BUNDLE_KEY_TORCH_MODES = "com.greenlog.smarttorch.TORCH_MODES";
	private final static String IS_DIALOG_TO_CONFIGURE_SHOWING = "com.greenlog.smarttorch.IS_DIALOG_TO_CONFIGURE_SHOWING";
	private final static String LAST_SHAKE_SENSITIVITY_MODE = "com.greenlog.smarttorch.LAST_SHAKE_SENSITIVITY_MODE";
	private final static String SHAKE_SENSITIVITY_CALIBRATED_VALUE = "com.greenlog.smarttorch.SHAKE_SENSITIVITY_CALIBRATED_VALUE";
	private final static String SHAKE_SENSITIVITY_MODE_RESET = "com.greenlog.smarttorch.SHAKE_SENSITIVITY_MODE_RESET";

	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	private TorchModeAdapter mTorchModeAdapter;

	private StackView mStackView;
	private SmartButton mTrashButton;
	private SmartButton mAddButton;

	private ImageView mFlyingTorch;

	private boolean mFlyingTorchIsInAnimation = false;

	private SmartSpinner mAccelerometerSensPicker;

	private AlertDialog mDialogToConfigure;
	private ShakeSensitivityCalibrateDialog mShakeSensitivityCalibrateDialog;

	private int mLastShakeSensMode;
	private float mShakeSensitivityCalibratedValue;

	private SettingsManager mSettingsManager;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setResult(RESULT_CANCELED);
		setContentView(R.layout.configure_activity);

		mSettingsManager = new SettingsManager(this);

		// Find the widget id from the intent.
		final Intent intent = getIntent();
		final Bundle extras = intent.getExtras();
		TorchMode torchMode = null;
		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
			if (TorchMode.isTorchModePresents(extras)) {
				torchMode = new TorchMode(extras);
			}
		}

		final boolean isCreateFromExistingWidget = (!AppWidgetManager.ACTION_APPWIDGET_CONFIGURE
				.equals(intent.getAction()));

		// Save/"OK" button
		final Button saveButton = (Button) findViewById(R.id.save_button);
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				mTorchModeAdapter.saveTorchModes();
				mSettingsManager.writeShakeSensorSensitivity(
						mLastShakeSensMode, mShakeSensitivityCalibratedValue);
				// Update ALL widgets config!
				SmartTorchService.sendCommandToService(
						SmartTorchWidgetConfigure.this,
						SmartTorchService.SERVICE_ACTION_UPDATE_WIDGETS_CONFIG,
						null);

				// when creating NOT from existing widget
				if (!isCreateFromExistingWidget) {
					final Intent resultValue = new Intent();
					resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
							mAppWidgetId);
					setResult(RESULT_OK, resultValue);

					Toast.makeText(SmartTorchWidgetConfigure.this,
							R.string.double_click_to_configure,
							Toast.LENGTH_SHORT).show();
				}
				finish();
			}
		});

		// StackView
		mStackView = (StackView) findViewById(R.id.stack_view);
		if (savedInstanceState != null) {
			mTorchModeAdapter = new TorchModeAdapter(this,
					savedInstanceState.getBundle(BUNDLE_KEY_TORCH_MODES),
					mSettingsManager);
		} else {
			mTorchModeAdapter = new TorchModeAdapter(this, mSettingsManager);
		}

		// show selected on widget mode
		mStackView.setAdapter(mTorchModeAdapter);
		if (torchMode != null) {
			final int position = mTorchModeAdapter.findPosition(torchMode);
			if (position >= 0) {
				mStackView.setSelection(position);
			}
		}

		// Trash button
		mTrashButton = (SmartButton) findViewById(R.id.trash_button);
		mTrashButton.setClickable(true);
		mTrashButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				if (mFlyingTorchIsInAnimation) {
					return;
				}
				moveFlyingTorchTo(mStackView, mTrashButton, null);
				mTorchModeAdapter.remove(mStackView.getDisplayedChild()
						% mTorchModeAdapter.getCount());
				// setButtonsState();
			}
		});

		// Add button
		mAddButton = (SmartButton) findViewById(R.id.add_button);
		mAddButton.setClickable(true);
		mAddButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				if (mFlyingTorchIsInAnimation) {
					return;
				}
				moveFlyingTorchTo(mAddButton, mStackView, new Runnable() {
					@Override
					public void run() {
						final TorchMode torchMode = TorchMode
								.fromString(getString(R.string.default_mode));
						if (torchMode != null) {
							mTorchModeAdapter.add(torchMode);
							mStackView.setDisplayedChild(mTorchModeAdapter
									.getCount() - 1);
						}
					}
				});
			}
		});

		// Flying torch
		mFlyingTorch = (ImageView) findViewById(R.id.flying_torch);

		// Shake sensitivity picker
		mAccelerometerSensPicker = (SmartSpinner) findViewById(R.id.accelerometer_sens_picker);
		final ArrayAdapter<CharSequence> sensAdapter = ArrayAdapter
				.createFromResource(this,
						R.array.accelerometer_sensitivity_modes,
						android.R.layout.simple_spinner_item);
		sensAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mAccelerometerSensPicker.setAdapter(sensAdapter);
		mAccelerometerSensPicker
				.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(final AdapterView<?> parent,
							final View view, final int position, final long id) {
						switch (position) {
						case SettingsManager.SHAKE_SENSITIVITY_LOW:
						case SettingsManager.SHAKE_SENSITIVITY_MEDIUM:
						case SettingsManager.SHAKE_SENSITIVITY_HIGH:
							// Save last selected
							mLastShakeSensMode = mAccelerometerSensPicker
									.getSelectedItemPosition();
							break;
						case SettingsManager.SHAKE_SENSITIVITY_CALIBRATED:
							showDialogToConfigure();
							break;
						}
					}
				});

		if (savedInstanceState == null) {
			mLastShakeSensMode = mSettingsManager
					.readShakeSensorSensitivityMode();
			mShakeSensitivityCalibratedValue = mSettingsManager
					.readShakeSensorSensitivityCalibratedValue();
			mAccelerometerSensPicker.setSelectionSilently(mLastShakeSensMode);
		}

		if (savedInstanceState != null) {
			mLastShakeSensMode = savedInstanceState.getInt(
					LAST_SHAKE_SENSITIVITY_MODE,
					SettingsManager.SHAKE_SENSITIVITY_DEFAULT);
			mShakeSensitivityCalibratedValue = savedInstanceState.getFloat(
					SHAKE_SENSITIVITY_CALIBRATED_VALUE, -1);
			if (savedInstanceState.getBoolean(SHAKE_SENSITIVITY_MODE_RESET,
					false)) {
				// Spinner don't want to setSelection when saved instance is
				// present. So, postpone the call
				final Handler handler = new Handler();
				handler.post(new Runnable() {
					@Override
					public void run() {
						mAccelerometerSensPicker
								.setSelectionSilently(mLastShakeSensMode);
					}
				});
			}
		}
		restoreDialogToConfigure(savedInstanceState);

		// Init buttons state
		if (savedInstanceState == null) {
			setButtonsAnimated(false);
			setButtonsState(false);
			setButtonsAnimated(true);
			setButtonsState();
		} else {
			setButtonsAnimated(false);
			setButtonsState();
			setButtonsAnimated(true);
		}
	}

	private void setButtonsAnimated(final boolean animated) {
		mTrashButton.setAnimated(animated);
		mAddButton.setAnimated(animated);
	}

	private void setButtonsState(final boolean enabled) {
		mTrashButton.setEnabled(enabled);
		mAddButton.setEnabled(enabled);
	}

	private void setButtonsState() {
		mTrashButton.setEnabled(mTorchModeAdapter.getCount() > 1);
		mAddButton
				.setEnabled(mTorchModeAdapter.getCount() < SettingsManager.MAX_MODE_COUNT);
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBundle(BUNDLE_KEY_TORCH_MODES,
				mTorchModeAdapter.getTorchModesBundle());
		outState.putBoolean(IS_DIALOG_TO_CONFIGURE_SHOWING,
				(mDialogToConfigure != null && mDialogToConfigure.isShowing()));
		outState.putInt(LAST_SHAKE_SENSITIVITY_MODE, mLastShakeSensMode);
		outState.putFloat(SHAKE_SENSITIVITY_CALIBRATED_VALUE,
				mShakeSensitivityCalibratedValue);

		if (mShakeSensitivityCalibrateDialog != null) {
			// Calibration dialog is shown. So, kill it onDestroy and set the
			// flag for reset Spinner state onCreate
			outState.putBoolean(SHAKE_SENSITIVITY_MODE_RESET, true);
		}
	}

	private void showDialogToConfigure() {
		if (mDialogToConfigure != null) {
			mDialogToConfigure.dismiss();
		}

		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.configure_dialog_to_configure);
		builder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						mShakeSensitivityCalibrateDialog = new ShakeSensitivityCalibrateDialog(
								SmartTorchWidgetConfigure.this);
						mShakeSensitivityCalibrateDialog
								.setOnCancelListener(new OnCancelListener() {
									@Override
									public void onCancel(
											final DialogInterface dialog) {
										cancelCalibration();
									}
								});
						mShakeSensitivityCalibrateDialog
								.setOnCalibratedListener(new OnCalibratedListener() {
									@Override
									public void onCalibrated(
											final DialogInterface dialog) {
										mLastShakeSensMode = SettingsManager.SHAKE_SENSITIVITY_CALIBRATED;
										Toast.makeText(
												SmartTorchWidgetConfigure.this,
												R.string.calibrated_successful,
												Toast.LENGTH_SHORT).show();
									}
								});
						mShakeSensitivityCalibrateDialog.show();
					}
				});
		builder.setNegativeButton(android.R.string.cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						mAccelerometerSensPicker
								.setSelectionSilently(mLastShakeSensMode);
					}
				});
		builder.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(final DialogInterface dialog) {
				mAccelerometerSensPicker
						.setSelectionSilently(mLastShakeSensMode);
			}
		});
		mDialogToConfigure = builder.create();
		mDialogToConfigure.show();
	}

	private void restoreDialogToConfigure(final Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			if (savedInstanceState.getBoolean(IS_DIALOG_TO_CONFIGURE_SHOWING,
					false)) {
				showDialogToConfigure();
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		cancelCalibration();
	}

	@Override
	protected void onDestroy() {
		if (mDialogToConfigure != null) {
			mDialogToConfigure.dismiss();
			mDialogToConfigure = null;
		}

		// So, we Spinner.setSelectionSilently() don't work here, and we put the
		// flag onSaveInstanceState
		cancelCalibration();

		super.onDestroy();
	}

	private void cancelCalibration() {
		if (mShakeSensitivityCalibrateDialog != null) {
			mShakeSensitivityCalibrateDialog.cancel();
			mShakeSensitivityCalibrateDialog = null;
			Toast.makeText(this, R.string.calibration_cancelled,
					Toast.LENGTH_SHORT).show();

			mAccelerometerSensPicker.setSelectionSilently(mLastShakeSensMode);
		}
	}

	private void moveFlyingTorchTo(final View from, final View to,
			final Runnable endAction) {
		class CalcFlyingTorchDimensions {
			private final float mScale;
			private final float mX;
			private final float mY;

			public CalcFlyingTorchDimensions(final View view) {
				final ImageView flyingTorch = (ImageView) findViewById(R.id.flying_torch);
				final float scaleX = (float) view.getHeight()
						/ flyingTorch.getHeight();
				final float scaleY = (float) view.getHeight()
						/ flyingTorch.getHeight();
				mScale = scaleX < scaleY ? scaleX : scaleY;

				mX = view.getLeft() + view.getWidth() / 2f
						- flyingTorch.getWidth() / 2f - flyingTorch.getLeft();
				mY = view.getTop() + view.getHeight() / 2f
						- flyingTorch.getHeight() / 2f - flyingTorch.getTop();
			}

			public float getScale() {
				return mScale;
			}

			public float getX() {
				return mX;
			}

			public float getY() {
				return mY;
			}
		}

		mFlyingTorch.animate().setListener(new AnimatorListener() {
			@Override
			public void onAnimationStart(final Animator animation) {
				mFlyingTorchIsInAnimation = true;
			}

			@Override
			public void onAnimationRepeat(final Animator animation) {
				mFlyingTorchIsInAnimation = true;
			}

			@Override
			public void onAnimationEnd(final Animator animation) {
				if (endAction != null) {
					endAction.run();
				}
				setButtonsState();
				mFlyingTorchIsInAnimation = false;
				mFlyingTorch.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onAnimationCancel(final Animator animation) {
				setButtonsState();
				mFlyingTorchIsInAnimation = false;
				mFlyingTorch.setVisibility(View.INVISIBLE);
			}
		});

		final CalcFlyingTorchDimensions fromDimensions = new CalcFlyingTorchDimensions(
				from);

		mFlyingTorch.setScaleX(fromDimensions.getScale());
		mFlyingTorch.setScaleY(fromDimensions.getScale());
		mFlyingTorch.setTranslationX(fromDimensions.getX());
		mFlyingTorch.setTranslationY(fromDimensions.getY());
		mFlyingTorch.setAlpha(0.7f);

		mFlyingTorch.setVisibility(View.VISIBLE);

		final CalcFlyingTorchDimensions toDimensions = new CalcFlyingTorchDimensions(
				to);

		mFlyingTorch.animate().scaleX(toDimensions.getScale())
				.scaleY(toDimensions.getScale())
				.translationX(toDimensions.getX())
				.translationY(toDimensions.getY()).alpha(0f);

	}
}
