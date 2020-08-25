package com.unicef.dreamapp2.application;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.multidex.MultiDexApplication;

/**
 * @author Iman Augustine
 *
 * MyApplication extends MultiDexApplication.
 *
 * */

public class MyApplication extends MultiDexApplication {

    // Application instance
    private static MyApplication instance;

    // On creation
    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
    }

    // Returns the instance
    public static MyApplication getInstance() {
        return instance;
    }
}
