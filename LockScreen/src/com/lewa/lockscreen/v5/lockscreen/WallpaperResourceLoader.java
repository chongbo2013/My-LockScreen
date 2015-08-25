
package com.lewa.lockscreen.v5.lockscreen;

import java.io.InputStream;

import com.lewa.lockscreen.content.res.ThemeResources;
import com.lewa.lockscreen.laml.ResourceLoader;

public class WallpaperResourceLoader extends ResourceLoader {
    protected InputStream getInputStream(String path, long size[]) {
        return ThemeResources.getSystem().getFancyWallpaperFileStream(path, size);
    }

    protected boolean resourceExists(String path) {
        return ThemeResources.getSystem().containsFancyWallpaperEntry(path);
    }
}
