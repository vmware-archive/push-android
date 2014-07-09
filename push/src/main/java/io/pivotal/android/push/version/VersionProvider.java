/*
 * Copyright (C) 2014 Pivotal Software, Inc. All rights reserved.
 */
package io.pivotal.android.push.version;

public interface VersionProvider {
    /**
     * Gets the current application version code.
     *
     * @return Returns the current application version code from the manifest.
     */
    int getAppVersion();
}
