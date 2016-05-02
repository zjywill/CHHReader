package com.comic.chhreader.component;

import android.app.Activity;
import android.app.Application;


/**
 * Created by junyi on 3/22/16.
 */
public class BasicActivityContainer extends BasicContextContainer  {
    public static BasicActivity sBasicActivity;

    public static void notifyAppStart(Application application) {
        applicationStart(application);
    }

    public static void switchActivity(Activity activity) {
        if (activity instanceof BasicActivity) {
            sBasicActivity = (BasicActivity) activity;
        }
    }

    final public static void recycleBasicContext(Activity releaseActivity) {
        if (checkContext(releaseActivity)) {
            recycleContext();
            sBasicActivity = null;
        }
    }
}
