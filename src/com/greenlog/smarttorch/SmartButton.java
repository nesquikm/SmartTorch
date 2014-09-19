package com.greenlog.smarttorch;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

// TODO: animate on first appear
// TODO: save state on recreate (orientation change)
public class SmartButton extends ImageView {
	private boolean mIsDown = false;

	public SmartButton(final Context context) {
		super(context);
	}

	public SmartButton(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public SmartButton(final Context context, final AttributeSet attrs,
			final int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		if (isEnabled()) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				setDown(true);
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				if (mIsDown) {
					performClick();
				}
				setDown(false);
				break;
			case MotionEvent.ACTION_MOVE:
				if (!isEventInView(event)) {
					setDown(false);
				}
				break;
			}
			// super.onTouchEvent(event);
		}
		return true;
	}

	private boolean isEventInView(final MotionEvent event) {
		return (event.getX() >= 0 && event.getY() >= 0
				&& event.getX() < getWidth() && event.getY() < getHeight());
	}

	private void setDown(final boolean isDown) {
		if (mIsDown == isDown) {
			return;
		}
		mIsDown = isDown;

		if (mIsDown) {
			if (getDrawable() != null) {
				getDrawable().setColorFilter(0x77000000,
						PorterDuff.Mode.SRC_ATOP);
				invalidate();
			}
		} else {
			if (getDrawable() != null) {
				getDrawable().clearColorFilter();
				invalidate();
			}
		}
	}

	@Override
	public void setEnabled(final boolean enabled) {
		if (enabled != isEnabled()) {
			if (enabled) {
				animate().translationX(0);
			} else {
				// Shift right
				animate().translationXBy(
						getResources().getDisplayMetrics().widthPixels);
			}
		}
		super.setEnabled(enabled);
	}

}
