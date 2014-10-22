package com.greenlog.smarttorch;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;

public class TorchModeAdapter extends BaseAdapter {
	private TorchModes mTorchModes;
	private final Context mContext;
	private final LayoutInflater mLayoutInflater;
	private final int[] mTimerValues;
	private final ArrayAdapter<String> mTimerValuesAdapter;
	private final int[] mKnockCountValues;
	private final ArrayAdapter<String> mKnockCountValuesAdapter;
	private boolean mShowKnockCount;

	private final SettingsManager mSettingsManager;

	/**
	 * Constructor, reads modes from bundle
	 */
	public TorchModeAdapter(final Context context, final Bundle bundle,
			final SettingsManager settingsManager) {
		mContext = context;
		mSettingsManager = settingsManager;

		mLayoutInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mTimerValues = mContext.getResources()
				.getIntArray(R.array.timer_values);
		mTimerValuesAdapter = new ArrayAdapter<String>(mContext,
				android.R.layout.simple_spinner_item);
		for (int i = 0; i < mTimerValues.length; i++) {
			mTimerValuesAdapter.add(Utils.formatTimerTime(mContext,
					mTimerValues[i], true));
		}
		mTimerValuesAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		mKnockCountValues = mContext.getResources().getIntArray(
				R.array.knock_count_values);
		mKnockCountValuesAdapter = new ArrayAdapter<String>(mContext,
				android.R.layout.simple_spinner_item);
		for (int i = 0; i < mKnockCountValues.length; i++) {
			mKnockCountValuesAdapter.add(Utils.formatKnockCount(mContext,
					mKnockCountValues[i]));
		}
		mKnockCountValuesAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		if (bundle != null) {
			mTorchModes = new TorchModes(bundle);
		} else {
			mTorchModes = new TorchModes();
		}
	}

	/**
	 * Constructor, reads modes from settings
	 */
	public TorchModeAdapter(final Context context,
			final SettingsManager settingsManager) {
		this(context, null, settingsManager);

		mTorchModes = mSettingsManager.readTorchModes();
	}

	@Override
	public int getCount() {
		return mTorchModes.size();
	}

	@Override
	public TorchMode getItem(final int position) {
		return mTorchModes.get(position);
	}

	@Override
	public long getItemId(final int position) {
		return position;
	}

	@Override
	public View getView(final int position, final View convertView,
			final ViewGroup parent) {
		final View view;
		if (convertView == null) {
			view = mLayoutInflater.inflate(R.layout.configure_stackview_item,
					parent, false);
		} else {
			view = convertView;
		}

		final TorchMode torchMode = getItem(position);

		final CheckBox shakeCheckbox = (CheckBox) view
				.findViewById(R.id.shake_checkbox);
		shakeCheckbox.setChecked(torchMode.isShakeSensorEnabled());

		shakeCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(final CompoundButton buttonView,
					final boolean isChecked) {
				// save the selected state
				torchMode.setShakeSensorEnabled(isChecked);
			}
		});

		final Spinner timePicker;
		timePicker = (Spinner) view.findViewById(R.id.time_picker);
		// do it only once
		if (convertView == null) {
			timePicker.setAdapter(mTimerValuesAdapter);

			timePicker.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(final AdapterView<?> parent,
						final View view, final int position, final long id) {

					// save the selected timeout
					torchMode.setTimeoutSec(mTimerValues[position]);
				}

				@Override
				public void onNothingSelected(final AdapterView<?> parent) {
				}
			});
		}

		// hide shakeCheckbox when timer == infinity
		shakeCheckbox
				.setVisibility((mTimerValues[position] == 0) ? View.INVISIBLE
						: View.VISIBLE);

		int selectedTimePosition = 0;
		for (int i = 0; i < mTimerValues.length; i++) {
			if (mTimerValues[i] == torchMode.getTimeoutSec()) {
				selectedTimePosition = i;
				break;
			}
		}

		timePicker.setSelection(selectedTimePosition, false);

		final Spinner knockCountPicker = (Spinner) view
				.findViewById(R.id.knock_count_picker);
		// do it only once
		if (convertView == null) {
			knockCountPicker.setAdapter(mKnockCountValuesAdapter);

			knockCountPicker
					.setOnItemSelectedListener(new OnItemSelectedListener() {
						@Override
						public void onItemSelected(final AdapterView<?> parent,
								final View view, final int position,
								final long id) {
							// remove duplicated
							boolean removed = false;
							if (mKnockCountValues[position] > 0) {
								for (int i = 0; i < mTorchModes.size(); i++) {
									if (torchMode != mTorchModes.get(i)
											&& mTorchModes.get(i)
													.getKnockCount() == mKnockCountValues[position]) {
										mTorchModes.get(i).setKnockCount(0);
										removed = true;
									}
								}
							}
							torchMode
									.setKnockCount(mKnockCountValues[position]);
							if (removed) {
								notifyDataSetChanged();
							}
						}

						@Override
						public void onNothingSelected(
								final AdapterView<?> parent) {
						}
					});
		}

		int selectedKnockCountPosition = 0;
		for (int i = 0; i < mKnockCountValues.length; i++) {
			if (mKnockCountValues[i] == torchMode.getKnockCount()) {
				selectedKnockCountPosition = i;
				break;
			}
		}

		knockCountPicker.setSelection(selectedKnockCountPosition, false);
		knockCountPicker.setVisibility(mShowKnockCount ? View.VISIBLE
				: View.INVISIBLE);

		return view;
	}

	public Bundle getTorchModesBundle() {
		return mTorchModes.getBundle();
	}

	public void saveTorchModes() {
		mSettingsManager.writeTorchModes(mTorchModes);
	}

	public int findPosition(final TorchMode torchMode) {
		return mTorchModes.indexOf(torchMode);
	}

	public void add(final TorchMode torchMode) {
		mTorchModes.add(torchMode);
		notifyDataSetChanged();
	}

	public void remove(final int position) {
		mTorchModes.remove(position);
		notifyDataSetChanged();
	}

	public void setShowKnockCount(final boolean showKnockCount) {
		mShowKnockCount = showKnockCount;
		notifyDataSetChanged();
	}
}
