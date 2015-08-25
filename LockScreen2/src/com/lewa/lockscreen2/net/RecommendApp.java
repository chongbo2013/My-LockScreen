
package com.lewa.lockscreen2.net;

/**
 * Created by lewa on 3/5/15.
 */
public class RecommendApp {

    public String _id;
    public String name;
    public String icon_url;
    public String url;
    public String icon_name;
    public String packageName;
    public int versionCode;

    @Override
    public String toString() {
        return "RecommendApp _id = " + _id + ", name = " + name + ", icon_url = " + icon_url + ", url = "
                + url + ", icon_name:" + icon_name + ", packageName:" + packageName + ", versionCode:" + versionCode;
    }
}
