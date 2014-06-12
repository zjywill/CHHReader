package com.comic.chhreader.gallery;

import android.view.animation.Interpolator;

public class IntroInterpolator implements Interpolator {
	@Override
	public float getInterpolation(float input) {
		float f = input - 1.0F;
		return 1.0F + f * (f * (f * (f * f)));
	}
}
