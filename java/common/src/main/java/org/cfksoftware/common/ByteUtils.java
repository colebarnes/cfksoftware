/*
 * Copyright © 2026 cfksoftware@proton.me, https://cfksoftware.org
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the “Software”), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR  COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.cfksoftware.common;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

public class ByteUtils {
  public static String base64Encode(final byte[] bytes) {
    if (bytes == null) {
      throw new NullPointerException("'bytes' is null.");
    }

    return Base64.getEncoder().encodeToString(bytes);
  }

  public static byte[] base64Decode(final String str) {
    if (str == null) {
      throw new NullPointerException("'str' is null.");
    }

    return Base64.getDecoder().decode(str);
  }

  public static byte[] random(int numBytes) {
    byte[] bytes = new byte[numBytes];
    random(bytes);
    return bytes;
  }

  public static void random(byte[] bytes) {
    new SecureRandom().nextBytes(bytes);
  }

  public static byte[] fromInt(final int n) {
    return ByteBuffer.allocate(4).putInt(n).array();
  }

  public static int toInt(final byte[] bytes) {
    if ((bytes == null) || (bytes.length <= 0)) {
      throw new IllegalArgumentException("'bytes' cannot be null and must be at least 1 byte.");
    }

    return ByteBuffer.wrap(bytes).getInt();
  }

  public static byte[] fromLong(final long n) {
    return ByteBuffer.allocate(8).putLong(n).array();
  }

  public static long toLong(final byte[] bytes) {
    if ((bytes == null) || (bytes.length <= 0)) {
      throw new IllegalArgumentException("'bytes' cannot be null and must be at least 1 byte.");
    }

    return ByteBuffer.wrap(bytes).getLong();
  }

  public static String toHex(byte[] bytes) {
    StringBuilder hexString = new StringBuilder();
    for (byte b : bytes) {
      hexString.append(String.format("%02X", b));
    }
    return hexString.toString();
  }
}
