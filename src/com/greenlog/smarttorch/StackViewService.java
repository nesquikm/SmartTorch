package com.greenlog.smarttorch;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class StackViewService extends RemoteViewsService {
	@Override
	public RemoteViewsFactory onGetViewFactory(final Intent intent) {
		Log.v("sss", "onGetViewFactory");
		return new StackRemoteViewsFactory(getApplicationContext(), intent);
	}

}

class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
	private final Context mContext;

	private final List<TorchMode> mTorchModes = new ArrayList<TorchMode>();

	public StackRemoteViewsFactory(final Context context, final Intent intent) {
		mContext = context;
	}

	@Override
	public void onCreate() {
		Log.v("sss", "onCreate");
		// TODO: 02. remove this hardcode
		for (int i = 0; i < 5; i++) {
			mTorchModes.add((new TorchMode()).setShakeSensorEnabled(i % 2 == 0)
					.setTimeoutSec(i * 10));
		}
	}

	@Override
	public void onDataSetChanged() {
	}

	@Override
	public void onDestroy() {

	}

	@Override
	public int getCount() {
		return mTorchModes.size();
	}

	@Override
	public RemoteViews getViewAt(final int position) {
		final RemoteViews rv = new RemoteViews(mContext.getPackageName(),
				R.layout.stackview_item);

		final TorchMode torchMode = getItem(position);
		String itemText;
		if (torchMode.isInfinitely()) {
			itemText = mContext.getString(R.string.widget_infinity);
		} else {
			itemText = torchMode.getTimeoutSec()
					+ mContext.getString(R.string.widget_seconds);
		}
		rv.setTextViewText(R.id.stackview_item_text, itemText);
		rv.setViewVisibility(R.id.stackview_item_icon,
				torchMode.isShakeSensorEnabled() ? View.VISIBLE : View.GONE);

		final Bundle extras = torchMode.getBundle();

		final Intent fillInIntent = new Intent();
		fillInIntent.putExtras(extras);
		rv.setOnClickFillInIntent(R.id.stackview_item, fillInIntent);
		return rv;
	}

	@Override
	public RemoteViews getLoadingView() {
		return null;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public long getItemId(final int position) {
		return position;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	private TorchMode getItem(final int position) {
		return mTorchModes.get(position);
	}
}