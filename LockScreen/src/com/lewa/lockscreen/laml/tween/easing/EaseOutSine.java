package com.lewa.lockscreen.laml.tween.easing;

public class EaseOutSine implements IEasing {

    @Override
    public double tick(double t, double b, double c, double d) {
        return c * Math.sin(t / d * (Math.PI / 2)) + b;
    }

}
