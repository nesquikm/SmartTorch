package com.greenlog.smarttorch;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
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

	private final SettingsManager mSettingsManager;

	/**
	 * Constructor, reads modes from bundle
	 */
	public TorchModeAdapter(final Context context, final Bundle bundle) {
		mContext = context;
		mSettingsManager = new SettingsManager(context);

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

		if (bundle != null) {
			mTorchModes = new TorchModes(bundle);
		} else {
			mTorchModes = new TorchModes();
		}
	}

	/**
	 * Constructor, reads modes from settings
	 */
	public TorchModeAdapter(final Context context) {
		this(context, null);

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
					// hide shakeCheckbox when timer == infinity
					shakeCheckbox
							.setVisibility((mTimerValues[position] == 0) ? View.INVISIBLE
									: View.VISIBLE);
					// save the selected timeout
					torchMode.setTimeoutSec(mTimerValues[position]);
				}

				@Override
				public void onNothingSelected(final AdapterView<?> parent) {
				}
			});
		}

		int selectedTimePosition = 0;
		for (int i = 0; i < mTimerValues.length; i++) {
			if (mTimerValues[i] == torchMode.getTimeoutSec()) {
				selectedTimePosition = i;
				break;
			}
		}

		timePicker.setSelection(selectedTimePosition, false);

		// TODO: 02. Do we support drag'n'drop?
		// TODO: 02. What should i pass as tmp?!
		final String tmp = "";
		view.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(final View v) {
				final ClipData.Item item = new ClipData.Item(tmp);
				final String[] mimeTypes = { ClipDescription.MIMETYPE_TEXT_PLAIN };
				final ClipData dragData = new ClipData(tmp, mimeTypes, item);
				v.startDrag(dragData, // the data to be dragged
						new View.DragShadowBuilder(v), // the drag shadow
														// builder
						torchMode, // no need to use local data
						0 // flags (not currently used, set to 0)
				);
				Log.v("sss", "start drag: " + tmp);

				return false;
			}
		});
		view.setOnDragListener(new OnDragListener() {
			@Override
			public boolean onDrag(final View v, final DragEvent event) {
				switch (event.getAction()) {
				case DragEvent.ACTION_DRAG_STARTED:
					Log.v("sss",
							"drag started "
									+ ((TorchMode) event.getLocalState())
											.getTimeoutSec());
					break;
				case DragEvent.ACTION_DRAG_ENDED:
					Log.v("sss",
							"drag ended "
									+ ((TorchMode) event.getLocalState())
											.getTimeoutSec() + " result "
									+ event.getResult());
					break;
				}
				return false;
			}
		});
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
}
