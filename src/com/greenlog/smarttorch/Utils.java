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

	public static class AccelerationInterpolator {
		private final static float ACCELERATION_FILTER_ALPHA_SLOW = 0.2f;
		private final static float ACCELERATION_FILTER_ALPHA_FAST = 1f;

		private final float mAccelerationSlow[] = new float[3];
		private final float mAccelerationFast[] = new float[3];
		private boolean mIsFirst = true;

		public float getAcceleration(final float[] values) {
			if (mIsFirst) {
				mIsFirst = false;
				for (int i = 0; i < 3; i++) {
					mAccelerationSlow[i] = mAccelerationFast[i] = values[i];
				}
				return 0f;
			}

			float max = 0f;
			for (int i = 0; i < 3; i++) {
				mAccelerationSlow[i] = ACCELERATION_FILTER_ALPHA_SLOW
						* values[i] + (1 - ACCELERATION_FILTER_ALPHA_SLOW)
						* mAccelerationSlow[i];
				mAccelerationFast[i] = ACCELERATION_FILTER_ALPHA_FAST
						* values[i] + (1 - ACCELERATION_FILTER_ALPHA_FAST)
						* mAccelerationFast[i];
				final float absDiff = Math.abs(mAccelerationSlow[i]
						- mAccelerationFast[i]);
				if (absDiff > max) {
					max = absDiff;
				}
			}

			return max;
		}
	}
}
