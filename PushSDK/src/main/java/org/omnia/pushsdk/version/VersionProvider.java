package org.omnia.pushsdk.version;

public interface VersionProvider {
    /**
     * Gets the current application version code.
     *
     * @return Returns the current application version code from the manifest.
     */
    int getAppVersion();
}
