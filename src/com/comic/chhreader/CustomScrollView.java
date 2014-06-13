package com.comic.chhreader;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class CustomScrollView extends ScrollView {

	public CustomScrollView(Context context) {
		super(context);
	}

	public CustomScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private float gTouchStartX;
	private float gTouchStartY;

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				gTouchStartX = ev.getX();
				gTouchStartY = ev.getY();
				this.onTouchEvent(ev);
				break;
			case MotionEvent.ACTION_MOVE:
				this.onTouchEvent(ev);
				final float touchDistancesX = Math.abs(ev.getX() - gTouchStartX);
				final float touchDistancesY = Math.abs(ev.getY() - gTouchStartY);
				if (Math.abs(touchDistancesY) >= Math.abs(touchDistancesX)) {
					return true;
				}
				return false;
			case MotionEvent.ACTION_CANCEL:
				break;
			case MotionEvent.ACTION_UP:
				break;
		}
		this.onTouchEvent(ev);
		return false;
	}
}
