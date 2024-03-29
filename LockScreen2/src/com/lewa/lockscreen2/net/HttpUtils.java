package com.lewa.lockscreen2.net;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import com.lewa.lockscreen2.util.LogUtil;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * HttpUtils, use common userAgent
 *
 * @author gxwu@lewatek.com 2012-12-13
 */
public class HttpUtils {

    private static final String TAG = "HttpUtils";
    public static final String USEER_AGENT_PREFIX = "LewaApi/1.0-1";
    public static final String CLASS_NAME_BIAGENT = "lewa.bi.BIAgent";
    public static final String METHOD_NAME_GET_CLIENT_ID = "getBIClientId";
    public static final String CLASS_NAME_LEWA_BUILD = "lewa.os.Build";
    public static final String FIELD_NAME_LEWA_BUILD_VERSION = "LEWA_VERSION";

    private static String userAgent = null;
    /**
     * biclient id defined by lewa.bi.BIAgent.getBIClientId(Context context) method *
     */
    private static String biClientId = null;
    /**
     * android:versionCode in AndroidManifest.xml *
     */
    private static String appVersionCode = null;
    /**
     * lewa os version defined by lewa.os.Build.LEWA_VERSION *
     */
    private static String lewaVersion = null;
    private static boolean initUserAgent = false;
    private static boolean initBiClientId = false;
    private static boolean initAppVersionCode = false;
    private static boolean initLewaVersion = false;

    /**
     * http connect time out, the default value of 0 mean we will never time out, see
     * {@link java.net.HttpURLConnection#setConnectTimeout(int)}
     */
    public static int connectTimeout = 6000;
    /**
     * http read time out, the default value of 0 disables read timeouts. see
     * {@link java.net.HttpURLConnection#setReadTimeout(int)}
     */
    public static int readTimeout = 6000;

    public static final SimpleDateFormat GMT_FORMAT = new SimpleDateFormat(
            "EEE, d MMM yyyy HH:mm:ss z",
            Locale.ENGLISH);

    /**
     * get app version code, only init once
     *
     * @param context
     * @return
     */
    public static String getAppVersionCode(Context context) {
        if (!initAppVersionCode && context != null) {
            initAppVersionCode = true;
            PackageManager pm = context.getPackageManager();
            if (pm != null) {
                PackageInfo pi;
                try {
                    pi = pm.getPackageInfo(context.getPackageName(), 0);
                    if (pi != null) {
                        return (appVersionCode = Integer.toString(pi.versionCode));
                    }
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return appVersionCode;
    }

    /**
     * get biclient id, only init once
     *
     * @param context
     * @return null if not lewa os
     */
    public static String getBiClientId(Context context) {
        if (!initBiClientId && context != null) {
            initBiClientId = true;
            // get biclient id by lewa.bi.BIAgent.getBIClientId(Context context) method
            Class<?> demo = null;
            try {
                demo = Class.forName(CLASS_NAME_BIAGENT);
                Method method = demo.getMethod(METHOD_NAME_GET_CLIENT_ID, Context.class);
                return (biClientId = (String) method.invoke(demo.newInstance(), context));
            } catch (Exception e) {
                LogUtil.e("getBiClientId() ----------> error:" + e.getMessage());
                /**
                 * accept all exception, include ClassNotFoundException, NoSuchMethodException,
                 * InvocationTargetException, NullPointException
                 */
                e.printStackTrace();
            }
        }
        return biClientId;
    }

    /**
     * get lewa version, only init once
     *
     * @return null if not lewa os
     */
    public static String getLewaVersion() {
        if (!initLewaVersion) {
            initLewaVersion = true;
            // get lewa os version by lewa.os.Build.LEWA_VERSION
            Class<?> demo = null;
            try {
                demo = Class.forName(CLASS_NAME_LEWA_BUILD);
                Field field = demo.getField(FIELD_NAME_LEWA_BUILD_VERSION);
                return lewaVersion = (String) field.get(demo.newInstance());
            } catch (Exception e) {
                /**
                 * accept all exception, include ClassNotFoundException, NoSuchFieldException, InstantiationException,
                 * IllegalArgumentException, IllegalAccessException, NullPointException
                 */
                e.printStackTrace();
            }
        }
        return lewaVersion;
    }

    /**
     * get http user agent
     *
     * @param context
     * @return
     */
    public static String getUserAgent(Context context) {
        if (!initUserAgent && context != null) {
            initUserAgent = true;
            StringBuilder s = new StringBuilder(256);
            s.append(USEER_AGENT_PREFIX);
            s.append(" (Android ").append(Build.VERSION.RELEASE);
            String model = Build.MODEL;
            if (model != null) {
                s.append("; Model ").append(model.replace(" ", "_"));
            }
            if (!initLewaVersion) {
                getLewaVersion();
            }
            if (lewaVersion != null && lewaVersion.length() > 0) {
                s.append("; ").append(lewaVersion);
            }
            s.append(") ");
            if (!initAppVersionCode) {
                getAppVersionCode(context);
            }
            if (appVersionCode != null) {
                s.append(context.getPackageName()).append("/").append(appVersionCode);
            }
            if (!initBiClientId) {
                getBiClientId(context);
            }
            if (biClientId != null && biClientId.length() > 0) {
                s.append(" ClientID/").append(biClientId);
            }
            return (userAgent = s.toString());
        }
        return userAgent;
    }

    /**
     * @param context
     * @param httpUrl
     * @return {@link HttpUtils.HttpResponse#response}, the HttpResponse is return by {@link #httpGet(android.content.Context, String)}
     * @see {@link #httpGet(android.content.Context, String)}
     */
    public static String httpGetResponse(Context context, String httpUrl) {
        return httpGet(context, httpUrl).response;
    }

    /**
     * http get
     * <ul>
     * <li>use {@link #getUserAgent(android.content.Context)} as user-agent</li>
     * <li>use gzip compression default</li>
     * <li>use bufferedReader to improve the reading speed</li>
     * </ul>
     *
     * @param context
     * @param httpUrl
     * @return {@link HttpUtils.HttpResponse#response} is response content, {@link HttpUtils.HttpResponse#expires} is expires time
     */
    public static HttpResponse httpGet(Context context, String httpUrl) {
        BufferedReader input = null;
        StringBuilder sb = null;
        String expires = null;
        URL url = null;
        HttpURLConnection con = null;
        try {
            url = new URL(httpUrl);
            try {
                LogUtil.d("HttpUtils httpGet() -------------> httpUrl:" + httpUrl);
                // default gzip encode
                con = (HttpURLConnection) url.openConnection();

                con.setRequestProperty("User-Agent", getUserAgent(context));
                if (connectTimeout != 0) {
                    con.setConnectTimeout(connectTimeout);
                }
                if (readTimeout != 0) {
                    con.setReadTimeout(readTimeout);
                }
                input = new BufferedReader(new InputStreamReader(con.getInputStream()));
                sb = new StringBuilder();
                String s;
                while ((s = input.readLine()) != null) {
                    sb.append(s).append("\n");
                }
                expires = con.getHeaderField("Expires");
                LogUtil.d("HttpUtils httpGet() -------------> expires:" + expires);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } finally {
            // close buffered
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // disconnecting releases the resources held by a connection so they may be closed or reused
            if (con != null) {
                con.disconnect();
            }
        }

        return new HttpResponse(sb == null ? null : sb.toString(), parseGmtTime(expires));
    }

    public static void httpGet(Context context, String httpUrl, String savePath, String fileName) {
        URL url;
        HttpURLConnection con = null;
        String imageName = "";
        try {
            url = new URL(httpUrl);
            try {
                // default gzip encode
                con = (HttpURLConnection) url.openConnection();

                con.setRequestProperty("User-Agent", getUserAgent(context));
                if (connectTimeout != 0) {
                    con.setConnectTimeout(connectTimeout);
                }
                if (readTimeout != 0) {
                    con.setReadTimeout(readTimeout);
                }
                byte[] data = readInputStream(con.getInputStream());
                if (fileName == null || fileName.trim().length() == 0) {
                    imageName = UUID.randomUUID().toString() + ".jpg";
                } else {
                    imageName = fileName;
                }
                File imageFile = new File(savePath + File.separator + imageName);
                FileOutputStream outStream = new FileOutputStream(imageFile);
                outStream.write(data);
                outStream.close();
            } catch (Exception e) {
                LogUtil.d("HttpUtils httpGet() error -------------> msg:" + e.getMessage());
                e.printStackTrace();
            }
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } finally {
            // disconnecting releases the resources held by a connection so they may be closed or reused
            if (con != null) {
                con.disconnect();
            }
        }
    }

    public static void httpGetForApk(String httpUrl, String savePath, String fileName, Handler handler) {
        LogUtil.d("httpGetForApk()  -------------> httpUrl:" + httpUrl);
        URL url;
        HttpURLConnection con = null;
        try {
            url = new URL(httpUrl);
            try {
                // default gzip encode
                con = (HttpURLConnection) url.openConnection();

                if (connectTimeout != 0) {
                    con.setConnectTimeout(connectTimeout);
                }
                if (readTimeout != 0) {
                    con.setReadTimeout(readTimeout);
                }

                InputStream inputStream = con.getInputStream();

                long total = con.getContentLength();
                int oldProgress = 0;
                int newProgress;
                LogUtil.d("httpGetForApk()  -------------> total:" + total);
                File imageFile = new File(savePath + File.separator + fileName);
                FileOutputStream outStream = new FileOutputStream(imageFile);
                byte[] buffer = new byte[1024];
                int len;
                long count = 0;
                while ((len = inputStream.read(buffer)) != -1) {
                    count += len;
                    outStream.write(buffer, 0, len);
                    //LogUtil.d("httpGetForApk:" + (int) ((count / (float) total) * 100));
                    if (handler != null){
                        Message msg = new Message();
                        msg.what = 0;
                        newProgress = (int) ((count / (float) total) * 100);
                        if (newProgress > oldProgress){
                            msg.arg1 = newProgress;
                            oldProgress = newProgress;
                            handler.sendMessage(msg);
                        }
                    }
                }

                outStream.close();
                inputStream.close();
            } catch (Exception e) {
                LogUtil.d("HttpUtils httpGet() error -------------> msg:" + e.getMessage());
                e.printStackTrace();
            }
        } catch (MalformedURLException e1) {
            LogUtil.d("HttpUtils httpGet() error -------------> msg2:" + e1.getMessage());
            e1.printStackTrace();
        } finally {
            // disconnecting releases the resources held by a connection so they may be closed or reused
            if (con != null) {
                con.disconnect();
            }
        }
    }

    private static byte[] readInputStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        inStream.close();
        return outStream.toByteArray();
    }

    /**
     * @param context
     * @param httpUrl
     * @param parasMap paras map, key is para name, value is para value. will be transfrom to String by
     *                 {@link HttpUtils#getParas(java.util.Map)}
     * @return {@link HttpUtils.HttpResponse#response}, the HttpResponse is return by {@link #httpPost(android.content.Context, String, java.util.Map)}
     * @see {@link #httpPost(android.content.Context, String, java.util.Map)}
     */
    public static String httpPostResponse(Context context, String httpUrl,
                                          Map<String, String> parasMap) {
        return httpPost(context, httpUrl, parasMap).response;
    }

    /**
     * @param context
     * @param httpUrl
     * @param paras
     * @return {@link HttpUtils.HttpResponse#response}, the HttpResponse is return by {@link #httpPost(android.content.Context, String, java.util.Map)}
     * @see {@link #httpPost(android.content.Context, String, String)}
     */
    public static String httpPostResponse(Context context, String httpUrl, String paras) {
        return httpPost(context, httpUrl, paras).response;
    }

    /**
     * @param context
     * @param httpUrl
     * @param parasMap paras map, key is para name, value is para value. will be transfrom to String by
     *                 {@link HttpUtils#getParas(java.util.Map)}
     * @return
     * @see {@link #httpPost(android.content.Context, String, String)}
     */
    public static HttpResponse httpPost(Context context, String httpUrl,
                                        Map<String, String> parasMap) {
        return httpPost(context, httpUrl, getParas(parasMap));
    }

    /**
     * http post
     * <ul>
     * <li>use {@link #getUserAgent(android.content.Context)} as user-agent</li>
     * <li>use gzip compression default</li>
     * <li>use bufferedReader to improve the reading speed</li>
     * </ul>
     *
     * @param context
     * @param httpUrl
     * @param paras
     * @return {@link HttpUtils.HttpResponse#response} is response content, {@link HttpUtils.HttpResponse#expires} is expires time
     */
    public static HttpResponse httpPost(Context context, String httpUrl, String paras) {
        if (isEmpty(httpUrl) || paras == null) {
            return null;
        }

        BufferedReader input = null;
        StringBuilder sb = null;
        String expires = null;
        URL url = null;
        HttpURLConnection con = null;
        try {
            url = new URL(httpUrl);
            try {
                // default gzip encode
                con = (HttpURLConnection) url.openConnection();
                con.setRequestProperty("User-Agent", getUserAgent(context));
                con.setRequestMethod("POST");
                con.setDoOutput(true);
                if (connectTimeout != 0) {
                    con.setConnectTimeout(connectTimeout);
                }
                if (readTimeout != 0) {
                    con.setReadTimeout(readTimeout);
                }
                con.getOutputStream().write(paras.getBytes());
                input = new BufferedReader(new InputStreamReader(con.getInputStream()));
                sb = new StringBuilder();
                String s;
                while ((s = input.readLine()) != null) {
                    sb.append(s).append("\n");
                }
                expires = con.getHeaderField("Expires");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } finally {
            // close buffered
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // disconnecting releases the resources held by a connection so they may be closed or reused
            if (con != null) {
                con.disconnect();
            }
        }
        return new HttpResponse(sb == null ? null : sb.toString(), parseGmtTime(expires));
    }

    /**
     * join paras
     *
     * @param parasMap paras map, key is para name, value is para value
     * @return
     */
    public static String getParas(Map<String, String> parasMap) {
        if (parasMap == null || parasMap.size() == 0) {
            return null;
        }

        StringBuilder paras = new StringBuilder();
        Iterator<Map.Entry<String, String>> ite = parasMap.entrySet().iterator();
        while (ite.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) ite.next();
            paras.append(entry.getKey()).append("=").append(entry.getValue());
            if (ite.hasNext()) {
                paras.append("&");
            }
        }
        return paras.toString();
    }

    /**
     * join url and paras
     * <p/>
     * <pre>
     * getUrlWithParas(null, {(a, b)})                        =   "?a=b";
     * getUrlWithParas("baidu.com", {})                       =   "baidu.com";
     * getUrlWithParas("baidu.com", {(a, b), (i, j)})         =   "baidu.com?a=b&i=j";
     * getUrlWithParas("baidu.com", {(a, b), (i, j), (c, d)}) =   "baidu.com?a=b&i=j&c=d";
     * </pre>
     *
     * @param url      url
     * @param parasMap paras map, key is para name, value is para value
     * @return if url is null, process it as empty string
     */
    public static String getUrlWithParas(String url, Map<String, String> parasMap) {
        StringBuilder urlWithParas = new StringBuilder(isEmpty(url) ? "" : url);
        String paras = getParas(parasMap);
        if (!isEmpty(paras)) {
            urlWithParas.append("?").append(paras);
        }
        return urlWithParas.toString();
    }

    /**
     * HttpResponse
     *
     * @author gxwu@lewatek.com 2013-4-11
     */
    public static class HttpResponse {

        /**
         * http response content *
         */
        public String response;
        /**
         * http response header expires, -1 represent http error or no expires in response headers *
         */
        public long expires;

        public HttpResponse(String response, long expires) {
            this.response = response;
            this.expires = expires;
        }
    }

    /**
     * parse gmt time to long in gmt timezone
     *
     * @param gmtTime likes Thu, 11 Apr 2013 10:20:30 GMT
     * @return -1 represents exception
     */
    public static long parseGmtTime(String gmtTime) {
        try {
            // Calendar calendar = Calendar.getInstance();
            // long offset = (calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET));
            // return time to current timezone
            // return GMT_FORMAT.parse(gmtTime).getTime() + offset;
            return GMT_FORMAT.parse(gmtTime).getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static boolean isEmpty(String str) {
        return (str == null || str.length() == 0);
    }
}
