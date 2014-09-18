package com.greenlog.smarttorch;

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

	private final TorchModes mTorchModes;

	private final SettingsManager mSettingsManager;

	public StackRemoteViewsFactory(final Context context, final Intent intent) {
		mContext = context;
		mSettingsManager = new SettingsManager(context);

		mTorchModes = mSettingsManager.readTorchModes();
	}

	@Override
	public void onCreate() {
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
			itemText = mContext.getString(R.string.infinity);
		} else {
			itemText = torchMode.getTimeoutSec()
					+ mContext.getString(R.string.widget_seconds);
		}
		rv.setTextViewText(R.id.stackview_item_text, Utils.formatTimerTime(
				mContext, torchMode.getTimeoutSec(), true));
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
		// TODO: 11. eliminate "Loading..."
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