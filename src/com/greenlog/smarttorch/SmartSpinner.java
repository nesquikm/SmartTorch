package com.greenlog.smarttorch;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Spinner;

public class SmartSpinner extends Spinner {
	private OnItemClickListener mOnItemClickListener;

	public SmartSpinner(final Context context) {
		super(context);
	}

	public SmartSpinner(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public SmartSpinner(final Context context, final AttributeSet attrs,
			final int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public SmartSpinner(final Context context, final AttributeSet attrs,
			final int defStyle, final int mode) {
		super(context, attrs, defStyle, mode);
	}

	public SmartSpinner(final Context context, final int mode) {
		super(context, mode);
	}

	/**
	 * Haha! Now Spinner support item click events! :)
	 */
	@Override
	public void setOnItemClickListener(
			final android.widget.AdapterView.OnItemClickListener l) {
		mOnItemClickListener = l;
	}

	/**
	 * Same as setSelection(int position), but without calling
	 * OnItemClickListener
	 */
	public void setSelectionSilently(final int position) {
		super.setSelection(position);
	}

	/**
	 * Same as setSelection(int position, boolean animate), but without calling
	 * OnItemClickListener
	 */
	public void setSelectionSilently(final int position, final boolean animate) {
		super.setSelection(position, animate);
	}

	@Override
	public void setSelection(final int position) {
		super.setSelection(position);
		if (mOnItemClickListener != null) {
			mOnItemClickListener.onItemClick(this, getSelectedView(), position,
					getSelectedItemId());
		}
	}

	@Override
	public void setSelection(final int position, final boolean animate) {
		super.setSelection(position, animate);
		if (mOnItemClickListener != null) {
			mOnItemClickListener.onItemClick(this, getSelectedView(), position,
					getSelectedItemId());
		}
	}
}
