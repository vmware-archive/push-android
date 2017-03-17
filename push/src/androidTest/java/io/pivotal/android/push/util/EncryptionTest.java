package io.pivotal.android.push.util;

import static io.pivotal.android.push.util.Encryption.UserIdEncryptionMode.HEX;
import static io.pivotal.android.push.util.Encryption.UserIdEncryptionMode.STANDARD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.support.test.runner.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EncryptionTest {

  private final String CUSTOMUSERID = "bond007";
  private final String STANDARD_SECRET = "asev01L5kAa9145zJ5Zg3o08I8OINN8L";
  private final String HEX_SECRET = "4873357435657730734b49426133746b547a7a6b78656d74427a345837593447";

  private final String STANDARD_ENCRYPTED_CUSTOMUSERID = "eyJhbGciOiJIUzI1NiJ9.eyJjdXN0b21fdXNlcl9pZCI6ImJvbmQwMDcifQ.I_ebQ-2NZjzTRWE-nuwgZfGNtz_4m6Bh9TlLFs06GU4";
  private final String HEX_ENCRYPTED_CUSTOMUSERID = "eyJhbGciOiJIUzI1NiJ9.eyJjdXN0b21fdXNlcl9pZCI6ImJvbmQwMDcifQ.HgAet6Es9XHbveCPgaKMs1lMwI-aJQv3FQBvsAd3lyE";

  @Test
  public void encryptCustomUserId_withNullOrEmptyUserId() throws Exception {

    Throwable throwable = null;
    try {
      Encryption.encryptCustomUserId(STANDARD, null, STANDARD_SECRET);
    } catch (Throwable t) {
      throwable = t;
    }

    assertTrue(throwable instanceof IllegalArgumentException);

    throwable = null;
    try {
      Encryption.encryptCustomUserId(STANDARD, "", STANDARD_SECRET);
    } catch (Throwable t) {
      throwable = t;
    }

    assertTrue(throwable instanceof IllegalArgumentException);
  }

  @Test
  public void encryptCustomUserId_withNullOrEmptySecret() throws Exception {

    Throwable throwable = null;
    try {
      Encryption.encryptCustomUserId(STANDARD, CUSTOMUSERID, null);
    } catch (Throwable t) {
      throwable = t;
    }

    assertTrue(throwable instanceof IllegalArgumentException);

    throwable = null;
    try {
      Encryption.encryptCustomUserId(STANDARD, CUSTOMUSERID, "");
    } catch (Throwable t) {
      throwable = t;
    }

    assertTrue(throwable instanceof IllegalArgumentException);
  }

  @Test
  public void encryptCustomUserId_usingStandardMode() throws Exception {
    final String encryptedUserId = Encryption.encryptCustomUserId(STANDARD, CUSTOMUSERID, STANDARD_SECRET);

    assertEquals(STANDARD_ENCRYPTED_CUSTOMUSERID, encryptedUserId);
  }

  @Test
  public void encryptCustomUserId_usingHexMode() throws Exception {
    final String encryptedUserId = Encryption.encryptCustomUserId(HEX, CUSTOMUSERID, HEX_SECRET);

    assertEquals(HEX_ENCRYPTED_CUSTOMUSERID, encryptedUserId);
  }

  @Test
  public void encryptCustomUserId_usingHexMode_withInvalidHexSecret() throws Exception {
    Throwable throwable = null;
    try {
      Encryption.encryptCustomUserId(HEX, CUSTOMUSERID, STANDARD_SECRET);
    } catch (Throwable t) {
      throwable = t;
    }

    assertTrue(throwable instanceof IllegalArgumentException);
  }

  @Test
  public void encryptCustomUserId_usingHexMode_withHexSecretOfOddLength() throws Exception {
    Throwable throwable = null;
    try {
      Encryption.encryptCustomUserId(HEX, CUSTOMUSERID, HEX_SECRET + "1");
    } catch (Throwable t) {
      throwable = t;
    }

    assertTrue(throwable instanceof IllegalArgumentException);
  }
}