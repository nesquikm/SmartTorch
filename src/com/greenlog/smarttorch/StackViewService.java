package com.greenlog.smarttorch;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
	// TODO: remove hardcoded count
	private static final int mCount = 3;

	private final Context mContext;
	private final int mAppWidgetId;
	private int mTmp;

	public StackRemoteViewsFactory(final Context context, final Intent intent) {
		mContext = context;
		mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
				AppWidgetManager.INVALID_APPWIDGET_ID);
	}

	@Override
	public void onCreate() {
		Log.v("sss", "onCreate");
	}

	@Override
	public void onDataSetChanged() {
		mTmp++;
	}

	@Override
	public void onDestroy() {

	}

	@Override
	public int getCount() {
		return mCount;
	}

	@Override
	public RemoteViews getViewAt(final int position) {
		final RemoteViews rv = new RemoteViews(mContext.getPackageName(),
				R.layout.stackview_item);

		rv.setTextViewText(R.id.stackview_item_text, position * 10 + mTmp + "s");

		final Bundle extras = new Bundle();
		extras.putInt(SmartTorchWidget.CLICK_ACTION_ITEM, position);
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
		// TODO: maybe, we must store ID in the items
		return position;
	}

	@Override
	public boolean hasStableIds() {
		// TODO: has stable ids?
		return true;
	}
}