package com.comic.hcreader;

import android.app.Application;
import com.comic.hcreader.component.BasicActivityContainer;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by zhangjunyi on 4/30/16.
 */
public class MainApplication extends Application {

    private static MainApplication application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        BasicActivityContainer.notifyAppStart(this);
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().name("chhreader.storage")
            .deleteRealmIfMigrationNeeded()
            .build();
        Realm.deleteRealm(config);
        Realm.setDefaultConfiguration(config);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        application = null;
    }

    public static MainApplication getApplication() {
        return application;
    }
}
