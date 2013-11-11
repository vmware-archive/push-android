package com.gopivotal.pushlib;

import android.app.Application;
import android.content.Context;

import com.xtreme.commons.Logger;

public class PushLib {

    private static final String TAG_NAME = "PivotalPushLib";
    private static PushLib instance;

    public static PushLib getInstance(Context context) {
        if (instance == null) {
            instance = new PushLib(context);
        }
        return instance;
    }

    private final Context context;

    private PushLib(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context may not be null");
        }

        if (!(context instanceof Application)) {
            this.context = context.getApplicationContext();
        } else {
            this.context = context;
        }

        if (!Logger.isSetup()) {
            Logger.setup(context, TAG_NAME);
        }

        Logger.i("PushLib initialized");
    }
}
