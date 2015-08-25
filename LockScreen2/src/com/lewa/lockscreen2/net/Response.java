package com.lewa.lockscreen2.net;

import java.util.List;

/**
 * Created by lewa on 2/10/15.
 */
public class Response {

    public String type;
    public String[] images;
    public String style;
    public List<RecommendApp> recommendApps;

    @Override
    public String toString() {
        if (images == null) {
            return null;
        }
        String str = "";
        for (int i = 0, N = images.length; i < N; i++) {
            str = str + images[i] + ", ";
        }

        int recommendAppsCount = 0;
        if (recommendApps != null) {
            recommendAppsCount = recommendApps.size();
        }
        return "Response = (type:" + type + ", images:[" + str + "])" + ", recommendApps.size:" + recommendAppsCount;
    }
}
