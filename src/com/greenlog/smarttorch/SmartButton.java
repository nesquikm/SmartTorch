package com.greenlog.smarttorch;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

// TODO: 01. animate on first appear
// TODO: 01. save state on recreate (orientation change)
public class SmartButton extends ImageView {
	private boolean mIsDown = false;
	private boolean mIsAnimated = true;

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

	public void setAnimated(final boolean isAnimated) {
		mIsAnimated = isAnimated;
	}

	@Override
	public void setEnabled(final boolean enabled) {
		if (enabled != isEnabled()) {
			if (!enabled) {
				setDown(false);
			}

			setShow(enabled, mIsAnimated);

			SmartButton.super.setEnabled(enabled);
		}
	}

	private void setShow(final boolean show, final boolean animated) {
		if (((View) getParent()).getWidth() == 0) {
			post(new Runnable() {
				@Override
				public void run() {
					setShow(show, animated);
				}
			});
			return;
		}

		final float translateXTo = show ? 0 : ((View) getParent()).getWidth()
				- getLeft();

		if (animated) {
			animate().translationX(translateXTo);
		} else {
			setTranslationX(translateXTo);
		}
	}

}
