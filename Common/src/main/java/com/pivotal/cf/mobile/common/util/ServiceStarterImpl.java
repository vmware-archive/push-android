package com.pivotal.cf.mobile.common.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class ServiceStarterImpl implements ServiceStarter {

    @Override
    public ComponentName startService(Context context, Intent service) {
        return context.startService(service);
    }
}
