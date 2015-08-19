package io.pivotal.android.push.version;

import android.test.AndroidTestCase;
import android.test.MoreAsserts;

public class VersionTest extends AndroidTestCase {

    public void test1() {
        assertVersionsDifferent("1", "2");
    }

    public void test2() {
        assertVersionsDifferent("1.0", "2.0.");
        assertVersionsDifferent("1 0", "2 0 ");
    }

    public void test3() {
        assertVersionsDifferent("1.0", "1.1.0");
        assertVersionsDifferent("1 0", "1 1  0");
    }

    public void test4() {
        assertVersionsDifferent("1.0.1", "1.0.2");
        assertVersionsDifferent(" 1 0 1", "   1 0.2 ");
    }

    public void test5() {
        assertVersionsDifferent("2.0.0", "2.0.0.5.4.3.2.1");
    }

    public void test6() {
        assertVersionsDifferent("2.0", "2.0.0");
    }

    public void test7() {
        assertVersionsDifferent("2.0.0.a", "2.0.0.b");
    }

    public void test8() {
        assertVersionsEqual("2", "2");
        assertVersionsEqual("2.", "2");
        assertVersionsEqual("2.0", "2.0");
        assertVersionsEqual("2.0.", "2.0.");
        assertVersionsEqual("2.0.0.", "2.0.0");
    }

    public void test9() {
        assertVersionsDifferent("a", "b");
    }

    public void test10() {
        assertVersionsDifferent("2.0.0", "20.0");
        assertVersionsDifferent("1.1", "1.10");
        assertVersionsDifferent("1.11", "1.111");
    }

    public void test11() {
        assertVersionsDifferent("1a", "10a");
    }

    public void test12() {
        assertVersionsDifferent("10a", "10a.b");
        assertVersionsDifferent("10a", "10ab");
        assertVersionsDifferent("10a.b", "10ab");
    }

    public void test13() {
        assertVersionsDifferent("1", "123456");
        assertVersionsDifferent("12", "123456");
        assertVersionsDifferent("123", "123456");
        assertVersionsDifferent("1234", "123456");
        assertVersionsDifferent("12345", "123456");
        assertVersionsEqual("123456", "123456");
    }

    public void test14() {
        assertVersionsDifferent("0", "-1");
    }

    public void test15() {
        // NOTE: string comparison are case insensitive
        assertVersionsEqual("A", "a");
        assertVersionsEqual(" A", "a");
        assertVersionsEqual("A", " a");
    }

    public void test16() {
        // NOTE: Negative sign is ignored since it is used as delimiter
        assertVersionsEqual("1", "-1");
    }

    public void test17() {
        // NOTE: Negative sign is ignored since it is used as delimiter
        assertVersionsEqual("1.1.0", "1...1...0");
        assertVersionsEqual("1.1.0", "-1.-.1.-.0-");
    }

    public void test18() {
        // NOTE: Consecutive delimiters are collapsed are treated the same as each other
        assertVersionsEqual("1.ab*.0", "1.-ab*...0");
        assertVersionsEqual("1-ab*-0", "1.-ab*...0");
        assertVersionsEqual("1ab*.10", "-1ab*.-.10-");
        assertVersionsDifferent("1ab*.10", "-1ab*.-.11-");
    }

    public void test19() {
        assertVersionsDifferent("a", "abcdefg");
        assertVersionsDifferent("ab", "abcdefg");
        assertVersionsDifferent("abc", "abcdefg");
        assertVersionsDifferent("abcd", "abcdefg");
        assertVersionsDifferent("abcde", "abcdefg");
        assertVersionsDifferent("abcdef", "abcdefg");
        assertVersionsEqual("abcdefg", "abcdefg");
    }

    public void test20() {
        // NOTE: string comparisons are used to compare mixed items, so 'a' will be greater than '1' since 'a' has a
        // higher ASCII code (i.e.: lexigraphical comparison).
        assertVersionsDifferent("1", "a");
        assertVersionsDifferent("1000", "a");
    }

    public void test21() {
        assertVersionsDifferent("001", "2");
        assertVersionsDifferent("001", "02");
        assertVersionsDifferent("02", "005");
        assertVersionsDifferent("001", "002");
        assertVersionsDifferent("001", "20");
        assertVersionsDifferent("001", "020");
    }

    public void test22() {
        assertVersionsDifferent(" ", "1");
        assertVersionsDifferent(".", "1");
    }

    public void test23() {
        assertVersionsEqual("1.3.3.7", "1.3.3.7");
    }

    public void testNull(String s1, String s2) {
        try {
            new Version(null);
        } catch (IllegalArgumentException e) {
            return;
        }
        fail();
    }

    private void assertVersionsDifferent(String lesserVersion, String greaterVersion) {
        Version v1 = new Version(lesserVersion);
        Version v2 = new Version(greaterVersion);
        assertEquals(-1, v1.compareTo(v2));
        assertEquals(1, v2.compareTo(v1));
        MoreAsserts.assertNotEqual(v1, v2);
    }

    private void assertVersionsEqual(String s1, String s2) {
        Version v1 = new Version(s1);
        Version v2 = new Version(s2);
        assertEquals(0, v1.compareTo(v2));
        assertEquals(v1, v2);
    }

}
