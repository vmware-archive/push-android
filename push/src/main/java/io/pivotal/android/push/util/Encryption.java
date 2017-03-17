package io.pivotal.android.push.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minidev.json.JSONObject;

public class Encryption {

  public enum UserIdEncryptionMode {
    STANDARD,
    HEX
  }

  public static String encryptCustomUserId(final UserIdEncryptionMode mode,
      final String customUserId,
      final String secret) throws IllegalArgumentException {

    if (customUserId == null || customUserId.isEmpty()) {
      throw new IllegalArgumentException();
    }

    if (secret == null || secret.isEmpty()) {
      throw new IllegalArgumentException();
    }

    final Map<String, String> payload = new HashMap<>();
    payload.put("custom_user_id", customUserId);

    final JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS256), new Payload(
        new JSONObject(payload)
    ));

    try {
      switch (mode) {
        case STANDARD:
          jwsObject.sign(new MACSigner(secret));
          break;
        case HEX:
          jwsObject.sign(new MACSigner(parseHexBinary(secret)));
          break;
      }
    } catch (JOSEException e) {
      Logger.ex(e.getMessage(), e);
      return "";
    }

    return jwsObject.serialize();
  }

  private static byte[] parseHexBinary(final String hexBinaryAsString) {
    if (hexBinaryAsString == null ||
        hexBinaryAsString.isEmpty() ||
        hexBinaryAsString.length() % 2 != 0) {
      throw new IllegalArgumentException();
    }

    final List<Byte> bytes = new ArrayList<>();
    String hexStringToParse = hexBinaryAsString;
    while (!hexStringToParse.isEmpty()) {
      bytes.add(Byte.decode("0x" + hexStringToParse.substring(0, 2)));
      hexStringToParse = hexStringToParse.substring(2);
    }

    final byte[] result = new byte[bytes.size()];
    int index = 0;
    for (Byte data : bytes) {
      result[index] = data;
      index++;
    }

    return result;
  }
}
