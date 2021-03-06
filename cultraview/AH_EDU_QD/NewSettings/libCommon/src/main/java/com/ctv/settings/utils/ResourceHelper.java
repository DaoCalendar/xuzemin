package com.ctv.settings.utils;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Field;

/**
 * 反射调用资源和id
 * Created by wang on 2016/5/23 11:37
 */
public class ResourceHelper {
    private volatile static ResourceHelper mResource = null;
    private static String mPackagename = null;
    private static Class mLayout = null;
    private static Class mDrawable = null;
    private static Class mMipmap = null;
    private static Class mID = null;
    private static Class mString = null;
    private static Class mAttr = null;
    private static Class mStyle = null;

    public static ResourceHelper getInstance(Context context) {
        if (mResource == null) {
            synchronized (ResourceHelper.class) {
                if (mResource == null) {
                    mPackagename = (mPackagename == null ? context.getPackageName()
                            : mPackagename);
                    mResource = new ResourceHelper(mPackagename);
                }
            }
        }

        return mResource;
    }

    private ResourceHelper(String packageName) {
        try {
            mLayout = Class.forName(packageName + ".R$layout");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            mDrawable = Class.forName(packageName + ".R$drawable");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            mMipmap = Class.forName(packageName + ".R$mipmap");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            mID = Class.forName(packageName + ".R$id");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            mString = Class.forName(packageName + ".R$string");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            mAttr = Class.forName(packageName + ".R$attr");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            mStyle= Class.forName(packageName + ".R$style");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private int getResourceId(Class classType, String resourceName) {
        if (classType == null) {
            throw new IllegalArgumentException(
                    "ResClass is not initialized. Please make sure you have added neccessary resources. Also make sure you have "
                            + mPackagename
                            + ".R$* configured in obfuscation. field="
                            + resourceName);
        }
        try {
            Field field = classType.getField(resourceName);
            return field.getInt(resourceName);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ResourceHelper",
                    "Error getting resource. Make sure you have copied all resources (res/) from SDK to your project.");
        }
        return -1;
    }

    //
    public int getDrawableId(String resourceName) {
        return getResourceId(mDrawable, resourceName);
    }

    public int getMipmap(String resourceName) {
        return getResourceId(mMipmap, resourceName);
    }

    public int getLayoutId(String resourceName) {
        return getResourceId(mLayout, resourceName);
    }

    public int getId(String resourceName) {
        return getResourceId(mID, resourceName);
    }

    public int getStringId(String resourceName) {
        return getResourceId(mString, resourceName);
    }

    public int getAttrId(String resourceName) {
        return getResourceId(mAttr, resourceName);
    }
    public int getStyle(String resourceName) {
        return getResourceId(mStyle, resourceName);
    }

}
