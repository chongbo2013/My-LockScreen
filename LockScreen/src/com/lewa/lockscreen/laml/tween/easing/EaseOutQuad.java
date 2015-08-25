package com.lewa.lockscreen.laml.tween.easing;

public class EaseOutQuad implements IEasing {

    @Override
    public double tick(double t, double b, double c, double d) {
        return -c * (t /= d) * (t - 2) + b;
    }

}
