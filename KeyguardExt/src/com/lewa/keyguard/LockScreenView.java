package com.lewa.keyguard;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class LockScreenView extends FrameLayout implements LockScreen {
    private static final String TAG = "LockScreenView";
    private static final boolean DEBUG = true;
    public static final String DEFAULT_LOCKSCREEN = "com.lewa.lockscreen2";

    private LockscreenWrapper mWrapper;
    private View mRoot;

    private String mPackageName;

    public LockScreenView(Context context) {
        this(context, null);
    }

    public LockScreenView(Context context, AttributeSet attrs) {
        this(context, null, 0);
    }

    public LockScreenView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        installLockScreen();
    }

//    private void inflateCameraPreview() {
//        PreviewInflater previewInflater = new PreviewInflater(getContext(), mLockPatternUtils);
//        mPreviewView = previewInflater.inflatePreview(SECURE_LEWA_CAMERA_INTENT);
//        mPreviewView.setVisibility(View.INVISIBLE);
//        addView(mPreviewView, scrimPos + 1, lp);
//    }

    private void installLockScreen() {
        String packageName = Settings.System.getString(
                getContext().getContentResolver(), "lewa.theme.lockscreen");

        if (packageName == null) {
            packageName = DEFAULT_LOCKSCREEN;
        }

        setupLockScreenFromPackage(packageName, "lockscreen");
    }

    private void setupLockScreenFromPackage(String packageName, String layoutName) {
        if (mPackageName == null || !mPackageName.equals(packageName)) {
            if (mRoot != null) {
                removeView(mRoot);
                cleanUp();
                mWrapper = null;
                mRoot = null;

                System.gc();
            }

            try {
                Context pkgContext = getContext().createPackageContext(packageName,
                        Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
                LayoutInflater pkgInflater = LayoutInflater.from(pkgContext);
                int targetId = pkgContext.getResources().getIdentifier(
                        layoutName, "layout", packageName);
                mRoot = pkgInflater.inflate(targetId, null);
                mRoot.setVisibility(View.INVISIBLE);

                addView(mRoot, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            if (mRoot != null) {
                mWrapper = new LockscreenWrapper(mRoot);
            }

            if (mRoot != null && mWrapper != null) {
                mPackageName = packageName;
            }
        }
    }

    class LockscreenWrapper implements LockScreen {
        private Object mObj;

        private Method onAttachToKeyguard;
        private Method onDetachFromKeyguard;
        private Method showKeyguard;
        private Method hideKeyguard;
        private Method onBouncerShow;
        private Method onBouncerHide;
        private Method cleanUp;

        public LockscreenWrapper(Object obj) {
            mObj = obj;
        }

        @Override
        public void onAttachToKeyguard(Context context) {
            if (onAttachToKeyguard == null) {
                try {
                    onAttachToKeyguard = mObj.getClass().getDeclaredMethod(
                            "onAttachToKeyguard", Context.class);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }

            if (onAttachToKeyguard != null) {
                try {
                    onAttachToKeyguard.invoke(mObj, context);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onDetachFromKeyguard() {
            if (onDetachFromKeyguard == null) {
                try {
                    onDetachFromKeyguard = mObj.getClass().getDeclaredMethod(
                            "onDetachFromKeyguard");
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }

            if (onDetachFromKeyguard != null) {
                try {
                    onDetachFromKeyguard.invoke(mObj);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void showKeyguard() {
            if (showKeyguard == null) {
                try {
                    showKeyguard = mObj.getClass().getDeclaredMethod(
                            "showKeyguard");
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }

            if (showKeyguard != null) {
                try {
                    showKeyguard.invoke(mObj);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void hideKeyguard() {
            if (hideKeyguard == null) {
                try {
                    hideKeyguard = mObj.getClass().getDeclaredMethod(
                            "hideKeyguard");
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }

            if (hideKeyguard != null) {
                try {
                    hideKeyguard.invoke(mObj);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onBouncerShow() {
            if (onBouncerShow == null) {
                try {
                    onBouncerShow = mObj.getClass().getDeclaredMethod(
                            "onBouncerShow");
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }

            if (onBouncerShow != null) {
                try {
                    onBouncerShow.invoke(mObj);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onBouncerHide() {
            if (onBouncerHide == null) {
                try {
                    onBouncerHide = mObj.getClass().getDeclaredMethod(
                            "onBouncerHide");
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }

            if (onBouncerHide != null) {
                try {
                    onBouncerHide.invoke(mObj);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void cleanUp() {
            if (cleanUp == null) {
                try {
                    cleanUp = mObj.getClass().getDeclaredMethod(
                            "cleanUp");
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }

            if (cleanUp != null) {
                try {
                    cleanUp.invoke(mObj);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onAttachToKeyguard(Context context) {
        if (DEBUG) Log.d(TAG, "onAttachToKeyguard");

        installLockScreen();

        if (mWrapper != null) {
            mWrapper.onAttachToKeyguard(context);
        }
    }

    @Override
    public void onDetachFromKeyguard() {
        if (DEBUG) Log.d(TAG, "onDetachFromKeyguard");

        if (mWrapper != null) {
            mWrapper.onDetachFromKeyguard();
        }
    }

    @Override
    public void showKeyguard() {
        if (DEBUG) Log.d(TAG, "showKeyguard");

        if (mWrapper != null) {
            mWrapper.showKeyguard();
        }

        if (mRoot != null) {
            mRoot.setVisibility(VISIBLE);
        }
    }

    @Override
    public void hideKeyguard() {
        if (DEBUG) Log.d(TAG, "hideKeyguard");

        if (mWrapper != null) {
            mWrapper.hideKeyguard();
        }

        if (mRoot != null) {
            mRoot.setVisibility(INVISIBLE);
        }
    }

    @Override
    public void onBouncerShow() {
        if (DEBUG) Log.d(TAG, "onBouncerShow");

        if (mWrapper != null) {
            mWrapper.onBouncerShow();
        }
    }

    @Override
    public void onBouncerHide() {
        if (DEBUG) Log.d(TAG, "onBouncerHide");

        if (mWrapper != null) {
            mWrapper.onBouncerHide();
        }
    }

    @Override
    public void cleanUp() {
        if (DEBUG) Log.d(TAG, "cleanUp");

        if (mWrapper != null) {
            mWrapper.cleanUp();
        }
    }
}
