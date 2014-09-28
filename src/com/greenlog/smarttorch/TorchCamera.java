package com.greenlog.smarttorch;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class TorchCamera {
	private static final String TAG = TorchCamera.class.getSimpleName();

	private final Context mContext;
	private Camera mCamera = null;

	private LinearLayout mOverlay = null;
	private SurfaceView mSurfaceView;
	private final Lock mSurfaceLock = new ReentrantLock();

	private class PreviewHolder implements SurfaceHolder.Callback {
		private final String TAG = PreviewHolder.class.getSimpleName();

		@Override
		public void surfaceCreated(final SurfaceHolder holder) {
			Log.v(TAG, "surfaceCreated");
			mSurfaceLock.lock();
			try {
				mCamera.setPreviewDisplay(holder);
				mCamera.startPreview();
			} catch (final IOException e) {
				Log.e(TAG,
						"Camera.setPreviewDisplay says: "
								+ e.getLocalizedMessage());
			} finally {
				mSurfaceLock.unlock();
			}
		}

		@Override
		public void surfaceChanged(final SurfaceHolder holder,
				final int format, final int width, final int height) {
			Log.v(TAG, "surfaceChanged");
		}

		@Override
		public void surfaceDestroyed(final SurfaceHolder holder) {
			Log.v(TAG, "surfaceDestroyed");
		}

	}

	public TorchCamera(final Context context) {
		mContext = context;
	}

	public boolean turn(final boolean isOn) {
		Log.v(TAG, "turn " + isOn);
		if (isOn) {
			if (mCamera == null) {
				try {
					mCamera = Camera.open();
				} catch (final RuntimeException e) {
					return false;
				}
				if (mCamera == null) {
					return false;
				}
			}

			createOverlay();
			final SurfaceHolder holder = mSurfaceView.getHolder();
			holder.addCallback(new PreviewHolder());

			final Parameters parameters = mCamera.getParameters();
			parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
			mCamera.setParameters(parameters);

			mCamera.startPreview();
		} else {
			if (mCamera != null) {
				final Parameters params = mCamera.getParameters();
				params.setFlashMode(Parameters.FLASH_MODE_OFF);
				mCamera.setParameters(params);
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}
			removeOverlay();
		}
		return true;
	}

	private void createOverlay() {
		assert (mOverlay == null);
		final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				1, 1, WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, // technically
																			// automatically
																			// set
																			// by
																			// FLAG_NOT_FOCUSABLE
				PixelFormat.TRANSLUCENT);
		params.gravity = Gravity.BOTTOM;
		final LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mOverlay = (LinearLayout) inflater.inflate(R.layout.overlay, null);
		mSurfaceView = (SurfaceView) mOverlay.findViewById(R.id.surfaceview);
		final WindowManager wm = (WindowManager) mContext
				.getSystemService(Context.WINDOW_SERVICE);
		wm.addView(mOverlay, params);
	}

	private void removeOverlay() {
		if (mOverlay == null)
			return;
		final WindowManager wm = (WindowManager) mContext
				.getSystemService(Context.WINDOW_SERVICE);
		wm.removeView(mOverlay);
		mOverlay = null;
		mSurfaceView = null;
	}

	public void checkState() {
		// TODO: fix for HTC devices: they turn off the camera after screen
		// lock. Maybe, not only HTC?
		if (("HTC").contentEquals(android.os.Build.MANUFACTURER)) {
			final Parameters parameters = mCamera.getParameters();
			// if
			// (!parameters.getFlashMode().equals(Parameters.FLASH_MODE_TORCH))
			// {
			parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
			mCamera.setParameters(parameters);

			parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
			mCamera.setParameters(parameters);

			mCamera.startPreview();
			// }
		}
	}
}
