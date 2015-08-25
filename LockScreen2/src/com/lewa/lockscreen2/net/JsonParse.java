package com.lewa.lockscreen2.net;


import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import com.lewa.lockscreen2.util.Constant;
import com.lewa.lockscreen2.util.LogUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by lewa on 2/10/15.
 */
public class JsonParse {

    private static final int SUCCESS_CODE = 0;
    private static final String RECOMMENDAPP_TYPE_DOWNLOAD = "download";

    public Response parseResponse(String response, Context context) {

        if (response == null || response.trim().length() == 0) {
            return null;
        }

        try {
            JSONObject jsonObject = new JSONObject(response);
            if (SUCCESS_CODE != getJsonInt(jsonObject, "code")) {
                return null;
            }

            String next = getJsonString(jsonObject, "next");
            LogUtil.d("JsonParse ----------> next:" + next);
            if (next != null && next.length() > 6){
                Settings.System.putString(context.getContentResolver(), Constant.RECOMMENDAPP_URL, next);
            }

            Response info = new Response();
            JSONObject resultJson = new JSONObject(getJsonString(jsonObject, "result"));
            info.type = getJsonString(resultJson, "type");

            long servertime = getJsonLong(resultJson, "servertime");
            Settings.System.putLong(context.getContentResolver(), Constant.SERVER_TIME_DIFF, (servertime - System.currentTimeMillis()));

            // parse images url
            JSONArray imagesJson = new JSONObject(getJsonString(resultJson, "wallpaper")).getJSONArray("images");

            int imagesLength = imagesJson.length();
            String images[] = new String[imagesLength];
            for (int i = 0; i < imagesLength; i++) {
                images[i] = (String) imagesJson.get(i);
            }
            info.images = images;

            // parse recommendApp info
            JSONArray recommendAppJson = resultJson.getJSONArray("recommendApp");
            int recommendAppLength = recommendAppJson.length();
            List<RecommendApp> list = new ArrayList<RecommendApp>();
            for (int i = 0; i < recommendAppLength; i++) {
                JSONObject recommendAppItemJson = new JSONObject(recommendAppJson.get(i).toString());

                if (RECOMMENDAPP_TYPE_DOWNLOAD.equals(getJsonString(recommendAppItemJson, "type"))) {
                    JSONObject contentJson = new JSONObject(getJsonString(recommendAppItemJson, "content"));
                    RecommendApp item = new RecommendApp();
                    item.name = getJsonString(contentJson, "name");
                    item.icon_url = getJsonString(contentJson, "icon");
                    item.url = getJsonString(contentJson, "url");
                    item.packageName = getJsonString(contentJson, "package");
                    item.versionCode = getJsonInt(contentJson, "version_code");
                    item.icon_name = item.packageName + item.versionCode + ".jpg";
                    LogUtil.d("JsonParse parseResponse -------------> item:" + item.toString());
                    list.add(item);
                }
            }

            info.recommendApps = list;

            return info;
        } catch (Exception e) {
            LogUtil.d("JsonParse parseResponse -------------> error:" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String getJsonString(JSONObject jsonObject, String key) {
        String value = null;
        try {
            value = jsonObject.getString(key);
        } catch (Exception e) {
            LogUtil.d("JsonParse parseResponse -------------> error:" + e.getMessage());
            e.printStackTrace();
        }
        return value;
    }

    private int getJsonInt(JSONObject jsonObject, String key) {
        int value = 0;
        try {
            value = jsonObject.getInt(key);
        } catch (Exception e) {
            LogUtil.d("JsonParse parseResponse -------------> error:" + e.getMessage());
            e.printStackTrace();
        }
        return value;
    }

    private long getJsonLong(JSONObject jsonObject, String key) {
        long value = 0;
        try {
            value = jsonObject.getLong(key);
        } catch (Exception e) {
            LogUtil.d("JsonParse parseResponse -------------> error:" + e.getMessage());
            e.printStackTrace();
        }
        return value;
    }
}
