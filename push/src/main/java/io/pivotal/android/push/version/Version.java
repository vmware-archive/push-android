package io.pivotal.android.push.version;

import java.util.List;

import io.pivotal.android.push.util.Tokenizer;

public class Version implements Comparable<Version> {

    private String version;

    /**
     * Creates a new Version object
     *
     * @param version A version string to store. May not be null otherwise IllegalArgumentException will be thrown.
     */
    public Version(String version) {
        if (version == null) {
            throw new IllegalArgumentException("version may not be null");
        } else {
            this.version = version;
        }
    }

    /**
     * Compares two version objects.
     * <p>
     * NOTES:
     * <p>
     * Consecutive delimiters are collapsed are treated the same as each other.
     * <p>
     * Negative signs are ignored since they are used as delimiter.
     * <p>
     * String comparisons are case insensitive.
     * <p>
     * String comparisons are used to compare mixed items, so 'a' will be greater than '1' since 'a' has a higher ASCII
     * code (i.e.: lexigraphical comparison).
     * <p>
     * EXAMPLES:
     * <p>
     * "1" &lt; "1.0" &lt; "1.0.0"
     * <p>
     * "1.0" &lt; "1.0a" &lt; "1.0ab"
     * <p>
     * " " &lt; "1" &lt; "a" &lt; "ab"
     * <p>
     * "a" == "A"
     *
     * @return -1 if this version is less than the other version. 0 if they are equal. 1 if this version is greater
     * than the other version.
     */
    public int compareTo(Version other) {

        List<String> vals1 = Tokenizer.tokenize(this.version);
        List<String> vals2 = Tokenizer.tokenize(other.version);

        // taken from: http://stackoverflow.com/questions/6701948/efficient-way-to-compare-version-strings-in-java
        int i = 0;
        while (i < vals1.size() && i < vals2.size() && vals1.get(i).equals(vals2.get(i))) {
            i++;
        }

        if (i < vals1.size() && i < vals2.size()) {
            String s1 = vals1.get(i);
            String s2 = vals2.get(i);
            int diff;

            if (doesContainOnlyDigits(s1) && doesContainOnlyDigits(s2)) {
                diff = Integer.valueOf(s1).compareTo(Integer.valueOf(s2));
            } else {
                diff = s1.toLowerCase().compareTo(s2.toLowerCase());
            }

            return diff < 0 ? -1 : diff == 0 ? 0 : 1;
        }

        return vals1.size() < vals2.size() ? -1 : vals1.size() == vals2.size() ? 0 : 1;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof Version)) {
            return false;
        }
        return compareTo((Version) o) == 0;
    }

    private boolean doesContainOnlyDigits(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return version;
    }
}
