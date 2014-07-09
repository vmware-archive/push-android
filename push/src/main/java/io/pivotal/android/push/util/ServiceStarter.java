/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public interface ServiceStarter {
    public ComponentName startService(Context context, Intent service);
}
