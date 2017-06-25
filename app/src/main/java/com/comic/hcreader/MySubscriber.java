package com.comic.hcreader;

import rx.Subscriber;
import rx.functions.Action1;

/**
 * Created by junyi on 4/14/16.
 */
public abstract class MySubscriber<T> extends Subscriber<T> {
    public static <T> MySubscriber<T> create(final Action1<? super T> action) {
        return new MySubscriber<T>() {
            @Override
            public void onNext(T t) {
                action.call(t);
            }
        };
    }

    @Override
    public void onCompleted() {
    }

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
    }
}