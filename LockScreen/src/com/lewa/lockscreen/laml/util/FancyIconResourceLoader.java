
package com.lewa.lockscreen.laml.util;

import java.io.InputStream;

import com.lewa.lockscreen.content.res.ThemeResources;
import com.lewa.lockscreen.laml.ResourceLoader;

public class FancyIconResourceLoader extends ResourceLoader {

    private String mRelatviePathBaseIcons;

    public FancyIconResourceLoader(String relativePathBaseIcons) {
        mRelatviePathBaseIcons = relativePathBaseIcons;
    }

    protected InputStream getInputStream(String path, long[] size) {
        return ThemeResources.getSystem().getIconStream(mRelatviePathBaseIcons + path, size);
    }

    protected boolean resourceExists(String path) {
        return ThemeResources.getSystem().hasIcon(mRelatviePathBaseIcons + path);
    }

    public String toString() {
        return mRelatviePathBaseIcons;
    }
}
