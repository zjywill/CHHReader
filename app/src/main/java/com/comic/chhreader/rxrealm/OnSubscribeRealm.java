package com.comic.chhreader.rxrealm;

import android.content.Context;

import java.util.concurrent.atomic.AtomicBoolean;

import io.realm.Realm;
import io.realm.exceptions.RealmException;
import rx.Observable;
import rx.Subscriber;

abstract class OnSubscribeRealm<T> implements Observable.OnSubscribe<T> {
    private final Context context;

    //    private final List<Subscriber<? super T>> subscribers = new ArrayList<>();
    private final AtomicBoolean canceled = new AtomicBoolean();
    private final Object lock = new Object();

    public OnSubscribeRealm(Context context) {
        this.context = context;
    }

    @Override
    public void call(final Subscriber<? super T> subscriber) {

        Realm realm = Realm.getDefaultInstance();

        boolean withError = false;

        T object = null;
        try {
            if (!this.canceled.get()) {
                realm.beginTransaction();
                object = get(realm);
                if (object != null && !this.canceled.get()) {
                    realm.commitTransaction();
                } else {
                    realm.cancelTransaction();
                }
            }
        } catch (RuntimeException e) {
            realm.cancelTransaction();
            sendOnError(subscriber, new RealmException("Error during transaction.", e));
            withError = true;
        } catch (Error e) {
            realm.cancelTransaction();
            sendOnError(subscriber, e);
            withError = true;
        }
        if (object != null && !this.canceled.get() && !withError) {
            sendOnNext(subscriber, object);
        }

        try {
            realm.close();
        } catch (RealmException ex) {
            sendOnError(subscriber, ex);
            withError = true;
        }

        if (!withError) {
            sendOnCompleted(subscriber);
        }
        this.canceled.set(false);
    }

    private void sendOnNext(final Subscriber<? super T> subscriber, T object) {
        subscriber.onNext(object);
    }

    private void sendOnError(final Subscriber<? super T> subscriber, Throwable e) {
        subscriber.onError(e);
    }

    private void sendOnCompleted(final Subscriber<? super T> subscriber) {
        subscriber.onCompleted();
    }

    public abstract T get(Realm realm);
}
