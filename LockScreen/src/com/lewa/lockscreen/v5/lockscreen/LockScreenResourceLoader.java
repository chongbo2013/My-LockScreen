
package com.lewa.lockscreen.v5.lockscreen;

import java.io.File;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.lewa.lockscreen.content.res.ThemeResources;
import com.lewa.lockscreen.laml.ResourceLoader;

public class LockScreenResourceLoader extends ResourceLoader {
 
    protected InputStream getInputStream(String path, long size[]) {
        if(DEBUG_LOCKSTYLE){
            init();
            return testResourceLoader.getInputStream(path, size);
        }
        return ThemeResources.getSystem().getFancyLockscreenFileStream(path, size);
    }

    protected boolean resourceExists(String path) {
         if(DEBUG_LOCKSTYLE){
             init();
             return testResourceLoader.resourceExists(path);
         }
        return ThemeResources.getSystem().containsFancyLockscreenEntry(path);
    }
    
    private TestResourceLoader testResourceLoader ;
    private void  init(){
        if(testResourceLoader == null){
            testResourceLoader = new TestResourceLoader() ;
        }
            
    }

    public static final boolean DEBUG_LOCKSTYLE = false ;

    private class TestResourceLoader extends ResourceLoader {

        private static final String CONFIG_RES_ZIP_PATH = "advance/";
        private static final String DEBUG_LOCKSTYLE_PATH = "/storage/sdcard0/advance.zip";
        private ZipFile             mResFile;

        public TestResourceLoader(){
            try {
                File resFile = new File(DEBUG_LOCKSTYLE_PATH);
                if (resFile.exists()) {
                    mResFile = new ZipFile(resFile);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected InputStream getInputStream(String path, long[] ids) {
            if (mResFile != null) {
                try {
                    ZipEntry entry = mResFile.getEntry(CONFIG_RES_ZIP_PATH + path);
                    if (entry != null) {
                        if (ids != null) ids[0] = entry.getSize();
                        return mResFile.getInputStream(entry);
                    }
                } catch (Exception e) {
                }
            }
            return null;
        }

        @Override
        protected boolean resourceExists(String path) {
            return mResFile != null && mResFile.getEntry(CONFIG_RES_ZIP_PATH + path) != null;
        }

    }
}
