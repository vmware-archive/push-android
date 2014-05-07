package com.pivotal.cf.mobile.common.test.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.pivotal.cf.mobile.common.util.ServiceStarter;

public class FakeServiceStarter implements ServiceStarter {

    private ComponentName componentName;
    private boolean wasStarted;
    private Intent startedIntent;

    public void setReturnedComponentName(ComponentName componentName) {
        this.componentName = componentName;
    }

    public boolean wasStarted() {
        return wasStarted;
    }

    public Intent getStartedIntent() {
        return startedIntent;
    }

    @Override
    public ComponentName startService(Context context, Intent service) {
        wasStarted = true;
        startedIntent = service;
        return componentName;
    }
}
