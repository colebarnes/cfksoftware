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

package org.cfksoftware.common.logging;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import org.cfksoftware.common.DateUtils;
import org.cfksoftware.common.ThreadUtils;

public class CfkLogger {
  private static CfkLogger logger = new CfkLogger();

  public static void error(Throwable cause, Object... args) {
    String msgFmt;

    if (cause == null) {
      msgFmt = "An unknown exception occured.";
    } else {
      msgFmt = String.format("An exception occured: %s", cause.getMessage());
    }

    CfkLogger.error(msgFmt);
  }

  public static void error(String msgFmt, Object... args) {
    CfkLogger.logger.log(LV_ERROR, msgFmt, args);
  }

  public static void warn(Throwable cause, Object... args) {
    String msgFmt;

    if (cause == null) {
      msgFmt = "an unknown exception occured";
    } else {
      msgFmt = String.format("An exception occured: %s", cause.getMessage());
    }

    CfkLogger.warn(msgFmt);
  }

  public static void warn(String msgFmt, Object... args) {
    CfkLogger.logger.log(LV_WARN, msgFmt, args);
  }

  public static void info(String msgFmt, Object... args) {
    CfkLogger.logger.log(LV_INFO, msgFmt, args);
  }

  public static void trace(String msgFmt, Object... args) {
    CfkLogger.logger.log(LV_TRACE, msgFmt, args);
  }

  public static void entering() {
    CfkLogger.logger.log(LV_TRACE, ">>> ENTERING %s", ThreadUtils.getCallerInfoString(4, false, false));
  }

  public static void exiting() {
    CfkLogger.logger.log(LV_TRACE, "<<< EXITING %s", ThreadUtils.getCallerInfoString(4, false, false));
  }

  public static void exiting(Object retVal) {
    CfkLogger.logger.log(LV_TRACE, "<<< EXITING %s: %s", ThreadUtils.getCallerInfoString(4, false, false), (retVal == null) ? "" : retVal.toString());
  }

  public static void setLogLevel(int level) {
    CfkLogger.logger.level = level;
  }

  public static void addPrintStream(PrintStream printStream) {
    // TODO: check input
    CfkLogger.logger.printStreams.add(printStream);
  }

  /* IMPLEMENTATION */
  public static final int LV_OFF = 0;
  public static final int LV_ERROR = 1;
  public static final int LV_WARN = 2;
  public static final int LV_INFO = 3;
  public static final int LV_TRACE = 4;

  private volatile int level;
  private Collection<PrintStream> printStreams;

  private CfkLogger() {
    this.level = CfkLogger.LV_INFO;

    this.printStreams = Collections.synchronizedCollection(new HashSet<PrintStream>());
    this.printStreams.add(System.out);
  }

  private String getLevelString(int level) {
    String levelName;

    switch (level) {
    case CfkLogger.LV_OFF:
      levelName = "OFF";
      break;
    case CfkLogger.LV_ERROR:
      levelName = "ERR";
      break;
    case CfkLogger.LV_WARN:
      levelName = "WRN";
      break;
    case CfkLogger.LV_INFO:
      levelName = "INF";
      break;
    case CfkLogger.LV_TRACE:
      levelName = "TRC";
      break;
    default:
      levelName = "UNKNOWN";
      break;
    }

    return levelName;
  }

  private String formatDefaultLogEntry(int level, String msgFmt, Object... args) {
    StringBuffer msgBuffer = new StringBuffer();

    msgBuffer.append('[').append(ThreadUtils.threadId()).append(']');
    msgBuffer.append('[').append(DateUtils.iso8601CurrentDate()).append(']');
    msgBuffer.append('[').append(this.getLevelString(level)).append(']');
    msgBuffer.append('[').append(ThreadUtils.getCallerInfoString(7, true, true)).append(']');

    // TODO: OTHER STUFF in log entry?

    msgBuffer.append(':').append(String.format(msgFmt, args));

    return msgBuffer.toString();
  }

  private String formatLogEntry(int level, String msgFmt, Object... args) {
    return this.formatDefaultLogEntry(level, msgFmt, args);
  }

  private synchronized void log(int level, String msgFmt, Object... args) {
    if (this.level > CfkLogger.LV_OFF && this.level >= level) {
      synchronized (this.printStreams) {
        Iterator<PrintStream> iter = this.printStreams.iterator();
        while (iter.hasNext()) {
          String logEntry = this.formatLogEntry(level, msgFmt, args);
          iter.next().println(logEntry);
        }
      }
    }
  }
}
