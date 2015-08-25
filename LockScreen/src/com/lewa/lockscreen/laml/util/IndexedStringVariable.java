
package com.lewa.lockscreen.laml.util;

import com.lewa.lockscreen.laml.data.Variables;

public class IndexedStringVariable {

    private int mIndex = -1;

    private Variables mVars;

    public IndexedStringVariable(String object, String property, Variables vars) {
        mIndex = vars.registerStringVariable(object, property);
        mVars = vars;
    }

    public IndexedStringVariable(String name, Variables vars) {
        this(null, name, vars);
    }

    public String get() {
        return mVars.getStr(mIndex);
    }

    public int getIndex() {
        return mIndex;
    }

    public int getVersion() {
        return mVars.getStrVer(mIndex);
    }

    public void set(String value) {
        mVars.putStr(mIndex, value);
    }
}
