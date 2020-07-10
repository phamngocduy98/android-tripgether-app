package cf.bautroixa.tripgether.utils;

import android.os.Handler;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

public class HandlerHelper {
    Handler mHandler;
    Runnable mCallback;

    public HandlerHelper(LifecycleOwner lifecycleOwner, Runnable callback) {
        this.mHandler = new Handler();
        this.mCallback = callback;
        lifecycleOwner.getLifecycle().addObserver(new LifecycleObserver() {
            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            void onResume() {

            }

            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            void onPause() {
                mHandler.removeCallbacks(mCallback);
            }
        });
    }

    public HandlerHelper(Runnable callback) {
        this.mHandler = new Handler();
        this.mCallback = callback;
    }

    public HandlerHelper() {
        this.mHandler = new Handler();
    }

    public void setCallback(Runnable callback) {
        this.mCallback = callback;
    }

    public void postDelayed(long delayMillis) {
        mHandler.postDelayed(mCallback, delayMillis);
    }

    public void removeCallback() {
        mHandler.removeCallbacks(mCallback);
    }
}
