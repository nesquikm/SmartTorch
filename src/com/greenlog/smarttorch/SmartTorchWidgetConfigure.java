package com.greenlog.smarttorch;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.StackView;
import android.widget.Toast;

// TODO: 00. StackView in and out animations
// TODO: 03. Remove <action android:name="android.intent.action.MAIN" /> and <category android:name="android.intent.category.LAUNCHER" from manifest 
// TODO: 01. Orientation change tests!
// TODO: 01. Trash can with animation
// TODO: 01. Create new mode button with animation
// TODO: 02. Don't show this configuration window if one or more widgets are already placed
// TODO: 02. Show toast with "you always can double click to configure SmartTorch widget" 

public class SmartTorchWidgetConfigure extends Activity {
	private final static String BUNDLE_KEY_TORCH_MODES = "com.greenlog.smarttorch.TORCH_MODES";

	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	private TorchModeAdapter mTorchModeAdapter;

	private StackView mStackView;
	private SmartButton mTrashButton;
	private SmartButton mAddButton;

	private ImageView mFlyingTorch;

	private boolean mFlyingTorchIsInAnimation = false;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setResult(RESULT_CANCELED);
		setContentView(R.layout.configure_activity);

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

		final boolean isCreateFromExistingWidget = (torchMode != null);

		final Button saveButton = (Button) findViewById(R.id.save_button);
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				mTorchModeAdapter.saveTorchModes();
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

		mStackView = (StackView) findViewById(R.id.stack_view);
		if (savedInstanceState != null) {
			mTorchModeAdapter = new TorchModeAdapter(this,
					savedInstanceState.getBundle(BUNDLE_KEY_TORCH_MODES));
		} else {
			mTorchModeAdapter = new TorchModeAdapter(this);
		}

		// show selected on widget mode
		mStackView.setAdapter(mTorchModeAdapter);
		if (torchMode != null) {
			final int position = mTorchModeAdapter.findPosition(torchMode);
			if (position >= 0) {
				mStackView.setSelection(position);
			}
		}

		mTrashButton = (SmartButton) findViewById(R.id.trash_button);
		mAddButton = (SmartButton) findViewById(R.id.add_button);

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

		mFlyingTorch = (ImageView) findViewById(R.id.flying_torch);

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
				Log.v("sss", "--- start");
				mFlyingTorchIsInAnimation = true;
			}

			@Override
			public void onAnimationRepeat(final Animator animation) {
				Log.v("sss", "--- repeat");
				mFlyingTorchIsInAnimation = true;
			}

			@Override
			public void onAnimationEnd(final Animator animation) {
				Log.v("sss", "--- end " + (endAction != null));
				if (endAction != null) {
					endAction.run();
				}
				setButtonsState();
				mFlyingTorchIsInAnimation = false;
				mFlyingTorch.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onAnimationCancel(final Animator animation) {
				Log.v("sss", "--- cancel " + (endAction != null));
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
		mFlyingTorch.setAlpha(1.0f);

		mFlyingTorch.setVisibility(View.VISIBLE);

		final CalcFlyingTorchDimensions toDimensions = new CalcFlyingTorchDimensions(
				to);

		mFlyingTorch.animate().scaleX(toDimensions.getScale())
				.scaleY(toDimensions.getScale())
				.translationX(toDimensions.getX())
				.translationY(toDimensions.getY()).alpha(0);

	}
}
