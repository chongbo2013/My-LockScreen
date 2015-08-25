
package com.lewa.lockscreen2;

public class RecommendAppInfo {

    public String name;
    public String url;
    public String iconName;

    public String packageName;
    public String className;

    /** 0:isRecommend, 1:isApp, 2:isMore */
    public int type = 1;

    // public boolean isRecommend;
    // public boolean isMore;
    // public boolean isApp;

    @Override
    public String toString() {
        return "RecommendAppInfo name:" + name + ", type:" + type + ", packageName:" + packageName
                + ", className:" + className;
    }

}
