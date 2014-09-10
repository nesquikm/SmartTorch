package com.greenlog.smarttorch;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

// TODO: android:configure="com.example.android.ExampleAppWidgetConfigure"
// TODO: android:previewImage="@drawable/preview"
// TODO: disallow resizing

public class SmartTorchWidget extends AppWidgetProvider {
	private static final String CLICK_ACTION = "com.greenlog.smarttorch.CLICK_ACTION";
	public static final String CLICK_ACTION_ITEM = "com.greenlog.smarttorch.CLICK_ACTION_ITEM";

	private static boolean mTmp;

	@Override
	public void onReceive(final Context context, final Intent intent) {
		final AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(context);
		if (intent.getAction().equals(CLICK_ACTION)) {
			final int appWidgetId = intent.getIntExtra(
					AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
			final int viewIndex = intent.getIntExtra(CLICK_ACTION_ITEM, 0);
			Toast.makeText(context, "View " + viewIndex + mTmp,
					Toast.LENGTH_SHORT).show();

			// final Bundle options = appWidgetManager
			// .getAppWidgetOptions(appWidgetId);
			// options.putBoolean("TODO", false);
			// appWidgetManager.updateAppWidgetOptions(appWidgetId, options);
			// TODO: here final we can update final only dataset dataset
			appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId,
					R.id.stack_view);

			mTmp = !mTmp;
			update(context);
		}

		super.onReceive(context, intent);
	}

	@Override
	public void onUpdate(final Context context,
			final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
		update(context);
		// for (int i = 0; i < appWidgetIds.length; ++i) {
		// final Intent intent = new Intent(context, StackViewService.class);
		// intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
		// appWidgetIds[i]);
		// intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
		//
		// final RemoteViews rv = new RemoteViews(context.getPackageName(),
		// R.layout.appwidget);
		// if (Build.VERSION.SDK_INT >= 14) {
		// rv.setRemoteAdapter(R.id.stack_view, intent);
		// } else {
		// rv.setRemoteAdapter(appWidgetIds[i], R.id.stack_view, intent);
		// }
		//
		// final Intent clickIntent = new Intent(context,
		// SmartTorchWidget.class);
		// clickIntent.setAction(CLICK_ACTION);
		// clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
		// appWidgetIds[i]);
		// clickIntent.setData(Uri.parse(intent
		// .toUri(Intent.URI_INTENT_SCHEME)));
		//
		// final PendingIntent clickPendingIntent = PendingIntent
		// .getBroadcast(context, 0, clickIntent,
		// PendingIntent.FLAG_UPDATE_CURRENT);
		// rv.setPendingIntentTemplate(R.id.stack_view, clickPendingIntent);
		//
		// appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
		// Log.v("sss", "onGetViewFactory");
		//
		// }
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	@SuppressWarnings("deprecation")
	private void update(final Context context) {
		final AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(context);
		final int[] appWidgetIds = appWidgetManager
				.getAppWidgetIds(new ComponentName(context,
						SmartTorchWidget.class));

		for (int i = 0; i < appWidgetIds.length; ++i) {
			final Intent intent = new Intent(context, StackViewService.class);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					appWidgetIds[i]);
			intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

			final RemoteViews rv = new RemoteViews(context.getPackageName(),
					R.layout.appwidget);
			if (Build.VERSION.SDK_INT >= 14) {
				rv.setRemoteAdapter(R.id.stack_view, intent);
			} else {
				rv.setRemoteAdapter(appWidgetIds[i], R.id.stack_view, intent);
			}

			// TODO: rename tmp!
			rv.setViewVisibility(R.id.stack_view, !mTmp ? View.VISIBLE
					: View.GONE);
			rv.setViewVisibility(R.id.tmp, mTmp ? View.VISIBLE : View.GONE);

			final Intent clickIntent = new Intent(context,
					SmartTorchWidget.class);
			clickIntent.setAction(CLICK_ACTION);
			clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					appWidgetIds[i]);
			clickIntent.setData(Uri.parse(intent
					.toUri(Intent.URI_INTENT_SCHEME)));

			final PendingIntent clickPendingIntent = PendingIntent
					.getBroadcast(context, 0, clickIntent,
							PendingIntent.FLAG_UPDATE_CURRENT);

			if (!mTmp) {
				rv.setPendingIntentTemplate(R.id.stack_view, clickPendingIntent);
			} else {
				rv.setOnClickPendingIntent(R.id.tmp, clickPendingIntent);
			}

			appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
		}
	}
}
