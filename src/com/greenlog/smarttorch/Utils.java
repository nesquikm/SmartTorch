package com.greenlog.smarttorch;

import android.content.Context;
import android.content.res.Resources;

public class Utils {
	public static String formatTimerTime(final Context context,
			final long seconds, final boolean forWidget) {
		if (seconds == 0)
			return context.getString(R.string.infinity);

		final Resources res = context.getResources();
		final int remainMin = (int) seconds / 60;
		final int remainSec = (int) seconds % 60;

		if (!forWidget) {
			final String remainMinString = remainMin > 0 ? res
					.getQuantityString(R.plurals.min, remainMin, remainMin)
					+ " " : "";
			return context.getString(R.string.timer_time, remainMinString,
					res.getQuantityString(R.plurals.sec, remainSec, remainSec));
		} else {
			if (remainMin > 0) {
				return res.getQuantityString(R.plurals.min, remainMin,
						remainMin);
			} else {
				return res.getQuantityString(R.plurals.sec, remainSec,
						remainSec);
			}
		}
	}
}
