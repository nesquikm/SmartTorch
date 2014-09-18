package com.greenlog.smarttorch;

import java.util.ArrayList;
import java.util.List;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TorchModeAdapter extends BaseAdapter {
	private final List<TorchMode> mTorchModes = new ArrayList<TorchMode>();
	private final Context mContext;
	private final LayoutInflater mLayoutInflater;

	public TorchModeAdapter(final Context context) {
		mContext = context;
		mLayoutInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// TODO: 02. remove this hardcode
		for (int i = 0; i < 20; i++) {
			mTorchModes.add((new TorchMode()).setShakeSensorEnabled(i % 2 == 0)
					.setTimeoutSec(i * 10));
		}
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

		((TextView) view.findViewById(R.id.stackview_item_text)).setText(Utils
				.formatTimerTime(mContext, torchMode.getTimeoutSec(), true));
		((ImageView) view.findViewById(R.id.stackview_item_icon))
				.setVisibility(torchMode.isShakeSensorEnabled() ? View.VISIBLE
						: View.GONE);

		// TODO: 00. What should i pass as tmp?!
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
}
