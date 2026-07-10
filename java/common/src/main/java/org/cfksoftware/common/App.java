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

import java.util.Map;
import java.util.TreeMap;

import org.cfksoftware.common.logging.CfkLogger;

public class App {
  public static void main(String[] args) {
    CfkLogger.info(App.class.getCanonicalName());

    Map<String, String> data = new TreeMap<>();
    data.put("foo", "bar");
    data.put("Secret Message", "Hello world!!!");
    data.put("special_chars", "~!@#$%^&*()_+?/|\\`");
    data.put("blah", null);
    data.put("blee", "");

    try {
      String destUrl = "https://utils.cfksoftware.org/snoop/?format=json";
      String response = HttpUtils.doHttpRequest(destUrl, data);
      CfkLogger.info("RESPONSE:%n%s", response);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
