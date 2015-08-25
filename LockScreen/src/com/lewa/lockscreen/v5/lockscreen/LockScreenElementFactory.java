
package com.lewa.lockscreen.v5.lockscreen;

import org.w3c.dom.Element;

import com.lewa.lockscreen.laml.ScreenElementLoadException;
import com.lewa.lockscreen.laml.ScreenElementRoot;
import com.lewa.lockscreen.laml.elements.ScreenElement;
import com.lewa.lockscreen.laml.elements.ScreenElementFactory;

public class LockScreenElementFactory extends ScreenElementFactory {

    public ScreenElement createInstance(Element ele, ScreenElementRoot root)
            throws ScreenElementLoadException {
        String tag = ele.getTagName();
        if (tag.equalsIgnoreCase(UnlockerScreenElement.TAG_NAME))
            return new UnlockerScreenElement(ele, (LockScreenRoot) root);
        if (tag.equalsIgnoreCase(WallpaperScreenElement.TAG_NAME))
            return new WallpaperScreenElement(ele, root);
        else
            return super.createInstance(ele, root);
    }
}
