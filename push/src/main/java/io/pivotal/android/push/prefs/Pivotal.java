/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.prefs;

import android.content.Context;
import android.text.TextUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Pivotal {
    public enum SslCertValidationMode {
        DEFAULT,
        TRUST_ALL,
        PINNED,
        CALLBACK
    }
}
