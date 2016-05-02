package com.comic.chhreader.component;

import android.app.Activity;
import android.app.Application;
import android.content.Context;


/**
 * Created by junyi on 3/22/16.
 */
public abstract class BasicContextContainer {
    public static Application sApplication;
    public static Context sContext;
    public static Activity sActivity;
    protected static String sPackageName;

    private static int sToken;

    public static void applicationStart(Application application) {
        sApplication = application;
    }

    public static void activityStart(String pkgName, Activity activity) {
        sPackageName = pkgName;
        sContext = sActivity = activity;
        sToken = sContext.hashCode();
    }

    public static boolean checkContext(final Context context) {
        return context == sContext;
    }

    final protected static void recycleContext() {
        if (!matchToken()) {
            return;
        }
        sContext = null;
        sActivity = null;
    }

    private static boolean matchToken() {
        if (sContext != null && sContext.hashCode() == sToken) {
            return true;
        }
        return false;
    }
}
