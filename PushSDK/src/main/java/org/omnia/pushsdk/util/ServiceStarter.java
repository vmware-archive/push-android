package org.omnia.pushsdk.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public interface ServiceStarter {
    public ComponentName startService(Context context, Intent service);
}
