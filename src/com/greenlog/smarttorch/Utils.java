package com.greenlog.smarttorch;

import java.util.ArrayList;

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

	public static String formatKnockCount(final Context context, final int count) {
		final Resources res = context.getResources();
		if (count == 0) {
			return res.getString(R.string.knock_disabled);
		}
		return res.getQuantityString(R.plurals.knocks, count, count);
	}

	public static String formatProximityTimerTime(final Context context,
			final long seconds) {
		final Resources res = context.getResources();
		if (seconds == 0) {
			return res.getString(R.string.proximity_disabled);
		}

		return formatTimerTime(context, seconds, false);
	}

	public static class AccelerationHelper {
		private final static float ACCELERATION_FILTER_ALPHA_SLOW = 0.2f;
		private final static float ACCELERATION_FILTER_ALPHA_FAST = 1f;

		private final float KNOCK_ACCELERATION_HIGH_THRESHOLD = 9.0f;

		private final long WINDOW_SIZE = 900 * 1000 * 1000;
		private final long KNOCK_TIMEOUT_MIN = 80 * 1000 * 1000;

		private final float mAccelerationSlow[] = new float[3];
		private final float mAccelerationFast[] = new float[3];
		private boolean mIsFirst = true;

		private final float[] mLastAcceleration = new float[3];
		private long mLastTimeStamp;

		private Float mLastLinearAcceleration = null;
		private float[] mLastLinearAccelerationVector = null;

		private Long mPauseUntil = null;

		private final SampleQueue mSampleQueue = new SampleQueue();

		public void setEvent(final float[] values, final long timeStamp) {
			for (int i = 0; i < 3; i++) {
				mLastAcceleration[i] = values[i];
			}
			mLastTimeStamp = timeStamp;
			mLastLinearAccelerationVector = null;
			mLastLinearAcceleration = null;
		}

		private float[] getLinearAccelerationVector() {
			if (mLastLinearAccelerationVector != null) {
				return mLastLinearAccelerationVector;
			}

			if (mIsFirst) {
				mIsFirst = false;
				for (int i = 0; i < 3; i++) {
					mAccelerationSlow[i] = mAccelerationFast[i] = mLastAcceleration[i];
				}
				mLastLinearAcceleration = 0f;
				return (new float[3]);
			}

			for (int i = 0; i < 3; i++) {
				mAccelerationSlow[i] = ACCELERATION_FILTER_ALPHA_SLOW
						* mLastAcceleration[i]
						+ (1 - ACCELERATION_FILTER_ALPHA_SLOW)
						* mAccelerationSlow[i];
				mAccelerationFast[i] = ACCELERATION_FILTER_ALPHA_FAST
						* mLastAcceleration[i]
						+ (1 - ACCELERATION_FILTER_ALPHA_FAST)
						* mAccelerationFast[i];
			}

			mLastLinearAccelerationVector = new float[3];
			for (int i = 0; i < 3; i++) {
				mLastLinearAccelerationVector[i] = mAccelerationSlow[i]
						- mAccelerationFast[i];
			}

			return mLastLinearAccelerationVector;
		}

		public float getLinearAcceleration() {
			if (mLastLinearAcceleration != null) {
				return mLastLinearAcceleration;
			}

			mLastLinearAcceleration = getVectorLength(getLinearAccelerationVector());

			return mLastLinearAcceleration;
		}

		public void pauseKnockCount(final long timeout) {
			mPauseUntil = mLastTimeStamp + timeout * 1000 * 1000;
			resetKnockCount();
		}

		private void resetKnockCount() {
			mSampleQueue.flush();
		}

		public int getKnockCount() {
			if (mPauseUntil != null) {
				if (mLastTimeStamp < mPauseUntil) {
					return 0;
				}
				mPauseUntil = null;
			}

			mSampleQueue.add(getLinearAccelerationVector(), mLastTimeStamp);

			return mSampleQueue.getKnockCount();
		}

		private float getVectorLength(final float[] vector) {
			return (float) Math.sqrt(vector[0] * vector[0] + vector[1]
					* vector[1] + vector[2] * vector[2]);
		}

		private double getAbsAngleBetweenVectors(final float[] v1,
				final float[] v2) {
			final double v1v2 = v1[0] * v2[0] + v1[1] * v2[1] + v1[2] * v2[2];
			final double v1mod = Math.sqrt(v1[0] * v1[0] + v1[1] * v1[1]
					+ v1[2] * v1[2]);
			final double v2mod = Math.sqrt(v2[0] * v2[0] + v2[1] * v2[1]
					+ v2[2] * v2[2]);
			final double v1v2cos = v1v2 / (v1mod * v2mod);
			return Math.abs(Math.acos(v1v2cos));
		}

		private class SampleQueue {
			private final ArrayList<Sample> mQueue = new ArrayList<Sample>();
			private int mLastKnockCount = 0;

			public void add(final float[] vector, final long timeStamp) {
				removeOld(timeStamp);

				if (getLinearAcceleration() > KNOCK_ACCELERATION_HIGH_THRESHOLD) {
					mQueue.add(new Sample(vector, timeStamp));
				}
			}

			public int getKnockCount() {
				// find the longest
				Sample longestVector = null;
				int longestVectorLocation = -1;
				for (int i = 0; i < mQueue.size(); i++) {
					final Sample s = mQueue.get(i);
					if (longestVector == null
							|| s.getLength() > longestVector.getLength()) {
						longestVector = s;
						longestVectorLocation = i;
					}
				}

				int knockCount = 0;
				if (longestVector != null) {

					knockCount = 1;

					Sample last = longestVector;
					for (int i = longestVectorLocation; i >= 0; i--) {
						final Sample s = mQueue.get(i);
						if ((Math.abs(last.getTimeStamp() - s.getTimeStamp()) > KNOCK_TIMEOUT_MIN)
								&& (getAbsAngleBetweenVectors(last.getVector(),
										s.getVector()) * 180 / Math.PI < 30)) {
							last = s;
							knockCount++;
						}
					}
					last = longestVector;
					for (int i = longestVectorLocation; i < mQueue.size(); i++) {
						final Sample s = mQueue.get(i);
						if ((Math.abs(last.getTimeStamp() - s.getTimeStamp()) > KNOCK_TIMEOUT_MIN)
								&& (getAbsAngleBetweenVectors(last.getVector(),
										s.getVector()) * 180 / Math.PI < 30)) {
							last = s;
							knockCount++;
						}
					}
				}

				if (mLastKnockCount > 1 && mLastKnockCount > knockCount) {
					flush();
					final int ret = mLastKnockCount;
					mLastKnockCount = 0;
					return ret;
				}
				mLastKnockCount = knockCount;

				return 0;
			}

			public void flush() {
				mQueue.clear();
			}

			private void removeOld(final long now) {
				while (!mQueue.isEmpty()) {
					final Sample sample = mQueue.get(0);
					if (now - sample.getTimeStamp() > WINDOW_SIZE) {
						mQueue.remove(0);
					} else {
						break;
					}
				}
			}
		}

		private class Sample {
			private final float[] mVector = new float[3];
			private final long mTimeStamp;
			private final float mLength;

			public Sample(final float[] vector, final long timeStamp) {
				copyVector(vector, mVector);
				mTimeStamp = timeStamp;
				mLength = getVectorLength(mVector);
			}

			public long getTimeStamp() {
				return mTimeStamp;
			}

			public float getLength() {
				return mLength;
			}

			public float[] getVector() {
				return mVector;
			}

			private void copyVector(final float[] from, final float[] to) {
				for (int i = 0; i < 3; i++) {
					to[i] = from[i];
				}
			}
		}
	}
}
