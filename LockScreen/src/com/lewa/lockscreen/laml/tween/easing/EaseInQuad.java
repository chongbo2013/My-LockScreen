package com.lewa.lockscreen.laml.tween.easing;

public class EaseInQuad implements IEasing {

    @Override
    public double tick(double t, double b, double c, double d) {
        return c * (t /= d) * t + b;
    }

}
