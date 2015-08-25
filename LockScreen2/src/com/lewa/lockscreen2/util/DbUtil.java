
package com.lewa.lockscreen2.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.lewa.lockscreen2.net.RecommendApp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lewa on 3/4/15.
 */
public class DbUtil {

    private DbHelper mDbHelper;
    private Context mContext;

    public DbUtil(Context context) {
        mContext = context;
        mDbHelper = new DbHelper(context);
    }

    public synchronized void insert(List<RecommendApp> list) {
        LogUtil.d("DbUtil insert start ----------------> ");
        if (list == null || list.size() <= 0) {
            return;
        }

//        List<PackageInfo> localList = mContext.getPackageManager().getInstalledPackages(0);
//        int packageSize = list.size();
//        HashMap<String, Integer> localMap = new HashMap<String, Integer>();
//        for (int i = 0; i < packageSize; i++) {
//            PackageInfo info = localList.get(i);
//            localMap.put(info.packageName, info.versionCode);
////            LogUtil.d("insert ---------> " + info.packageName + ", " + info.versionCode + ", " + info.versionName);
//        }

        int count = 0;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        db.beginTransaction();

        for (int i = 0, N = list.size(); i < N; i++) {

//            if (localMap.containsKey(list.get(i).packageName) && list.get(i).versionCode >= localMap.get(list.get(i).packageName)) {
//                continue;
//            }
            RecommendApp item = list.get(i);
            cv.clear();
            cv.put(DbHelper.NAME, item.name);
            cv.put(DbHelper.ICON_URL, item.icon_url);
            cv.put(DbHelper.ICON_NAME, item.icon_name);
            cv.put(DbHelper.URL, item.url);
            cv.put(DbHelper.PACKAGE_NAME, item.packageName);
            cv.put(DbHelper.VERSION_CODE, item.versionCode);
            cv.put(DbHelper.MODIFY_TIME, System.currentTimeMillis());
            long index = db.insert(DbHelper.TABLE_NAME, null, cv);
//            LogUtil.d("DbUtil insert ---------------->  index:" + index);
            if (index > 0) {
                count++;
            }
        }

        db.setTransactionSuccessful();
        db.endTransaction();
        LogUtil.d("DbUtil insert success ----------------> count:" + count);
        db.close();
    }

    public void query() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        db.query(DbHelper.TABLE_NAME, new String[]{}, "selection", new String[]{}, null, null, "");
    }

    public void update(ContentValues values, String where, String[] wheresrgs) {
        String sql = "values:" + values + ", where:" + where + ", wheresrgs:";
        if (wheresrgs != null) {
            for (int i = 0; i < wheresrgs.length; i++) {
                sql = sql + wheresrgs[i] + "  ";
            }
        }
        LogUtil.d("update --------> sql:" + sql);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int i = db.update(DbHelper.TABLE_NAME, values, where, wheresrgs);
        LogUtil.d("update --------> result:" + i);
        db.close();
    }

    public void delete(String where, String[] wheresrgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.delete(DbHelper.TABLE_NAME, where, wheresrgs);
        db.close();
    }

    public List<RecommendApp> queryAll() {
        LogUtil.d("queryAll --------> start");
        String where = "isInstalled = 0  group by package_name order by  _id  desc  limit 6 ";
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor c = db.query(DbHelper.TABLE_NAME, null, where, null, null, null, null);
        List<RecommendApp> list = new ArrayList<RecommendApp>();

        LogUtil.d("queryAll --------> " + c.getCount());
        int _idIndex = c.getColumnIndex(DbHelper._ID);
        int appId = c.getColumnIndex(DbHelper.APP_ID);
        int nameIndex = c.getColumnIndex(DbHelper.NAME);
        int icon_urlIndex = c.getColumnIndex(DbHelper.ICON_URL);
        int icon_nameIndex = c.getColumnIndex(DbHelper.ICON_NAME);
        int urlIndex = c.getColumnIndex(DbHelper.URL);
        int packageNameIndex = c.getColumnIndex(DbHelper.PACKAGE_NAME);
        int versionCodeIndex = c.getColumnIndex(DbHelper.VERSION_CODE);

        try {
            boolean isHave = c.moveToFirst();
            while (c != null && isHave) {
                RecommendApp item = new RecommendApp();
                item._id = c.getString(_idIndex);
                item.name = c.getString(nameIndex);
                item.icon_url = c.getString(icon_urlIndex);
                item.icon_name = c.getString(icon_nameIndex);
                item.url = c.getString(urlIndex);
                item.packageName = c.getString(urlIndex);
                item.url = c.getString(urlIndex);
                item.packageName = c.getString(packageNameIndex);
                item.versionCode = c.getInt(versionCodeIndex);
                LogUtil.d("queryAll --------> " + item);
                list.add(item);
                isHave = c.moveToNext();
            }
        } catch (Exception e) {
            LogUtil.d("queryAll---------> e:" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }

        LogUtil.d("queryAll --------> end");
        return list;
    }

    public void dropTable() {
        try {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            db.execSQL(" drop table " + DbHelper.TABLE_NAME);
        } catch (Exception e) {

        }
    }

    private class DbHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "lockscreenrecommend.db";
        private static final int DATABASE_VERSION = 1;
        private static final String TABLE_NAME = "recommend";

        private static final String _ID = "_id";
        private static final String APP_ID = "app_id";
        private static final String NAME = "name";
        private static final String ICON_URL = "icon_url";
        private static final String ICON_NAME = "icon_name";
        private static final String URL = "url";
        private static final String PACKAGE_NAME = "package_name";
        private static final String VERSION_CODE = "version_code";
        private static final String MODIFY_TIME = "modify_time";
        /**
         * 1:installed, 0:not installed
         */
        private static final String IS_INSTALLED = "isInstalled";

        public DbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            String sql = "CREATE TABLE recommend( _id INTEGER PRIMARY KEY, app_id INTEGER  DEFAULT 0, name TEXT, icon_url TEXT, icon_name TEXT, url TEXT, package_name TEXT, version_code INTEGER  DEFAULT 0, modify_time TEXT, isInstalled INTEGER DEFAULT 0);";
            LogUtil.d("DbHelper onCreate  ----------------> sql:" + sql);
            try {
                sqLiteDatabase.execSQL(sql);
            } catch (Exception e) {
                LogUtil.d("DbHelper onCreate  ----------------> " + e.getMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
            // TODO
        }
    }
}
