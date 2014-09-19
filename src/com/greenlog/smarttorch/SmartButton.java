package com.greenlog.smarttorch;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

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
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			setDown(true);
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			setDown(false);
			break;
		case MotionEvent.ACTION_MOVE:
			if (event.getX() < 0 || event.getY() < 0
					|| event.getX() > getWidth() || event.getY() > getHeight()) {
				setDown(false);
			}
			break;
		}
		return true;
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

}
