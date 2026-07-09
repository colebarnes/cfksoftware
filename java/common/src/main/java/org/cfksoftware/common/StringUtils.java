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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StringUtils {
  public static String fromBytes(byte[] data) {
    return StringUtils.fromBytes(data, StringUtils.defaultCharset());
  }

  public static String fromBytes(byte[] data, Charset charset) {
    // TODO: check inputs
    return new String(data, charset);
  }

  public static byte[] toBytes(String str) {
    return StringUtils.toBytes(str, StringUtils.defaultCharset());
  }

  public static byte[] toBytes(String str, Charset charset) {
    // TODO: check inputs
    return str.getBytes(charset);
  }

  public static boolean isNullOrBlank(String str) {
    return str == null || str.isBlank();
  }

  public static Charset defaultCharset() {
    return StandardCharsets.UTF_8;
  }
}
