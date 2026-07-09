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

public class ThreadUtils {
  public static long threadId() {
    return Thread.currentThread().threadId();
  }

  public static String getCallerInfoString(int level, boolean includeFileName, boolean includeLineNum) {
    StringBuffer callerInfo = new StringBuffer("caller.unknown");
    StackTraceElement element = ThreadUtils.getCaller(level);

    if (element != null) {
      callerInfo.delete(0, callerInfo.length());
      callerInfo.append(element.getClassName());
      callerInfo.append('.');
      callerInfo.append(element.getMethodName());

      if (includeFileName) {
        callerInfo.append(':');
        callerInfo.append(element.getFileName());
      }

      if (includeLineNum) {
        callerInfo.append(':');
        callerInfo.append(element.getLineNumber());
      }
    }

    return callerInfo.toString();
  }

  public static StackTraceElement getCaller(int level) {
    StackTraceElement element = null;
    StackTraceElement[] elements = Thread.currentThread().getStackTrace();

    if (elements != null && elements.length >= level) {
      element = elements[level];
    }

    return element;
  }
}
