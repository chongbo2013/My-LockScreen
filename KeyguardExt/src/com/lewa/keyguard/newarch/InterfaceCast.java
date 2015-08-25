package com.lewa.keyguard.newarch;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by ning on 15-3-31.
 */
class InterfaceCast implements InvocationHandler {
    private Class<?> mInterfaceClass;
    private Object mTarget;
    private boolean mIsLockScreen;
    private Class<?> mLockScreenActivityClass;

    private InterfaceCast() {

    }

    public static Object cast(Class<?> cls, Object target) {
        return new InterfaceCast().bind(cls, target);
    }

    private Object bind(Class<?> cls, Object target) {
        mInterfaceClass = cls;
        mTarget = target;

        mIsLockScreen = mInterfaceClass.getName().equals("com.lewa.keyguard.newarch.LockScreen");
        if (mIsLockScreen) {
            try {
                mLockScreenActivityClass = mTarget.getClass().getClassLoader().
                        loadClass("com.lewa.keyguard.newarch.LockScreenBaseActivity");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return Proxy.newProxyInstance(cls.getClassLoader(),
                new Class[]{cls}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = null;
        Method realMethod = null;
        if (mIsLockScreen) {
            realMethod = mLockScreenActivityClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
        } else {
            realMethod = mTarget.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
        }
        realMethod.setAccessible(true);
        result = realMethod.invoke(mTarget, args);
        return result;
    }
}
