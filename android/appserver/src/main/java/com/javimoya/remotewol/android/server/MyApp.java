package com.javimoya.remotewol.android.server;

import android.app.Application;

import com.parse.Parse;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, BuildConfig.PARSE_APPLICATION_ID, BuildConfig.PARSE_CLIENT_KEY);
    }
}
