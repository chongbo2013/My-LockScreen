
package com.lewa.lockscreen.v5.lockscreen.activity;

import android.content.Context;
import android.graphics.Canvas;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

import com.lewa.lockscreen.laml.LifecycleResourceManager;
import com.lewa.lockscreen.laml.RenderThread;
import com.lewa.lockscreen.laml.RendererController;
import com.lewa.lockscreen.laml.RendererController.IRenderable;
import com.lewa.lockscreen.laml.ScreenContext;
import com.lewa.lockscreen.laml.ScreenElementRoot;
import com.lewa.lockscreen.laml.SingleRootListener;
import com.lewa.lockscreen.laml.util.Utils;
import com.lewa.lockscreen.v5.lockscreen.LockScreenElementFactory;
import com.lewa.lockscreen.v5.lockscreen.WallpaperResourceLoader;

public class FancyWallpaper extends WallpaperService {
    
    @Override
    public Engine onCreateEngine() {
        return new FancyEngine(this);
    }

    private class FancyEngine extends Engine implements IRenderable {

        private static final String VARIABLE_VIEW_HEIGHT = "view_height";

        private static final String VARIABLE_VIEW_WIDTH = "view_width";

        protected RendererController mRendererController;
        
        private RenderThread mThread;

        protected ScreenElementRoot mRoot;
        
        private SingleRootListener mListener;

        private ScreenContext mContext;

        private LifecycleResourceManager mResourceMgr;
        
        public FancyEngine(Context context) {
            WallpaperResourceLoader res = new WallpaperResourceLoader();
            res.setLocal(context.getResources().getConfiguration().locale);
            mResourceMgr = new LifecycleResourceManager(res, LifecycleResourceManager.TIME_DAY, LifecycleResourceManager.TIME_HOUR);
            mContext = new ScreenContext(context, mResourceMgr, new LockScreenElementFactory());
            mRoot = new ScreenElementRoot(mContext);
            mRoot.load();
            mListener = new SingleRootListener(mRoot, this);
            mRendererController = new RendererController(mListener);
            mRoot.setRenderController(mRendererController);
            mRendererController.init();
            mThread = new RenderThread();
            mThread.start();
            mThread.addRendererController(mRendererController);
            mThread.setPaused(true);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                mRendererController.selfResume();
            } else {
                mRendererController.selfPause();
            }
        }
        
        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            mThread.setPaused(false);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            mThread.setPaused(true);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Utils.putVariableNumber(VARIABLE_VIEW_WIDTH, mRoot.getContext().mVariables,
                    (double) width / mRoot.getScale());
            Utils.putVariableNumber(VARIABLE_VIEW_HEIGHT, mRoot.getContext().mVariables,
                    (double) height / mRoot.getScale());
            doRender();
        }
        
        @Override
        public void onDestroy() {
            super.onDestroy();
            cleanUp(false);
        }

        public void cleanUp(boolean keepResource) {
            mRoot.setKeepResource(keepResource);
            mRoot.setRenderController(null);
            if (mThread != null) {
                mThread.setStop();
                mThread.removeRendererController(mRendererController);
                mRendererController.finish();
            }
        }

        @Override
        public void doRender() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas c = null;
            try {
                if (mThread == null || !mThread.isStarted()) {
                    return;
                }
                c = holder.lockCanvas();
                if (c != null) {
                    mRoot.render(c);
                }
            } finally {
                if (c != null)
                    holder.unlockCanvasAndPost(c);
            }
        }
    }
}
