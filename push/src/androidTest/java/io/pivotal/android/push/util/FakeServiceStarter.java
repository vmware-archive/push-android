package io.pivotal.android.push.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import java.util.LinkedList;
import java.util.List;

public class FakeServiceStarter implements ServiceStarter {

    private ComponentName componentName;
    private boolean wasStarted;
    private List<Intent> startedIntents = new LinkedList<>();

    public void setReturnedComponentName(ComponentName componentName) {
        this.componentName = componentName;
    }

    public boolean wasStarted() {
        return wasStarted;
    }

    public List<Intent> getStartedIntents() {
        return startedIntents;
    }

    @Override
    public ComponentName startService(Context context, Intent service) {
        wasStarted = true;
        startedIntents.add(service);
        return componentName;
    }
}
