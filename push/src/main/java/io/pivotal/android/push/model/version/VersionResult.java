package io.pivotal.android.push.model.version;

import com.google.gson.annotations.SerializedName;

import io.pivotal.android.push.version.Version;

public class VersionResult {

    @SerializedName("version")
    private String versionStr;
    private Version versionVer;

    public Version getVersion() {
        if (versionStr == null) {
            return null;
        }
        if (versionVer == null) {
            versionVer = new Version(versionStr);
        }
        return versionVer;
    }

    @Override
    public String toString() {
        return "VersionResult [version=" + versionStr + "]";
    }
}
