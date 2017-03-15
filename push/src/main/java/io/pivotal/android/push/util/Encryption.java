package io.pivotal.android.push.util;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Bytes;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import java.util.ArrayList;
import java.util.List;
import net.minidev.json.JSONObject;

public class Encryption {

  public enum UserIdEncryptionMode {
    STANDARD,
    HEX
  }

  public static String encryptCustomUserId(final UserIdEncryptionMode mode,
      final String customUserId,
      final String secret) throws IllegalArgumentException {

    checkArgument(!Strings.isNullOrEmpty(customUserId));
    checkArgument(!Strings.isNullOrEmpty(secret));

    final JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS256), new Payload(
        new JSONObject(ImmutableMap.of("custom_user_id", customUserId))
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
    checkArgument(!Strings.isNullOrEmpty(hexBinaryAsString));
    checkArgument(hexBinaryAsString.length() % 2 == 0);

    final List<Byte> bytes = new ArrayList<>();
    String hexStringToParse = hexBinaryAsString;
    while (!hexStringToParse.isEmpty()) {
      bytes.add(Byte.decode("0x" + hexStringToParse.substring(0, 2)));
      hexStringToParse = hexStringToParse.substring(2);
    }

    return Bytes.toArray(bytes);
  }
}
