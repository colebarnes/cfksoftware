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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class HttpUtils {
  public static String urlEncode(Map<String, String> data) {
    StringBuilder sb = new StringBuilder();

    if (data != null) {
      for (Entry<String, String> entry : data.entrySet()) {
        String key = entry.getKey();
        String value = entry.getValue();

        if (sb.length() > 0) {
          sb.append("&");
        }

        if (!StringUtils.isNullOrBlank(key)) {
          sb.append(StringUtils.urlEncode(key)).append("=").append(StringUtils.urlEncode(value));
        }
      }
    }

    return sb.toString();
  }

  public static Map<String, String> urlDecode(String urlEncoded) {
    Map<String, String> data = new TreeMap<>();

    for (String encodedPair : urlEncoded.split("&")) {
      String[] encodedPairComponents = encodedPair.split("=", 2);

      if (encodedPairComponents != null && encodedPairComponents.length > 0) {
        String key = StringUtils.urlDecode(encodedPairComponents[0]);
        String value = encodedPairComponents.length > 1 ? StringUtils.urlDecode(encodedPairComponents[1]) : "";
        data.put(key, value);
      }
    }

    return data;
  }

  public static String doHttpRequest(String destUrl) throws MalformedURLException, IOException {
    return HttpUtils.doHttpRequest(destUrl, null);
  }

  public static String doHttpRequest(String destUrl, Map<String, String> postData) throws MalformedURLException, IOException {
    byte[] responseData = HttpUtils.doGetOrPost(destUrl, postData);
    return StringUtils.fromBytes(responseData);
  }
  
  public static byte[] doGet(String destUrl) throws MalformedURLException, IOException {
    return HttpUtils.doGetOrPost(destUrl, null);
  }

  public static byte[] doPost(String destUrl, Map<String, String> data) throws MalformedURLException, IOException {
    return HttpUtils.doGetOrPost(destUrl, data);
  }

  private static byte[] doGetOrPost(String destUrl, Map<String, String> data) throws MalformedURLException, IOException {
    // TODO: check input
    URL url = URI.create(destUrl).toURL();
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

    if (data == null) {
      conn.setRequestMethod("GET");
      conn.setDoOutput(false);
      conn.connect();
    } else {
      conn.setRequestMethod("POST");
      conn.setDoOutput(true);

      byte[] dataBytes = StringUtils.toBytes(HttpUtils.urlEncode(data));
      int length = dataBytes.length;

      conn.setFixedLengthStreamingMode(length);
      conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
      conn.connect();

      try (OutputStream out = conn.getOutputStream()) {
        out.write(dataBytes);
      }
    }

    try (InputStream in = conn.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      int bytesRead = 0;
      byte[] buffer = new byte[1024];

      while ((bytesRead = in.read(buffer)) >= 0) {
        out.write(buffer, 0, bytesRead);
      }

      out.flush();
      return out.toByteArray();
    }
  }
}
