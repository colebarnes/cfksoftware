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

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
  public static final String ISO_8601 = "yyyy-MM-dd'T'HH:mm:ssXXX";

  private DateUtils() {
    /* This utility class should not be instantiated */
  }

  public static Date getCurrentDate() {
    return new Date();
  }

  public static String formatCurrentDate(String fmt) {
    return DateUtils.formatDate(DateUtils.getCurrentDate(), fmt);
  }

  public static String iso8601CurrentDate() {
    return DateUtils.formatDate(DateUtils.getCurrentDate(), DateUtils.ISO_8601);
  }

  public static String iso8601(Date data) {
    return DateUtils.formatDate(data, DateUtils.ISO_8601);
  }

  public static String formatDate(Date date, String fmt) {
    SimpleDateFormat dtFmt = new SimpleDateFormat(fmt);
    return dtFmt.format(date);
  }
}
