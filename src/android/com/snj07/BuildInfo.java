package com.snj07;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;

public class BuildInfo {
    private static final String TAG = "SafetyNetPlugin";

    /**
     * Cache of result JSON
     */

    private static JSONObject mBuildInfoCache;

    public static JSONObject GetBuildInfo(String buildConfigClassName, Activity cordovaActivity) throws Exception {
        // Cached check
        if (null != mBuildInfoCache) {
            return mBuildInfoCache;
        }

        // Load PackageInfo
        String packageName = cordovaActivity.getPackageName();
        String basePackageName = packageName;
        CharSequence displayName = "";

        PackageManager pm = cordovaActivity.getPackageManager();

        try {
            PackageInfo pi = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);

            if (null != pi.applicationInfo) {
                displayName = pi.applicationInfo.loadLabel(pm);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // Load BuildConfig class
        Class c = null;

        if (null == buildConfigClassName) {
            buildConfigClassName = packageName + ".BuildConfig";
        }

        try {
            c = Class.forName(buildConfigClassName);
        } catch (ClassNotFoundException e) {
        }

        if (null == c) {
            basePackageName = cordovaActivity.getClass().getPackage().getName();
            buildConfigClassName = basePackageName + ".BuildConfig";

            try {
                c = Class.forName(buildConfigClassName);
            } catch (ClassNotFoundException e) {
                throw new Exception("BuildConfig ClassNotFoundException: " + e.getMessage());
            }
        }
        // Create result
        mBuildInfoCache = new JSONObject();
        try {
            boolean debug = getClassFieldBoolean(c, "DEBUG", false);

            mBuildInfoCache.put("packageName", packageName);
            mBuildInfoCache.put("basePackageName", basePackageName);
            mBuildInfoCache.put("displayName", displayName);
            mBuildInfoCache.put("name", displayName); // same as displayName
            mBuildInfoCache.put("version", getClassFieldString(c, "VERSION_NAME", ""));
            mBuildInfoCache.put("versionCode", getClassFieldInt(c, "VERSION_CODE", 0));
            mBuildInfoCache.put("buildType", getClassFieldString(c, "BUILD_TYPE", ""));
            mBuildInfoCache.put("flavor", getClassFieldString(c, "FLAVOR", ""));

            if (debug) {
                Log.d(TAG, "packageName    : \"" + mBuildInfoCache.getString("packageName") + "\"");
                Log.d(TAG, "basePackageName: \"" + mBuildInfoCache.getString("basePackageName") + "\"");
                Log.d(TAG, "displayName    : \"" + mBuildInfoCache.getString("displayName") + "\"");
                Log.d(TAG, "name           : \"" + mBuildInfoCache.getString("name") + "\"");
                Log.d(TAG, "version        : \"" + mBuildInfoCache.getString("version") + "\"");
                Log.d(TAG, "versionCode    : " + mBuildInfoCache.getInt("versionCode"));
                Log.d(TAG, "flavor         : \"" + mBuildInfoCache.getString("flavor") + "\"");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            throw new Exception("JSONException: " + e.getMessage());
        }

        return mBuildInfoCache;
    }

    /**
     * Get boolean of field from Class
     * @param c
     * @param fieldName
     * @param defaultReturn
     * @return
     */
    private static boolean getClassFieldBoolean(Class c, String fieldName, boolean defaultReturn) {
        boolean ret = defaultReturn;
        Field field = getClassField(c, fieldName);

        if (null != field) {
            try {
                ret = field.getBoolean(c);
            } catch (IllegalAccessException iae) {
                iae.printStackTrace();
            }
        }

        return ret;
    }

    /**
     * Get field from Class
     * @param c
     * @param fieldName
     * @return
     */
    private static Field getClassField(Class c, String fieldName) {
        Field field = null;

        try {
            field = c.getField(fieldName);
        } catch (NoSuchFieldException nsfe) {
            nsfe.printStackTrace();
        }

        return field;
    }

    /**
     * Get string of field from Class
     * @param c
     * @param fieldName
     * @param defaultReturn
     * @return
     */
    private static String getClassFieldString(Class c, String fieldName, String defaultReturn) {
        String ret = defaultReturn;
        Field field = getClassField(c, fieldName);

        if (null != field) {
            try {
                ret = (String)field.get(c);
            } catch (IllegalAccessException iae) {
                iae.printStackTrace();
            }
        }

        return ret;
    }

    /**
     * Get int of field from Class
     * @param c
     * @param fieldName
     * @param defaultReturn
     * @return
     */
    private static int getClassFieldInt(Class c, String fieldName, int defaultReturn) {
        int ret = defaultReturn;
        Field field = getClassField(c, fieldName);

        if (null != field) {
            try {
                ret = field.getInt(c);
            } catch (IllegalAccessException iae) {
                iae.printStackTrace();
            }
        }

        return ret;
    }


}
