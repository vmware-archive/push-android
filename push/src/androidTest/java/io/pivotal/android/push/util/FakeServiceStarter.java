/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

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
